package tn.esprit.services;

import tn.esprit.entities.User;
import tn.esprit.tools.MyDataBase;
import tn.esprit.entities.Offre;
import tn.esprit.entities.Paiement;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.time.YearMonth;

public class paiementService implements Iservicesp <Paiement> {
    Connection cnx;

    public paiementService() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void ajouter(Paiement paiement) throws SQLException {
        String sql = "INSERT INTO paiement (id_user, id_offre, date_paiement) VALUES (?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Utilisez directement l'ID de l'utilisateur dans l'objet Paiement
            long userId = paiement.getUser().getId();

            // Vérifiez si l'utilisateur existe dans la base de données
            String checkUserQuery = "SELECT id FROM user WHERE id = ?";
            try (PreparedStatement checkUserStmt = cnx.prepareStatement(checkUserQuery)) {
                checkUserStmt.setLong(1, userId);
                try (ResultSet rs = checkUserStmt.executeQuery()) {
                    if (rs.next()) {
                        // Utilisateur trouvé, on procède à l'insertion du paiement
                        pst.setLong(1, userId);  // ID de l'utilisateur
                        pst.setLong(2, paiement.getOffre().getId());  // ID de l'offre
                        pst.setTimestamp(3, Timestamp.valueOf(paiement.getPaymentDate()));  // Date du paiement

                        int affectedRows = pst.executeUpdate();

                        // Vérifier si l'insertion a réussi
                        if (affectedRows > 0) {
                            // Récupérer les clés générées (ID auto-incrémenté)
                            try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                                if (generatedKeys.next()) {
                                    paiement.setId(generatedKeys.getLong(1));  // Affecter l'ID à l'objet Paiement
                                    System.out.println("Paiement ID après insertion : " + paiement.getId());
                                }
                            }
                        }
                    } else {
                        System.out.println("Utilisateur non trouvé avec l'ID : " + userId);
                    }
                }
            }
        }
    }
    public List<User> getUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, name FROM user";  // Requête pour récupérer id et name des utilisateurs
        try (PreparedStatement pst = cnx.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getLong("id"));        // Récupérer l'ID de l'utilisateur
                user.setName(rs.getString("name"));  // Récupérer le nom de l'utilisateur
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();  // Gérer l'erreur correctement
        }
        return users;
    }


    @Override
    public void supprimer(Paiement paiement) throws SQLException {
        String sql = "DELETE FROM paiement WHERE id_paiement = " + paiement.getId();
        Statement st = cnx.createStatement();
        int rowsDeleted = st.executeUpdate(sql);

        if (rowsDeleted > 0) {
            System.out.println("Paiement supprimé avec succès !");
        } else {
            System.out.println("Aucun paiement trouvé avec cet ID.");
        }
    }

    @Override
    public void modifier(int id, Paiement newPaiement) throws SQLException {
        String sql = "UPDATE paiement SET " +
                "id_user = " + newPaiement.getUser().getId() + ", " +
                "id_offre = " + newPaiement.getOffre().getId() + ", " +
                "date_paiement = '" + Timestamp.valueOf(newPaiement.getPaymentDate()) + "' " +
                "WHERE id_paiement = " + id;

        Statement st = cnx.createStatement();
        int rowsUpdated = st.executeUpdate(sql);

        if (rowsUpdated > 0) {
            System.out.println("Paiement modifié avec succès !");
        } else {
            System.out.println("Aucun paiement trouvé avec l'ID : " + id);
        }
    }

    @Override
    public List<Paiement> recuperer() throws SQLException {
        String sql = """
        SELECT p.id_paiement, p.date_paiement,
               u.id AS user_id, u.name AS user_nom,
               o.id_offre AS offre_id, o.nom_offre AS offre_nom
        FROM paiement p
        JOIN user u ON p.id_user = u.id
        JOIN offre o ON p.id_offre = o.id_offre
    """;

        List<Paiement> paiements = new ArrayList<>();
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            User user = new User();
            user.setId(rs.getLong("user_id"));
            user.setName(rs.getString("user_nom"));

            Offre offre = new Offre();
            offre.setId(rs.getLong("offre_id"));
            offre.setName(rs.getString("offre_nom"));

            Paiement paiement = new Paiement(user, offre,
                    rs.getTimestamp("date_paiement").toLocalDateTime());
            paiement.setId(rs.getLong("id_paiement"));

            paiements.add(paiement);
        }

        return paiements;
    }


    public List<String> getAllUserNames() throws SQLException {
        List<String> names = new ArrayList<>();
        String sql = "SELECT nom FROM user";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            names.add(rs.getString("nom"));
        }
        return names;
    }

    public int getTotalPaymentCount() {
        int count = 0;
        String query = "SELECT COUNT(*) FROM paiement";
        
        try (Connection conn = MyDataBase.getInstance().getCnx();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting payments: " + e.getMessage());
        }
        
        return count;
    }

    public double getCurrentMonthRevenue() {
        double revenue = 0.0;
        String query = "SELECT SUM(o.price) as revenue " +
                      "FROM paiement p " +
                      "JOIN offre o ON p.id_offre = o.id " +
                      "WHERE MONTH(p.date_paiement) = MONTH(CURRENT_DATE()) " +
                      "AND YEAR(p.date_paiement) = YEAR(CURRENT_DATE())";
        
        try (Connection conn = MyDataBase.getInstance().getCnx();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                revenue = rs.getDouble("revenue");
            }
        } catch (SQLException e) {
            System.err.println("Error calculating revenue: " + e.getMessage());
        }
        
        return revenue;
    }

    public Map<String, Double> getPaymentAmountsByOffer() {
        Map<String, Double> result = new HashMap<>();
        String query = "SELECT o.name, SUM(o.price) as total_amount " +
                      "FROM paiement p " +
                      "JOIN offre o ON p.id_offre = o.id " +
                      "GROUP BY o.name";
        
        try (Connection conn = MyDataBase.getInstance().getCnx();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                String offerName = rs.getString("name");
                double amount = rs.getDouble("total_amount");
                
                result.put(offerName, amount);
            }
        } catch (SQLException e) {
            System.err.println("Error getting payment amounts by offer: " + e.getMessage());
        }
        
        return result;
    }

    public Map<YearMonth, Double> getRevenueByMonth() {
        Map<YearMonth, Double> result = new HashMap<>();
        String query = "SELECT YEAR(p.date_paiement) as year, MONTH(p.date_paiement) as month, " +
                      "SUM(o.price) as revenue " +
                      "FROM paiement p " +
                      "JOIN offre o ON p.id_offre = o.id " +
                      "GROUP BY YEAR(p.date_paiement), MONTH(p.date_paiement) " +
                      "ORDER BY year, month";
        
        try (Connection conn = MyDataBase.getInstance().getCnx();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                int year = rs.getInt("year");
                int month = rs.getInt("month");
                double revenue = rs.getDouble("revenue");
                
                YearMonth yearMonth = YearMonth.of(year, month);
                result.put(yearMonth, revenue);
            }
        } catch (SQLException e) {
            System.err.println("Error getting revenue by month: " + e.getMessage());
        }
        
        return result;
    }
}
