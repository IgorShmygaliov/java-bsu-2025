package strategy;

import model.Transaction;
import model.Account;
import service.AccountService;

public class TransferStrategy implements TransactionStrategy {
    private final AccountService accountService;

    public TransferStrategy(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public void execute(Transaction tx) {
        Account from = accountService.get(tx.getFromId());
        Account to = accountService.get(tx.getToId());
        from.withdraw(tx.getAmount());
        to.deposit(tx.getAmount());
        accountService.update(from);
        accountService.update(to);
    }
}
