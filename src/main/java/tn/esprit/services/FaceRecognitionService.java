package tn.esprit.services;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FaceRecognitionService {
    private static final String FACE_DATA_DIR = "face_data/";
    private static Net faceNet;
    private static boolean initialized = false;
    private static final Size INPUT_SIZE = new Size(300, 300);
    private static final double SCALE_FACTOR = 1.0;
    private static final Scalar MEAN = new Scalar(104.0, 177.0, 123.0);
    private static final double THRESHOLD = 0.5;

    static {
        try {
            // Load OpenCV library
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

            // Create directory for face data if it doesn't exist
            Path path = Paths.get(FACE_DATA_DIR);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }

            // Initialize with DNN instead of cascade
            initialized = true;
            System.out.println("FaceRecognitionService initialized successfully");
        } catch (Exception e) {
            System.err.println("FaceRecognitionService initialization failed: " + e.getMessage());
            e.printStackTrace();
            initialized = false;
        }
    }

    public static boolean isInitialized() {
        return initialized;
    }

    // Alternative face detection method that doesn't rely on cascades
    private static MatOfRect detectFaces(Mat frame) {
        MatOfRect faces = new MatOfRect();

        // Simple approach: use color-based skin detection as an alternative
        Mat hsvFrame = new Mat();
        Imgproc.cvtColor(frame, hsvFrame, Imgproc.COLOR_BGR2HSV);

        // Define range for skin color in HSV
        Scalar lowerBound = new Scalar(0, 20, 70);
        Scalar upperBound = new Scalar(20, 255, 255);

        Mat skinMask = new Mat();
        Core.inRange(hsvFrame, lowerBound, upperBound, skinMask);

        // Clean up the mask
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(11, 11));
        Imgproc.erode(skinMask, skinMask, kernel);
        Imgproc.dilate(skinMask, skinMask, kernel);

        // Apply GaussianBlur to reduce noise
        Imgproc.GaussianBlur(skinMask, skinMask, new Size(3, 3), 0);

        // Find contours in the skin mask
        Mat hierarchy = new Mat();
        java.util.List<MatOfPoint> contours = new java.util.ArrayList<>();
        Imgproc.findContours(skinMask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Process contours to find face-sized regions
        for (MatOfPoint contour : contours) {
            double area = Imgproc.contourArea(contour);
            // Faces typically have a significant area
            if (area > 5000) {
                Rect boundingRect = Imgproc.boundingRect(contour);
                // Faces are usually more square than very elongated
                double aspectRatio = (double) boundingRect.width / boundingRect.height;
                if (aspectRatio > 0.5 && aspectRatio < 1.5) {
                    faces.push_back(new MatOfRect(boundingRect));
                }
            }
        }

        return faces;
    }

    public static boolean registerFace(long userId, ImageView imageView) {
        try {
            Mat frame = convertImageToMat(imageView.getImage());
            MatOfRect faceDetections = detectFaces(frame);

            if (faceDetections.toArray().length == 0) return false;

            Rect faceRect = faceDetections.toArray()[0];
            Mat face = new Mat(frame, faceRect);

            String filename = FACE_DATA_DIR + userId + ".png";
            return Imgcodecs.imwrite(filename, face);
        } catch (Exception e) {
            System.err.println("Error registering face: " + e.getMessage());
            return false;
        }
    }

    public static boolean registerFace(long userId, Mat face) {
        try {
            String filename = FACE_DATA_DIR + userId + ".png";
            return Imgcodecs.imwrite(filename, face);
        } catch (Exception e) {
            System.err.println("Error registering face: " + e.getMessage());
            return false;
        }
    }

    public static boolean verifyFace(long userId, ImageView imageView) {
        try {
            String registeredFacePath = FACE_DATA_DIR + userId + ".png";
            File registeredFile = new File(registeredFacePath);
            if (!registeredFile.exists()) return false;

            Mat registeredFace = Imgcodecs.imread(registeredFacePath);
            if (registeredFace.empty()) return false;

            Mat currentFrame = convertImageToMat(imageView.getImage());
            MatOfRect faceDetections = detectFaces(currentFrame);
            if (faceDetections.toArray().length == 0) return false;

            Rect faceRect = faceDetections.toArray()[0];
            Mat currentFace = new Mat(currentFrame, faceRect);

            return compareFaces(registeredFace, currentFace);
        } catch (Exception e) {
            System.err.println("Error verifying face: " + e.getMessage());
            return false;
        }
    }

    private static boolean compareFaces(Mat img1, Mat img2) {
        Mat gray1 = new Mat();
        Mat gray2 = new Mat();
        Imgproc.cvtColor(img1, gray1, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(img2, gray2, Imgproc.COLOR_BGR2GRAY);
        Imgproc.equalizeHist(gray1, gray1);
        Imgproc.equalizeHist(gray2, gray2);

        Size size = new Size(100, 100);
        Imgproc.resize(gray1, gray1, size);
        Imgproc.resize(gray2, gray2, size);

        double similarity = calculateSSIM(gray1, gray2);
        return similarity > 0.6;
    }

    private static double calculateSSIM(Mat img1, Mat img2) {
        Mat mat1 = new Mat();
        Mat mat2 = new Mat();
        img1.convertTo(mat1, CvType.CV_32F);
        img2.convertTo(mat2, CvType.CV_32F);

        MatOfDouble mean1 = new MatOfDouble();
        MatOfDouble mean2 = new MatOfDouble();
        MatOfDouble stdDev1 = new MatOfDouble();
        MatOfDouble stdDev2 = new MatOfDouble();

        Core.meanStdDev(mat1, mean1, stdDev1);
        Core.meanStdDev(mat2, mean2, stdDev2);

        Mat covar = new Mat();
        Core.multiply(mat1, mat2, covar);
        double cov = Core.mean(covar).val[0] - mean1.toArray()[0] * mean2.toArray()[0];

        double c1 = Math.pow(0.01 * 255, 2);
        double c2 = Math.pow(0.03 * 255, 2);

        double ssim = ((2 * mean1.toArray()[0] * mean2.toArray()[0] + c1) * (2 * cov + c2)) /
                ((Math.pow(mean1.toArray()[0], 2) + Math.pow(mean2.toArray()[0], 2) + c1) *
                        (Math.pow(stdDev1.toArray()[0], 2) + Math.pow(stdDev2.toArray()[0], 2) + c2));

        return ssim;
    }

    public static Mat convertImageToMat(Image image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        Mat mat = new Mat(height, width, CvType.CV_8UC3);

        byte[] buffer = new byte[width * height * 3];
        int index = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = image.getPixelReader().getColor(x, y);
                buffer[index++] = (byte) (color.getBlue() * 255);
                buffer[index++] = (byte) (color.getGreen() * 255);
                buffer[index++] = (byte) (color.getRed() * 255);
            }
        }

        mat.put(0, 0, buffer);
        return mat;
    }

    public static Image convertMatToImage(Mat mat) {
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".png", mat, buffer);
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }
}