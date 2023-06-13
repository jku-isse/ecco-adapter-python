package at.jku.isse.ecco.adapter.python.test;

import at.jku.isse.ecco.adapter.python.PythonPlugin;
import at.jku.isse.ecco.core.Commit;
import at.jku.isse.ecco.service.EccoService;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;

import static at.jku.isse.ecco.adapter.python.test.IntegrationTestUtil.*;
import static at.jku.isse.ecco.adapter.python.test.PythonAdapterTestUtil.*;
import static org.testng.Assert.assertTrue;

public class PythonAdapterIntegrationTest {

    private Path repoPath;
    private EccoService service;
    private Logger logger;
    private PythonAdapterIntegrationTestLogger log;

    private void warmupJava(){
        service = new EccoService();
        checkPathInitService(PATH_POMMERMAN);
        String commit = getCommits(repoPath)[0]; // make 1st commit of Pommerman (26 files)
        service.setBaseDir(repoPath.resolve(commit));
        service.commit(commit);
        service.close();
    }

    @BeforeTest(groups = {"integration"})
    public void setUpEccoService() {
        warmupJava();
        service = new EccoService();
        logger = Logger.getLogger(PythonPlugin.class.getName());
    }

    @BeforeMethod(groups = {"integration"})
    public void initMeasures() {
        log = new PythonAdapterIntegrationTestLogger();
    }

    @AfterMethod(groups = {"integration"})
    public void closeEccoService() {
        service.close();
        finishLogging(repoPath, log);
    }

    @Test(groups = {"integration"})
    public void pythonTests() {

        preparePathAndEnableLogging(PATH_PYTHON);

        // make commits
        String[] commits = getCommits(repoPath);
        makeCommits(commits);

        // extensional correctness - reproduce commits
        checkExtensionalCorrectness(commits, EXT_PYTHON);

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

        logPythonDetails(repoPath, log);
    }

    @Test(groups = {"integration"})
    public void jupyterTests() {

        preparePathAndEnableLogging(PATH_JUPYTER);

        // make commits
        String[] commits = getCommits(repoPath);
        makeCommits(commits);

        // extensional correctness - reproduce commits
        checkExtensionalCorrectness(commits, EXT_JUPYTER);

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
        logJuypterDetails(repoPath, log);
    }

    @Test(groups = {"integration"}) //, enabled=false
    public void pommermanTests() {

        preparePathAndEnableLogging(PATH_POMMERMAN);

        // make commits
        String[] commits = getCommits(repoPath);
        makeCommits(commits);

        // extensional correctness - reproduce commits
        checkExtensionalCorrectness(commits, EXT_PYTHON);

        // checkout valid variants
        String[] invalidCheckouts = new String[]{
                "framework.2, learning.4, dqnmodel.1, simplevslearning.1", // invalid algo-model combination
                "framework.1, heuristic.8, ppomodel.2, simplevsheuristic.1", // invalid algo-model combination
        };

        checkoutInvalidVariants(invalidCheckouts);

        // checkout valid variants
        String[] validCheckouts = new String[]{
                "framework.2, learning.3, dqnmodel.2, simplevslearning.1",
                "framework.2, learning.4, ppomodel.2, simplevslearning.1",
                "framework.2, heuristic.10, simplevsheuristic.1",
        };

        checkoutValidVariants(validCheckouts);
        logPythonDetails(repoPath, log);
    }

    @Test(groups = {"integration"}) //, enabled=false
    public void pommermanPerformanceTests() {

        preparePathAndEnableLogging(PATH_POMMERMAN_FAST);

        // make commits
        String[] commits = getCommits(repoPath);
        makeCommits(commits);

        // extensional correctness - reproduce commits
        checkExtensionalCorrectness(commits, EXT_PYTHON);
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
                "framework.2, redundant.1, heuristic.10, simplevsheuristic.1",
        };

        checkoutValidVariants(validCheckouts);
        logPythonDetails(repoPath, log);
    }

    @Test(groups = {"integration"})
    public void imageVariantsPythonTests() {

        preparePathAndEnableLogging(PATH_PYTHON);

        // make commits
        String[] commits = getCommits(repoPath);
        makeCommits(commits);

        // extensional correctness - reproduce commits
        checkExtensionalCorrectness(commits, EXT_PYTHON);

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


    private void checkPathInitService(String repository) {
        Path cwd = Path.of(System.getProperty("user.dir"));
        repoPath = cwd.resolve("src/integrationTest/resources/data").resolve(repository);
        Path p = repoPath.resolve(".ecco");
        deleteDir(p);
        Assert.assertFalse(Files.exists(p));
        service.setRepositoryDir(p);
        service.init();

        assertTrue(pythonPluginIsLoaded(), "Python Plugin not loaded ... skipping tests...");
    }

    private void preparePathAndEnableLogging(String repository) {
        checkPathInitService(repository);
        enableLoggingToFile(repoPath);
    }

    private void makeCommits(String[] commits) {
        long accumulatedTime = 0;
        log.add(0, "Config","commitECCOTime", "accumulatedCommitECCOTime");
        for (int i = 0; i < commits.length; i++) {
            Path commitPath = repoPath.resolve(commits[i]);
            service.setBaseDir(commitPath);
            String configString = service.getConfigStringFromFile(commitPath);

            String finalCommit = commits[i];
            long timeElapsed = measureTime(() ->  service.commit(finalCommit));

            accumulatedTime += timeElapsed / 1000000;
            log.add(i + 1,
                    configString,
                    String.valueOf(((float) (timeElapsed / 1000000)) / 1000f),
                    String.valueOf((float) accumulatedTime / 1000f));

            logger.info("Commit " + (i + 1) + " successful");
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

            logger.info("Checkout " + shortcut + i + " successful");
        }
    }

    private void checkExtensionalCorrectness(String[] commits, String ending) {
        log.add(0, "CheckoutECCOTime", "accumulatedCheckoutECCOTime");
        long accumulatedTime = 0;
        int k = 1;
        for (Commit c : service.getCommits()) {
            System.out.println(c.getConfiguration().toString());

            Path compositionPath = repoPath.resolve(PATH_EXTENSIONAL + "/Commit" + k);

            recreateDir(compositionPath);

            service.setBaseDir(compositionPath);
            String config = c.getConfiguration().toString();

            long timeElapsed = measureTime(() -> service.checkout(config));
            accumulatedTime += timeElapsed / 1000000;
            log.add(k,
                    String.valueOf(((float) (timeElapsed / 1000000)) / 1000f),
                    String.valueOf((float) accumulatedTime / 1000f));

            logger.info("Checkout of Commit  " + k + " successful");

            // check all files of certain type
            List<Path> relPaths = Objects.requireNonNull(getRelativeFilePaths(compositionPath, ending));
            for (Path relPath : relPaths) {
                assertTrue(
                        compareFiles(compositionPath.resolve(relPath),
                                repoPath.resolve(commits[k - 1]).resolve(relPath)),
                        "Commit " + k + " extensional correctness test failed for " + relPath);
            }
            k++;
        }
    }

    private long measureTime(Runnable runnable) {
        long startCheckout = System.nanoTime();
        runnable.run();
        long finishCheckout = System.nanoTime();
        return finishCheckout - startCheckout;
    }

    private void recreateCommitsWithRedundancies(Path repoPath) {
        log.add(0, "redCheckoutECCOTime", "accumulatedRedCheckoutECCOTime", "redConfig");
        int k = 1;
        long accumulatedTime = 0;
        for (Commit c : service.getCommits()) {
            Path compositionPath = repoPath.resolve(PATH_EXTENSIONAL + "_red/Commit" + k);

            recreateDir(compositionPath);

            service.setBaseDir(compositionPath);
            String config = c.getConfiguration().toString();
            if (!config.contains("redundant.1"))
                config += ", redundant.1";

            System.out.println(config);

            String finalConfig = config;
            long timeElapsed = measureTime(() -> service.checkout(finalConfig));

            accumulatedTime += timeElapsed / 1000000;
            log.add(k,
                    String.valueOf(((float) (timeElapsed / 1000000)) / 1000f),
                    String.valueOf((float) accumulatedTime / 1000f),
                    config);

            System.out.printf("Checkout of Commit %d (with redundancy) successful\n", k);
            k++;
        }
    }
}
