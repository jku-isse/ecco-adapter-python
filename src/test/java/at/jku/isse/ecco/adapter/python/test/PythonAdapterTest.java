package at.jku.isse.ecco.adapter.python.test;

import at.jku.isse.ecco.adapter.python.PythonReader;
import at.jku.isse.ecco.adapter.python.PythonWriter;
import at.jku.isse.ecco.storage.mem.dao.MemEntityFactory;
import at.jku.isse.ecco.tree.Node;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static at.jku.isse.ecco.adapter.python.test.PythonAdapterTestUtil.*;
import static org.testng.Assert.assertTrue;


public class PythonAdapterTest {

    private static Path readPath;
    private static Path writePath;

    final static String python = ".py";
    final static String jupyter = "ipynb";
    final static String json = "json";

    static PythonReader reader;
    static PythonWriter writer;

    @BeforeSuite(groups = {"unit"})
    static void start() {
        reader = new PythonReader(new MemEntityFactory());
        writer = new PythonWriter();

        Path cwd = Paths.get("").toAbsolutePath();

        readPath = cwd.resolve("src/test/resources/data/read/").toAbsolutePath();
        writePath = cwd.resolve("src/test/resources/data/write/").toAbsolutePath();

        deleteDir(writePath);
    }

    @AfterSuite(groups = {"unit"})
    static void calculateMetricsForWholeTestSet() {
        reader = null;
        writer = null;
    }

    @Test(dataProvider = "python", groups = {"python"})
    void testPython(final Path path) {
        testFiles(path);
    }

    @Test(dataProvider = "jupyter", groups = {"jupyter"})
    void testJupyter(final Path path) {
        testFiles(path);
    }

    @Test(dataProvider = "json", groups = {"json"})
    void testJson(final Path path) {
        testFiles(path);
    }

    @Test(groups = {"python"}, enabled = false)
    void testSingleFile() {
        reader = new PythonReader(new MemEntityFactory());
        writer = new PythonWriter();

        Path cwd = Paths.get("").toAbsolutePath();
        readPath = cwd.resolve("src/test/resources/data/read/").toAbsolutePath();
        writePath = cwd.resolve("src/test/resources/data/write/").toAbsolutePath();
        recreateDir(writePath);

        Path path = readPath.resolve("testExcept.py");
        testFiles(path);
        path = readPath.resolve("testExcept2.py");
        testFiles(path);
        path = readPath.resolve("testExcept3.py");
        testFiles(path);
    }

    void testFiles(final Path path) {

        Set<Node.Op> readNodes = reader.read(readPath, new Path[]{path});
        Set<Node> writeNodes = new HashSet<>(readNodes);
        writer.write(writePath, writeNodes);

        Path outputPath = readPath.relativize(path);
        outputPath = writePath.resolve(outputPath);

        boolean identical = false;
        if (path.toString().endsWith(python)) {
            identical = comparePythonFiles(path, outputPath);
        } else if (path.toString().endsWith(json)) {
            identical = compareJsonFiles(path, outputPath);
        } else if (path.toString().endsWith(jupyter)) {
            identical = compareJupyterFiles(path, outputPath);
        } else {
            System.out.println("Unknown File extension");
        }

        assertTrue(identical);
    }

    @DataProvider(name = "python")
    public static Object[] filesPython() {
        return files(python);
    }

    @DataProvider(name = "jupyter")
    public static Object[] filesJupyter() {
        return files(jupyter);
    }

    @DataProvider(name = "json")
    public static Object[] filesJson() {
        return files(json);
    }

    public static Object[] files(String type) {
        List<Path> files;

        //final String[] excludedFolders = new String[]{"yolov5-master", "ass1", "ass2"};
        final String[] excludedFolders = new String[]{};

        try (Stream<Path> s1 = Files.walk(readPath);
             Stream<Path> s2 = Files.walk(readPath)) {

            s1.filter(Files::isDirectory).map(in -> writePath.resolve(readPath.relativize(in)))
                    .forEach(out -> {
                                try {
                                    Files.createDirectories(out);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );

            files = s2.filter(f -> !Files.isDirectory(f))
                    .filter(f -> Arrays.stream(excludedFolders).noneMatch(s -> f.toAbsolutePath().toString().contains(s)))
                    .filter(f -> f.getFileName().toString().endsWith(type)).toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return files.toArray(new Object[0]);
    }

}
