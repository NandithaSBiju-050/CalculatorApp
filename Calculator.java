package CalculatorApp;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;

public class Calculator extends JFrame implements ActionListener {
    private JTextField display;
    private String operator = "";
    private double num1 = 0;
    private boolean startNewNumber = true;

    // JDBC variables
    private Connection conn;

    public Calculator() {
        setTitle("Calculator with JDBC");
        setSize(300, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Display field
        display = new JTextField();
        display.setEditable(false);
        display.setFont(new Font("Arial", Font.BOLD, 24));
        add(display, BorderLayout.NORTH);

        // Button grid
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 4, 5, 5));

        String[] buttonLabels = {
                "7","8","9","/",
                "4","5","6","*",
                "1","2","3","-",
                "0","C","=","+"
        };

        for (String text : buttonLabels) {
            JButton button = new JButton(text);
            button.setFont(new Font("Arial", Font.BOLD, 20));
            button.addActionListener(this);
            panel.add(button);
        }

        add(panel, BorderLayout.CENTER);

        // Connect to database
        connectToDatabase();

        setVisible(true);
    }

    private void connectToDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://localhost:3306/calculatordb";
            String user = "root";
            String pass = "password"; // change this
            conn = DriverManager.getConnection(url, user, pass);

            // Create table if not exists
            String sql = "CREATE TABLE IF NOT EXISTS history (" +
                         "id INT AUTO_INCREMENT PRIMARY KEY, " +
                         "expression VARCHAR(100), " +
                         "result VARCHAR(50))";
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);

            System.out.println("✅ Database connected.");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();

        if (cmd.matches("[0-9]")) {
            if (startNewNumber) {
                display.setText(cmd);
                startNewNumber = false;
            } else {
                display.setText(display.getText() + cmd);
            }
        } else if (cmd.equals("C")) {
            display.setText("");
            operator = "";
            num1 = 0;
            startNewNumber = true;
        } else if (cmd.equals("=")) {
            try {
                double num2 = Double.parseDouble(display.getText());
                double result = 0;
                String expression = "";

                switch (operator) {
                    case "+": result = num1 + num2; break;
                    case "-": result = num1 - num2; break;
                    case "*": result = num1 * num2; break;
                    case "/":
                        if (num2 == 0) {
                            display.setText("Error");
                            startNewNumber = true;
                            return;
                        }
                        result = num1 / num2;
                        break;
                }

                display.setText(String.valueOf(result));
                expression = num1 + " " + operator + " " + num2 + " = " + result;

                saveCalculation(expression, String.valueOf(result));
                operator = "";
                startNewNumber = true;
            } catch (NumberFormatException ex) {
                display.setText("Error");
                startNewNumber = true;
            }
        } else { // operator
            try {
                num1 = Double.parseDouble(display.getText());
                operator = cmd;
                startNewNumber = true;
            } catch (NumberFormatException ex) {
                display.setText("Error");
                startNewNumber = true;
            }
        }
    }

    private void saveCalculation(String expression, String result) {
        if (conn == null) return;
        try {
            String sql = "INSERT INTO history (expression, result) VALUES (?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, expression);
            pstmt.setString(2, result);
            pstmt.executeUpdate();
            System.out.println("✅ Saved: " + expression);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Calculator());
    }
}
