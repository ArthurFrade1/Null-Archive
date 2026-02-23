package com.arthurfrade.nullarchive.repository;

import java.io.IOException;
import java.io.InputStream;
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
import java.util.Properties;
import com.arthurfrade.nullarchive.dto.UserSessionData;
import com.arthurfrade.nullarchive.dto.AuthenticatedUserRequest;

public class UserRepository{

    // 1) Dados de conexão
    private static String URL;
    private static String USER;
    private static String PASSWORD;

    public UserRepository(){
        Properties props = new Properties();
        try{
            InputStream in = getClass().getResourceAsStream("db.properties");
            if(in == null){
                throw new NullPointerException("Could not find properties");}
                props.load(in);
            }
        catch(IOException e){
            throw new RuntimeException(
                "Error"
            );
        }
        URL = props.getProperty("DB_URL");
        USER = props.getProperty("DB_USER");
        PASSWORD = props.getProperty("DB_PASSWORD");

    }

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
    
    public void createBookFiles(int book_id,String file_kind,String storage_path_file, String storage_path_image, String original_filename,String mime_type, long size_bytes){
        String sql = "INSERT INTO book_files ( book_id, file_kind, storage_path_file, storage_path_image, original_filename, mime_type, size_bytes,created_at) VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

         // 2) Tenta conectar
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // 3) Define os valores
            stmt.setInt(1, book_id);
            stmt.setString(2, file_kind);
            stmt.setString(3, storage_path_file);
            stmt.setString(4, storage_path_image);
            stmt.setString(5, original_filename);
            stmt.setString(6, mime_type);
            stmt.setLong(7, size_bytes);

            // 4) Executa
            int rows = stmt.executeUpdate();

            System.out.println("Linhas inseridas: " + rows);

        } catch (SQLException e) {
            System.err.println("Erro ao inserir usuario:");
            e.printStackTrace();
        }
    }

    public int createBook(String title, String author_name, String description, String language_code, Integer published_year, String license, int account_id, String source_url, Boolean era) throws SQLIntegrityConstraintViolationException, SQLException {
        String sql = "INSERT INTO books (title, author_name, description, language_code, published_year, license, account_id, source_url, is_bc) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
            stmt.setBoolean(9, era);

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

    public void approveBook(int id){
         String sql = "UPDATE books SET approved = 1 WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

           stmt.executeUpdate();
                
        } catch (SQLException e) {
            System.err.println("Erro ao aprovar book:");
            e.printStackTrace();
        }
    }

    public void deleteBookInfo(int id){
         String sql = "DELETE FROM books WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

           stmt.executeUpdate();
                
        } catch (SQLException e) {
            System.err.println("Erro ao excluir book:");
            e.printStackTrace();
        }
    }
    
    public List<Map<String, Object>> searchCatalog(String[] tagIds, String searchTerm, int approved) {
    
    StringBuilder sql = new StringBuilder(
        "SELECT b.id, b.title, b.author_name, " +
        "MAX(f.storage_path_image) as storage_path_image, " +
        "GROUP_CONCAT(t.name) as tags_list " +
        "FROM books b " +
        "LEFT JOIN book_files f ON b.id = f.book_id " +
        "LEFT JOIN book_tags bt ON b.id = bt.book_id " +
        "LEFT JOIN tags t ON bt.tag_id = t.id " +
        "WHERE b.approved = ? "
    );

    // 1. Filtro de Texto
    boolean hasText = (searchTerm != null && !searchTerm.trim().isEmpty());
    if (hasText) {
        sql.append(" AND (b.title LIKE ? OR b.author_name LIKE ?) ");
    }

    // 2. Filtro de Tags
    boolean hasTags = (tagIds != null && tagIds.length > 0 && !tagIds[0].isEmpty());
    if (hasTags) {
        sql.append(" AND b.id IN ( " +
                   "SELECT bt2.book_id FROM book_tags bt2 " +
                   "WHERE bt2.tag_id IN (");
        for (int i = 0; i < tagIds.length; i++) {
            sql.append("?");
            if (i < tagIds.length - 1) sql.append(",");
        }
        sql.append(") GROUP BY bt2.book_id HAVING COUNT(DISTINCT bt2.tag_id) = ?) ");
    }

    sql.append(" GROUP BY b.id");

    List<Map<String, Object>> lista = new ArrayList<>();

    try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
         PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

        int index = 1;
        stmt.setInt(index++, approved);

        if (hasText) {
            String filter = "%" + searchTerm.trim() + "%";
            stmt.setString(index++, filter);
            stmt.setString(index++, filter);
        }

        if (hasTags) {
            for (String id : tagIds) {
                stmt.setInt(index++, Integer.parseInt(id.trim()));
            }
            stmt.setInt(index++, tagIds.length);
        }

        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> book = new HashMap<>();
                book.put("id", rs.getInt("id"));
                book.put("title", rs.getString("title"));
                book.put("author_name", rs.getString("author_name"));
                book.put("storage_path_image", rs.getString("storage_path_image"));
                book.put("tags", rs.getString("tags_list"));
                lista.add(book);
            }
        }
    } catch (Exception e) { 
        e.printStackTrace(); 
    }
    return lista;
}

    public String[] getFileName(int id){
        String sql = "SELECT storage_path_image, storage_path_file FROM book_files WHERE book_id = ?";
        String[] paths = new String[2];

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

           try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    paths[0]=rs.getString("storage_path_file");
                    paths[1]=rs.getString("storage_path_image");
                }
            }
                
        } catch (SQLException e) {
            System.err.println("Erro ao excluir book:");
            e.printStackTrace();
        }
        return paths;
    }

    public Map<String, Object> getBooksInfo(int bookId) {
        String sql = "SELECT b.id, b.title, b.author_name, b.description, b.language_code, " +
                    "b.published_year, b.license, b.account_id, b.source_url, f.file_kind, f.original_filename, f.size_bytes, f.created_at, a.username, b.is_bc, " +
                    "MAX(f.storage_path_file) as storage_path_file, " + 
                    "MAX(f.storage_path_image) as storage_path_image, " +
                    "GROUP_CONCAT(t.name) as names_das_tags " +
                    "FROM books b " +
                    "LEFT JOIN book_files f ON b.id = f.book_id " +
                    "LEFT JOIN book_tags bt ON b.id = bt.book_id " +
                    "LEFT JOIN tags t ON bt.tag_id = t.id " +
                    "LEFT JOIN accounts a ON b.account_id = a.id " +
                    "WHERE b.id = ? " + 
                    "GROUP BY b.id, b.title, b.author_name, b.description, b.language_code, " +
                    "b.published_year, b.license, b.account_id, b.source_url, f.file_kind, f.original_filename, f.size_bytes, f.created_at, a.username, b.is_bc";

        Map<String, Object> book = new HashMap<>();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bookId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    book.put("id", rs.getInt("id"));
                    book.put("title", rs.getString("title"));
                    book.put("author_name", rs.getString("author_name"));
                    book.put("description", rs.getString("description"));
                    book.put("language_code", rs.getString("language_code"));
                    book.put("published_year", rs.getInt("published_year"));
                    book.put("license", rs.getString("license"));
                    book.put("username", rs.getString("username"));
                    book.put("source_url", rs.getString("source_url"));
                    book.put("storage_path_image", rs.getString("storage_path_image"));
                    book.put("storage_path_file", rs.getString("storage_path_file"));
                    book.put("tags", rs.getString("names_das_tags"));
                    book.put("file_kind", rs.getString("file_kind"));
                    book.put("original_filename", rs.getString("original_filename"));
                    book.put("size_bytes", rs.getLong("size_bytes"));
                    book.put("created_at", rs.getString("created_at"));
                    book.put("is_bc", rs.getBoolean("is_bc"));

                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar book:");
            e.printStackTrace();
        }
        return book;
    }

public List<Map<String, Object>> searchOpenBooks(int user_id, String[] tagIds, String searchTerm) {
    StringBuilder sql = new StringBuilder(
        "SELECT b.id, b.title, b.author_name, b.language_code, " +
        "b.published_year, b.approved, rp.updated_at, " +
        "MAX(f.storage_path_image) as storage_path_image, " +
        "GROUP_CONCAT(t.name) as names_das_tags " +
        "FROM books b " +
        "LEFT JOIN book_files f ON b.id = f.book_id " +
        "LEFT JOIN book_tags bt ON b.id = bt.book_id " +
        "LEFT JOIN tags t ON bt.tag_id = t.id " +
        "LEFT JOIN reading_progress rp ON rp.book_id = b.id " +
        "WHERE b.approved = 1 AND rp.user_id = ? "
    );

    // 1. Filtro de Texto (Título ou Autor)
    boolean hasText = (searchTerm != null && !searchTerm.trim().isEmpty());
    if (hasText) {
        sql.append(" AND (b.title LIKE ? OR b.author_name LIKE ?) ");
    }

    // 2. Filtro de Tags (Subquery para garantir que tenha TODAS as tags selecionadas)
    boolean hasTags = (tagIds != null && tagIds.length > 0 && !tagIds[0].isEmpty());
    if (hasTags) {
        sql.append(" AND b.id IN ( " +
                   "SELECT bt2.book_id FROM book_tags bt2 " +
                   "WHERE bt2.tag_id IN (");
        for (int i = 0; i < tagIds.length; i++) {
            sql.append("?");
            if (i < tagIds.length - 1) sql.append(",");
        }
        sql.append(") GROUP BY bt2.book_id HAVING COUNT(DISTINCT bt2.tag_id) = ?) ");
    }

    sql.append(" GROUP BY b.id, b.title, b.author_name, b.language_code, b.published_year, b.approved, rp.updated_at ");
    sql.append(" ORDER BY rp.updated_at DESC");

    List<Map<String, Object>> listaBooks = new ArrayList<>();

    try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
         PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

        int index = 1;
        stmt.setInt(index++, user_id); // Primeiro parâmetro é sempre o user_id

        // Preenche busca por texto
        if (hasText) {
            String filter = "%" + searchTerm.trim() + "%";
            stmt.setString(index++, filter);
            stmt.setString(index++, filter);
        }

        // Preenche IDs das tags e o contador do HAVING
        if (hasTags) {
            for (String id : tagIds) {
                stmt.setInt(index++, Integer.parseInt(id.trim()));
            }
            stmt.setInt(index++, tagIds.length);
        }

        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> book = new HashMap<>();
                book.put("id", rs.getString("id"));
                book.put("title", rs.getString("title"));
                book.put("author_name", rs.getString("author_name"));
                book.put("language_code", rs.getString("language_code"));
                book.put("published_year", rs.getInt("published_year"));
                book.put("storage_path_image", rs.getString("storage_path_image"));
                book.put("tags", rs.getString("names_das_tags"));
                book.put("approved", rs.getString("approved"));
                book.put("updated_at", rs.getString("updated_at"));
                book.put("keepreading", true); 
                
                listaBooks.add(book);
            }
        }

    } catch (SQLException e) {
        System.err.println("Erro ao buscar open books:");
        e.printStackTrace();
    }

    return listaBooks;
}
public List<Map<String, Object>> searchFavorites(int user_id, String[] tagIds, String searchTerm) {
    StringBuilder sql = new StringBuilder(
        "SELECT b.id, b.title, b.author_name, b.language_code, " +
        "b.published_year, b.approved, " +
        "MAX(bf.storage_path_image) as storage_path_image, " +
        "GROUP_CONCAT(t.name) as names_das_tags " +
        "FROM books b " +
        "LEFT JOIN book_files bf ON b.id = bf.book_id " +
        "LEFT JOIN book_tags bt ON b.id = bt.book_id " +
        "LEFT JOIN tags t ON bt.tag_id = t.id " +
        "LEFT JOIN favorites f ON f.book_id = b.id " +
        "WHERE b.approved = 1 AND f.user_id = ? "
    );

    // 1. Filtro de Texto (Título ou Autor)
    boolean hasText = (searchTerm != null && !searchTerm.trim().isEmpty());
    if (hasText) {
        sql.append(" AND (b.title LIKE ? OR b.author_name LIKE ?) ");
    }

    // 2. Filtro de Tags (Subquery para garantir que tenha TODAS as tags selecionadas)
    boolean hasTags = (tagIds != null && tagIds.length > 0 && !tagIds[0].isEmpty());
    if (hasTags) {
        sql.append(" AND b.id IN ( " +
                   "SELECT bt2.book_id FROM book_tags bt2 " +
                   "WHERE bt2.tag_id IN (");
        for (int i = 0; i < tagIds.length; i++) {
            sql.append("?");
            if (i < tagIds.length - 1) sql.append(",");
        }
        sql.append(") GROUP BY bt2.book_id HAVING COUNT(DISTINCT bt2.tag_id) = ?) ");
    }

    sql.append(" GROUP BY b.id, b.title, b.author_name, b.language_code, b.published_year ");

    List<Map<String, Object>> listaBooks = new ArrayList<>();

    try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
         PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

        int index = 1;
        stmt.setInt(index++, user_id); // Define o usuário dono dos favoritos

        // Preenche busca por texto
        if (hasText) {
            String filter = "%" + searchTerm.trim() + "%";
            stmt.setString(index++, filter); // b.title
            stmt.setString(index++, filter); // b.author_name
        }

        // Preenche IDs das tags e o contador do HAVING
        if (hasTags) {
            for (String id : tagIds) {
                stmt.setInt(index++, Integer.parseInt(id.trim()));
            }
            stmt.setInt(index++, tagIds.length);
        }

        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> book = new HashMap<>();
                book.put("id", rs.getString("id"));
                book.put("title", rs.getString("title"));
                book.put("author_name", rs.getString("author_name"));
                book.put("language_code", rs.getString("language_code"));
                book.put("published_year", rs.getInt("published_year"));
                book.put("storage_path_image", rs.getString("storage_path_image"));
                book.put("tags", rs.getString("names_das_tags"));
                book.put("approved", rs.getString("approved"));
                book.put("favorite", true); 
                
                listaBooks.add(book);
            }
        }

    } catch (SQLException e) {
        System.err.println("Erro ao buscar favoritos:");
        e.printStackTrace();
    }

    return listaBooks;
}

    public List<Map<String, Object>> verificaOpenBooks(int user_id, List<Map<String, Object>> books) {
        String sql ="SELECT rp.id " +
                    "FROM reading_progress rp " +
                    "LEFT JOIN user_sessions u ON u.id = rp.user_id " +
                    "LEFT JOIN books b ON b.id = rp.book_id " +
                    "WHERE u.id = ? AND b.id = ? ";

        // 1. No parênteses, apenas DECLARAMOS o que o Java deve fechar sozinho
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            PreparedStatement stmt = conn.prepareStatement(sql)) {
                
            stmt.setInt(1, user_id);
            for (Map<String,Object> book : books) {
                String idString = String.valueOf(book.get("id"));
    
                // 2. Converte essa String para um int puro
                int bookId = Integer.parseInt(idString);
                
                stmt.setInt(2, bookId);


                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        book.put("keepreading", true);
                        
                    }else{
                        
                        book.put("keepreading", false);
                    }
                }
                
            }
            


        } catch (SQLException e) {
            System.err.println("Erro ao buscar books:");
            e.printStackTrace();
        }

        return books;
    }
    public List<Map<String, Object>> verificaFavorito(int user_id, List<Map<String, Object>> books) {
        String sql ="SELECT f.id " +
                    "FROM favorites f " +
                    "LEFT JOIN user_sessions u ON u.id = f.user_id " +
                    "LEFT JOIN books b ON b.id = f.book_id " +
                    "WHERE u.id = ? AND b.id = ? ";

        // 1. No parênteses, apenas DECLARAMOS o que o Java deve fechar sozinho
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            PreparedStatement stmt = conn.prepareStatement(sql)) {
                
            stmt.setInt(1, user_id);
            for (Map<String,Object> book : books) {
                String idString = String.valueOf(book.get("id"));
    
                // 2. Converte essa String para um int puro
                int bookId = Integer.parseInt(idString);
                
                stmt.setInt(2, bookId);


                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        book.put("favorite", true);
                        
                    }else{
                        
                        book.put("favorite", false);
                    }
                }
                
            }
            


        } catch (SQLException e) {
            System.err.println("Erro ao buscar books:");
            e.printStackTrace();
        }

        return books;
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

    public void createUserSession(String user_token) throws SQLException {

        String sql = """
            INSERT INTO user_sessions (user_token, last_seen_at)
            VALUES (?, CURRENT_TIMESTAMP)
        """;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user_token);

            stmt.executeUpdate();
        }
    }


    public void updateReadBook(int book_id, int user_id){
        
        String sql = "INSERT INTO reading_progress (book_id, user_id) " +
             "VALUES (?, ?) " +
             "ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP";
        
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
        PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, book_id);
            stmt.setInt(2, user_id);
            stmt.executeUpdate();
            
        }catch (SQLException e) {
            System.err.println("Erro ao atualizar usuario:");
            e.printStackTrace();
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

    public int getUserId(String user_token){
        String sql = "SELECT id FROM user_sessions WHERE user_token = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, user_token);

                try(ResultSet rs = stmt.executeQuery()){
                    if(!rs.next()) return 0;
                    return rs.getInt("id");
                }
            }
        catch(Exception e){
            System.out.println("Erro ao buscar id");
        }
        return 0;
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
        String sql = "SELECT u.id, u.username, u.role " + //
                        "FROM account_sessions s " + //
                        "JOIN accounts u ON u.id = s.account_id " + //
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

    public void createFavorite(int book_id, int user_id){

        String sql = "INSERT INTO favorites (book_id, user_id) " +
             "VALUES (?, ?)";
        
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
        PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, book_id);
            stmt.setInt(2, user_id);
            stmt.executeUpdate();
            
        }catch (SQLException e) {
            System.err.println("Erro ao criar favorito:");
            e.printStackTrace();
        }

    }


    public void deleteFavorite(int book_id, int user_id){

        String sql = "DELETE FROM favorites WHERE " +
             "book_id = ? AND user_id = ?";
        
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
        PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, book_id);
            stmt.setInt(2, user_id);
            stmt.executeUpdate();
            
        }catch (SQLException e) {
            System.err.println("Erro ao deletear favorito:");
            e.printStackTrace();
        }

    }
   
}


