package tn.esprit.services.project;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet; // Pour JDBC
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import tn.esprit.entities.group.GroupStudent;
import tn.esprit.entities.project.Project;
import tn.esprit.services.IService;
import tn.esprit.tools.MyDataBase;

public class ProjectService implements IService<Project> {
    private Connection cnx;
    private static ProjectService instance;

    private ProjectService() {
        this.cnx = MyDataBase.getInstance().getCnx();
    }

    public static synchronized ProjectService getInstance() {
        if (instance == null) {
            instance = new ProjectService();
        }
        return instance;
    }

    @Override
    public void ajouter(Project project) {
        String query = "INSERT INTO project (titre, description_project, fichier_pdf, date_creation_project, difficulte, date_limite, image, group_id) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            setProjectParameters(pst, project);
            pst.executeUpdate();

            try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    project.setId(generatedKeys.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error adding project", e);
        }
    }

public void updateProject(Project project) throws SQLException {
    String query = "UPDATE project SET titre = ?, description_project = ?, difficulte = ?, deadline = ?, pdf_file = ?, image = ? WHERE id = ?";
    
    try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/votre_db", "user", "password");
         PreparedStatement statement = connection.prepareStatement(query)) {
        
        statement.setString(1, project.getTitre());
        statement.setString(2, project.getDescriptionProject());
        statement.setInt(3, project.getDifficulte());
        statement.setDate(4, java.sql.Date.valueOf(project.getDeadline()));
        statement.setString(5, project.getPdfFile());
        statement.setString(6, project.getImage());
        statement.setLong(7, project.getId());
        
        statement.executeUpdate();
    }
}
    @Override
    public void modifier(Project project) {
        String query = "UPDATE project SET titre = ?, description_project = ?, fichier_pdf = ?, date_creation_project = ?, difficulte = ?, date_limite = ?, image = ?, group_id = ? " +
                      "WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            setProjectParameters(pst, project);
            pst.setLong(9, project.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating project", e);
        }
    }

    @Override
    public void supprimer(Long id) {
        String query = "DELETE FROM project WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setLong(1, id);
            if (pst.executeUpdate() == 0) {
                throw new NoSuchElementException("No project found with id: " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting project", e);
        }
    }

    @Override
    public Project getOne(Long id) {
        String query = "SELECT * FROM project WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setLong(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProject(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting project", e);
        }
        throw new NoSuchElementException("Project not found with id: " + id);
    }

    @Override
    public List<Project> getAll() {
        List<Project> projects = new ArrayList<>();
        String query = "SELECT * FROM project ORDER BY date_creation_project DESC";
        try (Statement st = cnx.createStatement(); 
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                projects.add(mapResultSetToProject(rs));
            }
            return projects;
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching projects", e);
        }
    }

    // ==================== Méthodes spécifiques ====================

    public Optional<Project> findByTitle(String title) {
        String req = "SELECT * FROM project WHERE titre = ?";
        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setString(1, title);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToProject(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding project by title", e);
        }
        return Optional.empty();
    }

    public List<Project> getByGroupId(Long groupId) {
        List<Project> projects = new ArrayList<>();
        String query = "SELECT * FROM project WHERE group_id = ? ORDER BY date_creation_project DESC";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setLong(1, groupId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    projects.add(mapResultSetToProject(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting projects by group", e);
        }
        return projects;
    }

    public List<Project> searchProjects(String searchTerm, LocalDate startDate, LocalDate endDate, Integer difficulty) {
        List<Project> projects = new ArrayList<>();
        StringBuilder query = new StringBuilder("SELECT * FROM project WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (searchTerm != null && !searchTerm.isEmpty()) {
            query.append(" AND (titre LIKE ? OR description_project LIKE ?)");
            params.add("%" + searchTerm + "%");
            params.add("%" + searchTerm + "%");
        }
        if (startDate != null) {
            query.append(" AND date_creation_project >= ?");
            params.add(Date.valueOf(startDate));
        }
        if (endDate != null) {
            query.append(" AND date_creation_project <= ?");
            params.add(Date.valueOf(endDate));
        }
        if (difficulty != null) {
            query.append(" AND difficulte = ?");
            params.add(difficulty);
        }

        try (PreparedStatement pst = cnx.prepareStatement(query.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pst.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    projects.add(mapResultSetToProject(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error searching projects", e);
        }
        return projects;
    }

    // ==================== Méthodes pour les statistiques ====================

    public Map<Integer, Long> countProjectsByDifficulty() {
        return getAll().stream()
                .collect(Collectors.groupingBy(
                        Project::getDifficulte,
                        Collectors.counting()
                ));
    }

    public Map<LocalDate, Long> countProjectsByCreationDate() {
        return getAll().stream()
                .collect(Collectors.groupingBy(
                        Project::getCreationDate,
                        TreeMap::new,
                        Collectors.counting()
                ));
    }

    public List<String> getAllProjectTitles() {
        String sql = "SELECT titre FROM project";
        try (PreparedStatement stmt = cnx.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            List<String> titles = new ArrayList<>();
            while (rs.next()) {
                titles.add(rs.getString("titre"));
            }
            return titles;
        } catch (SQLException e) {
            throw new RuntimeException("Error getting project titles", e);
        }
    }

    public List<LocalDate> getAllDeadlines() {
        String sql = "SELECT date_limite FROM project WHERE date_limite IS NOT NULL";
        try (PreparedStatement stmt = cnx.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            List<LocalDate> deadlines = new ArrayList<>();
            while (rs.next()) {
                deadlines.add(rs.getDate("date_limite").toLocalDate());
            }
            return deadlines;
        } catch (SQLException e) {
            throw new RuntimeException("Error getting deadlines", e);
        }
    }

    // ==================== Méthodes helper ====================

    private void setProjectParameters(PreparedStatement pst, Project project) throws SQLException {
        pst.setString(1, project.getTitre());
        pst.setString(2, project.getDescriptionProject());
        pst.setString(3, project.getPdfFile());
        pst.setDate(4, Date.valueOf(project.getCreationDate()));
        pst.setInt(5, project.getDifficulte());
        if (project.getDeadline() != null) {
            pst.setDate(6, Date.valueOf(project.getDeadline()));
        } else {
            pst.setNull(6, Types.DATE);
        }
        pst.setString(7, project.getImage());
        if (project.getGroup() != null) {
            pst.setLong(8, project.getGroup().getId());
        } else {
            pst.setNull(8, Types.BIGINT);
        }
    }

    private Project mapResultSetToProject(ResultSet rs) throws SQLException {
        Project project = new Project();
        project.setId(rs.getLong("id"));
        project.setTitre(rs.getString("titre"));
        project.setDescriptionProject(rs.getString("description_project"));
        project.setPdfFile(rs.getString("fichier_pdf"));
        project.setCreationDate(rs.getDate("date_creation_project").toLocalDate());
        project.setDifficulte(rs.getInt("difficulte"));
        
        Date deadline = rs.getDate("date_limite");
        project.setDeadline(deadline != null ? deadline.toLocalDate() : null);
        
        project.setImage(rs.getString("image"));

        long groupId = rs.getLong("group_id");
        if (!rs.wasNull()) {
            GroupStudent group = new GroupStudent();
            group.setId(groupId);
            project.setGroup(group);
        }

        return project;
    }


    public List<Project> getProjectsFiltered(String searchTerm, LocalDate startDate, LocalDate endDate, Integer difficulty) {
        // Récupère tous les projets
        List<Project> allProjects = getAll();
        List<Project> filteredProjects = new ArrayList<>();
    
        for (Project project : allProjects) {
            boolean matches = true;
    
            // Filtre par texte (titre ou description)
            if (searchTerm != null && !searchTerm.isEmpty()) {
                String searchLower = searchTerm.toLowerCase();
                matches = project.getTitre().toLowerCase().contains(searchLower) ||
                         project.getDescriptionProject().toLowerCase().contains(searchLower);
            }
    
            // Filtre par date de création
            if (matches && startDate != null) {
                matches = !project.getCreationDate().isBefore(startDate);
            }
    
            if (matches && endDate != null) {
                matches = !project.getCreationDate().isAfter(endDate);
            }
    
            // Filtre par difficulté
            if (matches && difficulty != null) {
                matches = project.getDifficulte().equals(difficulty);
            }
    
            if (matches) {
                filteredProjects.add(project);
            }
        }
    
        return filteredProjects;
    }

}