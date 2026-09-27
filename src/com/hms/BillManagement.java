package com.hms;

import java.awt.*;
import java.sql.*;
import java.text.DecimalFormat;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

class BillManagement extends JFrame {

    private Integer selectedBillId;

    private JComboBox<ComboItem> patientBox;
    private JTextField consultationFeeField;
    private JTextField roomChargeField;
    private JTextField medicineChargeField;
    private JTextField totalAmountField; 
    private JTextField billDateField;    
    private JTextField searchIdField;
    private JComboBox<String> paymentStatusBox;

    private JTable billTable;
    private DefaultTableModel tableModel;

    private final String role;
    private JButton deleteButton;

    public BillManagement(String role) {

        this.role = role;

        setTitle("Bill Management (" + role + ")");
        setSize(1050, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("HOSPITAL MANAGEMENT SYSTEM");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 26));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Bill Management");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(5));
        headerPanel.add(subtitleLabel);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(4, 4, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Bill Details"));

        patientBox = new JComboBox<>();
        consultationFeeField = new JTextField();
        roomChargeField = new JTextField();
        medicineChargeField = new JTextField();

        totalAmountField = new JTextField();
        totalAmountField.setEditable(false);
        totalAmountField.setBackground(new Color(230, 230, 230));

        billDateField = new JTextField();

        paymentStatusBox = new JComboBox<>(
                new String[]{"Select Status", "Paid", "Pending", "Partial"}
        );

        formPanel.add(new JLabel("Patient"));
        formPanel.add(patientBox);

        formPanel.add(new JLabel("Consultation Fee"));
        formPanel.add(consultationFeeField);

        formPanel.add(new JLabel("Room Charge"));
        formPanel.add(roomChargeField);

        formPanel.add(new JLabel("Medicine Charge"));
        formPanel.add(medicineChargeField);

        formPanel.add(new JLabel("Total Amount"));
        formPanel.add(totalAmountField);

        formPanel.add(new JLabel("Bill Date (yyyy-mm-dd)"));
        formPanel.add(billDateField);

        formPanel.add(new JLabel("Payment Status"));
        formPanel.add(paymentStatusBox);

        formPanel.add(new JLabel(""));
        formPanel.add(new JLabel(""));

        mainPanel.add(formPanel, BorderLayout.CENTER);

        DocumentListener recalcListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { recalcTotal(); }
            public void removeUpdate(DocumentEvent e) { recalcTotal(); }
            public void changedUpdate(DocumentEvent e) { recalcTotal(); }
        };
        consultationFeeField.getDocument().addDocumentListener(recalcListener);
        roomChargeField.getDocument().addDocumentListener(recalcListener);
        medicineChargeField.getDocument().addDocumentListener(recalcListener);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

        JButton addButton = new JButton("Add");
        JButton updateButton = new JButton("Update");
        deleteButton = new JButton("Delete");
        JButton searchButton = new JButton("Search");
        JButton clearButton = new JButton("Clear");
        JButton refreshPatientsButton = new JButton("Refresh Patients");
        JButton backButton = new JButton("Back to Menu");
        searchIdField = new JTextField(6);
        searchIdField.setToolTipText("Enter a bill ID to search");

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
        buttonPanel.add(refreshPatientsButton);
        buttonPanel.add(backButton);


        tableModel = new DefaultTableModel(
                new String[]{"Bill ID", "Patient ID", "Patient", "Consultation", "Room", "Medicine", "Total", "Date", "Status"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        billTable = new JTable(tableModel);
        billTable.setRowHeight(25);
        billTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));

        JScrollPane scrollPane = new JScrollPane(billTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Bill Records"));

        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.add(scrollPane, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addBill());
        updateButton.addActionListener(e -> updateBill());
        deleteButton.addActionListener(e -> deleteBill());
        searchButton.addActionListener(e -> searchBill());
        searchIdField.addActionListener(e -> searchBill());
        clearButton.addActionListener(e -> {
            clearFields();
            viewBills();
        });
        refreshPatientsButton.addActionListener(e -> loadPatientList());

        backButton.addActionListener(e -> {
            dispose();
            new MainMenu(role);
        });

        billTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {

                int row = billTable.getSelectedRow();

                if (row >= 0) {
                    selectedBillId = Integer.valueOf(
                            tableModel.getValueAt(row, 0).toString()
                    );
                    loadBillIntoForm(selectedBillId);
                }
            }
        });

        setContentPane(mainPanel);
        setVisible(true);

        loadPatientList();
        viewBills();
    }

    private void loadPatientList() {

        patientBox.removeAllItems();

        String sql = "SELECT patient_id, name FROM Patient";

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                patientBox.addItem(new ComboItem(rs.getInt("patient_id"), rs.getString("name")));
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading patients: " + e.getMessage());
        }
    }

    private void selectComboItemById(JComboBox<ComboItem> box, int id) {
        for (int i = 0; i < box.getItemCount(); i++) {
            if (box.getItemAt(i).getId() == id) {
                box.setSelectedIndex(i);
                return;
            }
        }
    }


    private void recalcTotal() {

        double consultation = parseChargeOrZero(consultationFeeField.getText());
        double room = parseChargeOrZero(roomChargeField.getText());
        double medicine = parseChargeOrZero(medicineChargeField.getText());

        double total = consultation + room + medicine;

        DecimalFormat df = new DecimalFormat("0.00");
        totalAmountField.setText(df.format(total));
    }

    private double parseChargeOrZero(String text) {
        try {
            return Double.parseDouble(text.trim());
        } catch (Exception e) {
            return 0.0;
        }
    }


    private boolean validateForm() {

        if (patientBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select a patient.\n(If the list is empty, add a patient first, then click 'Refresh Patients'.)");
            return false;
        }

        if (!isValidAmount(consultationFeeField.getText())) {
            JOptionPane.showMessageDialog(this, "Please enter a valid consultation fee (e.g. 500.00).");
            return false;
        }

        if (!isValidAmount(roomChargeField.getText())) {
            JOptionPane.showMessageDialog(this, "Please enter a valid room charge (e.g. 1000.00). Use 0 if not applicable.");
            return false;
        }

        if (!isValidAmount(medicineChargeField.getText())) {
            JOptionPane.showMessageDialog(this, "Please enter a valid medicine charge (e.g. 250.00). Use 0 if not applicable.");
            return false;
        }

        String date = billDateField.getText().trim();
        if (!date.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            JOptionPane.showMessageDialog(this, "Bill date must be in yyyy-mm-dd format, e.g. 2026-09-15.");
            return false;
        }

        String status = (String) paymentStatusBox.getSelectedItem();
        if (status == null || status.equals("Select Status")) {
            JOptionPane.showMessageDialog(this, "Please select a payment status.");
            return false;
        }

        return true;
    }

    private boolean isValidAmount(String text) {
        try {
            double val = Double.parseDouble(text.trim());
            return val >= 0;
        } catch (Exception e) {
            return false;
        }
    }

    private Integer getSelectedBillId() {

        int viewRow = billTable.getSelectedRow();
        if (viewRow >= 0) {
            int modelRow = billTable.convertRowIndexToModel(viewRow);
            selectedBillId = Integer.valueOf(tableModel.getValueAt(modelRow, 0).toString());
        }

        if (selectedBillId == null) {
            JOptionPane.showMessageDialog(this, "Please select a bill from the table first.");
            return null;
        }
        return selectedBillId;
    }

    private Integer getSearchBillId() {
        String value = searchIdField.getText().trim();
        if (value.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a bill ID to search.");
            return null;
        }

        try {
            int id = Integer.parseInt(value);
            if (id <= 0) {
                throw new NumberFormatException();
            }
            return id;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Bill ID must be a positive whole number.");
            return null;
        }
    }

    private void addBill() {

        if (!validateForm()) {
            return;
        }

        String sql = "INSERT INTO Bill " +
                "(patient_id, consultation_fee, room_charge, medicine_charge, total_amount, bill_date, payment_status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            double consultation = parseChargeOrZero(consultationFeeField.getText());
            double room = parseChargeOrZero(roomChargeField.getText());
            double medicine = parseChargeOrZero(medicineChargeField.getText());
            double total = consultation + room + medicine;

            ps.setInt(1, ((ComboItem) patientBox.getSelectedItem()).getId());
            ps.setDouble(2, consultation);
            ps.setDouble(3, room);
            ps.setDouble(4, medicine);
            ps.setDouble(5, total);
            ps.setDate(6, Date.valueOf(billDateField.getText().trim()));
            ps.setString(7, paymentStatusBox.getSelectedItem().toString());

            ps.executeUpdate();

            String newId = null;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    newId = String.valueOf(keys.getLong(1));
                }
            }

            JOptionPane.showMessageDialog(
                    this,
                    newId != null ? "Bill Added Successfully! Assigned ID: " + newId : "Bill Added Successfully!"
            );

            clearFields();
            viewBills();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void viewBills() {

        tableModel.setRowCount(0);

        String sql = "SELECT b.bill_id, b.patient_id, p.name AS patient_name, b.consultation_fee, b.room_charge, " +
                "b.medicine_charge, b.total_amount, b.bill_date, b.payment_status " +
                "FROM Bill b " +
                "JOIN Patient p ON b.patient_id = p.patient_id " +
                "ORDER BY b.bill_date DESC";

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("bill_id"),
                        rs.getInt("patient_id"),
                        rs.getString("patient_name"),
                        rs.getBigDecimal("consultation_fee"),
                        rs.getBigDecimal("room_charge"),
                        rs.getBigDecimal("medicine_charge"),
                        rs.getBigDecimal("total_amount"),
                        rs.getDate("bill_date"),
                        rs.getString("payment_status")
                });
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void loadBillIntoForm(int billId) {

        String sql = "SELECT * FROM Bill WHERE bill_id=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, billId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    selectComboItemById(patientBox, rs.getInt("patient_id"));
                    consultationFeeField.setText(rs.getBigDecimal("consultation_fee").toPlainString());
                    roomChargeField.setText(rs.getBigDecimal("room_charge").toPlainString());
                    medicineChargeField.setText(rs.getBigDecimal("medicine_charge").toPlainString());
                    billDateField.setText(rs.getDate("bill_date").toString());
                    paymentStatusBox.setSelectedItem(rs.getString("payment_status"));
                    recalcTotal();
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void updateBill() {

        Integer id = getSelectedBillId();
        if (id == null) {
            return;
        }

        if (!validateForm()) {
            return;
        }

        String sql = "UPDATE Bill SET patient_id=?, consultation_fee=?, room_charge=?, medicine_charge=?, " +
                "total_amount=?, bill_date=?, payment_status=? WHERE bill_id=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            double consultation = parseChargeOrZero(consultationFeeField.getText());
            double room = parseChargeOrZero(roomChargeField.getText());
            double medicine = parseChargeOrZero(medicineChargeField.getText());
            double total = consultation + room + medicine;

            ps.setInt(1, ((ComboItem) patientBox.getSelectedItem()).getId());
            ps.setDouble(2, consultation);
            ps.setDouble(3, room);
            ps.setDouble(4, medicine);
            ps.setDouble(5, total);
            ps.setDate(6, Date.valueOf(billDateField.getText().trim()));
            ps.setString(7, paymentStatusBox.getSelectedItem().toString());
            ps.setInt(8, id);

            int rows = ps.executeUpdate();

            JOptionPane.showMessageDialog(
                    this,
                    rows > 0 ? "Bill Updated Successfully!" : "Bill ID not found."
            );

            clearFields();
            viewBills();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }


    private void deleteBill() {

        if (!"Admin".equalsIgnoreCase(role)) {
            JOptionPane.showMessageDialog(this, "Only Admin users can delete records.");
            return;
        }

        Integer id = getSelectedBillId();
        if (id == null) {
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete bill ID " + id + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = "DELETE FROM Bill WHERE bill_id=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            int rows = ps.executeUpdate();

            JOptionPane.showMessageDialog(
                    this,
                    rows > 0 ? "Bill Deleted Successfully!" : "Bill ID not found."
            );

            clearFields();
            viewBills();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }


    private void searchBill() {

        Integer id = getSearchBillId();
        if (id == null) {
            return;
        }

        String sql = "SELECT b.bill_id, b.patient_id, p.name AS patient_name, b.consultation_fee, b.room_charge, " +
                "b.medicine_charge, b.total_amount, b.bill_date, b.payment_status " +
                "FROM Bill b " +
                "JOIN Patient p ON b.patient_id = p.patient_id " +
                "WHERE b.bill_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {

                tableModel.setRowCount(0);

                if (rs.next()) {

                    selectedBillId = rs.getInt("bill_id");
                    loadBillIntoForm(id);

                    tableModel.addRow(new Object[]{
                            rs.getInt("bill_id"),
                            rs.getInt("patient_id"),
                            rs.getString("patient_name"),
                            rs.getBigDecimal("consultation_fee"),
                            rs.getBigDecimal("room_charge"),
                            rs.getBigDecimal("medicine_charge"),
                            rs.getBigDecimal("total_amount"),
                            rs.getDate("bill_date"),
                            rs.getString("payment_status")
                    });

                } else {
                    JOptionPane.showMessageDialog(this, "Bill ID not found.");
                    viewBills();
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }


    private void clearFields() {

        selectedBillId = null;
        searchIdField.setText("");

        if (patientBox.getItemCount() > 0) patientBox.setSelectedIndex(0);

        consultationFeeField.setText("");
        roomChargeField.setText("");
        medicineChargeField.setText("");
        totalAmountField.setText("");
        billDateField.setText("");
        paymentStatusBox.setSelectedIndex(0);

        billTable.clearSelection();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BillManagement("Admin"));
    }
}
