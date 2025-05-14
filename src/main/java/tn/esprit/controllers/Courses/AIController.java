package tn.esprit.controllers.Courses;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextBox;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import tn.esprit.controllers.NotesAIController;
import tn.esprit.models.AICourseSlide;
import tn.esprit.services.DeepInfraService;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;

public class AIController extends NotesAIController {

    @FXML
    private TextArea promptField;
    @FXML
    private ComboBox<String> modelCombo;

    @FXML
    private ProgressIndicator progressIndicator;

    private final DeepInfraService aiService = new DeepInfraService();

    @FXML
    private ListView<AICourseSlide> resultListView;

    @FXML
    private ComboBox<String> formatCombo;




    @FXML
    private void initialize() {
        // Initialize model selection
        modelCombo.getSelectionModel().selectFirst();

        // Set preferred width for the ListView
        resultListView.setPrefWidth(Control.USE_COMPUTED_SIZE);
        // Set the cell factory for the ListView
        resultListView.setCellFactory(new AICellFactory());
    }

    @FXML
    private void generateCourse() {
        String prompt = promptField.getText().trim();
        String model = modelCombo.getValue();

        if (prompt.isEmpty()) {
            showAlert("Please enter a course description");
            return;
        }

        if (model == null || model.isEmpty()) {
            showAlert("Please select an AI model");
            return;
        }

        progressIndicator.setVisible(true);
        resultListView.getItems().clear();

        CompletableFuture.supplyAsync(() -> {
            try {
                // Must return exactly List<AICourseSlide>
                return aiService.generateCourse(prompt, model);
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("API Error: " + e.getMessage()));
                return Collections.<AICourseSlide>emptyList(); // Specify type here too
            }
        }).thenAccept(slides -> {
            Platform.runLater(() -> {
                if (slides != null && !slides.isEmpty()) {
                    resultListView.getItems().addAll(slides); // No casting needed
                }
                progressIndicator.setVisible(false);
            });
        });
    }




    @FXML
    private void saveAsFile() {
        if (resultListView.getItems().isEmpty()) {
            showAlert("No content to export.");
            return;
        }

        String subject = promptField.getText().trim();
        if (subject.isEmpty()) {
            showAlert("Subject is empty.");
            return;
        }

        String format = formatCombo.getValue();
        if (format == null || format.isEmpty()) {
            showAlert("Please choose a format (PDF, DOCX, PPTX).");
            return;
        }

        String userHome = System.getProperty("user.home");
        String downloadsPath;
        if (Files.exists(Paths.get(userHome, "Downloads"))) {
            downloadsPath = Paths.get(userHome, "Downloads").toString();
        } else if (Files.exists(Paths.get(userHome, "Téléchargements"))) {
            downloadsPath = Paths.get(userHome, "Téléchargements").toString();
        } else {
            downloadsPath = userHome;
        }

        String filename = downloadsPath + "/" + subject.replaceAll("[\\\\/:*?\"<>|]", "_") + "." + format.toLowerCase();

        try {
            switch (format) {
                case "PDF":
                    exportAsPDF(filename);
                    break;
                case "DOCX":
                    exportAsDocx(filename);
                    break;
                case "PPTX":
                    exportAsPptx(filename);
                    break;
                default:
                    showAlert("Unsupported format.");
                    return;
            }
            showAlert("File saved as: " + filename);
        } catch (Exception e) {
            showAlert("Error saving file: " + e.getMessage());
        }
    }


    private void exportAsPDF(String filepath) throws Exception {
        com.lowagie.text.Document document = new com.lowagie.text.Document();
        com.lowagie.text.pdf.PdfWriter.getInstance(document, new java.io.FileOutputStream(filepath));
        document.open();

        for (AICourseSlide slide : resultListView.getItems()) {
            document.add(new com.lowagie.text.Paragraph("Title: " + slide.title()));
            document.add(new com.lowagie.text.Paragraph(slide.content()));
            document.add(new com.lowagie.text.Paragraph("\n\n"));
        }

        document.close();
    }

    private void exportAsDocx(String filepath) throws Exception {
        org.apache.poi.xwpf.usermodel.XWPFDocument document = new org.apache.poi.xwpf.usermodel.XWPFDocument();
        try (java.io.FileOutputStream out = new java.io.FileOutputStream(filepath)) {
            for (AICourseSlide slide : resultListView.getItems()) {
                XWPFParagraph title = document.createParagraph();
                title.setStyle("Heading1");
                title.createRun().setText("Title: " + slide.title());

                XWPFParagraph content = document.createParagraph();
                content.createRun().setText(slide.content());

                document.createParagraph().createRun().addBreak();
            }
            document.write(out);
        }
    }

    private void exportAsPptx(String filepath) throws Exception {
        XMLSlideShow ppt = new XMLSlideShow();
        java.awt.Dimension size = new java.awt.Dimension(1024, 768);
        ppt.setPageSize(size);

        for (AICourseSlide slideData : resultListView.getItems()) {
            XSLFSlide slide = ppt.createSlide();
            XSLFTextBox title = slide.createTextBox();
            title.setAnchor(new java.awt.Rectangle(50, 50, 900, 50));
            title.setText("Title: " + slideData.title());

            XSLFTextBox content = slide.createTextBox();
            content.setAnchor(new java.awt.Rectangle(50, 120, 900, 500));
            content.setText(slideData.content());
        }

        try (java.io.FileOutputStream out = new java.io.FileOutputStream(filepath)) {
            ppt.write(out);
        }
    }




    @FXML
    private void clearResults() {
        resultListView.getItems().clear();
        promptField.clear();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class AICellFactory implements Callback<ListView<AICourseSlide>, ListCell<AICourseSlide>> {
        @Override
        public ListCell<AICourseSlide> call(ListView<AICourseSlide> param) {
            return new ListCell<>() {
                private final VBox container = new VBox(5);
                private final Label titleLabel = new Label();
                private final TextArea contentArea = new TextArea();

                {
                    // Initialize cell components once
                    titleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #4B0082;");
                    contentArea.setEditable(false);
                    contentArea.setWrapText(true);
                    contentArea.setStyle("-fx-background-color: transparent; -fx-border-color: #D8BFD8;");
                    container.getChildren().addAll(titleLabel, contentArea);
                }

                @Override
                protected void updateItem(AICourseSlide slide, boolean empty) {
                    super.updateItem(slide, empty);
                    if (empty || slide == null) {
                        setGraphic(null);
                    } else {
                        titleLabel.setText(slide.title());
                        contentArea.setText(slide.content());
                        setGraphic(container);
                    }
                }
            };
        }
    }
}
