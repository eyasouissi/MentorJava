package tn.esprit.services;

import tn.esprit.entities.Evenement;
import tn.esprit.tools.MyDataBase;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class EvenementService {

    private Connection connection;
    private static final String IMAGE_DIRECTORY = "images/";  // Dossier de stockage des images

    public EvenementService() {
        connection = MyDataBase.getInstance().getCnx();
    }

    // Ajouter un événement avec gestion de l'image
    public void addEvenement(Evenement evenement) throws SQLException, IOException {
        // Vérifier si le répertoire des images existe, sinon le créer
        File dir = new File(IMAGE_DIRECTORY);
        if (!dir.exists()) {
            dir.mkdir();  // Créer le dossier si nécessaire
        }

        // Obtenir le chemin du fichier image sélectionné par l'utilisateur
        String imagePath = evenement.getImageE();  // Le chemin de l'image sélectionnée

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

        // S'assurer que la dateDebut et dateFin sont définies (si elles n'ont pas été fournies, utiliser la date actuelle)
        LocalDateTime dateDebut = evenement.getDateDebut() != null ? evenement.getDateDebut() : LocalDateTime.now();
        LocalDateTime dateFin = evenement.getDateFin() != null ? evenement.getDateFin() : LocalDateTime.now();

        // Insertion dans la base de données avec le nouveau chemin de l'image
        String query = "INSERT INTO evenement (titre_e, description_e, date_debut, date_fin, image_e, lieu, user_id) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, evenement.getTitreE());
            statement.setString(2, evenement.getDescriptionE());
            statement.setTimestamp(3, Timestamp.valueOf(dateDebut));  // Insertion de la date de début
            statement.setTimestamp(4, Timestamp.valueOf(dateFin));    // Insertion de la date de fin
            statement.setString(5, newImagePath);  // Sauvegarder le chemin de l'image dans la base
            statement.setString(6, evenement.getLieu());
            statement.setInt(7, evenement.getUserId());

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("L'insertion a échoué, aucune ligne affectée");
            }

            // Récupérer l'ID généré pour l'événement et le définir
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    evenement.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Échec de récupération de l'ID généré");
                }
            }
        }
    }

    // Récupérer tous les événements
    public List<Evenement> getAllEvenements() throws SQLException {
        List<Evenement> evenements = new ArrayList<>();
        String query = "SELECT id, titre_e, description_e, date_debut, date_fin, image_e, lieu, user_id FROM evenement";

        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Evenement evenement = new Evenement(
                        resultSet.getString("titre_e"),
                        resultSet.getString("description_e"),
                        resultSet.getTimestamp("date_debut").toLocalDateTime(),
                        resultSet.getTimestamp("date_fin").toLocalDateTime(),
                        resultSet.getString("image_e"),
                        resultSet.getString("lieu"),
                        resultSet.getInt("user_id")
                );
                evenement.setId(resultSet.getInt("id"));
                evenements.add(evenement);
            }
        }
        return evenements;
    }

    // Supprimer un événement
    public boolean deleteEvenement(int id) throws SQLException {
        String query = "DELETE FROM evenement WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);  // Passer l'ID de l'événement à supprimer
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;  // Si une ligne a été supprimée, retourner true
        }
    }

    // Modifier un événement
    public boolean modifier(Evenement evenement) throws SQLException {
        String query = "UPDATE evenement SET titre_e = ?, description_e = ?, date_debut = ?, date_fin = ?, image_e = ?, lieu = ? WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, evenement.getTitreE());
            statement.setString(2, evenement.getDescriptionE());
            statement.setTimestamp(3, Timestamp.valueOf(evenement.getDateDebut()));
            statement.setTimestamp(4, Timestamp.valueOf(evenement.getDateFin()));
            statement.setString(5, evenement.getImageE());  // Le chemin de l'image
            statement.setString(6, evenement.getLieu());
            statement.setInt(7, evenement.getId());

            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        }
    }

    // Récupérer un événement par son ID
    public Evenement getEvenementById(int id) throws SQLException {
        String query = "SELECT id, titre_e, description_e, date_debut, date_fin, image_e, lieu, user_id FROM evenement WHERE id = ?";
        Evenement evenement = null;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    evenement = new Evenement(
                            resultSet.getString("titre_e"),
                            resultSet.getString("description_e"),
                            resultSet.getTimestamp("date_debut").toLocalDateTime(),
                            resultSet.getTimestamp("date_fin").toLocalDateTime(),
                            resultSet.getString("image_e"),
                            resultSet.getString("lieu"),
                            resultSet.getInt("user_id")
                    );
                    evenement.setId(resultSet.getInt("id"));
                }
            }
        }
        return evenement;
    }

    // Méthode pour récupérer le nombre total d'événements (pour la pagination)
    public int getTotalEvenements() throws SQLException {
        String query = "SELECT COUNT(*) FROM evenement";
        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        }
        return 0;
    }

    // Récupérer les événements paginés
    public List<Evenement> getEvenementsByPage(int start, int pageSize) throws SQLException {
        String query = "SELECT id, titre_e, description_e, date_debut, date_fin, image_e, lieu, user_id FROM evenement LIMIT ?, ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, start); // Début de la page
            statement.setInt(2, pageSize); // Nombre d'éléments par page

            ResultSet resultSet = statement.executeQuery();
            List<Evenement> evenements = new ArrayList<>();

            while (resultSet.next()) {
                Evenement evenement = new Evenement(
                        resultSet.getString("titre_e"),
                        resultSet.getString("description_e"),
                        resultSet.getTimestamp("date_debut").toLocalDateTime(),
                        resultSet.getTimestamp("date_fin").toLocalDateTime(),
                        resultSet.getString("image_e"),
                        resultSet.getString("lieu"),
                        resultSet.getInt("user_id")
                );
                evenement.setId(resultSet.getInt("id"));
                evenements.add(evenement);
            }
            return evenements;
        }
    }

    // Method to get events for a given month and year
    public List<Evenement> getEvenementsForMonth(int month, int year) throws SQLException {
        List<Evenement> events = new ArrayList<>();

        String query = "SELECT id, titre_e, description_e, date_debut, date_fin, image_e, lieu, user_id FROM evenement " +
                "WHERE MONTH(date_debut) = ? AND YEAR(date_debut) = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, month);
            statement.setInt(2, year);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Evenement evenement = new Evenement(
                            resultSet.getString("titre_e"),
                            resultSet.getString("description_e"),
                            resultSet.getTimestamp("date_debut").toLocalDateTime(),
                            resultSet.getTimestamp("date_fin").toLocalDateTime(),
                            resultSet.getString("image_e"),
                            resultSet.getString("lieu"),
                            resultSet.getInt("user_id")
                    );
                    evenement.setId(resultSet.getInt("id"));
                    events.add(evenement);
                }
            }
        }
        return events;
    }

    // New method added: Get events between two dates
    public List<Evenement> getEventsBetweenDates(LocalDate startDate, LocalDate endDate) throws SQLException {
        List<Evenement> events = new ArrayList<>();

        String query = "SELECT id, titre_e, description_e, date_debut, date_fin, image_e, lieu, user_id FROM evenement " +
                "WHERE date_debut BETWEEN ? AND ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setDate(1, Date.valueOf(startDate));  // Convert LocalDate to SQL Date
            statement.setDate(2, Date.valueOf(endDate));    // Convert LocalDate to SQL Date

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Evenement evenement = new Evenement(
                            resultSet.getString("titre_e"),
                            resultSet.getString("description_e"),
                            resultSet.getTimestamp("date_debut").toLocalDateTime(),
                            resultSet.getTimestamp("date_fin").toLocalDateTime(),
                            resultSet.getString("image_e"),
                            resultSet.getString("lieu"),
                            resultSet.getInt("user_id")
                    );
                    evenement.setId(resultSet.getInt("id"));
                    events.add(evenement);
                }
            }
        }
        return events;
    }
    // Dans EvenementService.java

    // Statistiques par jour
    public Map<LocalDate, Long> getEventsCountByDay() throws SQLException {
        Map<LocalDate, Long> stats = new TreeMap<>();
        String query = "SELECT DATE(date_debut) as day, COUNT(*) as count FROM evenement GROUP BY DATE(date_debut)";

        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                LocalDate day = resultSet.getDate("day").toLocalDate();
                long count = resultSet.getLong("count");
                stats.put(day, count);
            }
        }
        return stats;
    }

    // Statistiques par semaine
    public Map<YearWeek, Long> getEventsCountByWeek() throws SQLException {
        Map<YearWeek, Long> stats = new TreeMap<>();
        String query = "SELECT YEAR(date_debut) as year, WEEK(date_debut) as week, COUNT(*) as count " +
                "FROM evenement GROUP BY YEAR(date_debut), WEEK(date_debut)";

        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                int year = resultSet.getInt("year");
                int week = resultSet.getInt("week");
                YearWeek yearWeek = YearWeek.of(year, week);
                long count = resultSet.getLong("count");
                stats.put(yearWeek, count);
            }
        }
        return stats;
    }

    // Statistiques par mois
    public Map<YearMonth, Long> getEventsCountByMonth() throws SQLException {
        Map<YearMonth, Long> stats = new TreeMap<>();
        String query = "SELECT YEAR(date_debut) as year, MONTH(date_debut) as month, COUNT(*) as count " +
                "FROM evenement GROUP BY YEAR(date_debut), MONTH(date_debut)";

        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                int year = resultSet.getInt("year");
                int month = resultSet.getInt("month");
                YearMonth yearMonth = YearMonth.of(year, month);
                long count = resultSet.getLong("count");
                stats.put(yearMonth, count);
            }
        }
        return stats;
    }
}
