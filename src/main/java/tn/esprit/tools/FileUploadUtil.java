package tn.esprit.tools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class FileUploadUtil {
    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/src/main/resources/assets/uploads/";

    public static String uploadFile(File file, String subfolder) throws IOException {
        if (file == null || !file.exists()) {
            throw new IOException("File does not exist");
        }

        if (file.length() > MAX_FILE_SIZE) {
            throw new IOException("File size exceeds 10MB limit");
        }

        Path uploadPath = Paths.get(UPLOAD_DIR + subfolder);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFileName = file.getName();
        String fileExtension = originalFileName.contains(".") ?
                originalFileName.substring(originalFileName.lastIndexOf(".")) : "";
        String uniqueFileName = UUID.randomUUID() + fileExtension;

        Path targetLocation = uploadPath.resolve(uniqueFileName);
        Files.copy(file.toPath(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return "assets/uploads/" + subfolder + "/" + uniqueFileName;
    }

    public static File getUploadedFile(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return null;
        }
        String fullPath = UPLOAD_DIR + relativePath.replace("assets/uploads/", "");
        File file = new File(fullPath);
        return file.exists() ? file : null;
    }
}