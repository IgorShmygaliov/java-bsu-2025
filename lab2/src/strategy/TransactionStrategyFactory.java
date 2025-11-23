package strategy;

import model.Transaction;
import model.TransactionType;
import service.AccountService;

public class TransactionStrategyFactory {
    private final AccountService accountService;

    public TransactionStrategyFactory(AccountService accountService) {
        this.accountService = accountService;
    }

    public TransactionStrategy get(Transaction tx) {
        switch (tx.getType()) {
            case DEPOSIT: return new DepositStrategy(accountService);
            case WITHDRAW: return new WithdrawStrategy(accountService);
            case TRANSFER: return new TransferStrategy(accountService);
            case FREEZE: return new FreezeStrategy(accountService);
            default: throw new IllegalArgumentException("Unknown transaction type: " + tx.getType());
        }
    }
}
