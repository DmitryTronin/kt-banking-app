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
                        * {
                            box-sizing: border-box;
                            margin: 0;
                            padding: 0;
                        }
                        
                        body {
                            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                            background: linear-gradient(135deg, #0f0f23 0%, #1a1a2e 50%, #16213e 100%);
                            min-height: 100vh;
                            padding: 20px;
                            color: #333;
                        }
                        
                        .container {
                            max-width: 500px;
                            margin: 0 auto;
                            background: white;
                            border-radius: 20px;
                            box-shadow: 0 20px 40px rgba(0,0,0,0.1);
                            overflow: hidden;
                            animation: slideIn 0.3s ease-out;
                        }
                        
                        @keyframes slideIn {
                            from { opacity: 0; transform: translateY(30px); }
                            to { opacity: 1; transform: translateY(0); }
                        }
                        
                        .header {
                            background: linear-gradient(135deg, #0f0f23 0%, #1a1a2e 50%, #16213e 100%);
                            color: white;
                            text-align: center;
                            padding: 30px 20px;
                        }
                        
                        h1 {
                            font-size: 2.2em;
                            font-weight: 300;
                            margin-bottom: 5px;
                        }
                        
                        .subtitle {
                            opacity: 0.9;
                            font-size: 1.1em;
                        }
                        
                        .content {
                            padding: 40px 30px;
                        }
                        
                        h2 {
                            color: #4a5568;
                            margin-bottom: 25px;
                            font-weight: 500;
                            font-size: 1.4em;
                        }
                        
                        .input-group {
                            margin-bottom: 20px;
                        }
                        
                        input {
                            width: 100%;
                            padding: 15px 20px;
                            border: 2px solid #e2e8f0;
                            border-radius: 12px;
                            font-size: 16px;
                            transition: all 0.15s ease;
                            background: #f8fafc;
                        }
                        
                        input:focus {
                            outline: none;
                            border-color: #667eea;
                            background: white;
                            box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
                        }
                        
                        .button-group {
                            display: flex;
                            gap: 12px;
                            flex-wrap: wrap;
                            margin-top: 25px;
                        }
                        
                        button {
                            flex: 1;
                            min-width: 120px;
                            padding: 15px 20px;
                            border: none;
                            border-radius: 12px;
                            font-size: 16px;
                            font-weight: 500;
                            cursor: pointer;
                            transition: all 0.15s ease;
                            position: relative;
                            overflow: hidden;
                        }
                        
                        .btn-primary {
                            background: linear-gradient(135deg, #0f0f23 0%, #1a1a2e 50%, #16213e 100%);
                            color: white;
                        }
                        
                        .btn-success {
                            background: linear-gradient(135deg, #48bb78 0%, #38a169 100%);
                            color: white;
                        }
                        
                        .btn-warning {
                            background: linear-gradient(135deg, #ed8936 0%, #dd6b20 100%);
                            color: white;
                        }
                        
                        .btn-info {
                            background: linear-gradient(135deg, #4299e1 0%, #3182ce 100%);
                            color: white;
                        }
                        
                        button:hover {
                            transform: translateY(-2px);
                            box-shadow: 0 10px 25px rgba(0,0,0,0.15);
                        }
                        
                        button:active {
                            transform: translateY(0);
                        }
                        
                        button.loading {
                            pointer-events: none;
                            opacity: 0.8;
                        }
                        
                        button.loading::after {
                            content: '';
                            position: absolute;
                            top: 50%;
                            left: 50%;
                            transform: translate(-50%, -50%);
                            width: 20px;
                            height: 20px;
                            border: 2px solid transparent;
                            border-top: 2px solid white;
                            border-radius: 50%;
                            animation: spin 0.8s linear infinite;
                        }
                        
                        @keyframes spin {
                            to { transform: translate(-50%, -50%) rotate(360deg); }
                        }
                        
                        .result {
                            margin: 25px 0 0 0;
                            border-radius: 12px;
                            transition: all 0.15s ease;
                        }
                        
                        .success {
                            background: linear-gradient(135deg, #c6f6d5 0%, #9ae6b4 100%);
                            color: #22543d;
                            padding: 20px;
                            border-left: 4px solid #48bb78;
                        }
                        
                        .error {
                            background: linear-gradient(135deg, #fed7d7 0%, #feb2b2 100%);
                            color: #742a2a;
                            padding: 20px;
                            border-left: 4px solid #f56565;
                        }
                        
                        .fade-in {
                            animation: fadeIn 0.2s ease-in;
                        }
                        
                        @keyframes fadeIn {
                            from { opacity: 0; transform: translateX(-20px); }
                            to { opacity: 1; transform: translateX(0); }
                        }
                        
                        @media (max-width: 640px) {
                            body {
                                padding: 10px;
                            }
                            
                            .container {
                                margin: 10px auto;
                                border-radius: 15px;
                            }
                            
                            .content {
                                padding: 30px 20px;
                            }
                            
                            h1 {
                                font-size: 1.8em;
                            }
                            
                            .button-group {
                                flex-direction: column;
                            }
                            
                            button {
                                min-width: auto;
                            }
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Banking App</h1>
                            <div class="subtitle">Secure Digital Banking</div>
                        </div>
                        
                        <div class="content">
                            <div id="login-section">
                                <h2>Welcome Back</h2>
                                <div class="input-group">
                                    <input type="text" id="username" placeholder="Enter your username">
                                </div>
                                <div class="button-group">
                                    <button class="btn-primary" onclick="login()">Login</button>
                                </div>
                            </div>
                            
                            <div id="banking-section" style="display: none;">
                                <h2>Banking Operations</h2>
                                <div class="input-group">
                                    <input type="number" id="amount" placeholder="Enter amount" step="0.01">
                                </div>
                                <div class="button-group">
                                    <button class="btn-success" onclick="deposit()">Deposit</button>
                                    <button class="btn-warning" onclick="withdraw()">Withdraw</button>
                                    <button class="btn-info" onclick="getBalance()">Check Balance</button>
                                </div>
                            </div>
                            
                            <div id="result"></div>
                        </div>
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
                            const buttons = document.querySelectorAll('button');
                            const activeButton = event.target;
                            
                            // Add loading state
                            activeButton.classList.add('loading');
                            activeButton.disabled = true;
                            
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
                            .catch(error => showResult('Error: ' + error, false))
                            .finally(() => {
                                // Remove loading state
                                activeButton.classList.remove('loading');
                                activeButton.disabled = false;
                            });
                        }
                        
                        function showResult(message, isSuccess) {
                            const result = document.getElementById('result');
                            result.innerHTML = '<div class="result ' + (isSuccess ? 'success' : 'error') + ' fade-in">' + message + '</div>';
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