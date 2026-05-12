package banking

import java.util.*

fun main() {
    val scanner = Scanner(System.`in`)
    val lightBlueBackground = "\u001B[104m"
    val resetTerminalColors = "\u001B[0m"

    print(lightBlueBackground)
    Runtime.getRuntime().addShutdownHook(Thread {
        print(resetTerminalColors)
    })

    println("Welcome to banking app")

    val validUserNames = listOf("karl", "johan", "sophie", "manuel", "lucas", "emma", "liam", "noah", "mila", "lina")

    var name: String
    while (true) {
        println("Please log in using username:")
    name = scanner.nextLine()
    if (validUserNames.contains(name)) {
        break
    } else {
        println("Invalid username")
    }
    }


    val accActions = AccountActions()
//    val user = User(name, account)

    var exit = false

    fun getAmount(): Double? {
        return try {
            val input = scanner.nextDouble()
            scanner.nextLine() // consume the newline character
            if (input <= 0) {
                println("Negative or zero amounts are not allowed.")
                null
            } else {
                input
            }
        } catch (e: InputMismatchException) {
            println("Invalid input. Please enter a number.")
            scanner.nextLine() // consume the newline character
            null
        }
    }

    fun getUserChoice(): Int? {
        return try {
            val rawInput = scanner.nextLine()
            if (rawInput.isBlank()) { // check if the input is empty or only contains whitespaces
                println("Input cannot be empty. Please enter a number.")
                return null
            }
            val choice = rawInput.trim().toInt() // trim the input and convert it to Int
            choice
        } catch (e: NumberFormatException) {
            println("Invalid input. Please enter a number.")
            null
        }
    }

    while (!exit) {
        println("\nAvailable operations:")
        println("1: Deposit")
        println("2: Withdraw")
        println("3: Check balance")
        println("4: Exit")

        val choice = getUserChoice() ?: continue
        when(choice) {
            1 -> {
                println("Enter amount to deposit: ")
                val amount = getAmount() ?: continue
                accActions.deposit(amount)
            }
            2 -> {
                println("Enter amount to withdraw: ")
                val amount = getAmount()
                if (amount != null) {
                    accActions.withdraw(amount)
                } else {
                    println("Invalid operation. Try again.")
                }
            }
            3 -> accActions.displayBalance()
            4 -> {
                exit = true
                print(resetTerminalColors)
            }
            else -> {
                println("Invalid operation. Try again.")
            }
        }

    }
}
