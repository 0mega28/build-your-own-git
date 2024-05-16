import exceptions.NotEnoughArgumentException;
import util.ArrayUtil;
import util.Zlib;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) {
        final String command = args[0];

        try {
            switch (command) {
                case "init" -> {
                    try {
                        Files.createDirectory(Path.of(".git"));
                        Files.createDirectory(Path.of(".git", "objects"));
                        Files.createDirectory(Path.of(".git", "refs"));
                        Files.writeString(Path.of(".git/HEAD"), "ref: refs/heads/main\n");
                        System.out.println("Initialized git directory");
                    } catch (IOException e) {
                        System.err.println("Error initializing git directory: " + e.getMessage());
                    }
                }
                case "cat-file" -> {
                    args = ArrayUtil.shift(args);
                    handleCatFile(args);
                }
                default -> System.out.println("Unknown command: " + command);
            }
        } catch (NotEnoughArgumentException ex) {
            System.err.println("Not enough arguments: " + ex.getMessage());
        }
    }

    private static void handleCatFile(String[] args) {
        final String flag = args[0];

        switch (flag) {
            case "-p" -> {
                args = ArrayUtil.shift(args);
                final String blobSha = args[0];
                final int MIN_BLOB_SHA_LENGTH = 2;
                if (blobSha.length() <= MIN_BLOB_SHA_LENGTH) {
                    System.err.printf("Invalid blob sha: %s expected length more than %d length%n"
                            , blobSha, MIN_BLOB_SHA_LENGTH);
                    System.exit(1);
                }

                final String directory = blobSha.substring(0, 2);
                final String fileName = blobSha.substring(2);

                boolean directoryExists = Files.isDirectory(Path.of(".git", "objects", directory));

                if (!directoryExists) {
                    System.err.println("Directory does not exist: " + directory);
                    System.exit(1);;
                }

                try {
                    InputStream compressedData = Files.newInputStream(
                            Path.of(".git", "objects", directory, fileName));
                    String data = Zlib.decompress(compressedData);

                    Pattern blobPattern = Pattern.compile("^blob\\s(\\d+)\0(.*)$");
                    Matcher matcher = blobPattern.matcher(data);
                    if (!matcher.find()) {
                        System.err.println("Invalid blob sha: " + data);
                        System.exit(1);
                    }
                    Integer bytes = Integer.parseInt(matcher.group(1));
                    String content = matcher.group(2);

                    if (content.length() != bytes) {
                        System.err.printf("Invalid blob sha: %s expected %d bytes but got %d bytes\n"
                                , data, bytes, content.length());
                        System.exit(1);
                    }

                    System.out.print(content);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            default -> {
                System.err.println("Unknown flag: " + flag);
            }
        }
    }
}
