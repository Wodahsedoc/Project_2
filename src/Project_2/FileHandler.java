package Project_2;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Handles exporting system reports to the file system.
 * Legacy .txt data persistence has been replaced by the Derby Database.
 * @param <T>
 */
public class FileHandler<T> {

    private final String dataDirectory;
    private static final String DEFAULT_DIRECTORY = "data/";
    private static final String REPORTS_DIRECTORY = "data/reports/";

    public FileHandler() {
        this.dataDirectory = DEFAULT_DIRECTORY;
        ensureDirectoriesExist();
    }

    public FileHandler(String dataDirectory) {
        this.dataDirectory = dataDirectory;
        ensureDirectoriesExist();
    }

    private void ensureDirectoriesExist() {
        File dataDir = new File(dataDirectory);
        if (!dataDir.exists()) {
            dataDir.mkdirs();
            System.out.println("Created data directory: " + dataDirectory);
        }

        File reportsDir = new File(REPORTS_DIRECTORY);
        if (!reportsDir.exists()) {
            reportsDir.mkdirs();
            System.out.println("Created reports directory: " + REPORTS_DIRECTORY);
        }
    }

    /**
     * Exports a generated text report to the reports directory.
     * @param reportContent
     * @param baseFilename
     * @return 
     */
    public boolean exportReport(String reportContent, String baseFilename) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filepath = REPORTS_DIRECTORY + baseFilename + "_" + timestamp + ".txt";
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filepath))) {
            writer.write(reportContent);
            System.out.println("Report successfully exported to " + filepath);
            return true;
        } catch (IOException e) {
            System.out.println("Error exporting report: " + e.getMessage());
            return false;
        }
    }
    
    public void listReportFiles() {
        File dir = new File(REPORTS_DIRECTORY);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".txt"));
        if (files == null || files.length == 0) {
            System.out.println("No reports found in: " + REPORTS_DIRECTORY);
            return;
        }
        System.out.println("===== Generated Reports =====");
        for (File file : files) {
            System.out.printf("%-40s %.2f KB%n",
                    file.getName(), file.length() / 1024.0);
        }
    }
}