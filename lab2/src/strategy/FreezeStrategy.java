package strategy;

import model.Account;
import model.AccountStatus;
import model.Transaction;
import service.AccountService;

public class FreezeStrategy implements TransactionStrategy {
    private final AccountService accountService;

    public FreezeStrategy(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public void execute(Transaction tx) {
        Account acc = accountService.get(tx.getToId());
        if (acc == null) return;

        acc.toggleFreeze();

        accountService.update(acc);
    }
}
