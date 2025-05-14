package tn.esprit.services;

import tn.esprit.entities.Annonce;
import tn.esprit.tools.MyDataBase;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class AnnonceService {
    private static final List<Consumer<Annonce>> listeners = new ArrayList<>();

    private Connection connection;
    private static final String IMAGE_DIRECTORY = "images/";  // Dossier de stockage des images

    public AnnonceService() {
        connection = MyDataBase.getInstance().getCnx();
    }

    // Ajouter une annonce avec gestion de l'image et de la date
    public void addAnnonce(Annonce annonce) throws SQLException, IOException {
        // Vérifier si le répertoire des images existe, sinon le créer
        File dir = new File(IMAGE_DIRECTORY);
        if (!dir.exists()) {
            dir.mkdir();  // Créer le dossier si nécessaire
        }

        // Obtenir le chemin du fichier image sélectionné par l'utilisateur
        String imagePath = annonce.getImageUrl();  // Le chemin de l'image sélectionnée

        // Vérifier que le chemin de l'image n'est pas vide
        if (imagePath == null || imagePath.isEmpty()) {
            throw new IOException("L'image spécifiée n'est pas valide ou n'a pas été fournie.");
        }

        // Vérifier que le fichier existe
        File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            throw new IOException("L'image spécifiée n'existe pas.");
        }

        // Définir un nouveau chemin pour l'image dans le répertoire spécifique
        String newImagePath = IMAGE_DIRECTORY + imageFile.getName();

        // Copier l'image dans le répertoire "images/"
        Files.copy(imageFile.toPath(), Paths.get(newImagePath), StandardCopyOption.REPLACE_EXISTING);

        // S'assurer que la dateA est définie (si elle n'est pas fournie, utiliser la date actuelle)
        LocalDateTime dateA = annonce.getDateA() != null ? annonce.getDateA() : LocalDateTime.now();

        // Insertion dans la base de données avec le nouveau chemin de l'image
        String query = "INSERT INTO annonce (titre_a, description_a, date_a, imageUrl, evenement_id, image_a, dateA) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, annonce.getTitreA());
            statement.setString(2, annonce.getDescriptionA());
            statement.setTimestamp(3, Timestamp.valueOf(dateA));  // Insertion de la date
            statement.setString(4, newImagePath);  // Sauvegarder le chemin de l'image dans la base
            statement.setInt(5, annonce.getEvenementId());
            statement.setString(6, newImagePath); // Sauvegarder aussi dans la colonne image_a
            statement.setTimestamp(7, Timestamp.valueOf(dateA)); // Ajouter la date dans la colonne dateA

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("L'insertion a échoué, aucune ligne affectée");
            }

            // Récupérer l'ID généré pour l'annonce et le définir
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    annonce.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Échec de récupération de l'ID généré");
                }
            }
        }
    }

    // Récupérer les annonces par événement
    public List<Annonce> getAnnoncesByEvenement(int evenementId) throws SQLException {
        List<Annonce> annonces = new ArrayList<>();
        String query = "SELECT id, titre_a, description_a, date_a, imageUrl, evenement_id FROM annonce WHERE evenement_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, evenementId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Annonce annonce = new Annonce(
                            resultSet.getString("titre_a"),
                            resultSet.getString("description_a"),
                            resultSet.getTimestamp("date_a").toLocalDateTime(),
                            resultSet.getString("imageUrl"),
                            resultSet.getInt("evenement_id")
                    );
                    annonce.setId(resultSet.getInt("id"));
                    annonces.add(annonce);
                }
            }
        }
        return annonces;
    }

    // Supprimer une annonce
    public boolean supprimer(int id) throws SQLException {
        String query = "DELETE FROM annonce WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        }
    }

    // Modifier une annonce
    public boolean modifier(Annonce annonce) throws SQLException {
        String query = "UPDATE annonce SET titre_a = ?, description_a = ?, date_a = ?, imageUrl = ? WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, annonce.getTitreA());
            statement.setString(2, annonce.getDescriptionA());
            statement.setTimestamp(3, Timestamp.valueOf(annonce.getDateA()));
            statement.setString(4, annonce.getImageUrl());  // Le chemin de l'image
            statement.setInt(5, annonce.getId());

            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        }
    }

    // Récupérer toutes les annonces
    public List<Annonce> getAllAnnonces() throws SQLException {
        List<Annonce> annonces = new ArrayList<>();
        String query = "SELECT id, titre_a, description_a, date_a, imageUrl, evenement_id FROM annonce";

        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Annonce annonce = new Annonce(
                        resultSet.getString("titre_a"),
                        resultSet.getString("description_a"),
                        resultSet.getTimestamp("date_a").toLocalDateTime(),
                        resultSet.getString("imageUrl"),
                        resultSet.getInt("evenement_id")
                );
                annonce.setId(resultSet.getInt("id"));
                annonces.add(annonce);
            }
        }
        return annonces;
    }

    // Récupérer une annonce par son ID
    public Annonce getAnnonceById(int id) throws SQLException {
        String query = "SELECT id, titre_a, description_a, date_a, imageUrl, evenement_id FROM annonce WHERE id = ?";
        Annonce annonce = null;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    annonce = new Annonce(
                            resultSet.getString("titre_a"),
                            resultSet.getString("description_a"),
                            resultSet.getTimestamp("date_a").toLocalDateTime(),
                            resultSet.getString("imageUrl"),
                            resultSet.getInt("evenement_id")
                    );
                    annonce.setId(resultSet.getInt("id"));
                }
            }
        }
        return annonce;
    }

    public Annonce getLatestAnnonce() throws SQLException {
        String query = "SELECT * FROM annonce ORDER BY dateA DESC LIMIT 1";

        try (Connection connection = MyDataBase.getInstance().getCnx();
             PreparedStatement pstmt = connection.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                return mapResultSetToAnnonce(rs); // Use your existing mapping logic
            }
            return null;
        }
    }
    public void addAnnonceCreatedListener(Consumer<Annonce> listener) {
        listeners.add(listener);
    }

    private void notifyAnnonceCreated(Annonce annonce) {
        listeners.forEach(listener -> listener.accept(annonce));
    }

    public void createAnnonce(Annonce annonce) throws SQLException {
        // Your existing create logic
        notifyAnnonceCreated(annonce); // Call this after successful creation
    }

    private Annonce mapResultSetToAnnonce(ResultSet rs) throws SQLException {
        Annonce annonce = new Annonce(
                rs.getString("titre_a"),
                rs.getString("description_a"),
                rs.getTimestamp("date_a").toLocalDateTime(),
                rs.getString("imageUrl"),
                rs.getInt("evenement_id")
        );
        annonce.setId(rs.getInt("id"));

        // Add any additional fields from your database table
        if (rs.getTimestamp("dateA") != null) {
            annonce.setDateA(rs.getTimestamp("dateA").toLocalDateTime());
        }

        return annonce;
    }

    public Map<String, Integer> getAnnonceCountByEventType() {
        Map<String, Integer> result = new HashMap<>();
        String query = "SELECT e.type, COUNT(a.id) as count " +
                       "FROM annonce a " +
                       "JOIN evenement e ON a.evenement_id = e.id " +
                       "GROUP BY e.type";
        
        try (Connection conn = MyDataBase.getInstance().getCnx();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                String type = rs.getString("type");
                int count = rs.getInt("count");
                
                // If type is null, use "Other" as the category
                if (type == null || type.isEmpty()) {
                    type = "Other";
                }
                
                result.put(type, count);
            }
        } catch (SQLException e) {
            System.err.println("Error getting annonce counts by event type: " + e.getMessage());
        }
        
        return result;
    }
}
