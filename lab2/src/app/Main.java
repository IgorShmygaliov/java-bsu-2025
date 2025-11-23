package app;

import model.Account;
import model.User;
import service.AccountService;
import service.UserService;
import processor.AsyncTransactionProcessor;
import strategy.TransactionStrategyFactory;
import observer.EventBus;

import java.math.BigDecimal;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException {
        UserService userService = new UserService();
        AccountService accountService = new AccountService();

        if (userService.getAll().isEmpty()) {
            User u1 = new User("Alice");
            User u2 = new User("Bob");

            userService.save(u1);
            userService.save(u2);

            Account a1 = new Account(BigDecimal.valueOf(500), u1.getId(), u1.getNickname());
            Account a2 = new Account(BigDecimal.valueOf(1000), u2.getId(), u2.getNickname());
            Account a3 = new Account(BigDecimal.valueOf(700), u1.getId(), u1.getNickname());

            accountService.save(a1);
            accountService.save(a2);
            accountService.save(a3);
        }

        TransactionStrategyFactory factory = new TransactionStrategyFactory(accountService);
        AsyncTransactionProcessor processor = new AsyncTransactionProcessor(factory);

        EventBus.get().subscribe(tx -> System.out.println("LOG: " + tx.getType()));

        UI ui = new UI(accountService, processor, userService);
    }
}
