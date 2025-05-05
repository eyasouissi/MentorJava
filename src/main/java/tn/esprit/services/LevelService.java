package tn.esprit.services;

import tn.esprit.entities.Level;
import tn.esprit.entities.Courses;
import tn.esprit.tools.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LevelService implements IServices<Level> {
    private Connection cnx;
    private static LevelService instance;

    public LevelService() {
        this.cnx = MyDataBase.getInstance().getCnx();
    }

    public static LevelService getInstance() {
        if (instance == null) {
            instance = new LevelService();
        }
        return instance;
    }

    @Override
    public void ajouter(Level level) {
        String query = "INSERT INTO level (name, course_id, previous_level_id, is_complete) " +
                "VALUES (?, ?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, level.getName());
            pst.setInt(2, level.getCourse().getId());
            if (level.getPreviousLevel() != null) {
                pst.setInt(3, level.getPreviousLevel().getId());
            } else {
                pst.setNull(3, Types.INTEGER);  // Handle no previous level
            }
            pst.setBoolean(4, level.isComplete());

            int affectedRows = pst.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating level failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    level.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating level failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add level: " + e.getMessage(), e);
        }
    }

    @Override
    public void modifier(Level level) {
        String query = "UPDATE level SET name = ?, course_id = ?, previous_level_id = ?, is_complete = ? WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setString(1, level.getName());
            pst.setInt(2, level.getCourse().getId());
            if (level.getPreviousLevel() != null) {
                pst.setInt(3, level.getPreviousLevel().getId());
            } else {
                pst.setNull(3, Types.INTEGER);  // Handle no previous level
            }
            pst.setBoolean(4, level.isComplete());
            pst.setInt(5, level.getId());

            int rowsUpdated = pst.executeUpdate();
            if (rowsUpdated == 0) {
                System.out.println("No level found with ID: " + level.getId());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update level: " + e.getMessage(), e);
        }
    }

    @Override
    public void supprimer(int id) {
        String query = "DELETE FROM level WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, id);
            int rowsDeleted = pst.executeUpdate();
            if (rowsDeleted == 0) {
                System.out.println("No level found with ID: " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete level: " + e.getMessage(), e);
        }
    }

    @Override
    public Level getOne(Level level) {
        return getById(level.getId());
    }

    public Level getById(int id) {
        String query = "SELECT * FROM level WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return mapResultSetToLevel(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get level: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<Level> getAll() {
        List<Level> levels = new ArrayList<>();
        String query = "SELECT * FROM level";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                levels.add(mapResultSetToLevel(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get levels: " + e.getMessage(), e);
        }
        return levels;
    }

    private Level mapResultSetToLevel(ResultSet rs) throws SQLException {
        Level level = new Level();
        level.setId(rs.getInt("id"));
        level.setName(rs.getString("name"));

        int courseId = rs.getInt("course_id");
        level.setCourse(new CoursesService().getById(courseId));

        int previousLevelId = rs.getInt("previous_level_id");
        if (previousLevelId != 0) {
            level.setPreviousLevel(getById(previousLevelId));
        }

        level.setComplete(rs.getBoolean("is_complete"));

        return level;
    }
}
