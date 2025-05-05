package tn.esprit.services;

import tn.esprit.entities.File;
import tn.esprit.entities.Level;
import tn.esprit.tools.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FileService implements IServices<File> {
    private Connection cnx;
    private static FileService instance;

    public FileService() {
        this.cnx = MyDataBase.getInstance().getCnx();
    }

    public static FileService getInstance() {
        if (instance == null) {
            instance = new FileService();
        }
        return instance;
    }

    @Override
    public void ajouter(File file) {
        // Validation améliorée
        if (file.getFilePath() == null || file.getFilePath().trim().isEmpty()) {
            System.err.println("Chemin invalide: " + file.getFilePath()); // Log supplémentaire
            throw new IllegalArgumentException("Le chemin du fichier ne peut pas être null ou vide. Reçu: '" + file.getFilePath() + "'");
        }

        String query = "INSERT INTO file (file_name, file_path, level_id, is_viewed) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, file.getFileName());
            pst.setString(2, file.getFilePath());
            pst.setInt(3, file.getLevel().getId());
            pst.setBoolean(4, file.isViewed());

            System.out.println("Exécution de la requête: " + pst); // Debug SQL

            int affectedRows = pst.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Échec de l'insertion, aucune ligne affectée.");
            }

            try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    file.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur SQL lors de l'ajout du fichier: " + e.getMessage(), e);
        }
    }

    @Override
    public void modifier(File file) {
        String query = "UPDATE file SET file_name = ?, level_id = ?, is_viewed = ? WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setString(1, file.getFileName());
            pst.setInt(2, file.getLevel().getId());
            pst.setBoolean(3, file.isViewed());
            pst.setLong(4, file.getId());

            int rowsUpdated = pst.executeUpdate();
            if (rowsUpdated == 0) {
                System.out.println("No file found with ID: " + file.getId());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update file: " + e.getMessage(), e);
        }
    }

    @Override
    public void supprimer(int id) {
        String query = "DELETE FROM file WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, id);
            int rowsDeleted = pst.executeUpdate();
            if (rowsDeleted == 0) {
                System.out.println("No file found with ID: " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete file: " + e.getMessage(), e);
        }
    }

    @Override
    public File getOne(File file) {
        return getById(file.getId());
    }

    public File getById(long id) {
        String query = "SELECT * FROM file WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setLong(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return mapResultSetToFile(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get file: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<File> getAll() {
        List<File> files = new ArrayList<>();
        String query = "SELECT * FROM file";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                files.add(mapResultSetToFile(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get files: " + e.getMessage(), e);
        }
        return files;
    }

    private File mapResultSetToFile(ResultSet rs) throws SQLException {
        File file = new File();
        file.setId((int) rs.getLong("id"));
        file.setFileName(rs.getString("file_name"));
        file.setFilePath(rs.getString("file_path")); // Cette ligne est cruciale
        file.setViewed(rs.getBoolean("is_viewed"));

        int levelId = rs.getInt("level_id");
        file.setLevel(new LevelService().getById(levelId));

        return file;
    }



    public List<File> getFilesForLevel(int levelId) {
        List<File> files = new ArrayList<>();
        String query = "SELECT id, file_name, file_path, is_viewed FROM file WHERE level_id = ?";

        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, levelId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                File file = new File();
                file.setId(rs.getInt("id"));
                file.setFileName(rs.getString("file_name"));
                file.setFilePath(rs.getString("file_path")); // Important!
                file.setViewed(rs.getBoolean("is_viewed"));

                Level level = new Level();
                level.setId(levelId);
                file.setLevel(level);

                files.add(file);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get files for level " + levelId, e);
        }
        return files;
    }
}
