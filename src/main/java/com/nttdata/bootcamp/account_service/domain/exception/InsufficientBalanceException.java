package com.nttdata.bootcamp.account_service.domain.exception;

/**
 * Domain business exception thrown when an account transaction (withdrawal, transfer, fee debit)
 * exceeds the available account balance
 *
 * Technical & Business Rules:
 * - Pure Java domain exception independent of Spring Framework or HTTP transport layer
 * - Projects bank account from falling into unauthorized negative balances
 * - Provides overloaded constructors for custom message or audit context parameters
 */
public class InsufficientBalanceException extends RuntimeException{
    private static final long serialVersionUID = 1L;


    /**
     * Constructs a new InsufficientBalanceException with a custom detail message
     *
     * @param message
     */
    public InsufficientBalanceException(String message){
        super(message);
    }


    /**
     * Constructs a new InsufficientBalanceException with formatted account balance details
     *
     * @param accountNumber Unique bank account number.
     * @param currentBalance Current available account balance.
     * @param requestAmount Transaction debit amount attempted.
     */
    public InsufficientBalanceException(String accountNumber, Double currentBalance, Double requestAmount){
        super(String.format("Transaction rejected for account '%s'. Available balance: S/ %.2f, Request amount: S/ %.2f",
                accountNumber, currentBalance,requestAmount));
    }
}
