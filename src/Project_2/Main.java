package Project_2;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {

        DatabaseManager.getInstance();


        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            DatabaseManager.getInstance().shutdown();
        }));

        SwingUtilities.invokeLater(() -> new LoginFrame());
    }
}