package at.jku.isse.ecco.adapter.python;

import at.jku.isse.ecco.storage.mem.dao.MemEntityFactory;
import at.jku.isse.ecco.tree.Node;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class AdapterTest {

    public static void main(String[] args) {

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

            files = s2.filter(f -> !Files.isDirectory(f)).filter(f -> f.getFileName().toString().endsWith(".py")).toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Path[] inputFiles = files.toArray(new Path[files.size()]);

        System.out.println("reading ...");
        PythonReader reader = new PythonReader(new MemEntityFactory());
        Set<Node.Op> readNodes = reader.read(Paths.get(readFolder.getAbsolutePath()), inputFiles);
        System.out.println(readNodes.size());
        System.out.println("reading ... successful");

        System.out.print("writing ...");
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

            if (!compareFileArrays(inputPath, outputPath)) {
                System.out.println("Writing of " + outputPath + " NOT identical");
                allIdentical = false;
            }
        }
        if (allIdentical) {
            System.out.println("checking for equality ... successful - all files are identical");
        }
    }

    private static boolean compareFileArrays(Path File_One, Path File_Two) {
        try {
            if (Files.size(File_One) != Files.size(File_Two)) {
                return false;
            }

            byte[] First_File = Files.readAllBytes(File_One);
            byte[] Second_File = Files.readAllBytes(File_Two);
            return Arrays.equals(First_File, Second_File);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * delete all files / folders of a directory (excluding root-folder)
     * source: https://howtodoinjava.com/java/io/delete-directory-recursively/
     */
    private static void deleteDir(Path rootDir) {
        try {
            Files.walkFileTree(rootDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                        throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir,
                                                          IOException exc) throws IOException {
                    if (exc == null) {
                        if (dir != rootDir) Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    } else {
                        throw exc;
                    }
                }

            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}