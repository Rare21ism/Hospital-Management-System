package com.hms;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/hospital";

    private static final String USER = "root";

    private static final String PASSWORD =
            "Antara@1209";

    public static Connection getConnection() {

        try {
            return DriverManager.getConnection(
                    URL,
                    USER,
                    PASSWORD
            );

        } catch (Exception e) {
            System.out.println("Database Connection Failed!");
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {

        Connection con = getConnection();

        if (con != null) {
            System.out.println("Database Connected Successfully!");

            try {
                con.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
