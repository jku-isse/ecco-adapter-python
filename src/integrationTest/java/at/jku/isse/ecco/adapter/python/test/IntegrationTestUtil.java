package at.jku.isse.ecco.adapter.python.test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IntegrationTestUtil {
    // repository paths
    public static final String PATH_POMMERMAN = "pommerman";
    public static final String PATH_POMMERMAN_FAST = "pommerman_small";
    public static final String PATH_PYTHON = "image_variants_python";
    public static final String PATH_JUPYTER = "jupyter_variants";

    // relative checkout paths
    public static final String PATH_EXTENSIONAL = "extensional_correctness_check";
    public static final String PATH_INTENSIONAL_VALID = "intensional_correctness_valid";
    public static final String PATH_INTENSIONAL_INVALID = "intensional_correctness_invalid";

    public static void createCSV(Path p, List<String[]> measures) {
        File csvOutputFile = new File(p.toAbsolutePath() + "\\times.csv");
        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
            measures.stream()
                    .map(IntegrationTestUtil::convertToCSV)
                    .forEach(pw::println);
        } catch (Exception ignored) {
        }
    }

    private static String convertToCSV(String[] data) {
        return Stream.of(data)
                .map(IntegrationTestUtil::escapeSpecialCharacters)
                .collect(Collectors.joining(","));
    }

    private static String escapeSpecialCharacters(String data) {
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }

    public static List<Path> getRelativePaths(Path folder, String ending) {
        try (Stream<Path> paths = Files.walk(folder)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(f -> f.getFileName().toString().endsWith(ending))
                    .map(folder::relativize)
                    .toList();
        } catch (IOException e) {
            // process exception
        }
        return null;
    }
}
