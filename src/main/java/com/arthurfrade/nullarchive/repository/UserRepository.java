package com.arthurfrade.nullarchive.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

public class UserRepository{

    // 1) Dados de conexão
    private static final String URL =
            "jdbc:mysql://localhost:3306/nullarchive?useSSL=false&serverTimezone=UTC";

    private static final String USER = "root";        // ou seu usuário
    private static final String PASSWORD = "LB6p2ozMhBUM7MTMDUKCU&vK3";

    public void createUser(String username, String password_hash, String role, String email) throws SQLIntegrityConstraintViolationException, SQLException {

        String sql = "INSERT INTO users (username, password_hash, role, email) VALUES (?, ?, ?, ?)";

        // 2) Tenta conectar
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // 3) Define os valores
            stmt.setString(1, username);
            stmt.setString(2, password_hash);
            stmt.setString(3, role);
            stmt.setString(4, email);

            // 4) Executa
            int rows = stmt.executeUpdate();

            System.out.println("Linhas inseridas: " + rows);

        } catch (SQLException e) {
            System.err.println("Erro ao inserir usuario:");
            e.printStackTrace();
        }
    }


    public String findPasswordHashByEmail(String email) throws SQLException {
        String sql = "SELECT password_hash FROM users WHERE email = ? LIMIT 1";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("password_hash");
                }
                return null;
            }
        }
    }

}
