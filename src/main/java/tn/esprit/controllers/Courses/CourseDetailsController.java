package tn.esprit.controllers.Courses;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import tn.esprit.entities.Courses;
import tn.esprit.entities.File;
import tn.esprit.entities.Level;
import tn.esprit.services.CoursesService;
import tn.esprit.services.FileService;
import tn.esprit.services.LevelService;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class CourseDetailsController {

    // FXML Components
    @FXML private Label titleLabel;
    @FXML private Label tutorLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label categoryLabel;
    @FXML private Label createdAtLabel;
    @FXML private Label premiumLabel;
    @FXML private VBox levelsContainer;
    @FXML private VBox addFilesContainer;
    @FXML private TextField fileNameField;
    @FXML private ListView<File> filesListView;
    @FXML private Label selectedLevelLabel;

    // Data
    private Courses course;
    private Level selectedLevel;

    // Services
    private final CoursesService coursesService = CoursesService.getInstance();
    private final FileService fileService = FileService.getInstance();
    private final LevelService levelService = LevelService.getInstance();

    // Initialize course with its data
    public void setCourse(Courses course) {
        // Load complete course with levels from database
        Courses fullCourse = coursesService.getByIdWithLevels(course.getId());
        this.course = fullCourse;

        // Debug
        System.out.println("[DEBUG] Course loaded - ID: " + fullCourse.getId());
        System.out.println("[DEBUG] Levels count: " +
                (fullCourse.getLevels() != null ? fullCourse.getLevels().size() : "null"));

        updateCourseDetails();
        displayLevels();
    }

    // Update basic course information
    private void updateCourseDetails() {
        if (course != null) {
            titleLabel.setText(course.getTitle());
            tutorLabel.setText("By " + course.getTutorName());
            descriptionLabel.setText(course.getDescription());
            categoryLabel.setText("Category: " +
                    (course.getCategory() != null ? course.getCategory().getName() : "None"));
            createdAtLabel.setText("Created: " + course.getCreatedAt().toString());
            premiumLabel.setText(course.getIsPremium() ? "Premium" : "Free");
        }
    }

    // Display course levels
    private void displayLevels() {
        levelsContainer.getChildren().clear();

        if (course == null) {
            showNoLevelsMessage("No course loaded");
            return;
        }

        if (course.getLevels() == null || course.getLevels().isEmpty()) {
            showNoLevelsMessage("No levels available for this course");
            return;
        }

        Accordion accordion = new Accordion();

        for (Level level : course.getLevels()) {
            TitledPane levelPane = createLevelPane(level);
            accordion.getPanes().add(levelPane);
        }

        levelsContainer.getChildren().add(accordion);
    }

    // Create a pane for a level
    private TitledPane createLevelPane(Level level) {
        VBox levelContent = new VBox(10);
        levelContent.setPadding(new Insets(10));

        // Header with action buttons
        HBox levelHeader = createLevelHeader(level);

        // Files list
        VBox filesContainer = createFilesContainer(level);

        levelContent.getChildren().addAll(levelHeader, filesContainer);

        return new TitledPane("Level " + level.getName(), levelContent);
    }

    private HBox createLevelHeader(Level level) {
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label("Level: " + level.getName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #5d3a5d;");

        Button addFilesBtn = new Button("Add Files");
        styleButton(addFilesBtn, "#D8BFD8");
        addFilesBtn.setOnAction(e -> showAddFilesForm(level));

        Button editBtn = new Button("Edit");
        styleButton(editBtn, "#b38cb3");
        editBtn.setOnAction(e -> editLevel(level));

        Button deleteBtn = new Button("Delete");
        styleButton(deleteBtn, "#a57ca5");
        deleteBtn.setOnAction(e -> deleteLevel(level));

        header.getChildren().addAll(nameLabel, addFilesBtn, editBtn, deleteBtn);
        return header;
    }

    private HBox createFileRow(File file, Level level) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 8; -fx-background-color: #f9f5fa; -fx-background-radius: 5;");

        Node icon = createFileIcon(file.getFileName());

        Label nameLabel = new Label(file.getFileName());
        nameLabel.setStyle("-fx-text-fill: #5d3a5d; -fx-font-weight: bold;");

        Button openBtn = new Button("Open");
        styleButton(openBtn, "#9d65a5", 12);
        openBtn.setOnAction(e -> openAndMarkFile(file, nameLabel));

        Button deleteBtn = new Button("Delete");
        styleButton(deleteBtn, "#a57ca5", 12);
        deleteBtn.setOnAction(e -> deleteFile(file, level));

        row.getChildren().addAll(icon, nameLabel, openBtn, deleteBtn);
        return row;
    }

    private void styleButton(Button button, String color) {
        button.setStyle("-fx-background-color: " + color + "; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: 5 15; " +
                "-fx-background-radius: 15;");
    }

    private void styleButton(Button button, String color, int fontSize) {
        button.setStyle("-fx-background-color: " + color + "; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: " + fontSize + "px; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: 3 10; " +
                "-fx-background-radius: 10;");
    }

    private VBox createFilesContainer(Level level) {
        VBox container = new VBox(5);
        Label title = new Label("Available files:");
        title.setStyle("-fx-font-weight: bold;");
        container.getChildren().add(title);

        if (level.getFiles() == null || level.getFiles().isEmpty()) {
            container.getChildren().add(
                    new Label("No files available for this level"));
        } else {
            for (File file : level.getFiles()) {
                HBox fileRow = createFileRow(file, level);
                container.getChildren().add(fileRow);
            }
        }

        return container;
    }

    private void showNoLevelsMessage(String message) {
        Label label = new Label(message);
        label.setStyle("-fx-font-size: 16px; -fx-text-fill: #95a5a6;");
        levelsContainer.getChildren().add(label);
    }

    private void showAddFilesForm(Level level) {
        this.selectedLevel = level;
        selectedLevelLabel.setText("Add files - Level: " + level.getName());

        try {
            // Explicitly reload files from database
            List<File> files = fileService.getFilesForLevel(level.getId());

            // Update level's files list
            level.setFiles(files);

            // Display in ListView
            filesListView.getItems().setAll(files);

            if (files.isEmpty()) {
                filesListView.setPlaceholder(new Label("No files available for this level"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load files");
            filesListView.getItems().clear();
            filesListView.setPlaceholder(new Label("Error loading files"));
        }

        addFilesContainer.setVisible(true);
    }

    @FXML
    private void hideAddFilesForm() {
        addFilesContainer.setVisible(false);
        fileNameField.clear();
    }

    @FXML
    private void handleFileUpload() {
        if (selectedLevel == null) {
            showAlert("Error", "No level selected!");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        java.io.File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            try {
                // 1. Prepare upload
                String uploadPath = "C:/xampp/htdocs/uploads/";
                Path uploadDir = Paths.get(uploadPath);

                if (!Files.exists(uploadDir)) {
                    Files.createDirectories(uploadDir);
                }

                // 2. Create unique name
                String uniqueFileName = System.currentTimeMillis() + "_" + selectedFile.getName();
                Path destination = uploadDir.resolve(uniqueFileName);

                // 3. Copy file
                Files.copy(selectedFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

                // 4. Save to database
                File newFile = new File();
                newFile.setFileName(selectedFile.getName());
                newFile.setFilePath(destination.toString());
                newFile.setViewed(false);
                newFile.setLevel(selectedLevel);

                fileService.ajouter(newFile);

                // 5. Add to list
                selectedLevel.getFiles().add(newFile);
                filesListView.getItems().add(newFile);
                displayLevels();

                showAlert("Success", "File added successfully!");
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error", "Failed to upload file:\n" + e.getMessage());
            }
        }
    }

    @FXML
    private void addFile() {
        String fileName = fileNameField.getText().trim();
        if (fileName.isEmpty() || selectedLevel == null) return;

        String uploadPath = "C:/xampp/htdocs/uploads/";
        String uniqueFileName = System.currentTimeMillis() + "_" + fileName;
        String destPath = uploadPath + uniqueFileName;

        File newFile = new File();
        newFile.setFileName(fileName);
        newFile.setFilePath(destPath);
        newFile.setLevel(selectedLevel);
        newFile.setViewed(false);

        try {
            fileService.ajouter(newFile);

            // Update display
            selectedLevel.getFiles().add(newFile);
            filesListView.getItems().add(newFile);
            fileNameField.clear();
            displayLevels();
        } catch (Exception e) {
            showAlert("Error", "Failed to add: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void deleteFile(File file, Level level) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Delete file");
        alert.setContentText("Are you sure you want to delete this file?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Delete physical file
                    Path filePath = Paths.get(file.getFilePath());
                    Files.deleteIfExists(filePath);

                    // Delete from database
                    fileService.supprimer((int) file.getId());
                    level.getFiles().remove(file);

                    // Refresh
                    displayLevels();
                    showAlert("Success", "File deleted!");
                } catch (IOException e) {
                    e.printStackTrace();
                    showAlert("Error", "Failed to delete file:\n" + e.getMessage());
                }
            }
        });
    }

    private void editLevel(Level level) {
        TextInputDialog dialog = new TextInputDialog(level.getName());
        dialog.setTitle("Edit Level");
        dialog.setHeaderText("Level name modification");
        dialog.setContentText("New name:");

        dialog.showAndWait().ifPresent(newName -> {
            if (!newName.trim().isEmpty()) {
                level.setName(newName.trim());
                levelService.modifier(level);
                displayLevels();
            }
        });
    }

    private void deleteLevel(Level level) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Delete level");
        alert.setContentText("This will also delete all associated files. Continue?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // First delete files
                level.getFiles().forEach(file ->
                        fileService.supprimer((int) file.getId()));

                // Then delete level
                levelService.supprimer(level.getId());
                course.getLevels().remove(level);
                displayLevels();
            }
        });
    }

    private Node createFileIcon(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        ImageView icon = new ImageView();

        String iconPath;
        switch (extension) {
            case "pdf":
                iconPath = "/assets/icons/pdf-icon2.png";
                break;
            case "doc":
            case "docx":
                iconPath = "/assets/icons/word-icon.png";
                break;
            case "xls":
            case "xlsx":
                iconPath = "/assets/icons/excel-icon.png";
                break;
            case "ppt":
            case "pptx":
                iconPath = "/assets/icons/ppt-icon.png";
                break;
            case "jpg":
            case "jpeg":
            case "png":
            case "gif":
                iconPath = "/assets/icons/image-icon.png";
                break;
            default:
                iconPath = "/assets/icons/file-icon.png";
                break;
        }

        try {
            icon.setImage(new Image(getClass().getResourceAsStream(iconPath)));
            icon.setFitWidth(24);
            icon.setFitHeight(24);
        } catch (Exception e) {
            System.out.println("[ERROR] Icon not found at path: " + iconPath);
            return new Label(extension.toUpperCase());
        }

        return icon;
    }

    private void openAndMarkFile(File file, Label nameLabel) {
        try {
            Path filePath = Paths.get(file.getFilePath());
            if (Files.exists(filePath)) {
                Desktop.getDesktop().open(filePath.toFile());

                // Mark as viewed
                file.setViewed(true);
                fileService.modifier(file);

                nameLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
            } else {
                showAlert("Error", "File not found!");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Error opening file:\n" + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleAddLevel() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Level");
        dialog.setHeaderText("Create a new level");
        dialog.setContentText("Level name:");

        dialog.showAndWait().ifPresent(levelName -> {
            if (!levelName.trim().isEmpty()) {
                try {
                    Level newLevel = new Level();
                    newLevel.setName(levelName.trim());
                    newLevel.setCourse(course);

                    // Save to database
                    levelService.ajouter(newLevel);

                    // Add to course's levels list
                    course.getLevels().add(newLevel);

                    // Refresh display
                    displayLevels();

                    showAlert("Success", "Level added successfully!");
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert("Error", "Failed to add level: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleShareCourse() {
        try {
            // 1. Create course image
            BufferedImage courseImage = createCourseImage();

            // 2. Save to web directory
            String fileName = "course_" + System.currentTimeMillis() + ".png";
            Path imagePath = Paths.get("C:/xampp/htdocs/qrcodes/" + fileName);
            Files.createDirectories(imagePath.getParent());
            ImageIO.write(courseImage, "png", imagePath.toFile());

            // 3. Generate accessible URL
            String url = "http://" + getLocalIp() + "/qrcodes/" + fileName;

            // 4. Generate and display QR Code
            showQRDialog(generateQRCode(url));

        } catch (Exception e) {
            showAlert("Error", "Generation error:\n" + e.getMessage());
        }
    }

    private String getLocalIp() throws SocketException {
        return Collections.list(NetworkInterface.getNetworkInterfaces()).stream()
                .flatMap(i -> Collections.list(i.getInetAddresses()).stream())
                .filter(ip -> ip instanceof Inet4Address && !ip.isLoopbackAddress())
                .findFirst()
                .orElseThrow()
                .getHostAddress();
    }

    private BufferedImage createCourseImage() {
        // Create HD image 1000x800
        BufferedImage image = new BufferedImage(1000, 800, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // Enable quality effects
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        /* ---------- LUXURY GRADIENT BACKGROUND ---------- */
        GradientPaint backgroundGradient = new GradientPaint(
                0, 0, new Color(40, 15, 60),
                1000, 800, new Color(93, 58, 125));
        g.setPaint(backgroundGradient);
        g.fillRect(0, 0, 1000, 800);

        /* ---------- MENTOR HEADER ---------- */
        // Rounded container
        g.setColor(new Color(255, 255, 255, 20));
        g.fillRoundRect(200, 30, 600, 100, 30, 30);

        // Shadow
        g.setColor(new Color(0, 0, 0, 100));
        g.drawRoundRect(200, 31, 600, 100, 30, 30);

        // Logo text
        g.setFont(new Font("Montserrat", Font.BOLD, 48));
        GradientPaint textGradient = new GradientPaint(
                350, 70, new Color(255, 215, 0),
                650, 70, new Color(255, 255, 255));
        g.setPaint(textGradient);
        drawCenteredText(g, "MENTOR", 500, 95);

        /* ---------- MAIN CARD ---------- */
        // Main container
        g.setColor(new Color(255, 255, 255, 15));
        g.fillRoundRect(100, 160, 800, 500, 40, 40);

        // Neon border
        Stroke oldStroke = g.getStroke();
        g.setStroke(new BasicStroke(3));
        g.setColor(new Color(157, 101, 165, 200));
        g.drawRoundRect(100, 160, 800, 500, 40, 40);
        g.setStroke(oldStroke);

        /* ---------- TITLE ---------- */
        g.setFont(new Font("Montserrat", Font.BOLD, 36));
        g.setColor(Color.WHITE);
        drawCenteredText(g, course.getTitle(), 500, 230);

        /* ---------- STATUS BADGE ---------- */
        Color premiumColor = new Color(230, 57, 70);  // Premium red
        Color freeColor = new Color(46, 204, 113);    // Free green

        int badgeWidth = 180;
        int badgeHeight = 50;
        int badgeX = 500 - badgeWidth/2;
        int badgeY = 270;

        // Badge background
        g.setColor(course.getIsPremium() ? premiumColor : freeColor);
        g.fillRoundRect(badgeX, badgeY, badgeWidth, badgeHeight, 25, 25);

        // 3D effect
        g.setColor(new Color(255, 255, 255, 50));
        g.fillRoundRect(badgeX + 5, badgeY + 5, badgeWidth - 10, 15, 20, 20);

        // Text
        g.setFont(new Font("Montserrat", Font.BOLD, 24));
        g.setColor(Color.WHITE);
        drawCenteredText(g, course.getIsPremium() ? "PREMIUM" : "FREE", 500, badgeY + 35);

        /* ---------- INFORMATION ---------- */
        // Info container
        g.setColor(new Color(255, 255, 255, 10));
        g.fillRoundRect(150, 350, 700, 200, 30, 30);

        // Icons and text
        g.setFont(new Font("Montserrat", Font.PLAIN, 24));
        g.setColor(new Color(220, 220, 220));

        // Instructor
        drawWithIcon(g, "👨‍🏫", "Instructor: " + course.getTutorName(), 500, 380);

        // Category
        drawWithIcon(g, "📚", "Category: " + course.getCategory().getName(), 500, 430);

        // Date
        drawWithIcon(g, "📅", "Created: " + course.getCreatedAt().toString(), 500, 480);

        /* ---------- DESCRIPTION ---------- */
        g.setFont(new Font("Montserrat", Font.PLAIN, 20));
        g.setColor(new Color(240, 240, 240));
        drawWrappedText(g, course.getDescription(), 200, 550, 600);

        /* ---------- ENCOURAGEMENT MESSAGE ---------- */
        String[] encouragements = {
                "✨ Your excellence starts here!",
                "🚀 Guaranteed learning!",
                "💡 Knowledge is your power!",
                "🎯 Your future awaits!"
        };
        String message = encouragements[new Random().nextInt(encouragements.length)];

        g.setFont(new Font("Montserrat", Font.ITALIC, 22));
        g.setColor(new Color(255, 255, 255, 200));
        drawCenteredText(g, message, 500, 650);

        /* ---------- FOOTER ---------- */
        g.setFont(new Font("Montserrat", Font.BOLD, 16));
        g.setColor(new Color(255, 255, 255, 150));
        drawCenteredText(g, "© 2023 MENTOR - Premium Education Platform", 500, 750);

        g.dispose();
        return image;
    }

    // Utility methods
    private void drawCenteredText(Graphics2D g, String text, int centerX, int baselineY) {
        FontMetrics fm = g.getFontMetrics();
        int x = centerX - fm.stringWidth(text) / 2;
        g.drawString(text, x, baselineY);
    }

    private void drawWithIcon(Graphics2D g, String icon, String text, int centerX, int y) {
        FontMetrics fm = g.getFontMetrics();
        String fullText = icon + "  " + text;
        int x = centerX - fm.stringWidth(fullText) / 2;
        g.drawString(fullText, x, y);
    }

    private void drawWrappedText(Graphics2D g, String text, int x, int startY, int maxWidth) {
        FontMetrics fm = g.getFontMetrics();
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            if (fm.stringWidth(currentLine + word) < maxWidth) {
                currentLine.append(word).append(" ");
            } else {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word + " ");
            }
        }
        lines.add(currentLine.toString());

        int y = startY;
        for (String line : lines) {
            g.drawString(line, x, y);
            y += fm.getHeight() + 5; // 5px line spacing
        }
    }

    private Image generateQRCode(String content) throws WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        int size = 600;
        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, size, size);

        WritableImage image = new WritableImage(size, size);
        PixelWriter pixelWriter = image.getPixelWriter();

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                pixelWriter.setArgb(x, y, bitMatrix.get(x, y) ? 0xFF5d3a5d : 0xFFFFFFFF);
            }
        }

        return image;
    }

    private void saveQRCodeImage(Image qrImage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save QR Code");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Image", "*.png"));
        fileChooser.setInitialFileName("QRCode_" + course.getTitle().replaceAll("[^a-zA-Z0-9]", "_") + ".png");

        java.io.File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try {
                BufferedImage bImage = SwingFXUtils.fromFXImage(qrImage, null);
                ImageIO.write(bImage, "png", file);
                showAlert("Success", "QR Code saved successfully!");
            } catch (IOException e) {
                showAlert("Error", "Save failed: " + e.getMessage());
            }
        }
    }

    private void showQRDialog(Image qrImage) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Share Course");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CLOSE);

        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20));

        Label infoLabel = new Label("Scan this QR code to view\ncourse details");
        infoLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #5d3a5d; -fx-font-size: 14px;");

        ImageView qrView = new ImageView(qrImage);
        qrView.setFitWidth(300);
        qrView.setFitHeight(300);

        content.getChildren().addAll(infoLabel, qrView);
        dialog.getDialogPane().setContent(content);

        // Handle Save button
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setOnAction(e -> saveQRCodeImage(qrImage));

        dialog.showAndWait();
    }
}