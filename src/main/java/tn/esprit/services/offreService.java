package tn.esprit.services;

import tn.esprit.entities.Offre;
import tn.esprit.tools.MyDataBase;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class offreService implements Iservicesp <Offre> {
    Connection cnx;

    public offreService() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void ajouter(Offre offre) throws SQLException {
        String sql = "INSERT INTO offre (nom_offre, image_offre, prix, date_debut, date_fin, description) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, offre.getName());
            pst.setString(2, offre.getImagePath());
            pst.setDouble(3, offre.getPrice());
            pst.setTimestamp(4, Timestamp.valueOf(offre.getStartDate()));
            pst.setDate(5, Date.valueOf(offre.getEndDate()));
            pst.setString(6, offre.getDescription());

            int affectedRows = pst.executeUpdate();

            // Vérifier si l'insertion a réussi
            if (affectedRows > 0) {
                // Récupérer les clés générées (ID auto-incrémenté)
                try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        offre.setId(generatedKeys.getLong(1)); // Affecter l'ID à l'objet Offre
                        System.out.println("Offre ID après insertion : " + offre.getId());
                    }
                }
            }
        }
    }



    @Override
    public void modifier(int id, Offre newOffre) throws SQLException {
        String sql = "UPDATE offre SET " +
                "nom_offre = '" + newOffre.getName() + "', " +
                "image_offre = '" + newOffre.getImagePath() + "', " +
                "prix = " + newOffre.getPrice() + ", " +
                "date_debut = '" + Timestamp.valueOf(newOffre.getStartDate()) + "', " +
                "date_fin = '" + Date.valueOf(newOffre.getEndDate()) + "', " +
                "description = '" + newOffre.getDescription() + "' " +
                "WHERE id_offre = " + id;

        Statement st = cnx.createStatement();
        int rowsUpdated = st.executeUpdate(sql);

        if (rowsUpdated > 0) {
            System.out.println(" Offre modifiée avec succès !");
        } else {
            System.out.println("Aucune offre trouvée avec l'ID : " + id);
        }
    }


    @Override
    public List<Offre> recuperer() throws SQLException {
        String sql = "SELECT * FROM offre";
        Statement ste = cnx.createStatement();
        ResultSet rs = ste.executeQuery(sql);
        List<Offre> offres = new ArrayList<>();

        while (rs.next()) {

            int id = rs.getInt("id_offre");
            String name = rs.getString("nom_offre");
            String imagePath = rs.getString("image_offre");
            double prix = rs.getDouble("prix");
            Timestamp dateDebutTimestamp = rs.getTimestamp("date_debut");
            Date dateFin = rs.getDate("date_fin");
            String description = rs.getString("description");


            LocalDateTime dateDebut = (dateDebutTimestamp != null) ? dateDebutTimestamp.toLocalDateTime() : null;
            LocalDate dateFinLocal = (dateFin != null) ? dateFin.toLocalDate() : null;


            Offre offre = new Offre(name, imagePath, prix, dateDebut, dateFinLocal, description);
            offre.setId((long) id);
            offres.add(offre);
        }

        return offres;
    }
    @Override
    public void supprimer(Offre offre) throws SQLException {
        String sql = "DELETE FROM offre WHERE id_offre = " + offre.getId();
        Statement st = cnx.createStatement();
        int rowsDeleted = st.executeUpdate(sql);

        if (rowsDeleted > 0) {
            System.out.println(" Offre supprimée !");
        } else {
            System.out.println(" Aucune offre trouvée avec cet ID.");
        }
    }
}