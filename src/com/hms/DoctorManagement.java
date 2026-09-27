package com.hms;

import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class DoctorManagement extends JFrame {

    private Integer selectedDoctorId;

    private JTextField nameField;
    private JTextField specializationField;
    private JTextField phoneField;
    private JTextField emailField;
    private JTextField searchIdField;

    private JTable doctorTable;
    private DefaultTableModel tableModel;

    private final String role;
    private JButton deleteButton;

    public DoctorManagement(String role) {

        this.role = role;

        setTitle("Doctor Management (" + role + ")");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("HOSPITAL MANAGEMENT SYSTEM");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 26));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Doctor Management");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(5));
        headerPanel.add(subtitleLabel);

        mainPanel.add(headerPanel, BorderLayout.NORTH);


        JPanel formPanel = new JPanel(new GridLayout(2, 4, 10, 10));
        formPanel.setBorder(
                BorderFactory.createTitledBorder("Doctor Details")
        );

        nameField = new JTextField();
        specializationField = new JTextField();
        phoneField = new JTextField();
        emailField = new JTextField();

        formPanel.add(new JLabel("Name"));
        formPanel.add(nameField);

        formPanel.add(new JLabel("Specialization"));
        formPanel.add(specializationField);

        formPanel.add(new JLabel("Phone"));
        formPanel.add(phoneField);

        formPanel.add(new JLabel("Email"));
        formPanel.add(emailField);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(
                new FlowLayout(FlowLayout.CENTER, 10, 5)
        );

        JButton addButton = new JButton("Add");
        JButton updateButton = new JButton("Update");
        deleteButton = new JButton("Delete");
        JButton searchButton = new JButton("Search");
        JButton clearButton = new JButton("Clear");
        JButton backButton = new JButton("Back to Menu");
        searchIdField = new JTextField(6);
        searchIdField.setToolTipText("Enter a doctor ID to search");

        
        if (!"Admin".equalsIgnoreCase(role)) {
            deleteButton.setEnabled(false);
            deleteButton.setToolTipText("Only Admin users can delete records.");
        }

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(new JLabel("Search ID"));
        buttonPanel.add(searchIdField);
        buttonPanel.add(searchButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(backButton);



        tableModel = new DefaultTableModel(
                new String[]{
                        "ID",
                        "Name",
                        "Specialization",
                        "Phone",
                        "Email"
                }, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };

        doctorTable = new JTable(tableModel);

        doctorTable.setRowHeight(25);
        doctorTable.getTableHeader().setFont(
                new Font("Arial", Font.BOLD, 13)
        );

        JScrollPane scrollPane = new JScrollPane(doctorTable);
        scrollPane.setBorder(
                BorderFactory.createTitledBorder("Doctor Records")
        );

        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));

        bottomPanel.add(scrollPane, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);


        addButton.addActionListener(e -> addDoctor());

        updateButton.addActionListener(e -> updateDoctor());

        deleteButton.addActionListener(e -> deleteDoctor());

        searchButton.addActionListener(e -> searchDoctor());
        searchIdField.addActionListener(e -> searchDoctor());

        clearButton.addActionListener(e -> {
            clearFields();
            viewDoctors();
        });

        backButton.addActionListener(e -> {
            dispose();
            new MainMenu(role);
        });

        doctorTable.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent e) {

                int row = doctorTable.getSelectedRow();

                if (row >= 0) {

                    selectedDoctorId = Integer.valueOf(
                            tableModel.getValueAt(row, 0).toString()
                    );

                    nameField.setText(
                            tableModel.getValueAt(row, 1).toString()
                    );

                    specializationField.setText(
                            tableModel.getValueAt(row, 2).toString()
                    );

                    phoneField.setText(
                            tableModel.getValueAt(row, 3).toString()
                    );

                    emailField.setText(
                            tableModel.getValueAt(row, 4).toString()
                    );
                }
            }
        });

        setContentPane(mainPanel);

        setVisible(true);

        viewDoctors();
    }

    private boolean validateForm() {

        String name = nameField.getText().trim();
        String specialization = specializationField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the doctor's name.");
            return false;
        }

        if (specialization.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a specialization.");
            return false;
        }

        if (phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a phone number.");
            return false;
        }

        if (!email.isEmpty() && !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.");
            return false;
        }

        return true;
    }

    private Integer getSelectedDoctorId() {

        int viewRow = doctorTable.getSelectedRow();
        if (viewRow >= 0) {
            int modelRow = doctorTable.convertRowIndexToModel(viewRow);
            selectedDoctorId = Integer.valueOf(tableModel.getValueAt(modelRow, 0).toString());
        }

        if (selectedDoctorId == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a doctor from the table first."
            );
            return null;
        }

        return selectedDoctorId;
    }

    private Integer getSearchDoctorId() {

        String value = searchIdField.getText().trim();
        if (value.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a doctor ID to search.");
            return null;
        }

        try {
            int id = Integer.parseInt(value);
            if (id <= 0) {
                throw new NumberFormatException();
            }
            return id;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Doctor ID must be a positive whole number.");
            return null;
        }
    }


    private void addDoctor() {

        if (!validateForm()) {
            return;
        }

        String sql =
                "INSERT INTO Doctor " +
                "(name, specialization, phone, email) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, nameField.getText().trim());
            ps.setString(2, specializationField.getText().trim());
            ps.setString(3, phoneField.getText().trim());
            ps.setString(4, emailField.getText().trim());

            ps.executeUpdate();

            String newId = null;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    newId = String.valueOf(keys.getLong(1));
                }
            }

            JOptionPane.showMessageDialog(
                    this,
                    newId != null
                            ? "Doctor Added Successfully! Assigned ID: " + newId
                            : "Doctor Added Successfully!"
            );

            clearFields();
            viewDoctors();

        } catch (Exception e) {

            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void viewDoctors() {

        tableModel.setRowCount(0);

        String sql = "SELECT * FROM Doctor";

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {

                tableModel.addRow(new Object[]{
                        rs.getInt("doctor_id"),
                        rs.getString("name"),
                        rs.getString("specialization"),
                        rs.getString("phone"),
                        rs.getString("email")
                });
            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void updateDoctor() {

        Integer id = getSelectedDoctorId();
        if (id == null) {
            return;
        }

        if (!validateForm()) {
            return;
        }

        String sql =
                "UPDATE Doctor SET name=?, specialization=?, phone=?, email=? " +
                "WHERE doctor_id=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nameField.getText().trim());
            ps.setString(2, specializationField.getText().trim());
            ps.setString(3, phoneField.getText().trim());
            ps.setString(4, emailField.getText().trim());
            ps.setInt(5, id);

            int rows = ps.executeUpdate();

            JOptionPane.showMessageDialog(
                    this,
                    rows > 0 ? "Doctor Updated Successfully!" : "Doctor ID not found."
            );

            clearFields();
            viewDoctors();

        } catch (Exception e) {

            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }


    private void deleteDoctor() {

        if (!"Admin".equalsIgnoreCase(role)) {
            JOptionPane.showMessageDialog(this, "Only Admin users can delete records.");
            return;
        }

        Integer id = getSelectedDoctorId();
        if (id == null) {
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete doctor ID " + id + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = "DELETE FROM Doctor WHERE doctor_id=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            int rows = ps.executeUpdate();

            JOptionPane.showMessageDialog(
                    this,
                    rows > 0 ? "Doctor Deleted Successfully!" : "Doctor ID not found."
            );

            clearFields();
            viewDoctors();

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Error: " + e.getMessage()
            );
        }
    }


    private void searchDoctor() {

        Integer id = getSearchDoctorId();
        if (id == null) {
            return;
        }

        String sql = "SELECT * FROM Doctor WHERE doctor_id=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {

                tableModel.setRowCount(0);

                if (rs.next()) {

                    selectedDoctorId = rs.getInt("doctor_id");

                    nameField.setText(rs.getString("name"));
                    specializationField.setText(rs.getString("specialization"));
                    phoneField.setText(rs.getString("phone"));
                    emailField.setText(rs.getString("email"));

                    tableModel.addRow(new Object[]{
                            rs.getInt("doctor_id"),
                            rs.getString("name"),
                            rs.getString("specialization"),
                            rs.getString("phone"),
                            rs.getString("email")
                    });

                } else {

                    JOptionPane.showMessageDialog(this, "Doctor ID not found.");
                    viewDoctors();
                }
            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }


    private void clearFields() {

        selectedDoctorId = null;
        searchIdField.setText("");
        nameField.setText("");
        specializationField.setText("");
        phoneField.setText("");
        emailField.setText("");

        doctorTable.clearSelection();
    }

    

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DoctorManagement("Admin"));
    }
}
