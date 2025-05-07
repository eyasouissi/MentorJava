package tn.esprit.services;

import tn.esprit.entities.Category;
import tn.esprit.tools.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CategoryService implements IServices<Category> {

    private Connection cnx;
    private static CategoryService instance;

    public CategoryService() {
        this.cnx = MyDataBase.getInstance().getCnx();
    }

    public static CategoryService getInstance() {
        if (instance == null) {
            instance = new CategoryService();
        }
        return instance;
    }

    @Override
    public void ajouter(Category category) {
        String query = "INSERT INTO category (name, description, created_at, is_active, icon) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, category.getName());
            pst.setString(2, category.getDescription());
            pst.setTimestamp(3, Timestamp.valueOf(
                    category.getCreatedAt() != null ? category.getCreatedAt() : LocalDateTime.now()));
            pst.setBoolean(4, category.getIsActive());
            pst.setString(5, category.getIcon());

            int affectedRows = pst.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating category failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    category.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating category failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add category: " + e.getMessage(), e);
        }
    }

    @Override
    public void modifier(Category category) {
        String query = "UPDATE category SET name = ?, description = ?, is_active = ?, icon = ? WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setString(1, category.getName());
            pst.setString(2, category.getDescription());
            pst.setBoolean(3, category.getIsActive());
            pst.setString(4, category.getIcon());
            pst.setInt(5, category.getId());

            int rowsUpdated = pst.executeUpdate();
            if (rowsUpdated == 0) {
                System.out.println("No category found with ID: " + category.getId());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update category: " + e.getMessage(), e);
        }
    }

    @Override
    public void supprimer(int id) {
        String updateCoursesQuery = "UPDATE courses SET category_id = NULL WHERE category_id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(updateCoursesQuery)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update associated courses: " + e.getMessage(), e);
        }

        String deleteQuery = "DELETE FROM category WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(deleteQuery)) {
            pst.setInt(1, id);
            int rowsDeleted = pst.executeUpdate();
            if (rowsDeleted == 0) {
                System.out.println("No category found with ID: " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete category: " + e.getMessage(), e);
        }
    }

    @Override
    public Category getOne(Category category) {
        return getById(category.getId());
    }

    public Category getById(int id) {
        String query = "SELECT * FROM category WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return mapResultSetToCategory(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get category: " + e.getMessage(), e);
        }
        return null;
    }

    public List<Category> getAll() {
        List<Category> categories = new ArrayList<>();
        String query = "SELECT * FROM category";

        try (Statement st = MyDataBase.getInstance().getCnx().createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                categories.add(mapResultSetToCategory(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to get categories: " + e.getMessage(), e);
        }

        return categories;
    }

    private Category mapResultSetToCategory(ResultSet rs) throws SQLException {
        Category category = new Category();
        category.setId(rs.getInt("id"));
        category.setName(rs.getString("name"));
        category.setDescription(rs.getString("description"));
        category.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        category.setIsActive(rs.getBoolean("is_active"));
        category.setIcon(rs.getString("icon"));
        return category;
    }

    public Category getByName(String name) {
        String query = "SELECT * FROM category WHERE name = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setString(1, name);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return mapResultSetToCategory(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get category by name: " + e.getMessage(), e);
        }
        return null;
    }

    public int getCourseCount(int categoryId) {
        String query = "SELECT COUNT(*) FROM courses WHERE category_id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, categoryId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get course count: " + e.getMessage(), e);
        }
        return 0;
    }
}
