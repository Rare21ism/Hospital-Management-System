package com.hms;

import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class PatientManagement extends JFrame {

    private Integer selectedPatientId;

    private JTextField nameField;
    private JTextField ageField;
    private JComboBox<String> genderBox;
    private JTextField phoneField;
    private JTextField addressField;
    private JTextField diseaseField;
    private JTextField searchIdField;

    private JTable patientTable;
    private DefaultTableModel tableModel;

    private final String role;
    private JButton deleteButton;

    public PatientManagement() {
        this("Staff"); 
    }

    public PatientManagement(String role) {

        this.role = role;

        setTitle("Hospital Management System (" + role + ")");
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

        JLabel subtitleLabel = new JLabel("Patient Management");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(5));
        headerPanel.add(subtitleLabel);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(3, 4, 10, 10));
        formPanel.setBorder(
                BorderFactory.createTitledBorder("Patient Details")
        );

        nameField = new JTextField();
        ageField = new JTextField();

        genderBox = new JComboBox<>(
                new String[]{"Select Gender", "Male", "Female", "Other"}
        );

        phoneField = new JTextField();
        addressField = new JTextField();
        diseaseField = new JTextField();

        formPanel.add(new JLabel("Name"));
        formPanel.add(nameField);

        formPanel.add(new JLabel("Age"));
        formPanel.add(ageField);

        formPanel.add(new JLabel("Gender"));
        formPanel.add(genderBox);

        formPanel.add(new JLabel("Phone"));
        formPanel.add(phoneField);

        formPanel.add(new JLabel("Address"));
        formPanel.add(addressField);

        formPanel.add(new JLabel("Disease"));
        formPanel.add(diseaseField);

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
        searchIdField.setToolTipText("Enter a patient ID to search");

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
                        "Age",
                        "Gender",
                        "Phone",
                        "Address",
                        "Disease"
                }, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };

        patientTable = new JTable(tableModel);

        patientTable.setRowHeight(25);
        patientTable.getTableHeader().setFont(
                new Font("Arial", Font.BOLD, 13)
        );

        JScrollPane scrollPane = new JScrollPane(patientTable);
        scrollPane.setBorder(
                BorderFactory.createTitledBorder("Patient Records")
        );

       
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));

        bottomPanel.add(scrollPane, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        

        addButton.addActionListener(e -> addPatient());

        updateButton.addActionListener(e -> updatePatient());

        deleteButton.addActionListener(e -> deletePatient());

        searchButton.addActionListener(e -> searchPatient());
        searchIdField.addActionListener(e -> searchPatient());

        clearButton.addActionListener(e -> {
            clearFields();
            viewPatients();
        });

        backButton.addActionListener(e -> {
            dispose();
            new MainMenu(role);
        });

        

        patientTable.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent e) {

                int row = patientTable.getSelectedRow();

                if (row >= 0) {

                    selectedPatientId = Integer.valueOf(
                            tableModel.getValueAt(row, 0).toString()
                    );

                    nameField.setText(
                            tableModel.getValueAt(row, 1).toString()
                    );

                    ageField.setText(
                            tableModel.getValueAt(row, 2).toString()
                    );

                    genderBox.setSelectedItem(
                            tableModel.getValueAt(row, 3).toString()
                    );

                    phoneField.setText(
                            tableModel.getValueAt(row, 4).toString()
                    );

                    addressField.setText(
                            tableModel.getValueAt(row, 5).toString()
                    );

                    diseaseField.setText(
                            tableModel.getValueAt(row, 6).toString()
                    );
                }
            }
        });

        setContentPane(mainPanel);

        setVisible(true);

        
        viewPatients();
    }

    private Integer validateForm() {

        String name = nameField.getText().trim();
        String age = ageField.getText().trim();
        String gender = (String) genderBox.getSelectedItem();
        String phone = phoneField.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the patient's name.");
            return null;
        }

        if (age.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the patient's age.");
            return null;
        }

        int ageValue;
        try {
            ageValue = Integer.parseInt(age);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Age must be a whole number.");
            return null;
        }

        if (ageValue <= 0 || ageValue > 150) {
            JOptionPane.showMessageDialog(this, "Please enter a valid age.");
            return null;
        }

        if (gender == null || gender.equals("Select Gender")) {
            JOptionPane.showMessageDialog(this, "Please select a gender.");
            return null;
        }

        if (phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a phone number.");
            return null;
        }

        return ageValue;
    }

    private Integer getSelectedPatientId() {

    
        int viewRow = patientTable.getSelectedRow();
        if (viewRow >= 0) {
            int modelRow = patientTable.convertRowIndexToModel(viewRow);
            selectedPatientId = Integer.valueOf(tableModel.getValueAt(modelRow, 0).toString());
        }

        if (selectedPatientId == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a patient from the table first."
            );
            return null;
        }

        return selectedPatientId;
    }

    private Integer getSearchPatientId() {

        String value = searchIdField.getText().trim();
        if (value.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a patient ID to search.");
            return null;
        }

        try {
            int id = Integer.parseInt(value);
            if (id <= 0) {
                throw new NumberFormatException();
            }
            return id;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Patient ID must be a positive whole number.");
            return null;
        }
    }


    private void addPatient() {

        Integer age = validateForm();
        if (age == null) {
            return;
        }

        String sql =
                "INSERT INTO Patient " +
                "(name, age, gender, phone, address, disease) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, nameField.getText().trim());
            ps.setInt(2, age);
            ps.setString(3, genderBox.getSelectedItem().toString());
            ps.setString(4, phoneField.getText().trim());
            ps.setString(5, addressField.getText().trim());
            ps.setString(6, diseaseField.getText().trim());

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
                            ? "Patient Added Successfully! Assigned ID: " + newId
                            : "Patient Added Successfully!"
            );

            clearFields();
            viewPatients();

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Error: " + e.getMessage()
            );
        }
    }

    

    private void viewPatients() {

        tableModel.setRowCount(0);

        String sql = "SELECT * FROM Patient";

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {

                tableModel.addRow(new Object[]{
                        rs.getInt("patient_id"),
                        rs.getString("name"),
                        rs.getInt("age"),
                        rs.getString("gender"),
                        rs.getString("phone"),
                        rs.getString("address"),
                        rs.getString("disease")
                });
            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Error: " + e.getMessage()
            );
        }
    }

    
    private void updatePatient() {

        Integer id = getSelectedPatientId();
        if (id == null) {
            return;
        }

        Integer age = validateForm();
        if (age == null) {
            return;
        }

        String sql =
                "UPDATE Patient SET name=?, age=?, gender=?, " +
                "phone=?, address=?, disease=? " +
                "WHERE patient_id=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nameField.getText().trim());
            ps.setInt(2, age);
            ps.setString(3, genderBox.getSelectedItem().toString());
            ps.setString(4, phoneField.getText().trim());
            ps.setString(5, addressField.getText().trim());
            ps.setString(6, diseaseField.getText().trim());
            ps.setInt(7, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {

                JOptionPane.showMessageDialog(
                        this,
                        "Patient Updated Successfully!"
                );

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "Patient ID not found."
                );
            }

            clearFields();
            viewPatients();

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Error: " + e.getMessage()
            );
        }
    }


    private void deletePatient() {

        if (!"Admin".equalsIgnoreCase(role)) {
            JOptionPane.showMessageDialog(this, "Only Admin users can delete records.");
            return;
        }

        Integer id = getSelectedPatientId();
        if (id == null) {
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete patient ID " + id + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String deleteBillsSql = "DELETE FROM Bill WHERE patient_id=?";
        String deletePatientSql = "DELETE FROM Patient WHERE patient_id=?";

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                throw new SQLException("Could not connect to the database.");
            }

            con.setAutoCommit(false);
            int rows;
            try {
                try (PreparedStatement bills = con.prepareStatement(deleteBillsSql)) {
                    bills.setInt(1, id);
                    bills.executeUpdate();
                }

                try (PreparedStatement patient = con.prepareStatement(deletePatientSql)) {
                    patient.setInt(1, id);
                    rows = patient.executeUpdate();
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }

            if (rows > 0) {

                JOptionPane.showMessageDialog(
                        this,
                        "Patient Deleted Successfully!"
                );

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "Patient ID not found."
                );
            }

            clearFields();
            viewPatients();

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Error: " + e.getMessage()
            );
        }
    }


    private void searchPatient() {

        Integer id = getSearchPatientId();
        if (id == null) {
            return;
        }

        String sql =
                "SELECT * FROM Patient WHERE patient_id=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {

                tableModel.setRowCount(0);

                if (rs.next()) {

                    selectedPatientId = rs.getInt("patient_id");

                    nameField.setText(rs.getString("name"));
                    ageField.setText(String.valueOf(rs.getInt("age")));
                    genderBox.setSelectedItem(rs.getString("gender"));
                    phoneField.setText(rs.getString("phone"));
                    addressField.setText(rs.getString("address"));
                    diseaseField.setText(rs.getString("disease"));

                    tableModel.addRow(new Object[]{
                            rs.getInt("patient_id"),
                            rs.getString("name"),
                            rs.getInt("age"),
                            rs.getString("gender"),
                            rs.getString("phone"),
                            rs.getString("address"),
                            rs.getString("disease")
                    });

                } else {

                    JOptionPane.showMessageDialog(
                            this,
                            "Patient ID not found."
                    );

                   
                    viewPatients();
                }
            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Error: " + e.getMessage()
            );
        }
    }

   
    private void clearFields() {

        selectedPatientId = null;
        searchIdField.setText("");
        nameField.setText("");
        ageField.setText("");
        genderBox.setSelectedIndex(0);
        phoneField.setText("");
        addressField.setText("");
        diseaseField.setText("");

        patientTable.clearSelection();
    }
    public static void main(String[] args) {

        SwingUtilities.invokeLater(LoginFrame::new);
    }
}
