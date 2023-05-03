package at.jku.isse.ecco.adapter.python.test;

import org.testng.annotations.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static at.jku.isse.ecco.adapter.python.test.IntegrationTestUtil.*;
import static at.jku.isse.ecco.adapter.python.test.PythonAdapterTestUtil.compareFiles;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class PommermanComparisonTest {

    @Test(groups = {"compare"})
    public void comparePommermanCheckoutsTest() {

        Path cwd = Path.of(System.getProperty("user.dir"));
        Path p1 = cwd.resolve("src/integrationTest/resources/data").resolve(PATH_POMMERMAN).resolve(PATH_EXTENSIONAL);
        Path p2 = cwd.resolve("src/integrationTest/resources/data").resolve(PATH_POMMERMAN_FAST).resolve("extensional_correctness_check_red");

        compareRepositories(p1, p2);
    }

    private void compareRepositories(Path p1, Path p2) {

        List<Path> relPaths1 = new ArrayList<>(Objects.requireNonNull(getRelativePaths(p1, "py")));
        List<Path> relPaths2 = new ArrayList<>(Objects.requireNonNull(getRelativePaths(p2, "py")));

        assertEquals(relPaths1.size(), relPaths2.size());

        if (relPaths1.size() == relPaths2.size()) {
            for (int i = 0; i < relPaths1.size(); i++) {
                assertTrue(
                        compareFiles(p1.resolve(relPaths1.get(i).toString()), p2.resolve(relPaths2.get(i).toString())), "Comparison failed for " + relPaths1.get(i).toString());
            }
        }
    }
}
