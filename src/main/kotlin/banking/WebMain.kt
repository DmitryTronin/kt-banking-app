package banking

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }
    
    val accountActions = AccountActions()
    val validUserNames = listOf("karl", "johan", "sophie", "manuel", "lucas", "emma", "liam", "noah", "mila", "lina")
    
    routing {
        get("/") {
            call.respondText("""
                <!DOCTYPE html>
                <html>
                <head>
                    <title>Banking App</title>
                    <style>
                        body { font-family: Arial, sans-serif; margin: 40px; }
                        .container { max-width: 600px; margin: 0 auto; }
                        input, button { padding: 10px; margin: 5px; font-size: 16px; }
                        button { background-color: #007bff; color: white; border: none; cursor: pointer; }
                        button:hover { background-color: #0056b3; }
                        .result { margin: 10px 0; padding: 10px; border-radius: 5px; }
                        .success { background-color: #d4edda; color: #155724; }
                        .error { background-color: #f8d7da; color: #721c24; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <h1>Banking App</h1>
                        
                        <div id="login-section">
                            <h2>Login</h2>
                            <input type="text" id="username" placeholder="Enter username">
                            <button onclick="login()">Login</button>
                        </div>
                        
                        <div id="banking-section" style="display: none;">
                            <h2>Banking Operations</h2>
                            <div>
                                <input type="number" id="amount" placeholder="Amount" step="0.01">
                                <button onclick="deposit()">Deposit</button>
                                <button onclick="withdraw()">Withdraw</button>
                                <button onclick="getBalance()">Check Balance</button>
                            </div>
                        </div>
                        
                        <div id="result"></div>
                    </div>
                    
                    <script>
                        const validUsers = ${validUserNames.joinToString(prefix = "[", postfix = "]") { "\"$it\"" }};
                        let loggedIn = false;
                        
                        function login() {
                            const username = document.getElementById('username').value;
                            if (validUsers.includes(username)) {
                                loggedIn = true;
                                document.getElementById('login-section').style.display = 'none';
                                document.getElementById('banking-section').style.display = 'block';
                                showResult('Login successful! Welcome ' + username, true);
                            } else {
                                showResult('Invalid username', false);
                            }
                        }
                        
                        function deposit() {
                            if (!loggedIn) return;
                            const amount = parseFloat(document.getElementById('amount').value);
                            makeRequest('/deposit', { amount: amount });
                        }
                        
                        function withdraw() {
                            if (!loggedIn) return;
                            const amount = parseFloat(document.getElementById('amount').value);
                            makeRequest('/withdraw', { amount: amount });
                        }
                        
                        function getBalance() {
                            if (!loggedIn) return;
                            makeRequest('/balance', {});
                        }
                        
                        function makeRequest(endpoint, data) {
                            fetch(endpoint, {
                                method: 'POST',
                                headers: { 'Content-Type': 'application/json' },
                                body: JSON.stringify(data)
                            })
                            .then(response => response.json())
                            .then(data => {
                                let message = data.message;
                                if (data.balance !== null && data.balance !== undefined) {
                                    message += ' Current balance: $' + data.balance.toFixed(2);
                                }
                                showResult(message, data.success);
                                document.getElementById('amount').value = '';
                            })
                            .catch(error => showResult('Error: ' + error, false));
                        }
                        
                        function showResult(message, isSuccess) {
                            const result = document.getElementById('result');
                            result.innerHTML = '<div class="result ' + (isSuccess ? 'success' : 'error') + '">' + message + '</div>';
                        }
                    </script>
                </body>
                </html>
            """.trimIndent(), ContentType.Text.Html)
        }
        
        post("/deposit") {
            val request = call.receive<TransactionRequest>()
            val response = accountActions.deposit(request.amount)
            call.respond(response)
        }
        
        post("/withdraw") {
            val request = call.receive<TransactionRequest>()
            val response = accountActions.withdraw(request.amount)
            call.respond(response)
        }
        
        post("/balance") {
            val response = accountActions.getBalance()
            call.respond(response)
        }
    }
}