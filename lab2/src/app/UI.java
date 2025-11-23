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

    private final DefaultListModel<String> userListModel = new DefaultListModel<>();
    private final JList<String> userList = new JList<>(userListModel);

    private final DefaultListModel<String> accountListModel = new DefaultListModel<>();
    private final JList<String> accountList = new JList<>(accountListModel);

    private final JTextField amountField = new JTextField(10);
    private final JTextField targetField = new JTextField(20);

    public UI(AccountService accountService, AsyncTransactionProcessor processor, UserService userService) {
        this.accountService = accountService;
        this.processor = processor;
        this.userService = userService;

        setTitle("Bank UI");
        setSize(750, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Кнопки
        JButton addUserBtn = new JButton("Добавить пользователя");
        JButton deleteUserBtn = new JButton("Удалить выбранного пользователя");
        JButton addAccountBtn = new JButton("Добавить счет выбранному пользователю");
        JButton deleteAccountBtn = new JButton("Удалить выбранный счет");
        JButton depositBtn = new JButton("Пополнить");
        JButton withdrawBtn = new JButton("Снять");
        JButton transferBtn = new JButton("Перевести");
        JButton freezeBtn = new JButton("Заморозить/Разморозить");

        // Панель пользователей
        panel.add(new JLabel("Пользователи:"));
        panel.add(new JScrollPane(userList));
        panel.add(addUserBtn);
        panel.add(deleteUserBtn);

        // Панель счетов
        panel.add(new JLabel("Счета:"));
        panel.add(new JScrollPane(accountList));
        panel.add(new JLabel("Сумма:"));
        panel.add(amountField);
        panel.add(new JLabel("Счет получателя (UUID, для перевода):"));
        panel.add(targetField);

        panel.add(addAccountBtn);
        panel.add(deleteAccountBtn);
        panel.add(depositBtn);
        panel.add(withdrawBtn);
        panel.add(transferBtn);
        panel.add(freezeBtn);

        add(panel);

        addUserBtn.addActionListener(e -> {
            String nickname = JOptionPane.showInputDialog(this, "Введите никнейм пользователя:");
            if (nickname == null || nickname.isEmpty()) return;
            User user = new User(nickname);
            userService.save(user);
            updateUsers();
        });

        deleteUserBtn.addActionListener(e -> {
            int selectedUserIndex = userList.getSelectedIndex();
            if (selectedUserIndex == -1) {
                JOptionPane.showMessageDialog(this, "Выберите пользователя");
                return;
            }

            User user = userService.getAll().get(selectedUserIndex);

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Вы уверены, что хотите удалить пользователя " + user.getNickname() + " и все его счета?",
                    "Подтверждение удаления",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                List<Account> accounts = accountService.getAll();
                for (Account acc : accounts) {
                    if (user.getId().equals(acc.getUserId())) {
                        accountService.delete(acc.getId());
                    }
                }
                userService.delete(user.getId());
                updateUsers();
                updateAccounts();
            }
        });

        addAccountBtn.addActionListener(e -> {
            int selectedUserIndex = userList.getSelectedIndex();
            if (selectedUserIndex == -1) {
                JOptionPane.showMessageDialog(this, "Выберите пользователя");
                return;
            }

            String input = JOptionPane.showInputDialog(this, "Введите начальный баланс нового счета:");
            if (input == null) return;

            BigDecimal initial;
            try {
                initial = new BigDecimal(input);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Введите корректное число");
                return;
            }

            User user = userService.getAll().get(selectedUserIndex);
            Account newAcc = new Account(initial, user.getId(), user.getNickname());
            accountService.save(newAcc);
            updateAccounts();
        });

        deleteAccountBtn.addActionListener(e -> {
            Account acc = getSelectedAccount();
            if (acc == null) return;

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Вы уверены, что хотите удалить счет " + acc.getId() + "?",
                    "Подтверждение удаления",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                accountService.delete(acc.getId());
                updateAccounts();
            }
        });

        depositBtn.addActionListener(e -> {
            Account acc = getSelectedAccount();
            if (acc == null) return;
            if (checkFrozen(acc)) return;

            BigDecimal amt;
            try {
                amt = new BigDecimal(amountField.getText());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Введите корректную сумму");
                return;
            }
            if (amt.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "Сумма должна быть положительной");
                return;
            }

            processor.submit(new Transaction(TransactionType.DEPOSIT, amt, null, acc.getId()))
                    .thenRun(() -> SwingUtilities.invokeLater(this::updateAccounts));
        });

        withdrawBtn.addActionListener(e -> {
            Account acc = getSelectedAccount();
            if (acc == null) return;
            if (checkFrozen(acc)) return;

            BigDecimal amt;
            try {
                amt = new BigDecimal(amountField.getText());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Введите корректную сумму");
                return;
            }
            if (amt.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "Сумма должна быть положительной");
                return;
            }
            processor.submit(new Transaction(TransactionType.WITHDRAW, amt, acc.getId(), null))
                    .thenRun(() -> SwingUtilities.invokeLater(this::updateAccounts));
        });

        transferBtn.addActionListener(e -> {
            Account from = getSelectedAccount();
            if (from == null) return;
            if (checkFrozen(from)) return;

            BigDecimal amt;
            try {
                amt = new BigDecimal(amountField.getText());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Введите корректную сумму");
                return;
            }
            if (amt.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "Сумма должна быть положительной");
                return;
            }

            UUID toId;
            try {
                toId = UUID.fromString(targetField.getText());
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, "Введите корректный UUID счета получателя");
                return;
            }

            Account to = accountService.get(toId);
            if (to == null) {
                JOptionPane.showMessageDialog(this, "Счет получателя не найден");
                return;
            }
            if (checkFrozen(to)) {
                JOptionPane.showMessageDialog(this, "Нельзя переводить на замороженный счет");
                return;
            }

            processor.submit(new Transaction(TransactionType.TRANSFER, amt, from.getId(), toId))
                    .thenRun(() -> SwingUtilities.invokeLater(this::updateAccounts));
        });

        freezeBtn.addActionListener(e -> {
            Account acc = getSelectedAccount();
            if (acc == null) return;

            processor.submit(new Transaction(TransactionType.FREEZE, BigDecimal.ZERO, null, acc.getId()))
                    .thenRun(() -> SwingUtilities.invokeLater(this::updateAccounts));
        });

        // Инициализация списков
        updateUsers();
        updateAccounts();

        setVisible(true);
    }

    private Account getSelectedAccount() {
        int index = accountList.getSelectedIndex();
        if (index == -1) {
            JOptionPane.showMessageDialog(this, "Выберите счет");
            return null;
        }
        List<Account> accounts = accountService.getAll();
        return accounts.get(index);
    }

    private boolean checkFrozen(Account acc) {
        if (acc.getStatus() == AccountStatus.FROZEN) {
            JOptionPane.showMessageDialog(this, "Этот счет заморожен. Операции невозможны.");
            return true;
        }
        return false;
    }

    private void updateUsers() {
        userListModel.clear();
        List<User> users = userService.getAll();
        for (User user : users) {
            userListModel.addElement(user.getNickname() + " | " + user.getId());
        }
    }

    private void updateAccounts() {
        accountListModel.clear();
        List<Account> accounts = accountService.getAll();
        for (Account acc : accounts) {
            String displayName = acc.toString() + " | Владелец: " + (acc.getUserName() != null ? acc.getUserName() : "Неизвестен");
            accountListModel.addElement(displayName);
        }
    }
}
