import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;

public class BillingSystem extends JFrame implements ActionListener {
    JCheckBox laptop, phone, tablet;
    JButton billBtn;
    JLabel result;
    Connection conn;

    public BillingSystem() {
        setTitle("Billing System with JDBC");
        setSize(350, 250);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // UI Components
        laptop = new JCheckBox("Laptop - ₹50,000");
        phone = new JCheckBox("Phone - ₹20,000");
        tablet = new JCheckBox("Tablet - ₹30,000");

        billBtn = new JButton("Generate Bill");
        billBtn.addActionListener(this);

        result = new JLabel("Total: ₹0");

        setLayout(new FlowLayout());
        add(laptop);
        add(phone);
        add(tablet);
        add(billBtn);
        add(result);

        // Connect to database
        connectToDatabase();

        setVisible(true);
    }

    private void connectToDatabase() {
        try {
            // Step 1: Load JDBC Driver (optional for newer versions)
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Step 2: Connect to database
            String url = "jdbc:mysql://localhost:3306/billingdb";  // change to your DB name
            String user = "root";   // your MySQL username
            String pass = "password";  // your MySQL password
            conn = DriverManager.getConnection(url, user, pass);

            // Step 3: Create table if not exists
            String createTable = "CREATE TABLE IF NOT EXISTS bills (" +
                                 "id INT AUTO_INCREMENT PRIMARY KEY, " +
                                 "items VARCHAR(255), " +
                                 "total INT)";
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(createTable);

            System.out.println("Database connected successfully!");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int total = 0;
        StringBuilder items = new StringBuilder();

        if (laptop.isSelected()) {
            total += 50000;
            items.append("Laptop ");
        }
        if (phone.isSelected()) {
            total += 20000;
            items.append("Phone ");
        }
        if (tablet.isSelected()) {
            total += 30000;
            items.append("Tablet ");
        }

        result.setText("Total: ₹" + total);

        // Insert data into database
        if (conn != null) {
            try {
                String sql = "INSERT INTO bills (items, total) VALUES (?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, items.toString().trim());
                pstmt.setInt(2, total);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Bill saved successfully!");
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new BillingSystem();
    }
}
