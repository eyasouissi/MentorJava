package tn.esprit.services.project;

import tn.esprit.entities.group.GroupStudent;
import tn.esprit.entities.project.Project;
import tn.esprit.services.IService;
import tn.esprit.tools.MyDataBase;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class ProjectService implements IService<Project> {
    private static ProjectService instance;

    public ProjectService() {
        // No connection initialization in constructor
    }

    public static synchronized ProjectService getInstance() {
        if (instance == null) {
            instance = new ProjectService();
        }
        return instance;
    }

    @Override
    public void ajouter(Project project) {
        String query = "INSERT INTO project (titre, description_project, difficulte, group_id, image, date_creation_project, description, title, difficulty) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection cnx = MyDataBase.getInstance().getCnx();
             PreparedStatement pst = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            setProjectParameters(pst, project);
            pst.setDate(6, Date.valueOf(LocalDate.now())); // Set current date

            pst.executeUpdate();

            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (rs.next()) {
                    project.setId(rs.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add project: " + e.getMessage(), e);
        }
    }

    public Project addProject(Project project) {
        ajouter(project);
        return project;
    }

    @Override
    public void modifier(Project project) {
        String query = "UPDATE project SET titre = ?, description_project = ?, difficulte = ?, " +
                "group_id = ?, image = ?, description = ?, title = ?, difficulty = ? WHERE id = ?";

        try (Connection cnx = MyDataBase.getInstance().getCnx();
             PreparedStatement pst = cnx.prepareStatement(query)) {

            setProjectParameters(pst, project);
            pst.setLong(9, project.getId());

            int rowsUpdated = pst.executeUpdate();
            if (rowsUpdated == 0) {
                System.out.println("No project found with ID: " + project.getId());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update project: " + e.getMessage(), e);
        }
    }

    public Project updateProject(Project project) {
        modifier(project);
        return project;
    }

    @Override
    public void supprimer(Long id) {
        String query = "DELETE FROM project WHERE id = ?";

        try (Connection cnx = MyDataBase.getInstance().getCnx();
             PreparedStatement pst = cnx.prepareStatement(query)) {

            pst.setLong(1, id);
            int rowsDeleted = pst.executeUpdate();

            if (rowsDeleted == 0) {
                System.out.println("No project found with ID: " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete project: " + e.getMessage(), e);
        }
    }

    @Override
    public Project getOne(Long id) {
        String query = "SELECT * FROM project WHERE id = ?";

        try (Connection cnx = MyDataBase.getInstance().getCnx();
             PreparedStatement pst = cnx.prepareStatement(query)) {

            pst.setLong(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProject(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get project by ID: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<Project> getAll() {
        List<Project> projects = new ArrayList<>();
        String query = "SELECT * FROM project";

        try (Connection cnx = MyDataBase.getInstance().getCnx();
             Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                projects.add(mapResultSetToProject(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get projects: " + e.getMessage(), e);
        }
        return projects;
    }

    public Optional<Project> findByTitle(String title) {
        String query = "SELECT * FROM project WHERE titre = ?";

        try (Connection cnx = MyDataBase.getInstance().getCnx();
             PreparedStatement pst = cnx.prepareStatement(query)) {

            pst.setString(1, title);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToProject(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find project by title: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public List<Project> getProjectsByGroupId(Long groupId) {
        List<Project> projects = new ArrayList<>();
        String query = "SELECT * FROM project WHERE group_id = ?";

        try (Connection cnx = MyDataBase.getInstance().getCnx();
             PreparedStatement pst = cnx.prepareStatement(query)) {

            pst.setLong(1, groupId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    projects.add(mapResultSetToProject(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get projects by group ID: " + e.getMessage(), e);
        }
        return projects;
    }

    public List<Project> getByGroupId(Long groupId) {
        List<Project> projects = new ArrayList<>();
        String query = "SELECT * FROM project WHERE group_id = ? ORDER BY date_creation_project DESC";

        try (Connection cnx = MyDataBase.getInstance().getCnx();
             PreparedStatement pst = cnx.prepareStatement(query)) {

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
        if (difficulty != null) {
            query.append(" AND difficulte = ?");
            params.add(difficulty);
        }
        if (startDate != null) {
            query.append(" AND date_creation_project >= ?");
            params.add(Date.valueOf(startDate));
        }
        if (endDate != null) {
            query.append(" AND date_creation_project <= ?");
            params.add(Date.valueOf(endDate));
        }

        try (Connection cnx = MyDataBase.getInstance().getCnx();
             PreparedStatement pst = cnx.prepareStatement(query.toString())) {

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

    public Map<Integer, Long> countProjectsByDifficulty() {
        return getAll().stream()
                .collect(Collectors.groupingBy(
                        Project::getDifficulte,
                        Collectors.counting()
                ));
    }

    public Map<LocalDate, Long> countProjectsByCreationDate() {
        Map<LocalDate, Long> result = new TreeMap<>();
        result.put(LocalDate.now(), (long) getAll().size());
        return result;
    }

    public List<String> getAllProjectTitles() {
        String sql = "SELECT titre FROM project";

        try (Connection cnx = MyDataBase.getInstance().getCnx();
             PreparedStatement stmt = cnx.prepareStatement(sql);
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
        return new ArrayList<>();
    }

    public List<Project> getProjectsFiltered(String searchTerm, LocalDate startDate, LocalDate endDate, Integer difficulty) {
        List<Project> allProjects = getAll();
        List<Project> filteredProjects = new ArrayList<>();

        for (Project project : allProjects) {
            boolean matches = true;

            if (searchTerm != null && !searchTerm.isEmpty()) {
                String searchLower = searchTerm.toLowerCase();
                matches = project.getTitre().toLowerCase().contains(searchLower) ||
                        project.getDescriptionProject().toLowerCase().contains(searchLower);
            }

            if (matches && startDate != null && project.getCreationDate() != null) {
                matches = !project.getCreationDate().isBefore(startDate);
            }

            if (matches && endDate != null && project.getCreationDate() != null) {
                matches = !project.getCreationDate().isAfter(endDate);
            }

            if (matches && difficulty != null) {
                matches = project.getDifficulte().equals(difficulty);
            }

            if (matches) {
                filteredProjects.add(project);
            }
        }

        return filteredProjects;
    }

    private Project mapResultSetToProject(ResultSet rs) throws SQLException {
        Project project = new Project();

        project.setId(rs.getLong("id"));
        project.setTitre(rs.getString("titre"));
        project.setDescriptionProject(rs.getString("description_project"));
        project.setDifficulte(rs.getInt("difficulte"));

        Date creationDate = rs.getDate("date_creation_project");
        if (creationDate != null) {
            project.setCreationDate(creationDate.toLocalDate());
        }

        Long groupId = rs.getLong("group_id");
        if (!rs.wasNull()) {
            GroupStudent group = new GroupStudent();
            group.setId(groupId);
            project.setGroup(group);
        }

        project.setImage(rs.getString("image"));
        return project;
    }

    private void setProjectParameters(PreparedStatement pst, Project project) throws SQLException {
        pst.setString(1, project.getTitre());
        pst.setString(2, project.getDescriptionProject());
        pst.setInt(3, project.getDifficulte());

        if (project.getGroup() != null && project.getGroup().getId() != null) {
            pst.setLong(4, project.getGroup().getId());
        } else {
            pst.setNull(4, Types.BIGINT);
        }

        pst.setString(5, project.getImage());
        pst.setString(6, project.getDescriptionProject());
        pst.setString(7, project.getTitre());
        pst.setString(8, String.valueOf(project.getDifficulte()));
    }
}