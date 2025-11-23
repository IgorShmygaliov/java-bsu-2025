package model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class User {
    private UUID id;
    private String nickname;
    private List<UUID> accountIds;

    public User(String nickname) {
        this.id = UUID.randomUUID();
        this.nickname = nickname;
        this.accountIds = new ArrayList<>();
    }

    public UUID getId() { return id; }
    public String getNickname() { return nickname; }
    public List<UUID> getAccountIds() { return accountIds; }

    public void setNickname(String nickname) { this.nickname = nickname; }
    public void addAccountId(UUID accountId) { accountIds.add(accountId); }

    @Override
    public String toString() {
        return "User{id=" + id + ", nickname='" + nickname + "', accounts=" + accountIds + '}';
    }
}
