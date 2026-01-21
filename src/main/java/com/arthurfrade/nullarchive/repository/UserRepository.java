package com.arthurfrade.nullarchive.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;


import com.arthurfrade.nullarchive.dto.UserSessionData;
import com.arthurfrade.nullarchive.dto.AuthenticatedUserRequest;

public class UserRepository{

    // 1) Dados de conexão
    private static final String URL =
            "jdbc:mysql://localhost:3306/nullarchive?useSSL=false&serverTimezone=UTC";

    private static final String USER = "root";        // ou seu usuário
    private static final String PASSWORD = "LB6p2ozMhBUM7MTMDUKCU&vK3";

    public void createEditor(String username, String password_hash, String email) throws SQLIntegrityConstraintViolationException, SQLException {

        String sql = "INSERT INTO accounts (username, password_hash, email) VALUES (?, ?, ?)";

        // 2) Tenta conectar
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // 3) Define os valores
            stmt.setString(1, username);
            stmt.setString(2, password_hash);
            stmt.setString(3, email);

            // 4) Executa
            int rows = stmt.executeUpdate();

            System.out.println("Linhas inseridas: " + rows);

        } catch (SQLException e) {
            System.err.println("Erro ao inserir usuario:");
            e.printStackTrace();
        }
    }

    public void createEditorSession(long userId, String token,
                          String ip, String userAgent) throws SQLException {

        String sql = """
            INSERT INTO account_sessions (account_id, account_token, ip, last_seen_at, user_agent)
            VALUES (?, ?, ?, CURRENT_TIMESTAMP, ?)
        """;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            stmt.setString(2, token);
            stmt.setString(3, ip);
            stmt.setString(4, userAgent);

            stmt.executeUpdate();
        }
    }

    public void createUserSession(String anon_token) throws SQLException {

        String sql = """
            INSERT INTO user_sessions (user_token, last_seen_at)
            VALUES (?, CURRENT_TIMESTAMP)
        """;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, anon_token);

            stmt.executeUpdate();
        }
    }

    public Boolean userExists(String anon_token) throws SQLException {
        String sql = "SELECT * FROM user_sessions WHERE user_token = ?";
        
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
        PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, anon_token);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return true;
                return false;
            }
        }
    }
    
    public AuthenticatedUserRequest getEditorCredentials(String email) throws SQLException {
        String sql = "SELECT id, password_hash FROM accounts WHERE email = ? LIMIT 1";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return null;
                return new AuthenticatedUserRequest(rs.getLong("id"), rs.getString("password_hash"));
            }
        }
    }

    public UserSessionData getEditorData(String token) throws SQLException{
        String sql = "SELECT u.username, u.role\r\n" + //
                        "FROM account_sessions s\r\n" + //
                        "JOIN accounts u ON u.id = s.account_id\r\n" + //
                        "WHERE s.account_token = ? ";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, token);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new UserSessionData(
                    rs.getString("username"),
                    rs.getString("role")
                    );
                }
                return null;
            }
        }
    }
   
}


