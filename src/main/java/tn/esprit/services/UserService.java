package tn.esprit.services;

import jakarta.mail.MessagingException;
import org.mindrot.jbcrypt.BCrypt;
import tn.esprit.entities.User;
import tn.esprit.tools.MyDataBase;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import org.json.JSONArray;
import org.json.JSONException;

public class UserService implements IServices<User> {
    private Connection cnx;
    private static UserService instance;
    private final EmailService emailService;

    public UserService() {
        this.cnx = MyDataBase.getInstance().getCnx();
        this.emailService = new EmailService();
    }

    public static UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }

    public Connection getCnx() {
        return this.cnx;
    }

    private void sendVerificationEmail(User user) {
        try {
            String verificationLink = "http://localhost:8085/verify?token=" + user.getVerificationToken();
            String subject = "Account Verification - WorkAway";

            emailService.sendEmail(user.getEmail(), subject, verificationLink);
            System.out.println("Verification email sent to: " + user.getEmail());
        } catch (MessagingException e) {
            System.err.println("Failed to send verification email: " + e.getMessage());
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    @Override
    public void ajouter(User user) {
        try {
            if (user.getEmail() == null || user.getEmail().isEmpty() ||
                    user.getPassword() == null || user.getPassword().isEmpty() ||
                    user.getName() == null || user.getName().isEmpty()) {
                throw new IllegalArgumentException("Email, password and name are required");
            }


            System.out.println("Password received by UserService: " + user.getPassword());
            String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
            System.out.println("Hashed password stored in DB: " + hashedPassword);
            user.setPassword(hashedPassword);

            user.setVerified(false);
            user.setVerificationToken(VerificationService.generateVerificationToken());
            user.setVerificationTokenExpiry(VerificationService.calculateExpiryDate());

            Set<String> roles = user.getRoles() != null ? user.getRoles() : new HashSet<>();
            if (roles.isEmpty()) {
                roles.add("ROLE_STUDENT");
            }
            String rolesJson = "[\"" + String.join("\",\"", roles) + "\"]";

            String query = "INSERT INTO user (email, password, name, roles, is_restricted, " +
                    "date_creation, is_verified, verification_token, verification_token_expiry, " +
                    "bio, gender, diplome, speciality, age, country, pfp, bg) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement statement = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, user.getEmail());
                statement.setString(2, user.getPassword());
                statement.setString(3, user.getName());
                statement.setString(4, rolesJson);
                statement.setBoolean(5, user.isRestricted());
                statement.setTimestamp(6, Timestamp.valueOf(
                        user.getCreationDate() != null ? user.getCreationDate() : LocalDateTime.now()
                ));
                statement.setBoolean(7, false);
                statement.setString(8, user.getVerificationToken());
                statement.setTimestamp(9, Timestamp.valueOf(user.getVerificationTokenExpiry()));
                statement.setString(10, user.getBio() != null ? user.getBio() : "");
                statement.setString(11, user.getGender() != null ? user.getGender() : "");
                statement.setString(12, user.getDiplome() != null ? user.getDiplome() : "");
                statement.setString(13, user.getSpeciality() != null ? user.getSpeciality() : "");
                statement.setObject(14, user.getAge(), Types.INTEGER);
                statement.setString(15, user.getCountry() != null ? user.getCountry() : "");
                statement.setString(16, user.getPfp() != null ? user.getPfp() : "");
                statement.setString(17, user.getBg() != null ? user.getBg() : "");

                int affectedRows = statement.executeUpdate();

                if (affectedRows == 0) {
                    throw new SQLException("Creating user failed, no rows affected.");
                }

                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getLong(1));
                    }
                }

                sendVerificationEmail(user);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error during registration: " + e.getMessage(), e);
        }
    }

    @Override
    public void modifier(User user) {
        try {
            String query = "UPDATE user SET " +
                    "email = ?, " +
                    "name = ?, " +
                    "bio = ?, " +
                    "gender = ?, " +
                    "diplome = ?, " +
                    "speciality = ?, " +
                    "age = ?, " +
                    "country = ?, " +
                    "pfp = ?, " +
                    "bg = ? " +
                    "WHERE id = ?";

            try (PreparedStatement pst = cnx.prepareStatement(query)) {
                pst.setString(1, user.getEmail());
                pst.setString(2, user.getName());
                pst.setString(3, user.getBio());
                pst.setString(4, user.getGender());
                pst.setString(5, user.getDiplome());
                pst.setString(6, user.getSpeciality());
                pst.setObject(7, user.getAge(), Types.INTEGER);
                pst.setString(8, user.getCountry());
                pst.setString(9, user.getPfp());
                pst.setString(10, user.getBg());
                pst.setLong(11, user.getId());

                int rowsUpdated = pst.executeUpdate();
                if (rowsUpdated > 0) {
                    System.out.println("User updated successfully!");
                } else {
                    System.out.println("No user found with ID: " + user.getId());
                }
            }
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
        }
    }

    public boolean updatePassword(String email, String newPassword) {
        try {
            User user = getByEmail(email);
            if (user == null) {
                return false;
            }

            String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());

            String query = "UPDATE user SET password = ? WHERE email = ?";
            try (PreparedStatement pst = cnx.prepareStatement(query)) {
                pst.setString(1, hashedPassword);
                pst.setString(2, email);

                int rowsUpdated = pst.executeUpdate();
                return rowsUpdated > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error updating password: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void supprimer(int id) {
        String query = "DELETE FROM user WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, id);
            int rowsDeleted = pst.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("User deleted successfully!");
            }
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
        }
    }

    @Override
    public User getOne(User user) {
        return getById(user.getId());
    }

    public User getById(long id) {
        String query = "SELECT * FROM user WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setLong(1, id);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user by ID: " + e.getMessage());
        }
        return null;
    }

    public User getByEmail(String email) {
        String query = "SELECT * FROM user WHERE email = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setString(1, email);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user by email: " + e.getMessage());
        }
        return null;
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setName(rs.getString("name"));
        user.setVerified(rs.getBoolean("is_verified"));
        user.setRestricted(rs.getBoolean("is_restricted"));
        user.setCreationDate(rs.getTimestamp("date_creation").toLocalDateTime());
        user.setBio(rs.getString("bio"));
        user.setGender(rs.getString("gender"));
        user.setDiplome(rs.getString("diplome"));
        user.setSpeciality(rs.getString("speciality"));
        user.setAge(rs.getInt("age"));
        user.setCountry(rs.getString("country"));
        user.setPfp(rs.getString("pfp"));
        user.setBg(rs.getString("bg"));

        try {
            user.setVerificationToken(rs.getString("verification_token"));
        } catch (SQLException e) {
            user.setVerificationToken(null);
        }

        try {
            Timestamp expiry = rs.getTimestamp("verification_token_expiry");
            user.setVerificationTokenExpiry(expiry != null ? expiry.toLocalDateTime() : null);
        } catch (SQLException e) {
            user.setVerificationTokenExpiry(null);
        }

        String rolesJson = rs.getString("roles");
        if (rolesJson != null && !rolesJson.isEmpty()) {
            try {
                JSONArray jsonArray = new JSONArray(rolesJson);
                Set<String> roles = new HashSet<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    roles.add(jsonArray.getString(i));
                }
                user.setRoles(roles);
            } catch (JSONException e) {
                System.err.println("Error parsing roles JSON: " + e.getMessage());
                user.setRoles(new HashSet<>(Collections.singletonList("ROLE_STUDENT")));
            }
        }

        return user;
    }

    @Override
    public List<User> getAll() {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM user";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching users: " + e.getMessage());
        }
        return users;
    }

    public boolean verifyUser(String token) {
        try {
            String query = "SELECT * FROM user WHERE verification_token=? AND is_verified=false";
            try (PreparedStatement stmt = cnx.prepareStatement(query)) {
                stmt.setString(1, token);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    LocalDateTime expiry = rs.getTimestamp("verification_token_expiry").toLocalDateTime();
                    if (expiry.isBefore(LocalDateTime.now())) {
                        return false; // Token expired
                    }

                    // Mark as verified
                    String update = "UPDATE user SET is_verified=true, verification_token=NULL, " +
                            "verification_token_expiry=NULL WHERE id=?";
                    try (PreparedStatement updateStmt = cnx.prepareStatement(update)) {
                        updateStmt.setLong(1, rs.getLong("id"));
                        return updateStmt.executeUpdate() > 0;
                    }
                }
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException("Verification failed", e);
        }
    }
    public boolean verifyPassword(String email, String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            System.err.println("Empty password provided");
            return false;
        }

        User user = getByEmail(email);
        if (user == null) {
            System.err.println("User not found for email: " + email);
            return false;
        }

        String hashedPassword = user.getPassword();
        if (hashedPassword == null || hashedPassword.isEmpty()) {
            System.err.println("No password stored for user: " + email);
            return false;
        }

        try {
            boolean matches = BCrypt.checkpw(plainPassword, hashedPassword);
            System.out.println("Password match result for " + email + ": " + matches);
            return matches;
        } catch (Exception e) {
            System.err.println("Password verification error for user: " + email);
            e.printStackTrace();
            return false;
        }
    }

    public boolean resendVerificationEmail(String email) {
        try {
            User user = getByEmail(email);
            if (user == null) {
                return false;
            }

            // Generate new token if expired
            if (user.getVerificationTokenExpiry() == null ||
                    user.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
                user.setVerificationToken(VerificationService.generateVerificationToken());
                user.setVerificationTokenExpiry(VerificationService.calculateExpiryDate());

                String update = "UPDATE user SET verification_token=?, verification_token_expiry=? WHERE id=?";
                try (PreparedStatement pst = cnx.prepareStatement(update)) {
                    pst.setString(1, user.getVerificationToken());
                    pst.setTimestamp(2, Timestamp.valueOf(user.getVerificationTokenExpiry()));
                    pst.setLong(3, user.getId());
                    pst.executeUpdate();
                }
            }

            sendVerificationEmail(user);
            return true;
        } catch (Exception e) {
            System.err.println("Error resending verification: " + e.getMessage());
            return false;
        }
    }
    public void checkAndUpdateSchema() throws SQLException {
        DatabaseMetaData dbMetaData = cnx.getMetaData();
        String[] columnsToCheck = {"verification_token", "verification_token_expiry"};

        for (String column : columnsToCheck) {
            ResultSet rs = dbMetaData.getColumns(null, null, "user", column);
            if (!rs.next()) {
                try (Statement stmt = cnx.createStatement()) {
                    stmt.execute("ALTER TABLE user ADD COLUMN " + column +
                            (column.equals("verification_token") ? " VARCHAR(255)" : " DATETIME"));
                }
            }
        }
    }


}