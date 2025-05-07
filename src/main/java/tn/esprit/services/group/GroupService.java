package tn.esprit.services.group;

import tn.esprit.entities.group.GroupStudent;
import tn.esprit.entities.project.Project;
import tn.esprit.services.IService;
import tn.esprit.services.project.ProjectService;
import tn.esprit.tools.MyDataBase;

import java.util.List;
import java.sql.*;
import java.util.ArrayList;
import java.util.Optional;

public class GroupService implements IService<GroupStudent> {
    private Connection cnx;
    private static GroupService instance;

    public GroupService() {
        this.cnx = MyDataBase.getInstance().getCnx();
    }

    public static GroupService getInstance() {
        if (instance == null) {
            instance = new GroupService();
        }
        return instance;
    }

  @Override
    public void ajouter(GroupStudent group) {
        String query = "INSERT INTO groupstudent (name, description, image, pdf_file, meeting_date) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pst = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            // Insérer les informations du groupe
            pst.setString(1, group.getName());
            pst.setString(2, group.getDescription());
            pst.setString(3, group.getImage());
            pst.setString(4, group.getPdfFile());
            pst.setDate(5, java.sql.Date.valueOf(group.getMeetingDate()));
            pst.executeUpdate();

            // Récupérer l'ID généré pour le groupe
            ResultSet rs = pst.getGeneratedKeys();
            if (rs.next()) {
                Long groupId = rs.getLong(1);
                group.setId(groupId);
            }

            // Ajouter les projets associés au groupe
            ProjectService projectService = ProjectService.getInstance();
            for (Project project : group.getProjects()) {
                // Si le projet n'existe pas déjà, on l'ajoute (selon votre logique de gestion des projets)
                Optional<Project> existingProject = projectService.findByTitle(project.getTitre());
                if (existingProject.isEmpty()) {
                    // Si le projet n'existe pas, on peut l'ajouter
                    String insertProjectQuery = "INSERT INTO project (titre, description_project, fichier_pdf, date_creation_project, difficulte, date_limite, image) VALUES (?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement insertProjectStmt = cnx.prepareStatement(insertProjectQuery, Statement.RETURN_GENERATED_KEYS)) {
                        insertProjectStmt.setString(1, project.getTitre());
                        insertProjectStmt.setString(2, project.getDescriptionProject());
                        insertProjectStmt.setString(3, project.getPdfFile());
                        insertProjectStmt.setDate(4, java.sql.Date.valueOf(project.getCreationDate()));
                        insertProjectStmt.setInt(5, project.getDifficulte());
                        insertProjectStmt.setDate(6, java.sql.Date.valueOf(project.getDeadline()));
                        insertProjectStmt.setString(7, project.getImage());
                        insertProjectStmt.executeUpdate();

                        // Récupérer l'ID du projet et le lier au groupe
                        ResultSet projectRs = insertProjectStmt.getGeneratedKeys();
                        if (projectRs.next()) {
                            Long projectId = projectRs.getLong(1);
                            project.setId(projectId);

                            // Associer le projet au groupe
                            String updateProjectQuery = "UPDATE project SET group_id = ? WHERE id = ?";
                            try (PreparedStatement updateStmt = cnx.prepareStatement(updateProjectQuery)) {
                                updateStmt.setLong(1, group.getId());
                                updateStmt.setLong(2, project.getId());
                                updateStmt.executeUpdate();
                            }
                        }
                    }
                } else {
                    // Si le projet existe déjà, on associe simplement le projet au groupe
                    String updateProjectQuery = "UPDATE project SET group_id = ? WHERE id = ?";
                    try (PreparedStatement updateStmt = cnx.prepareStatement(updateProjectQuery)) {
                        updateStmt.setLong(1, group.getId());
                        updateStmt.setLong(2, existingProject.get().getId());
                        updateStmt.executeUpdate();
                    }
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'ajout du groupe : " + e.getMessage(), e);
        }
    }


    @Override
    public void modifier(@SuppressWarnings("exports") GroupStudent group) {
        String query = "UPDATE groupstudent SET description_group = ?, nom_group = ?, "
                + "date_creation_group = ?, image = ?, date_meet = ? WHERE id = ?";

        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setString(1, group.getDescription());
            pst.setString(2, group.getName());
            pst.setDate(3, Date.valueOf(group.getCreationDate()));
            pst.setString(4, group.getImage());
            pst.setDate(5, Date.valueOf(group.getMeetingDate()));
            pst.setLong(6, group.getId());

            int rowsUpdated = pst.executeUpdate();
            if (rowsUpdated == 0) {
                System.out.println("No group found with ID: " + group.getId());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update group: " + e.getMessage(), e);
        }
    }

    @Override
    public void supprimer(Long id) {
        String query = "DELETE FROM groupstudent WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setLong(1, id);
            int rowsDeleted = pst.executeUpdate();
            if (rowsDeleted == 0) {
                System.out.println("No group found with ID: " + id);
            } else {
                System.out.println("Group with ID " + id + " was successfully deleted.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete group: " + e.getMessage(), e);
        }
    }

    @Override
    public GroupStudent getOne(Long id) {
        String query = "SELECT * FROM groupstudent WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setLong(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToGroup(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get group by ID: " + e.getMessage(), e);
        }
        return null;  // Si aucun groupe n'est trouvé, on retourne null
    }

    @Override
    public List<GroupStudent> getAll() {
        List<GroupStudent> groups = new ArrayList<>();
        String query = "SELECT * FROM groupstudent";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                groups.add(mapResultSetToGroup(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get groups: " + e.getMessage(), e);
        }
        return groups;
    }

    private GroupStudent mapResultSetToGroup(ResultSet rs) throws SQLException {
        GroupStudent group = new GroupStudent();
        
        group.setId(rs.getLong("id"));
        group.setDescription(rs.getString("description_group"));
        group.setName(rs.getString("nom_group"));
        group.setCreationDate(rs.getDate("date_creation_group").toLocalDate());
    
        Date meetingDate = rs.getDate("date_meet");
        if (meetingDate != null) {
            group.setMeetingDate(meetingDate.toLocalDate());
        } else {
            group.setMeetingDate(null);
        }
    
        group.setImage(rs.getString("image"));
        
        return group;
    }
}
