package app;

import model.Account;
import model.AccountStatus;
import model.User;
import processor.AsyncTransactionProcessor;
import service.AccountService;
import service.UserService;
import model.Transaction;
import model.TransactionType;

import javax.swing.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class UI extends JFrame {
    private final AccountService accountService;
    private final AsyncTransactionProcessor processor;
    private final UserService userService;

    private final DefaultListModel<Account> accountListModel = new DefaultListModel<>();
    private final JList<Account> accountList = new JList<>(accountListModel);
    private final JTextField amountField = new JTextField(10);
    private final JTextField targetField = new JTextField(20);

    public UI(AccountService accountService, AsyncTransactionProcessor processor, UserService userService) {
        this.accountService = accountService;
        this.processor = processor;
        this.userService = userService;

        setTitle("Bank UI");
        setSize(600, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        updateAccounts();

        panel.add(new JScrollPane(accountList));
        panel.add(new JLabel("Сумма:"));
        panel.add(amountField);
        panel.add(new JLabel("Счет получателя (для перевода, UUID):"));
        panel.add(targetField);

        JButton depositBtn = new JButton("Пополнить");
        JButton withdrawBtn = new JButton("Снять");
        JButton transferBtn = new JButton("Перевести");
        JButton freezeBtn = new JButton("Заморозить/Разморозить");
        JButton addAccountBtn = new JButton("Добавить счет");
        JButton addUserBtn = new JButton("Добавить пользователя");

        // Пополнить
        depositBtn.addActionListener(e -> {
            Account acc = accountList.getSelectedValue();
            if (acc == null) { JOptionPane.showMessageDialog(this, "Выберите счет"); return; }
            if (checkFrozen(acc)) return;

            BigDecimal amt;
            try { amt = new BigDecimal(amountField.getText()); }
            catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "Введите корректную сумму"); return; }

            processor.submit(new Transaction(TransactionType.DEPOSIT, amt, null, acc.getId()))
                    .thenRun(() -> SwingUtilities.invokeLater(this::updateAccounts));
        });

        // Снять
        withdrawBtn.addActionListener(e -> {
            Account acc = accountList.getSelectedValue();
            if (acc == null) { JOptionPane.showMessageDialog(this, "Выберите счет"); return; }
            if (checkFrozen(acc)) return;

            BigDecimal amt;
            try { amt = new BigDecimal(amountField.getText()); }
            catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "Введите корректную сумму"); return; }

            processor.submit(new Transaction(TransactionType.WITHDRAW, amt, acc.getId(), null))
                    .thenRun(() -> SwingUtilities.invokeLater(this::updateAccounts));
        });

        // Перевод
        transferBtn.addActionListener(e -> {
            Account from = accountList.getSelectedValue();
            if (from == null) { JOptionPane.showMessageDialog(this, "Выберите исходный счет"); return; }
            if (checkFrozen(from)) return;

            BigDecimal amt;
            try { amt = new BigDecimal(amountField.getText()); }
            catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "Введите корректную сумму"); return; }

            UUID toId;
            try { toId = UUID.fromString(targetField.getText()); }
            catch (IllegalArgumentException ex) { JOptionPane.showMessageDialog(this, "Введите корректный UUID счета получателя"); return; }

            Account to = accountService.get(toId);
            if (to == null) { JOptionPane.showMessageDialog(this, "Счет получателя не найден"); return; }
            if (checkFrozen(to)) { JOptionPane.showMessageDialog(this, "Нельзя переводить на замороженный счет"); return; }

            processor.submit(new Transaction(TransactionType.TRANSFER, amt, from.getId(), toId))
                    .thenRun(() -> SwingUtilities.invokeLater(this::updateAccounts));
        });

        // Заморозить / Разморозить
        freezeBtn.addActionListener(e -> {
            Account acc = accountList.getSelectedValue();
            if (acc == null) { JOptionPane.showMessageDialog(this, "Выберите счет"); return; }

            acc.toggleFreeze();
            accountService.update(acc);
            updateAccounts();
        });

        // Добавить счет
        addAccountBtn.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(this, "Введите начальный баланс нового счета:");
            if (input == null) return;

            BigDecimal initial;
            try { initial = new BigDecimal(input); }
            catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "Введите корректное число"); return; }

            List<User> users = userService.getAll();
            if (users.isEmpty()) { JOptionPane.showMessageDialog(this, "Нет пользователей"); return; }

            User user = users.get(0); // выбираем первого пользователя по умолчанию
            Account newAcc = new Account(initial, user.getId(), user.getNickname());
            accountService.save(newAcc);
            updateAccounts();
        });

        // Добавить пользователя
        addUserBtn.addActionListener(e -> {
            String nickname = JOptionPane.showInputDialog(this, "Введите никнейм нового пользователя:");
            if (nickname == null || nickname.isBlank()) return;

            User newUser = new User(nickname);
            userService.save(newUser);
            JOptionPane.showMessageDialog(this, "Пользователь " + nickname + " добавлен!");
        });

        panel.add(depositBtn);
        panel.add(withdrawBtn);
        panel.add(transferBtn);
        panel.add(freezeBtn);
        panel.add(addAccountBtn);
        panel.add(addUserBtn);

        add(panel);
        setVisible(true);
    }

    private boolean checkFrozen(Account acc) {
        if (acc.getStatus() == AccountStatus.FROZEN) {
            JOptionPane.showMessageDialog(this, "Этот счет заморожен. Операции невозможны.");
            return true;
        }
        return false;
    }

    private void updateAccounts() {
        accountListModel.clear();
        List<Account> accounts = accountService.getAll();
        for (Account acc : accounts) {
            User owner = userService.get(acc.getUserId());
            String displayName = acc.toString() + " | Владелец: " + (owner != null ? owner.getNickname() : "Неизвестен");
            accountListModel.addElement(acc);
        }
    }
}
