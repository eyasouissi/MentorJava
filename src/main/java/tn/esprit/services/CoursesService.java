package tn.esprit.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import tn.esprit.entities.Category;
import tn.esprit.entities.Courses;
import tn.esprit.entities.File;
import tn.esprit.entities.Level;
import tn.esprit.tools.MyDataBase;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class CoursesService implements IServices<Courses> {
    private Connection cnx;
    private static CoursesService instance;
    @PersistenceContext
    private EntityManager entityManager;

    public CoursesService() {
        this.cnx = MyDataBase.getInstance().getCnx();
    }

    public static CoursesService getInstance() {
        if (instance == null) {
            instance = new CoursesService();
        }
        return instance;
    }

    @Override
    public void ajouter(Courses course) {
        String query = "INSERT INTO courses (title, description, is_published, progress_points_required, " +
                "created_at, category_id, is_premium, tutor_name) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, course.getTitle());
            pst.setString(2, course.getDescription());
            pst.setBoolean(3, course.getIsPublished());
            pst.setInt(4, course.getProgressPointsRequired());
            pst.setTimestamp(5, Timestamp.from(course.getCreatedAt() != null ?
                    course.getCreatedAt() : Instant.now()));
            pst.setInt(6, course.getCategory().getId());
            pst.setBoolean(7, course.getIsPremium());
            pst.setString(8, course.getTutorName());

            int affectedRows = pst.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating course failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    course.setId(generatedKeys.getInt(1));

                    // Sauvegarder les niveaux si ils existent
                    if (course.getLevels() != null && !course.getLevels().isEmpty()) {
                        saveLevelsForCourse(course);
                    }
                } else {
                    throw new SQLException("Creating course failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add course: " + e.getMessage(), e);
        }
    }

    private void saveLevelsForCourse(Courses course) throws SQLException {
        String query = "INSERT INTO level (name, course_id) VALUES (?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            for (Level level : course.getLevels()) {
                pst.setString(1, level.getName());
                pst.setInt(2, course.getId());
                pst.addBatch();
            }
            pst.executeBatch();

            // Récupérer les IDs générés
            try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                int index = 0;
                while (generatedKeys.next()) {
                    course.getLevels().get(index++).setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    @Override
    public void modifier(Courses course) {
        String query = "UPDATE courses SET title = ?, description = ?, is_published = ?, " +
                "progress_points_required = ?, category_id = ?, is_premium = ?, " +
                "tutor_name = ? WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setString(1, course.getTitle());
            pst.setString(2, course.getDescription());
            pst.setBoolean(3, course.getIsPublished());
            pst.setInt(4, course.getProgressPointsRequired());
            pst.setInt(5, course.getCategory().getId());
            pst.setBoolean(6, course.getIsPremium());
            pst.setString(7, course.getTutorName());
            pst.setInt(8, course.getId());

            int rowsUpdated = pst.executeUpdate();
            if (rowsUpdated == 0) {
                System.out.println("No course found with ID: " + course.getId());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update course: " + e.getMessage(), e);
        }
    }

    @Override
    public void supprimer(int id) {
        // D'abord supprimer les dépendances (levels et ratings)
        String deleteLevelsQuery = "DELETE FROM level WHERE course_id = ?";
        String deleteRatingsQuery = "DELETE FROM rating WHERE course_id = ?";

        try (PreparedStatement pstLevels = cnx.prepareStatement(deleteLevelsQuery);
             PreparedStatement pstRatings = cnx.prepareStatement(deleteRatingsQuery)) {

            pstLevels.setInt(1, id);
            pstRatings.setInt(1, id);

            pstLevels.executeUpdate();
            pstRatings.executeUpdate();

            // Ensuite supprimer le cours
            String deleteCourseQuery = "DELETE FROM courses WHERE id = ?";
            try (PreparedStatement pstCourse = cnx.prepareStatement(deleteCourseQuery)) {
                pstCourse.setInt(1, id);
                int rowsDeleted = pstCourse.executeUpdate();
                if (rowsDeleted == 0) {
                    System.out.println("No course found with ID: " + id);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete course: " + e.getMessage(), e);
        }
    }

    @Override
    public Courses getOne(Courses course) {
        return getByIdWithLevels(course.getId());
    }

    public Courses getById(int id) {
        String query = "SELECT c.*, cat.name as category_name, cat.id as category_id " +
                "FROM courses c JOIN category cat ON c.category_id = cat.id WHERE c.id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return mapResultSetToCourse(rs, false);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get course: " + e.getMessage(), e);
        }
        return null;
    }

    public Courses getByIdWithLevels(int id) {
        String query = "SELECT c.*, cat.name as category_name, cat.id as category_id, " +
                "l.id as level_id, l.name as level_name " +
                "FROM courses c " +
                "JOIN category cat ON c.category_id = cat.id " +
                "LEFT JOIN level l ON c.id = l.course_id " +
                "WHERE c.id = ?";

        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();

            Courses course = null;
            List<Level> levels = new ArrayList<>();

            while (rs.next()) {
                if (course == null) {
                    course = mapResultSetToCourse(rs, false);
                }

                if (rs.getObject("level_id") != null) {
                    Level level = new Level();
                    level.setId(rs.getInt("level_id"));
                    level.setName(rs.getString("level_name"));
                    levels.add(level);
                }
            }

            if (course != null) {
                course.setLevels(levels);
            }

            return course;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get course with levels: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Courses> getAll() {
        return getAll(false);
    }

    public List<Courses> getAll(boolean withLevels) {
        List<Courses> courses = new ArrayList<>();

        if (withLevels) {
            // Utilisation d'une seule requête avec jointure pour meilleure performance
            String query = "SELECT c.*, cat.name as category_name, cat.id as category_id, " +
                    "l.id as level_id, l.name as level_name " +
                    "FROM courses c " +
                    "JOIN category cat ON c.category_id = cat.id " +
                    "LEFT JOIN level l ON c.id = l.course_id " +
                    "ORDER BY c.id";

            try (Statement st = cnx.createStatement();
                 ResultSet rs = st.executeQuery(query)) {

                Courses currentCourse = null;
                int currentCourseId = -1;

                while (rs.next()) {
                    int courseId = rs.getInt("id");

                    if (currentCourse == null || courseId != currentCourseId) {
                        if (currentCourse != null) {
                            courses.add(currentCourse);
                        }
                        currentCourse = mapResultSetToCourse(rs, false);
                        currentCourseId = courseId;
                        currentCourse.setLevels(new ArrayList<>());
                    }

                    if (rs.getObject("level_id") != null) {
                        Level level = new Level();
                        level.setId(rs.getInt("level_id"));
                        level.setName(rs.getString("level_name"));
                        currentCourse.getLevels().add(level);
                    }
                }

                if (currentCourse != null) {
                    courses.add(currentCourse);
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to get courses with levels: " + e.getMessage(), e);
            }
        } else {
            // Version simple sans les niveaux
            String query = "SELECT c.*, cat.name as category_name, cat.id as category_id " +
                    "FROM courses c JOIN category cat ON c.category_id = cat.id";
            try (Statement st = cnx.createStatement();
                 ResultSet rs = st.executeQuery(query)) {
                while (rs.next()) {
                    courses.add(mapResultSetToCourse(rs, false));
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to get courses: " + e.getMessage(), e);
            }
        }

        return courses;
    }

    public List<Level> getLevelsForCourse(int courseId) {
        List<Level> levels = new ArrayList<>();
        String query = "SELECT * FROM level WHERE course_id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, courseId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Level level = new Level();
                level.setId(rs.getInt("id"));
                level.setName(rs.getString("name"));
                levels.add(level);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get levels for course: " + e.getMessage(), e);
        }
        return levels;
    }

    public List<Courses> getByCategory(int categoryId) {
        return getByCategory(categoryId, false);
    }

    public List<Courses> getByCategory(int categoryId, boolean withLevels) {
        List<Courses> courses = new ArrayList<>();

        if (withLevels) {
            String query = "SELECT c.*, cat.name as category_name, cat.id as category_id, " +
                    "l.id as level_id, l.name as level_name " +
                    "FROM courses c " +
                    "JOIN category cat ON c.category_id = cat.id " +
                    "LEFT JOIN level l ON c.id = l.course_id " +
                    "WHERE c.category_id = ? " +
                    "ORDER BY c.id";

            try (PreparedStatement pst = cnx.prepareStatement(query)) {
                pst.setInt(1, categoryId);
                ResultSet rs = pst.executeQuery();

                Courses currentCourse = null;
                int currentCourseId = -1;

                while (rs.next()) {
                    int courseId = rs.getInt("id");

                    if (currentCourse == null || courseId != currentCourseId) {
                        if (currentCourse != null) {
                            courses.add(currentCourse);
                        }
                        currentCourse = mapResultSetToCourse(rs, false);
                        currentCourseId = courseId;
                        currentCourse.setLevels(new ArrayList<>());
                    }

                    if (rs.getObject("level_id") != null) {
                        Level level = new Level();
                        level.setId(rs.getInt("level_id"));
                        level.setName(rs.getString("level_name"));
                        currentCourse.getLevels().add(level);
                    }
                }

                if (currentCourse != null) {
                    courses.add(currentCourse);
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to get courses by category with levels: " + e.getMessage(), e);
            }
        } else {
            String query = "SELECT c.*, cat.name as category_name, cat.id as category_id " +
                    "FROM courses c JOIN category cat ON c.category_id = cat.id " +
                    "WHERE c.category_id = ?";
            try (PreparedStatement pst = cnx.prepareStatement(query)) {
                pst.setInt(1, categoryId);
                ResultSet rs = pst.executeQuery();
                while (rs.next()) {
                    courses.add(mapResultSetToCourse(rs, false));
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to get courses by category: " + e.getMessage(), e);
            }
        }

        return courses;
    }

    public List<Courses> getPublishedCourses() {
        return getPublishedCourses(false);
    }

    public List<Courses> getPublishedCourses(boolean withLevels) {
        List<Courses> courses = new ArrayList<>();

        if (withLevels) {
            String query = "SELECT c.*, cat.name as category_name, cat.id as category_id, " +
                    "l.id as level_id, l.name as level_name " +
                    "FROM courses c " +
                    "JOIN category cat ON c.category_id = cat.id " +
                    "LEFT JOIN level l ON c.id = l.course_id " +
                    "WHERE c.is_published = true " +
                    "ORDER BY c.id";

            try (Statement st = cnx.createStatement();
                 ResultSet rs = st.executeQuery(query)) {

                Courses currentCourse = null;
                int currentCourseId = -1;

                while (rs.next()) {
                    int courseId = rs.getInt("id");

                    if (currentCourse == null || courseId != currentCourseId) {
                        if (currentCourse != null) {
                            courses.add(currentCourse);
                        }
                        currentCourse = mapResultSetToCourse(rs, false);
                        currentCourseId = courseId;
                        currentCourse.setLevels(new ArrayList<>());
                    }

                    if (rs.getObject("level_id") != null) {
                        Level level = new Level();
                        level.setId(rs.getInt("level_id"));
                        level.setName(rs.getString("level_name"));
                        currentCourse.getLevels().add(level);
                    }
                }

                if (currentCourse != null) {
                    courses.add(currentCourse);
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to get published courses with levels: " + e.getMessage(), e);
            }
        } else {
            String query = "SELECT c.*, cat.name as category_name, cat.id as category_id " +
                    "FROM courses c JOIN category cat ON c.category_id = cat.id " +
                    "WHERE c.is_published = true";
            try (Statement st = cnx.createStatement();
                 ResultSet rs = st.executeQuery(query)) {
                while (rs.next()) {
                    courses.add(mapResultSetToCourse(rs, false));
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to get published courses: " + e.getMessage(), e);
            }
        }

        return courses;
    }

    public List<Courses> getPremiumCourses(boolean isPremium) {
        return getPremiumCourses(isPremium, false);
    }

    public List<Courses> getPremiumCourses(boolean isPremium, boolean withLevels) {
        List<Courses> courses = new ArrayList<>();

        if (withLevels) {
            String query = "SELECT c.*, cat.name as category_name, cat.id as category_id, " +
                    "l.id as level_id, l.name as level_name " +
                    "FROM courses c " +
                    "JOIN category cat ON c.category_id = cat.id " +
                    "LEFT JOIN level l ON c.id = l.course_id " +
                    "WHERE c.is_premium = ? " +
                    "ORDER BY c.id";

            try (PreparedStatement pst = cnx.prepareStatement(query)) {
                pst.setBoolean(1, isPremium);
                ResultSet rs = pst.executeQuery();

                Courses currentCourse = null;
                int currentCourseId = -1;

                while (rs.next()) {
                    int courseId = rs.getInt("id");

                    if (currentCourse == null || courseId != currentCourseId) {
                        if (currentCourse != null) {
                            courses.add(currentCourse);
                        }
                        currentCourse = mapResultSetToCourse(rs, false);
                        currentCourseId = courseId;
                        currentCourse.setLevels(new ArrayList<>());
                    }

                    if (rs.getObject("level_id") != null) {
                        Level level = new Level();
                        level.setId(rs.getInt("level_id"));
                        level.setName(rs.getString("level_name"));
                        currentCourse.getLevels().add(level);
                    }
                }

                if (currentCourse != null) {
                    courses.add(currentCourse);
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to get premium courses with levels: " + e.getMessage(), e);
            }
        } else {
            String query = "SELECT c.*, cat.name as category_name, cat.id as category_id " +
                    "FROM courses c JOIN category cat ON c.category_id = cat.id " +
                    "WHERE c.is_premium = ?";
            try (PreparedStatement pst = cnx.prepareStatement(query)) {
                pst.setBoolean(1, isPremium);
                ResultSet rs = pst.executeQuery();
                while (rs.next()) {
                    courses.add(mapResultSetToCourse(rs, false));
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to get premium courses: " + e.getMessage(), e);
            }
        }

        return courses;
    }

    private Courses mapResultSetToCourse(ResultSet rs, boolean loadLevels) throws SQLException {
        Courses course = new Courses();
        course.setId(rs.getInt("id"));
        course.setTitle(rs.getString("title"));
        course.setDescription(rs.getString("description"));
        course.setIsPublished(rs.getBoolean("is_published"));
        course.setProgressPointsRequired(rs.getInt("progress_points_required"));
        course.setCreatedAt(rs.getTimestamp("created_at").toInstant());
        course.setIsPremium(rs.getBoolean("is_premium"));
        course.setTutorName(rs.getString("tutor_name"));

        // Création de la catégorie associée
        Category category = new Category();
        category.setId(rs.getInt("category_id"));
        category.setName(rs.getString("category_name"));
        course.setCategory(category);

        // Initialiser la liste des niveaux
        course.setLevels(new ArrayList<>());

        // Charger les niveaux si demandé
        if (loadLevels) {
            List<Level> levels = getLevelsForCourse(rs.getInt("id"));
            course.setLevels(levels);
        }

        return course;
    }

    public double getAverageRating(int courseId) {
        String query = "SELECT AVG(rating) as average_rating FROM rating WHERE course_id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, courseId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getDouble("average_rating");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get average rating: " + e.getMessage(), e);
        }
        return 0.0;
    }

    public int getLevelCount(int courseId) {
        String query = "SELECT COUNT(*) as level_count FROM level WHERE course_id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, courseId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt("level_count");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get level count: " + e.getMessage(), e);
        }
        return 0;
    }

    public Courses getByIdWithLevelsAndFiles(int id) {
        // 1. Récupérer le cours avec ses niveaux
        Courses course = getById(id);

        if (course != null && course.getLevels() != null) {
            // 2. Pour chaque niveau, charger les fichiers
            for (Level level : course.getLevels()) {
                List<File> files = FileService.getInstance().getFilesForLevel(level.getId());
                level.setFiles(files);
            }
        }

        return course;
    }


    // Méthodes de pagination à ajouter à CoursesService

    public int getCount() {
        String query = "SELECT COUNT(*) FROM courses";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get courses count", e);
        }
        return 0;
    }

    public int getPremiumCount() {
        String query = "SELECT COUNT(*) FROM courses WHERE is_premium = true";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get premium courses count", e);
        }
        return 0;
    }

    public int getPopularCount() {
        // Implémentez votre logique pour déterminer les cours populaires
        // Par exemple, ceux avec une note moyenne > 4 ou un certain nombre d'inscriptions
        String query = "SELECT COUNT(*) FROM courses c " +
                "JOIN (SELECT course_id, AVG(rating) as avg_rating FROM rating GROUP BY course_id) r " +
                "ON c.id = r.course_id WHERE r.avg_rating >= 4";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get popular courses count", e);
        }
        return 0;
    }

    public int getCountByCategory(int categoryId) {
        String query = "SELECT COUNT(*) FROM courses WHERE category_id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, categoryId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get courses count by category", e);
        }
        return 0;
    }

    public List<Courses> getAllPaginated(int page, int itemsPerPage) {
        String query = "SELECT c.*, cat.name as category_name, cat.id as category_id " +
                "FROM courses c JOIN category cat ON c.category_id = cat.id " +
                "ORDER BY c.id LIMIT ? OFFSET ?";
        return getPaginatedCourses(query, page, itemsPerPage);
    }

    public List<Courses> getPremiumCoursesPaginated(int page, int itemsPerPage) {
        String query = "SELECT c.*, cat.name as category_name, cat.id as category_id " +
                "FROM courses c JOIN category cat ON c.category_id = cat.id " +
                "WHERE c.is_premium = true ORDER BY c.id LIMIT ? OFFSET ?";
        return getPaginatedCourses(query, page, itemsPerPage);
    }

    public List<Courses> getPopularCoursesPaginated(int page, int itemsPerPage) {
        // Version avec jointure pour les cours populaires
        String query = "SELECT c.*, cat.name as category_name, cat.id as category_id " +
                "FROM courses c JOIN category cat ON c.category_id = cat.id " +
                "JOIN (SELECT course_id, AVG(rating) as avg_rating FROM rating GROUP BY course_id) r " +
                "ON c.id = r.course_id WHERE r.avg_rating >= 4 " +
                "ORDER BY r.avg_rating DESC LIMIT ? OFFSET ?";
        return getPaginatedCourses(query, page, itemsPerPage);
    }

    public List<Courses> getByCategoryPaginated(int categoryId, int page, int itemsPerPage) {
        String query = "SELECT c.*, cat.name as category_name, cat.id as category_id " +
                "FROM courses c JOIN category cat ON c.category_id = cat.id " +
                "WHERE c.category_id = ? ORDER BY c.id LIMIT ? OFFSET ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, categoryId);
            pst.setInt(2, itemsPerPage);
            pst.setInt(3, page * itemsPerPage);

            ResultSet rs = pst.executeQuery();
            List<Courses> courses = new ArrayList<>();
            while (rs.next()) {
                courses.add(mapResultSetToCourse(rs, false));
            }
            return courses;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get paginated courses by category", e);
        }
    }

    private List<Courses> getPaginatedCourses(String baseQuery, int page, int itemsPerPage) {
        try (PreparedStatement pst = cnx.prepareStatement(baseQuery)) {
            pst.setInt(1, itemsPerPage);
            pst.setInt(2, page * itemsPerPage);

            ResultSet rs = pst.executeQuery();
            List<Courses> courses = new ArrayList<>();
            while (rs.next()) {
                courses.add(mapResultSetToCourse(rs, false));
            }
            return courses;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get paginated courses", e);
        }
    }
}