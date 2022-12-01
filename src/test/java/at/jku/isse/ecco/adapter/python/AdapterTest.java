package at.jku.isse.ecco.adapter.python;

import at.jku.isse.ecco.storage.mem.dao.MemEntityFactory;
import at.jku.isse.ecco.tree.Node;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Stream;

public class AdapterTest {

    public static void main(String[] args) {

        final String python = ".py";
        final String jupyter = "ipynb";

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

            files = s2.filter(f -> !Files.isDirectory(f)).filter(f -> f.getFileName().toString().endsWith(python) || f.getFileName().toString().endsWith(jupyter)).toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Path[] inputFiles = files.toArray(new Path[files.size()]);

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

            if (inputPath.toString().endsWith(python)) {
                if (!comparePythonFiles(inputPath, outputPath)) {
                    System.out.println("Writing of " + outputPath + " NOT identical");
                    allIdentical = false;
                }
            } else if (inputPath.toString().endsWith(jupyter)) {
                if (!compareJupyterFiles(inputPath, outputPath)) {
                    System.out.println("Writing of " + outputPath + " NOT identical");
                    allIdentical = false;
                }
            } else {
                System.out.println("Unknown File extension");
            }
        }
        if (allIdentical) {
            System.out.println("checking for equality ... successful - all files are identical");
        }
    }

    private static boolean comparePythonFiles(Path p1, Path p2) {
        try {
            BufferedReader reader1 = new BufferedReader(new FileReader(p1.toFile()));
            BufferedReader reader2 = new BufferedReader(new FileReader(p2.toFile()));

            String pattern = "\s*";

            String line1 = reader1.readLine();
            String line2 = reader2.readLine();
            while (line1 != null || line2 != null) {
                if (line1 == null || line2 == null) {
                    return false;
                } else if (line1.matches(pattern) && line2.matches(pattern)) {
                    // continue - empty lines are normalized
                } else if (!line1.equalsIgnoreCase(line2)) {
                    return false;
                }

                line1 = reader1.readLine();
                line2 = reader2.readLine();
            }
        } catch (FileNotFoundException e) {
            System.out.print("File not found while checking " + p2.getFileName() + " ...");
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static boolean compareJupyterFiles(Path p1, Path p2) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            JsonNode actualObj1 = mapper.readTree(new String(Files.readAllBytes(p1)));
            JsonNode actualObj2 = mapper.readTree(new String(Files.readAllBytes(p2)));

            JupyterComparator cmp = new JupyterComparator();
            return actualObj1.equals(cmp, actualObj2);
        } catch (NoSuchFileException e) {
            System.out.print("File not found while checking " + p2.getFileName() + " ...");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static class JupyterComparator implements Comparator<JsonNode> {
        private static final List<String> ignoreFields = Arrays.asList("outputs", "metadata", "execution_count");

        @Override
        public int compare(JsonNode j1, JsonNode j2) {

            if ((j1 instanceof ObjectNode o1) && (j2 instanceof ObjectNode o2)) {
                // remove fields that should be ignored before comparing
                o1.remove(ignoreFields);
                o2.remove(ignoreFields);

                if (o1.equals(o2)) {
                    return 0;
                }
            } else if (j1.equals(j2)) {
                return 0;
            }
            return 1;
        }
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