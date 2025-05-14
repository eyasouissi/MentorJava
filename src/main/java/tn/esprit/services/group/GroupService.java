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
import java.util.Map;
import java.util.HashMap;
import java.time.YearMonth;

public class GroupService implements IService<GroupStudent> {
    private static GroupService instance;

    public GroupService() {
        // No connection initialization in constructor
    }

    public static GroupService getInstance() {
        if (instance == null) {
            instance = new GroupService();
        }
        return instance;
    }

    @Override
    public void ajouter(GroupStudent group) {
        String query = "INSERT INTO groupstudent (nom_group, description, image, fichier_pdf, date_creation_group, date_meet, nbr_members) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection cnx = MyDataBase.getInstance().getCnx();
             PreparedStatement pst = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            // Existing parameters
            pst.setString(1, group.getName());
            pst.setString(2, group.getDescription());
            pst.setString(3, group.getImage());
            pst.setString(4, group.getPdfFile());
            pst.setDate(5, java.sql.Date.valueOf(group.getCreationDate()));
            pst.setDate(6, group.getMeetingDate() != null ? java.sql.Date.valueOf(group.getMeetingDate()) : null);
            pst.setInt(7, group.getMemberCount() != null ? group.getMemberCount() : 0);
            pst.setLong(8, group.getCreatedById());

            if (group.getMeetingDate() != null) {
                pst.setDate(6, java.sql.Date.valueOf(group.getMeetingDate()));
            } else {
                pst.setNull(6, Types.DATE);
            }

            pst.setInt(7, group.getMemberCount() != null ? group.getMemberCount() : 0);

            pst.executeUpdate();

            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (rs.next()) {
                    group.setId(rs.getLong(1));
                }
            }

            if (group.getProjects() != null && !group.getProjects().isEmpty()) {
                ProjectService projectService = ProjectService.getInstance();
                for (Project project : group.getProjects()) {
                    project.setGroup(group);
                    projectService.ajouter(project);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error adding group: " + e.getMessage(), e);
        }
    }

    public GroupStudent addGroup(GroupStudent group) {
        ajouter(group);
        return group;
    }

    @Override
    public void modifier(GroupStudent group) {
        String query = "UPDATE groupstudent SET nom_group = ?, description = ?, "
                + "date_creation_group = ?, image = ?, date_meet = ?, nbr_members = ?, meeting_url = ? WHERE id = ?";

        try (Connection cnx = MyDataBase.getInstance().getCnx();
             PreparedStatement pst = cnx.prepareStatement(query)) {

            pst.setString(1, group.getName());
            pst.setString(2, group.getDescription());
            pst.setDate(3, Date.valueOf(group.getCreationDate()));
            pst.setString(4, group.getImage());

            if (group.getMeetingDate() != null) {
                pst.setDate(5, Date.valueOf(group.getMeetingDate()));
            } else {
                pst.setNull(5, Types.DATE);
            }

            pst.setInt(6, group.getMemberCount() != null ? group.getMemberCount() : 0);

            if (group.getMeetingUrl() != null) {
                pst.setString(7, group.getMeetingUrl());
            } else {
                pst.setNull(7, Types.VARCHAR);
            }

            pst.setLong(8, group.getId());

            int rowsUpdated = pst.executeUpdate();
            if (rowsUpdated == 0) {
                System.out.println("No group found with ID: " + group.getId());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update group: " + e.getMessage(), e);
        }
    }

    public GroupStudent updateGroup(GroupStudent group) {
        modifier(group);
        return group;
    }

    @Override
    public void supprimer(Long id) {
        String query = "DELETE FROM groupstudent WHERE id = ?";
        try (Connection cnx = MyDataBase.getInstance().getCnx();
             PreparedStatement pst = cnx.prepareStatement(query)) {

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
        try (Connection cnx = MyDataBase.getInstance().getCnx();
             PreparedStatement pst = cnx.prepareStatement(query)) {

            pst.setLong(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToGroup(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get group by ID: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<GroupStudent> getAll() {
        List<GroupStudent> groups = new ArrayList<>();
        String query = "SELECT * FROM groupstudent";

        try (Connection cnx = MyDataBase.getInstance().getCnx();
             Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                groups.add(mapResultSetToGroup(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get groups: " + e.getMessage(), e);
        }
        return groups;
    }

    public List<GroupStudent> getAllGroups() {
        return getAll();
    }

    private GroupStudent mapResultSetToGroup(ResultSet rs) throws SQLException {
        GroupStudent group = new GroupStudent();

        group.setId(rs.getLong("id"));
        group.setName(rs.getString("nom_group"));
        group.setDescription(rs.getString("description"));

        Date creationDate = rs.getDate("date_creation_group");
        if (creationDate != null) {
            group.setCreationDate(creationDate.toLocalDate());
        }

        Date meetingDate = rs.getDate("date_meet");
        if (meetingDate != null) {
            group.setMeetingDate(meetingDate.toLocalDate());
        }

        group.setImage(rs.getString("image"));
        group.setPdfFile(rs.getString("fichier_pdf"));
        group.setMemberCount(rs.getInt("nbr_members"));

        try {
            group.setMeetingUrl(rs.getString("meeting_url"));
        } catch (SQLException e) {
            System.err.println("Warning: meeting_url column not found: " + e.getMessage());
        }

        return group;
    }

    public boolean isUserMemberOfGroup(Long userId, Long groupId) {
        String query = "SELECT * FROM group_student_members WHERE user_id = ? AND group_id = ?";
        try (Connection cnx = MyDataBase.getInstance().getCnx();
             PreparedStatement pst = cnx.prepareStatement(query)) {

            pst.setLong(1, userId);
            pst.setLong(2, groupId);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Database error in isUserMemberOfGroup: " + e.getMessage());
            throw new RuntimeException("Failed to check if user is a member: " + e.getMessage(), e);
        }
    }

    public boolean joinGroup(Long userId, Long groupId) {
        if (isUserMemberOfGroup(userId, groupId)) {
            return true;
        }

        try (Connection cnx = MyDataBase.getInstance().getCnx()) {
            cnx.setAutoCommit(false);

            try {
                String addMemberQuery = "INSERT INTO group_student_members (user_id, group_id, group_student_id) VALUES (?, ?, ?)";
                try (PreparedStatement pst = cnx.prepareStatement(addMemberQuery)) {
                    pst.setLong(1, userId);
                    pst.setLong(2, groupId);
                    pst.setLong(3, groupId);
                    pst.executeUpdate();
                }

                String updateCountQuery = "UPDATE groupstudent SET nbr_members = nbr_members + 1 WHERE id = ?";
                try (PreparedStatement pst = cnx.prepareStatement(updateCountQuery)) {
                    pst.setLong(1, groupId);
                    pst.executeUpdate();
                }

                cnx.commit();
                return true;
            } catch (SQLException e) {
                cnx.rollback();
                throw e;
            } finally {
                cnx.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to join group: " + e.getMessage(), e);
        }
    }

    public boolean leaveGroup(Long userId, Long groupId) {
        try (Connection cnx = MyDataBase.getInstance().getCnx()) {
            cnx.setAutoCommit(false);

            try {
                if (!isUserMemberOfGroup(userId, groupId)) {
                    return false;
                }

                String removeMemberQuery = "DELETE FROM group_student_members WHERE user_id = ? AND group_id = ?";
                try (PreparedStatement pst = cnx.prepareStatement(removeMemberQuery)) {
                    pst.setLong(1, userId);
                    pst.setLong(2, groupId);
                    pst.executeUpdate();
                }

                String updateCountQuery = "UPDATE groupstudent SET nbr_members = GREATEST(nbr_members - 1, 0) WHERE id = ?";
                try (PreparedStatement pst = cnx.prepareStatement(updateCountQuery)) {
                    pst.setLong(1, groupId);
                    pst.executeUpdate();
                }

                cnx.commit();
                return true;
            } catch (SQLException e) {
                cnx.rollback();
                throw e;
            } finally {
                cnx.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to leave group: " + e.getMessage(), e);
        }
    }

    public List<Long> getGroupMembers(Long groupId) {
        List<Long> memberIds = new ArrayList<>();
        String query = "SELECT user_id FROM group_student_members WHERE group_id = ?";

        try (Connection cnx = MyDataBase.getInstance().getCnx();
             PreparedStatement pst = cnx.prepareStatement(query)) {

            pst.setLong(1, groupId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    memberIds.add(rs.getLong("user_id"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get group members: " + e.getMessage(), e);
        }

        return memberIds;
    }

    public Map<String, Integer> getGroupProjectCounts() {
        Map<String, Integer> projectCounts = new HashMap<>();
        String query = "SELECT g.nom_group, COUNT(p.id) as project_count " +
                "FROM groupstudent g " +
                "LEFT JOIN project p ON p.group_id = g.id " +
                "GROUP BY g.id, g.nom_group " +
                "ORDER BY project_count DESC";

        try (Connection cnx = MyDataBase.getInstance().getCnx();
             PreparedStatement stmt = cnx.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                projectCounts.put(rs.getString("nom_group"), rs.getInt("project_count"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting group project counts: " + e.getMessage());
        }

        return projectCounts;
    }

    public Map<YearMonth, Integer> getGroupCountsByMonth() {
        Map<YearMonth, Integer> result = new HashMap<>();
        String query = "SELECT YEAR(date_creation_group) as year, MONTH(date_creation_group) as month, " +
                "COUNT(*) as count FROM groupstudent " +
                "GROUP BY YEAR(date_creation_group), MONTH(date_creation_group) " +
                "ORDER BY year, month";

        try (Connection cnx = MyDataBase.getInstance().getCnx();
             PreparedStatement stmt = cnx.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                result.put(
                        YearMonth.of(rs.getInt("year"), rs.getInt("month")),
                        rs.getInt("count")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error getting group counts by month: " + e.getMessage());
        }

        return result;
    }
}