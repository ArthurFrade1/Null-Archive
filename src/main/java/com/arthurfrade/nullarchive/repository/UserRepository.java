package com.arthurfrade.nullarchive.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    
    public void createBookFiles(int book_id,String file_kind,String storage_path,String original_filename,String mime_type, long size_bytes){
        String sql = "INSERT INTO book_files ( book_id, file_kind, storage_path, original_filename, mime_type, size_bytes,created_at) VALUES ( ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

         // 2) Tenta conectar
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // 3) Define os valores
            stmt.setInt(1, book_id);
            stmt.setString(2, file_kind);
            stmt.setString(3, storage_path);
            stmt.setString(4, original_filename);
            stmt.setString(5, mime_type);
            stmt.setLong(6, size_bytes);

            // 4) Executa
            int rows = stmt.executeUpdate();

            System.out.println("Linhas inseridas: " + rows);

        } catch (SQLException e) {
            System.err.println("Erro ao inserir usuario:");
            e.printStackTrace();
        }
    }

    public int createBook(String title, String author_name, String description, String language_code, Integer published_year, String license, int account_id, String source_url) throws SQLIntegrityConstraintViolationException, SQLException {
        String sql = "INSERT INTO books (title, author_name, description, language_code, published_year, license, account_id, source_url) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        // 2) Tenta conectar
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // 3) Define os valores
            stmt.setString(1, title);
            stmt.setString(2, author_name);
            stmt.setString(3, description);
            stmt.setString(4, language_code);
            if (published_year == null) {
                stmt.setNull(5, java.sql.Types.SMALLINT); // ou Types.INTEGER
            } else {
                stmt.setInt(5, published_year);
            }
            stmt.setString(6, license);
            stmt.setInt(7, account_id);
            stmt.setString(8, source_url);

            // 4) Executa
            int rows = stmt.executeUpdate();

            System.out.println("Linhas inseridas: " + rows);

            if (rows > 0) {
                // 2) Recupera o ID gerado
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro ao inserir usuario:");
            e.printStackTrace();
        }
        return 0;
    }

    public List<Map<String, Object>> getTags() {
    String sql = "SELECT id, name FROM tags";
    List<Map<String, Object>> listaTags = new ArrayList<>();

    try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
         PreparedStatement stmt = conn.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) { // Executa e guarda o resultado

        // Precisamos rodar um loop para cada linha que o banco devolveu
        while (rs.next()) {
            Map<String, Object> tag = new HashMap<>();
            tag.put("id", rs.getInt("id"));
            tag.put("nome", rs.getString("name"));
            
            listaTags.add(tag); // Adiciona na nossa lista
        }

    } catch (SQLException e) {
        System.err.println("Erro ao buscar tags:");
        e.printStackTrace();
    }

    return listaTags; // Agora sim, retorna a lista pronta!
    }


    public void logoutAccountSession(String token){
        String sql = "DELETE FROM account_sessions WHERE account_token = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, token);

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erro ao inserir usuario:");
            e.printStackTrace();
        }
    }
    

    public void createBookTag(int idBook, int[] tagsIdList){

        String sql = "INSERT INTO book_tags (book_id, tag_id) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idBook);

            for(int i=0; i<tagsIdList.length; i++){
                stmt.setInt(2, tagsIdList[i]);
                stmt.executeUpdate();
            }
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
        String sql = "SELECT u.id, u.username, u.role\r\n" + //
                        "FROM account_sessions s\r\n" + //
                        "JOIN accounts u ON u.id = s.account_id\r\n" + //
                        "WHERE s.account_token = ? ";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, token);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new UserSessionData(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("role")
                    );
                }
                return null;
            }
        }
    }
   
}


