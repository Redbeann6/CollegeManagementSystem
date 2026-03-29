package com.college.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseTest {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/college_management?useSSL=false&serverTimezone=UTC&characterEncoding=utf8&allowPublicKeyRetrieval=true";
        String username = "root";
        String password = "12345678";

        try (Connection conn = DriverManager.getConnection(url, username, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM users");) {

            System.out.println("Database connection successful!");
            System.out.println("Users table content:");
            System.out.println("ID | Username | Password | Name | Email | Phone | Role | Enabled");
            System.out.println("----------------------------------------------------------------");

            while (rs.next()) {
                long id = rs.getLong("id");
                String user = rs.getString("username");
                String pass = rs.getString("password");
                String name = rs.getString("name");
                String email = rs.getString("email");
                String phone = rs.getString("phone");
                String role = rs.getString("role");
                boolean enabled = rs.getBoolean("enabled");

                System.out.printf("%d | %s | %s | %s | %s | %s | %s | %b%n", 
                        id, user, pass, name, email, phone, role, enabled);
            }

        } catch (Exception e) {
            System.err.println("Database connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}