
package Project_2;

/**
 *
 * @author aneirinblosch
 */
import java.io.*;
import java.util.ArrayList;
import java.util.List;

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


    public boolean save(List<T> data, String filename) {
        String filepath = dataDirectory + filename;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filepath))) {
            for (T item : data) {
                writer.write(serialize(item));
                writer.newLine();
            }
            System.out.println("Saved " + data.size() + " records to " + filepath);
            return true;
        } catch (IOException e) {
            System.out.println("Error saving to file: " + filepath);
            System.out.println("Reason: " + e.getMessage());
            return false;
        }
    }


    public List<String> load(String filename) {
        String filepath = dataDirectory + filename;
        List<String> lines = new ArrayList<>();
        File file = new File(filepath);

        if (!file.exists()) {
            System.out.println("Data file not found: " + filepath
                             + ". Starting with empty data.");
            return lines;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(filepath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    lines.add(line);
                }
            }
            System.out.println("Loaded " + lines.size()
                             + " records from " + filepath);
        } catch (IOException e) {
            System.out.println("Error loading from file: " + filepath);
            System.out.println("Reason: " + e.getMessage());
        }
        return lines;
    }


    public boolean append(T item, String filename) {
        String filepath = dataDirectory + filename;
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(filepath, true))) {
            writer.write(serialize(item));
            writer.newLine();
            return true;
        } catch (IOException e) {
            System.out.println("Error appending to file: " + filepath);
            System.out.println("Reason: " + e.getMessage());
            return false;
        }
    }


    public boolean deleteFile(String filename) {
        File file = new File(dataDirectory + filename);
        if (file.exists()) {
            boolean deleted = file.delete();
            if (deleted) {
                System.out.println("Deleted file: " + filename);
            } else {
                System.out.println("Failed to delete file: " + filename);
            }
            return deleted;
        }
        System.out.println("File not found: " + filename);
        return false;
    }


    public boolean fileExists(String filename) {
        return new File(dataDirectory + filename).exists();
    }


    public boolean exportReport(String reportContent, String reportName) {
        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter
                .ofPattern("ddMMMyyyy_HHmmss"));
        String filename = REPORTS_DIRECTORY + reportName + "_" + timestamp + ".txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write(reportContent);
            System.out.println("Report exported to: " + filename);
            return true;
        } catch (IOException e) {
            System.out.println("Error exporting report: " + filename);
            System.out.println("Reason: " + e.getMessage());
            return false;
        }
    }


    public boolean backup(String filename) {
        String sourceFilepath = dataDirectory + filename;
        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter
                .ofPattern("ddMMMyyyy_HHmmss"));
        String backupFilepath = dataDirectory + "backup_"
                              + timestamp + "_" + filename;

        File source = new File(sourceFilepath);
        if (!source.exists()) {
            System.out.println("Source file not found: " + sourceFilepath);
            return false;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(source));
             BufferedWriter writer = new BufferedWriter(
                     new FileWriter(backupFilepath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.newLine();
            }
            System.out.println("Backup created: " + backupFilepath);
            return true;
        } catch (IOException e) {
            System.out.println("Error creating backup: " + e.getMessage());
            return false;
        }
    }


    private String serialize(T item) {
        try {
            return (String) item.getClass()
                    .getMethod("toFileString")
                    .invoke(item);
        } catch (Exception e) {

            return item.toString();
        }
    }


    public static boolean quickSave(String filepath, List<String> lines) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filepath))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            System.out.println("Quick save error: " + e.getMessage());
            return false;
        }
    }

    public static List<String> quickLoad(String filepath) {
        List<String> lines = new ArrayList<>();
        File file = new File(filepath);
        if (!file.exists()) return lines;

        try (BufferedReader reader = new BufferedReader(new FileReader(filepath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    lines.add(line.trim());
                }
            }
        } catch (IOException e) {
            System.out.println("Quick load error: " + e.getMessage());
        }
        return lines;
    }


    public void displayFileContents(String filename) {
        List<String> lines = load(filename);
        if (lines.isEmpty()) {
            System.out.println("File is empty or does not exist: " + filename);
            return;
        }
        System.out.println("===== Contents of " + filename + " =====");
        for (int i = 0; i < lines.size(); i++) {
            System.out.println((i + 1) + ". " + lines.get(i));
        }
        System.out.println("Total records: " + lines.size());
    }

    public void listDataFiles() {
        File dir = new File(dataDirectory);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".txt"));
        if (files == null || files.length == 0) {
            System.out.println("No data files found in: " + dataDirectory);
            return;
        }
        System.out.println("===== Data Files =====");
        for (File file : files) {
            System.out.printf("%-30s %.2f KB%n",
                    file.getName(), file.length() / 1024.0);
        }
    }

    @Override
    public String toString() {
        return "FileHandler[Directory: " + dataDirectory + "]";
    }
}