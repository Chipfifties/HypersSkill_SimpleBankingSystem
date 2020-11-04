package banking;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Accounts {
    private static final Scanner sc = new Scanner(System.in);
    private final Connection con;

    Accounts(Connection con) {
        this.con = con;
    }

    public void createAccount() throws SQLException {
        String cardNumber = LuhnAlgorithm.createNewCardNumber();
        String cardPin = String.format("%04d", ThreadLocalRandom.current().nextInt(9999));

        System.out.println("\nYour card has been created\nYour card number:\n" + cardNumber);
        System.out.println("Your card PIN:\n" + cardPin + "\n");

        // get max value from id column, or 0 if column is empty
        int id = con.createStatement()
                .executeQuery("SELECT COALESCE(MAX(id), 0) AS maxId FROM card")
                .getInt("maxId");

        // insert new card data
        String str = String.format("INSERT INTO card VALUES (%d, '%s', '%s', %d)", ++id, cardNumber, cardPin, 0);
        con.createStatement().execute(str);
    }

    public void logIntoAccount() throws SQLException {
        System.out.println("\nEnter your card number:");
        String currentCardNumber = sc.nextLine();
        System.out.println("Enter your pin:");
        String currentCardPin = sc.nextLine();

        boolean accountFound = false;

        ResultSet card = con.createStatement().executeQuery("SELECT * FROM card");
        while (card.next()) {
            String cardNumber = card.getString("number");
            String cardPin = card.getString("pin");

            if (cardNumber.equals(currentCardNumber) && cardPin.equals(currentCardPin)) {
                accountFound = true;
                System.out.println("\nYou have successfully logged in!\n");
                getAccountMenu(cardNumber);
            }
        }
        if (!accountFound) {
            System.out.println("\nWrong card number or PIN!\n");
        }
    }

    private void getAccountMenu(String cardNumber) throws SQLException {
        while (true) {
            System.out.println("1. Balance");
            System.out.println("2. Add income");
            System.out.println("3. Do transfer");
            System.out.println("4. Close account");
            System.out.println("5. Log out");
            System.out.println("0. Exit");

            String command = sc.nextLine();

            switch (command) {
                case "1":
                    System.out.println("\nBalance: " + getBalance(cardNumber) + "\n");
                    break;
                case "2":
                    addIncome(cardNumber);
                    break;
                case "3":
                    doTransfer(cardNumber);
                    break;
                case "4":
                    con.createStatement().execute("DELETE FROM card WHERE number=" + cardNumber);
                    System.out.println("\nThe account has been closed!\n");
                    return;
                case "5":
                    System.out.println("\nYou have successfully logged out!\n");
                    return;
                case "0":
                    System.exit(1);
            }
        }
    }

    private void addIncome(String cardNumber) throws SQLException {
        System.out.println("\nEnter income:");
        int balance = Integer.parseInt(sc.nextLine()) + getBalance(cardNumber);
        con.createStatement().execute(getStatementString(balance, cardNumber));
        System.out.println("Income was added!\n");
    }

    private void doTransfer(String cardNumber) throws SQLException {
        System.out.println("\nTransfer\nEnter card number:");
        String targetCardNumber = sc.nextLine();

        if (!LuhnAlgorithm.isValidCardNumber(targetCardNumber)) {
            System.out.println("Probably you made mistake in the card number. Please try again!\n");
            return;
        } else if (targetCardNumber.equals(cardNumber)) {
            System.out.println("You can't transfer money to the same account!");
            return;
        } else if (!cardExists(targetCardNumber)) {
            System.out.println("Such a card does not exist.\n");
            return;
        }

        System.out.println("Enter how much money you want to transfer:");
        int sum = Integer.parseInt(sc.nextLine());

        if (sum > getBalance(cardNumber)) {
            System.out.println("Not enough money!\n");
            return;
        }

        con.createStatement().execute(getStatementString(getBalance(targetCardNumber) + sum, targetCardNumber));
        con.createStatement().execute(getStatementString(getBalance(cardNumber) - sum, cardNumber));

        System.out.println("Success!\n");
    }

    private String getStatementString(int balance, String cardNumber) {
        return String.format("UPDATE card SET balance = %d WHERE number = %s", balance, cardNumber);
    }

    private int getBalance(String cardNumber) throws SQLException {
        return con.createStatement()
                .executeQuery("SELECT balance AS b FROM card WHERE number=" + cardNumber)
                .getInt("b");
    }

    private boolean cardExists(String cardNumber) throws SQLException {
        ResultSet card = con.createStatement().executeQuery("SELECT * FROM card");
        while (card.next()) {
            if (cardNumber.equals(card.getString("number"))) {
                return true;
            }
        }
        return false;
    }
}
