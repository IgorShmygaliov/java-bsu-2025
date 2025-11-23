package service;

import db.Database;
import model.Account;
import model.AccountStatus;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AccountService {

    public void save(Account acc) {
        try (PreparedStatement ps = Database.getInstance().getConnection()
                .prepareStatement(
                        "INSERT OR REPLACE INTO accounts(id, balance, status, user_id, user_name) VALUES(?,?,?,?,?)")) {
            ps.setString(1, acc.getId().toString());
            ps.setString(2, acc.getBalance().toPlainString());
            ps.setString(3, acc.getStatus().name());
            ps.setString(4, acc.getUserId() != null ? acc.getUserId().toString() : null);
            ps.setString(5, acc.getUserName());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Account get(UUID id) {
        try (PreparedStatement ps = Database.getInstance().getConnection()
                .prepareStatement("SELECT balance, status, user_id, user_name FROM accounts WHERE id=?")) {
            ps.setString(1, id.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                BigDecimal balance = new BigDecimal(rs.getString("balance"));
                UUID userId = rs.getString("user_id") != null ? UUID.fromString(rs.getString("user_id")) : null;
                String userName = rs.getString("user_name");
                Account acc = new Account(balance, userId, userName);
                if (AccountStatus.valueOf(rs.getString("status")) == AccountStatus.FROZEN) {
                    acc.toggleFreeze();
                }
                java.lang.reflect.Field field = Account.class.getDeclaredField("id");
                field.setAccessible(true);
                field.set(acc, id);
                return acc;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Account> getAll() {
        List<Account> list = new ArrayList<>();
        try (Statement stmt = Database.getInstance().getConnection().createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT id FROM accounts");
            while (rs.next()) {
                UUID id = UUID.fromString(rs.getString("id"));
                Account acc = get(id);
                if (acc != null) list.add(acc);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void update(Account acc) {
        save(acc);
    }

    public void delete(UUID id) {
        try (PreparedStatement ps = Database.getInstance().getConnection()
                .prepareStatement("DELETE FROM accounts WHERE id=?")) {
            ps.setString(1, id.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
