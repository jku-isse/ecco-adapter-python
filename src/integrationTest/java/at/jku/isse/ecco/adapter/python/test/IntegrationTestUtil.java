package at.jku.isse.ecco.adapter.python.test;

import at.jku.isse.ecco.adapter.python.PythonPlugin;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IntegrationTestUtil {

    public static final Path REPOSITORY_ROOT = Path.of(System.getProperty("user.dir")).resolve("src/integrationTest/resources/data");
    // repository paths
    public static final String PATH_POMMERMAN = "pommerman";
    public static final String PATH_POMMERMAN_FAST = "pommerman_small";
    public static final String PATH_PYTHON = "image_variants_python";
    public static final String PATH_JUPYTER = "jupyter_variants";

    // relative checkout paths
    public static final String PATH_EXTENSIONAL = "extensional_correctness_check";
    public static final String PATH_INTENSIONAL_VALID = "intensional_correctness_valid";
    public static final String PATH_INTENSIONAL_INVALID = "intensional_correctness_invalid";

    public static final String EXT_PYTHON = ".py";
    public static final String EXT_JUPYTER = ".ipynb";
    public static final String EXT_JSON = ".json";

    private static final String LOG_FILE = "logging.log";

    public static void createCSV(Path p, List<String[]> measures, String fileName) {
        File csvOutputFile = new File(p.toAbsolutePath() + "\\" + fileName + ".csv");
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

    public static List<Path> getAbsoluteFilePaths(Path folder, String ending) {
        try (Stream<Path> paths = Files.walk(folder)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(f -> f.getFileName().toString().endsWith(ending))
                    .toList();
        } catch (IOException e) {
            // process exception
            return null;
        }
    }

    public static List<Path> getRelativeFilePaths(Path folder, String ending) {
        try (Stream<Path> paths = Files.walk(folder)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(f -> f.getFileName().toString().endsWith(ending))
                    .map(folder::relativize)
                    .toList();
        } catch (IOException e) {
            // process exception
            return null;
        }
    }

    /**
     * get folder-names of commits (starting with "C*")
     * used to tell ECCO where the commit-folders are
     */
    public static String[] getCommits(Path path) {
        try (Stream<Path> paths = Files.walk(path, 1)) {
            return paths
                    .filter(Files::isDirectory)
                    .filter(f -> f.getFileName().toString().startsWith("C"))
                    .map(f -> f.getFileName().toString())
                    .toList()
                    .toArray(new String[0]);
        } catch (IOException e) {
            // process exception
        }
        return new String[0];
    }

    public static void enableLoggingToFile(Path repoPath) {
        Logger logger = Logger.getLogger(PythonPlugin.class.getName());
        FileHandler fh;
        try {
            // This block configure the logger with handler and formatter
            fh = new FileHandler(repoPath.resolve(LOG_FILE).toAbsolutePath().toString());
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void finishLogging(Path repoPath, PythonAdapterIntegrationTestLogger log) {
        Logger logger = Logger.getLogger(PythonPlugin.class.getName());

        for (Handler h : logger.getHandlers()) {
            logger.removeHandler(h);
            h.close();
        }

        parseLog(repoPath, log);

        String name = repoPath.getParent().relativize(repoPath).toString();
        log.createCSV(repoPath, name);
    }

    public static void parseLog(Path repoPath, PythonAdapterIntegrationTestLogger log) {

        try (BufferedReader reader = new BufferedReader(new FileReader(repoPath.resolve(LOG_FILE).toAbsolutePath().toString()))) {
            List<Integer> times = new LinkedList<>();
            float timeParsing = 0.0f, timeTraversing = 0.0f, timeReading = 0.0f, timeScriptInside = 0.0f;

            log.add(0, "nCommitFiles", "commitTotalScriptTimeOutside", "commitTotalScriptTimeInside", "commitAvgScriptTime", "commitScriptParsing", "commitScriptTraversing", "commitScriptReading");
            log.add(0, "nCheckoutFiles", "checkoutTotalScriptTime", "checkoutAvgScriptTime", "checkoutScriptParsing", "checkoutScriptTraversing");

            String matcher = ".*?\\((.*)ms.*"; // replaces everything outside (23.232ms) including brackets and 'ms'

            int commit = 1, checkout = 1;
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                if (line.contains("created")) {
                    String time = line.split("nodes in ")[1].split("ms")[0].replaceAll("\\s", "");
                    times.add(Integer.parseInt(time));
                } else if (line.startsWith("INFO: Commit")) { // commit done
                    int sum = 0;
                    for (int t : times) {
                        sum += t;
                    }
                    float avg = (float) sum / times.size();
                    log.add(commit++,
                            String.valueOf(times.size()),
                            format((float) sum),
                            format(timeScriptInside),
                            format(avg),
                            format(timeParsing),
                            format(timeTraversing),
                            format(timeReading)
                    );

                    timeParsing = 0.0f;
                    timeTraversing = 0.0f;
                    timeReading = 0.0f;
                    timeScriptInside = 0.0f;
                    times.clear();
                } else if (line.contains("Parsing (write) successful (exit-code: 0); wrote")) {
                    String time = line.split("file in ")[1].split("ms")[0].replace(" ", "");
                    times.add(Integer.parseInt(time));
                } else if (line.contains("INFO: Checkout of Commit")) { // checkout (extensional) done
                    int sum = 0;
                    for (int t : times) {
                        sum += t;
                    }
                    float avg = (float) sum / times.size();
                    log.add(checkout++,
                            String.valueOf(times.size()),
                            format((float) sum),
                            format(avg),
                            format(timeParsing),
                            format(timeTraversing)
                    );

                    timeParsing = 0.0f;
                    timeTraversing = 0.0f;
                    times.clear();
                } else if (line.contains("Successfully parsed to")) {
                    String time = line.replaceFirst(matcher, "$1");
                    timeParsing += Float.parseFloat(time);
                } else if (line.contains("Successfully traversed and parsed")) {
                    String time = line.replaceFirst(matcher, "$1");
                    timeTraversing += Float.parseFloat(time);
                } else if (line.contains("Successfully read file")) {
                    String time = line.replaceFirst(matcher, "$1");
                    timeReading += Float.parseFloat(time);
                } else if (line.contains("Successfully finished")) {
                    String time = line.replaceFirst(matcher, "$1");
                    timeScriptInside += Float.parseFloat(time);
                }
            }
            reader.close();
            Files.deleteIfExists(repoPath.resolve(LOG_FILE).toAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void logPythonDetails(Path repoPath, PythonAdapterIntegrationTestLogger log) {
        String[] commits = getCommits(repoPath);

        log.add(0, "msg", "nFiles", "nLines");

        for (int i = 0; i < commits.length; i++) {

            Path commitPath = repoPath.resolve(commits[i]);

            List<Path> files = getAbsoluteFilePaths(commitPath, EXT_PYTHON);
            assert files != null;

            // count lines (not LOC, because we also parse comments, etc... )
            long lines = 0;
            for (Path p : files) {
                lines += countLines(p.toFile());
            }

            String msg = commits[i].substring(commits[i].indexOf("_") + 1).replaceAll("_", " ");
            log.add(i + 1,
                    msg,
                    String.valueOf(files.size()),
                    String.valueOf(lines));
        }
    }

    public static void logJuypterDetails(Path repoPath, PythonAdapterIntegrationTestLogger log) {
        String[] commits = getCommits(repoPath);

        log.add(0, "msg");

        for (int i = 0; i < commits.length; i++) {
            Path commitPath = repoPath.resolve(commits[i]);

            List<Path> files = getAbsoluteFilePaths(commitPath, EXT_PYTHON);
            assert files != null;

            String msg = commits[i].substring(commits[i].indexOf("_") + 1).replaceAll("_", " ");
            log.add(i + 1,
                    msg);
        }
    }

    public static long countLines(File fileName) {
        long lines = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                lines++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    private static String format(float seconds) {
        return String.format(Locale.US, "%.03f", seconds / 1000);
    }
}
