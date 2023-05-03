package at.jku.isse.ecco.adapter.python.test;

import at.jku.isse.ecco.core.Commit;
import at.jku.isse.ecco.service.EccoService;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static at.jku.isse.ecco.adapter.python.test.IntegrationTestUtil.*;
import static at.jku.isse.ecco.adapter.python.test.PythonAdapterTestUtil.*;
import static org.testng.Assert.assertTrue;

public class PythonAdapterRepositoryTest {

    private Path repoPath;
    private StringBuilder timemeasure;
    private EccoService service;

    private long accumulatedCommitTime;

    private List<String[]> measures;

    @BeforeTest(groups = {"integration"})
    public void setUpEccoService() {
        service = new EccoService();
    }

    @BeforeMethod(groups = {"integration"})
    public void initMeasures() {
        timemeasure = new StringBuilder();
        measures = new ArrayList<>();
        accumulatedCommitTime = 0;
    }

    @AfterMethod(groups = {"integration"})
    public void closeEccoService() {
        service.close();
        System.out.println(timemeasure);
        IntegrationTestUtil.createCSV(repoPath, measures);
    }


    @Test(groups = {"integration"})
    public void populatePythonTests() {

        checkPathAndInitService(PATH_PYTHON);

        // make commits
        String[] commits = getCommits();
        makeCommits(commits);

        // extensional correctness - reproduce commits
        checkExtensionalCorrectness(commits, "py");

        // checkout valid variants
        String[] checkouts = new String[]{
                "person.1, purpleshirt.1, glasses.1, hat.1",
        };

        checkoutValidVariants(checkouts);

        // checkout valid variants
        String[] invalidCheckouts = new String[]{
                "person.1",
                "purpleshirt.1",
                "jacket.1",
                "stripedshirt.1",
                "glasses.1",
                "hat.1"
        };

        checkoutInvalidVariants(invalidCheckouts);
    }

    private Path prepareRepoPath(String folder) {
        Path cwd = Path.of(System.getProperty("user.dir"));
        Path repoPath = cwd.resolve("src/integrationTest/resources/data").resolve(folder);
        Path p = repoPath.resolve(".ecco");
        deleteDir(p);
        return repoPath;
    }

    @Test(groups = {"integration"})
    public void jupyterTests() {

        checkPathAndInitService(PATH_JUPYTER);

        // make commits
        String[] commits = getCommits();
        makeCommits(commits);

        // extensional correctness - reproduce commits
        checkExtensionalCorrectness(commits, "ipynb");

        // checkout valid variants
        String[] invalidCheckouts = new String[]{
                "train.1, resolution.1",
                "dataset.2, export.1, log.1"
        };

        checkoutInvalidVariants(invalidCheckouts);

        // checkout valid variants
        String[] validCheckouts = new String[]{
                "train.1, resolution.1, parameters.1, weights.1, dataset.1, export.1, notify.1, log.1",
                "train.1, resolution.2, parameters.2, weights.2, dataset.2, export.1",
                "train.1, resolution.2, parameters.2, weights.2, dataset.1, export.1, log.1, notify.1",
        };

        checkoutValidVariants(validCheckouts);

    }

    @Test(groups = {"integration"}) //, enabled=false
    public void pommermanTests() {

        checkPathAndInitService(PATH_POMMERMAN);

        // make commits
        String[] commits = getCommits();
        makeCommits(commits);

        // extensional correctness - reproduce commits
        checkExtensionalCorrectness(commits, "ipynb");

        // checkout valid variants
        String[] invalidCheckouts = new String[]{
                "framework.2, learning.4, dqnmodel.1, simplevslearning.1", // invalid algo-model combination
                "framework.1, heuristic.8, ppomodel.2, simplevsheuristic.1", // invalid algo-model combination
        };

        checkoutInvalidVariants(invalidCheckouts);

        // checkout valid variants
        String[] validCheckouts = new String[]{
                "framework.2, learning.3, dqnmodel.2, ppomodel.1, simplevslearning.1",
                "framework.2, heuristic.10,	simplevsheuristic.1,",
        };

        checkoutValidVariants(validCheckouts);

    }

    @Test(groups = {"integration"}) //, enabled=false
    public void pommermanPerformanceTests() {

        checkPathAndInitService(PATH_POMMERMAN_FAST);

        // make commits
        String[] commits = getCommits();
        makeCommits(commits);

        // extensional correctness - reproduce commits
        checkExtensionalCorrectness(commits, "ipynb");
        recreateCommitsWithRedundancies(repoPath); // to check for equality with original

        // checkout valid variants
        String[] invalidCheckouts = new String[]{
                "framework.2, redundant.1, learning.4, dqnmodel.1, simplevsheuristic.1", // invalid algo-model combination
                "framework.1, redundant.1, heuristic.8, ppomodel.2, simplevslearning.1", // invalid algo-model combination
        };

        checkoutInvalidVariants(invalidCheckouts);

        // checkout valid variants
        String[] validCheckouts = new String[]{
                "framework.2, redundant.1, learning.3, dqnmodel.2, simplevslearning.1",
                "framework.2, redundant.1, learning.4, ppomodel.2, simplevslearning.1",
        };

        checkoutValidVariants(validCheckouts);
    }

    @Test(groups = {"integration"})
    public void imageVariantsPythonTests() {

        checkPathAndInitService(PATH_PYTHON);

        // make commits
        String[] commits = getCommits();
        makeCommits(commits);

        // extensional correctness - reproduce commits
        checkExtensionalCorrectness(commits, "py");

        // checkout valid variants
        String[] invalidCheckouts = new String[]{
                "person.1",
                "purpleshirt.1",
                "glasses.1",
                "hat.1",
                "stripedshirt.1",
                "jacket.1"
        };

        checkoutInvalidVariants(invalidCheckouts);

        // checkout valid variants
        String[] validCheckouts = new String[]{
                "person.1, purpleshirt.1, glasses.1, hat.1",
                "person.1, purpleshirt.1, glasses.1"
        };

        checkoutValidVariants(validCheckouts);
    }

    // Helper Methods ----------------------------------------------------------------------------------------
    private boolean pythonPluginIsLoaded() {
        return service.getArtifactPlugins().stream().anyMatch(pl -> pl.getName().equals("PythonArtifactPlugin"));
    }

    private void checkPathAndInitService(String repository) {

        repoPath = prepareRepoPath(repository);
        Path p = repoPath.resolve(".ecco");
        Assert.assertFalse(Files.exists(p));

        service.setRepositoryDir(p);
        service.init();

        assertTrue(pythonPluginIsLoaded(), "Python Plugin not loaded ... skipping tests...");
    }

    private String[] getCommits() {
        try (Stream<Path> paths = Files.walk(repoPath, 1)) {
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

    private void makeCommits(String[] commits) {
        for (int i = 0; i < commits.length; i++) {
            service.setBaseDir(repoPath.resolve(commits[i]));

            long startCommit = System.nanoTime();
            service.commit(commits[i]);
            long finishCommit = System.nanoTime();
            long timeElapsed = finishCommit - startCommit;

            accumulatedCommitTime += timeElapsed / 1000000;
            measures.add(new String[]{String.valueOf(i+1), String.valueOf(((float) (timeElapsed / 1000000)) / 1000f), String.valueOf((float) accumulatedCommitTime / 1000f)});

            timemeasure.append("C" + i + ": " + ((float) (timeElapsed / 1000000)) / 1000f + " sec. \n");

            System.out.printf("Commit %d successful\n", i + 1);
        }
    }

    private void checkoutValidVariants(String[] validVariants) {
        checkoutVariants(repoPath.resolve(PATH_INTENSIONAL_VALID), validVariants, "VV");
    }

    private void checkoutInvalidVariants(String[] invalidVariants) {
        checkoutVariants(repoPath.resolve(PATH_INTENSIONAL_INVALID), invalidVariants, "IV");
    }

    private void checkoutVariants(Path repoPath, String[] variants, String shortcut) {
        for (int i = 1; i <= variants.length; i++) {
            String name = shortcut + i + "_" + variants[i - 1].replaceAll("[.][0-9]+", "").replaceAll(", ", "_");
            Path compositionPath = repoPath.resolve(name);

            recreateDir(compositionPath);

            service.setBaseDir(compositionPath);
            service.checkout(variants[i - 1]);

            System.out.printf("Checkout " + shortcut + i + " successful\n");
        }
    }

    private void checkExtensionalCorrectness(String[] commits, String ending) {
        int k = 1;
        for (Commit c : service.getCommits()) {
            System.out.println(c.getConfiguration().toString());

            Path compositionPath = repoPath.resolve(PATH_EXTENSIONAL + "/Commit" + k);

            recreateDir(compositionPath);

            service.setBaseDir(compositionPath);
            service.checkout(c.getConfiguration().toString());
            System.out.printf("Checkout of Commit %d successful\n", k);

            // check all files of certain type
            List<Path> relPaths = Objects.requireNonNull(getRelativePaths(compositionPath, ending));
            for (Path relPath : relPaths) {
                assertTrue(
                        compareFiles(compositionPath.resolve(relPath),
                                repoPath.resolve(commits[k - 1]).resolve(relPath)),
                        "Commit " + k + " extensional correctness test failed for " + relPath);
            }
            k++;
        }
    }

    private void recreateCommitsWithRedundancies(Path repoPath) {
        int k = 1;
        for (Commit c : service.getCommits()) {
            Path compositionPath = repoPath.resolve(PATH_EXTENSIONAL + "_red/Commit" + k);

            recreateDir(compositionPath);

            service.setBaseDir(compositionPath);
            String config = c.getConfiguration().toString();
            if (!config.contains("redundant.1"))
                config += ", redundant.1";

            System.out.println(config);
            service.checkout(config);
            System.out.printf("Checkout of Commit %d successful\n", k);
            k++;
        }
    }
}
