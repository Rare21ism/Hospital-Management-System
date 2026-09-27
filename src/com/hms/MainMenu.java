package com.hms;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class MainMenu extends JFrame {

    private final String role;

    public MainMenu(String role) {

        this.role = role;

        setTitle("Hospital Management System (" + role + ")");
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(20, 40, 20, 40));

        JLabel titleLabel = new JLabel("HOSPITAL MANAGEMENT SYSTEM", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 10, 10));

        JButton patientButton = new JButton("Patient Management");
        JButton doctorButton = new JButton("Doctor Management");
        JButton billButton = new JButton("Bill Management");
        JButton logoutButton = new JButton("Logout");

        for (JButton b : new JButton[]{
                patientButton, doctorButton, billButton, logoutButton
        }) {
            b.setFont(new Font("Arial", Font.PLAIN, 15));
        }

        buttonPanel.add(patientButton);
        buttonPanel.add(doctorButton);
        buttonPanel.add(billButton);
        buttonPanel.add(logoutButton);

        mainPanel.add(buttonPanel, BorderLayout.CENTER);

        setContentPane(mainPanel);

        patientButton.addActionListener(e -> {
            dispose();
            new PatientManagement(role);
        });

        doctorButton.addActionListener(e -> {
            dispose();
            new DoctorManagement(role);
        });

        billButton.addActionListener(e -> {
            dispose();
            new BillManagement(role);
        });

        logoutButton.addActionListener(e -> {
            dispose();
            new LoginFrame();
        });

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainMenu("Admin"));
    }
}
