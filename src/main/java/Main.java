import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;

public class Main {
    public static void main(String[] args) {
        // You can use print statements as follows for debugging, they'll be visible when running tests.
        System.out.println("Logs from your program will appear here!");

        // Uncomment this block to pass the first stage

        final String command = args[0];

        switch (command) {
            case "init" -> {
                try {
                    Files.createDirectory(Path.of(".git"));
                    Files.createDirectory(Path.of(".git", "objects"));
                    Files.createDirectory(Path.of(".git", "refs"));
                    Files.writeString(Path.of(".git/HEAD"), "ref: refs/heads/main\n");
                    System.out.println("Initialized git directory");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            default -> System.out.println("Unknown command: " + command);
        }
    }
}
