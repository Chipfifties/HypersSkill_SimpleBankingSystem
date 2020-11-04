package banking;

import org.sqlite.SQLiteDataSource;

import java.sql.*;
import java.util.Arrays;
import java.util.Scanner;

public class Main {
    private static final Scanner sc = new Scanner(System.in);
    private static String DBUrl = "C:\\sqlite\\DBs\\card.db";

    public static void main(String[] args) {
        if (Arrays.asList(args).contains("-fileName")) {
            DBUrl = Arrays.asList(args).get(Arrays.asList(args).indexOf("-fileName") + 1);
        }

        String url = "jdbc:sqlite:" + DBUrl;

        SQLiteDataSource ds = new SQLiteDataSource();
        ds.setUrl(url);

        try (Connection con = ds.getConnection()) {
            if (!tableExists(con)) {
                createNewTable(con);
            }

            Accounts accounts = new Accounts(con);
            while (true) {
                System.out.println("1. Create an account");
                System.out.println("2. Log into account");
                System.out.println("0. Exit");

                String action = sc.nextLine();
                switch (action) {
                    case "1":
                        accounts.createAccount();
                        break;
                    case "2":
                        accounts.logIntoAccount();
                        break;
                    case "0":
                        return;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private static boolean tableExists(Connection con) throws SQLException {
        DatabaseMetaData meta = con.getMetaData();
        ResultSet table = meta.getTables(null, null, "card", null);
        return table.next();
    }

    private static void createNewTable(Connection con) throws SQLException {
        Statement statement = con.createStatement();
        statement.execute("CREATE TABLE IF NOT EXISTS card (" +
                "id INTEGER UNIQUE, " +
                "number TEXT, " +
                "pin TEXT," +
                "balance INTEGER DEFAULT 0);");
    }
}
