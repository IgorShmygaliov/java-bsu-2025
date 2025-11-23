package strategy;

import model.Transaction;
import model.Account;
import service.AccountService;

public class DepositStrategy implements TransactionStrategy {
    private final AccountService accountService;

    public DepositStrategy(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public void execute(Transaction tx) {
        Account acc = accountService.get(tx.getToId());
        acc.deposit(tx.getAmount());
        accountService.update(acc);
    }
}
