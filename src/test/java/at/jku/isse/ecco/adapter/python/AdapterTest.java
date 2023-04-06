package at.jku.isse.ecco.adapter.python;

import at.jku.isse.ecco.storage.mem.dao.MemEntityFactory;
import at.jku.isse.ecco.tree.Node;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static at.jku.isse.ecco.adapter.python.AdapterTestUtil.*;

public class AdapterTest {

    public static void main(String[] args) {

        final String python = ".py";
        final String jupyter = "ipynb";
        final String json = "json";

        //final String[] excludedFolders = new String[]{"yolov5-master", "ass1", "ass2"};
        final String[] excludedFolders = new String[] {};

        File readFolder = new File("adapter/python/src/test/resources/read/");
        File writeFolder = new File("adapter/python/src/test/resources/write/");

        Path readPath = Paths.get(readFolder.getAbsolutePath());
        Path writePath = Paths.get(writeFolder.getAbsolutePath());

        deleteDir(writePath);

        List<Path> files;
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
                    .filter(f -> f.getFileName().toString().endsWith(python) || f.getFileName().toString().endsWith(jupyter) || f.getFileName().toString().endsWith(json)).toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Path[] inputFiles = files.toArray(new Path[0]);

        System.out.println("reading ...");
        PythonReader reader = new PythonReader(new MemEntityFactory());
        Set<Node.Op> readNodes = reader.read(Paths.get(readFolder.getAbsolutePath()), inputFiles);
        System.out.println("reading ... successful");

        System.out.println("writing ...");
        PythonWriter writer = new PythonWriter();
        Set<Node> writeNodes = new HashSet<>(readNodes);
        writer.write(Paths.get(writeFolder.getAbsolutePath()), writeNodes);
        System.out.println("writing ... successful");

        // compare all Files
        System.out.println("checking for equality ...");
        boolean allIdentical = true;
        for (Path inputPath : files) {

            Path outputPath = readPath.relativize(inputPath);
            outputPath = writePath.resolve(outputPath);

            boolean identical = false;
            if (inputPath.toString().endsWith(python)) {
                identical = comparePythonFiles(inputPath, outputPath);
            } else if (inputPath.toString().endsWith(json)) {
                identical = compareJsonFiles(inputPath, outputPath);
            } else if (inputPath.toString().endsWith(jupyter)) {
                identical = compareJupyterFiles(inputPath, outputPath);
            } else {
                System.out.println("Unknown File extension");
            }

            if (!identical) {
                System.out.println("Writing of " + outputPath + " NOT identical");
                allIdentical = false;
            }

        }
        if (allIdentical) {
            System.out.println("checking for equality ... successful - all files are identical");
        }

    }
}