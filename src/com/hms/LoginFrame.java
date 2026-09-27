package com.hms;

import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginFrame() {

        setTitle("Hospital Management System - Login");
        setSize(400, 280);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel titleLabel = new JLabel("HOSPITAL LOGIN", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 15));

        usernameField = new JTextField();
        passwordField = new JPasswordField();

        formPanel.add(new JLabel("Username"));
        formPanel.add(usernameField);

        formPanel.add(new JLabel("Password"));
        formPanel.add(passwordField);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        JButton loginButton = new JButton("Login");
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(loginButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);

        loginButton.addActionListener(e -> attemptLogin());

        passwordField.addActionListener(e -> attemptLogin());

        setVisible(true);
    }

    private void attemptLogin() {

        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password.");
            return;
        }

        String sql = "SELECT password, role FROM Users WHERE username = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    JOptionPane.showMessageDialog(this, "Invalid username or password.");
                    passwordField.setText("");
                    return;
                }

                String storedPassword = rs.getString("password");
                String role = rs.getString("role");
                

                if (!password.equals(storedPassword)) {
                    JOptionPane.showMessageDialog(this, "Invalid username or password.");
                    passwordField.setText("");
                    return;
                }

                dispose();
                SwingUtilities.invokeLater(() -> new MainMenu(role));
            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginFrame::new);
    }
}
