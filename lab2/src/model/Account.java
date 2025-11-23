package model;

import java.math.BigDecimal;
import java.util.UUID;

public class Account {
    private UUID id;
    private BigDecimal balance;
    private AccountStatus status;
    private UUID userId;
    private String userName;

    public Account(BigDecimal initial, UUID userId, String userName) {
        this.id = UUID.randomUUID();
        this.balance = initial;
        this.status = AccountStatus.ACTIVE;
        this.userId = userId;
        this.userName = userName;
    }

    public UUID getId() { return id; }
    public BigDecimal getBalance() { return balance; }
    public AccountStatus getStatus() { return status; }
    public UUID getUserId() { return userId; }
    public String getUserName() { return userName; }

    public void deposit(BigDecimal amt) { balance = balance.add(amt); }
    public void withdraw(BigDecimal amt) { balance = balance.subtract(amt); }

    public void toggleFreeze() {
        if (status == AccountStatus.ACTIVE) status = AccountStatus.FROZEN;
        else status = AccountStatus.ACTIVE;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", balance=" + balance +
                ", status=" + status +
                ", userName=" + userName +
                '}';
    }
}
