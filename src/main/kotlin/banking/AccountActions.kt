package banking

import kotlinx.serialization.Serializable

@Serializable
data class BankingResponse(val success: Boolean, val message: String, val balance: Double? = null)

@Serializable
data class TransactionRequest(val amount: Double)


class AccountActions {
    var balance: Double = 0.0

    fun deposit(amount: Double): BankingResponse {
        return if (amount <= 0) {
            BankingResponse(false, "Deposit unsuccessful: Invalid value. Amount deposited must be greater than zero.")
        } else {
            try {
                balance += amount
                BankingResponse(true, "Deposit successful.", balance)
            } catch (e: Exception) {
                BankingResponse(false, "Deposit unsuccessful: An error occurred.")
            }
        }
    }

    /**
    *   
    */
    fun withdraw(amount: Double): BankingResponse {
        return if (amount <= 0) {
            BankingResponse(false, "Withdrawal unsuccessful: Invalid value. Amount to withdraw must be greater than zero.")
        } else if (balance < amount) {
            BankingResponse(false, "Withdrawal unsuccessful: Insufficient balance.")
        } else {
            try {
                balance -= amount
                BankingResponse(true, "Withdrawal successful.", balance)
            } catch (e: Exception) {
                BankingResponse(false, "Withdrawal unsuccessful: An error occurred.")
            }
        }
    }
    
    fun getBalance(): BankingResponse {
        return BankingResponse(true, "Balance retrieved successfully.", balance)
    }
}