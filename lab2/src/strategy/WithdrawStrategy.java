package strategy;

import model.Transaction;
import model.Account;
import service.AccountService;

public class WithdrawStrategy implements TransactionStrategy {
    private final AccountService accountService;

    public WithdrawStrategy(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public void execute(Transaction tx) {
        Account acc = accountService.get(tx.getFromId());
        acc.withdraw(tx.getAmount());
        accountService.update(acc);
    }
}
