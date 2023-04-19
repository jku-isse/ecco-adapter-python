package at.jku.isse.ecco.adapter.python.test;

import at.jku.isse.ecco.core.Commit;
import at.jku.isse.ecco.service.EccoService;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static at.jku.isse.ecco.adapter.python.test.PythonAdapterTestUtil.*;
import static org.testng.Assert.assertTrue;

public class PythonAdapterRepositoryTest {

    private EccoService service;

    @BeforeTest(groups = {"integration"})
    public void setUpEccoService() {
        service = new EccoService();
    }

    @AfterMethod(groups = {"integration"})
    public void closeEccoService() {
        service.close();
    }

    private boolean pythonPluginIsLoaded() {
        return service.getArtifactPlugins().stream().anyMatch(pl -> pl.getName().equals("PythonArtifactPlugin"));
    }

    @Test(groups = {"integration"})
    public void populatePythonTests() {

        Path repoPath = prepareRepoPath("python_variants");

        Path p = repoPath.resolve(".ecco");
        Assert.assertFalse(Files.exists(p));

        service.setRepositoryDir(p);
        service.init();

        assertTrue(pythonPluginIsLoaded(), "Python Plugin not loaded ... skipping tests...");

        // make commits
        String[] commits = new String[]{
                "C1_purpleshirt",
                "C2_stripedshirt",
                "C3_purpleshirt_jacket",
                "C4_purpleshirt_jacket_glasses",
                "C5_stripedshirt_jacket_glasses",
                "C6_stripedshirt_glasses",
                "C7_purpleshirt_glasses",
                "C8_stripedshirt_jacket",
                "C9_stripedshirt_jacket_hat"
        };

        for (int i = 0; i < commits.length; i++) {
            service.setBaseDir(repoPath.resolve(commits[i]));
            service.commit(commits[i]);

            System.out.printf("Commit %d successful\n", i + 1);
        }

        // extensional correctness - reproduce commits
        checkExtensionalCorrectness(repoPath, commits, "py");

        // checkout valid variants
        String[] checkouts = new String[]{
                "person.1, purpleshirt.1, glasses.1, hat.1",
        };

        checkoutValidVariants(repoPath, checkouts);

        // checkout valid variants
        String[] invalidCheckouts = new String[]{
                "person.1",
                "purpleshirt.1",
                "jacket.1",
                "stripedshirt.1",
                "glasses.1",
                "hat.1"
        };

        checkoutInvalidVariants(repoPath, invalidCheckouts);
    }

    private Path prepareRepoPath(String folder) {
        Path cwd = Path.of(System.getProperty("user.dir"));
        Path repoPath = cwd.resolve("src/integrationTest/resources/data").resolve(folder);
        Path p = repoPath.resolve(".ecco");
        deleteDir(p);
        return repoPath;
    }

    @Test(groups = {"integration"})
    public void populateJupyterTests() {

        Path repoPath = prepareRepoPath("jupyter_variants");

        Path p = repoPath.resolve(".ecco");
        Assert.assertFalse(Files.exists(p));

        service.setRepositoryDir(p);
        service.init();

        assertTrue(pythonPluginIsLoaded(), "Python Plugin not loaded ... skipping tests...");

        // make commits
        String[] commits = new String[]{
                "C1_initial_commit",
                "C2_new_dataset",
                "C3_adjusted_resolution",
                "C4_switched_to_smaller_model",
                "C5_added_tensorboard_logging",
                "C6_new_hyperparameter_set",
                "C7_removed_email_notification",
        };

        for (int i = 0; i < commits.length; i++) {
            service.setBaseDir(repoPath.resolve(commits[i]));
            service.commit(commits[i]);

            System.out.printf("Commit %d successful\n", i + 1);
        }

        // extensional correctness - reproduce commits
        checkExtensionalCorrectness(repoPath, commits, "ipynb");

        // checkout valid variants
        String[] invalidCheckouts = new String[]{
                "train.1, resolution.1",
                "dataset.2, export.1, log.1"
        };

        checkoutInvalidVariants(repoPath, invalidCheckouts);

        // checkout valid variants
        String[] validCheckouts = new String[]{
                "train.1, resolution.1, parameters.1, weights.1, dataset.1, export.1, notify.1, log.1",
                "train.1, resolution.2, parameters.2, weights.2, dataset.2, export.1",
                "train.1, resolution.2, parameters.2, weights.2, dataset.1, export.1, log.1, notify.1",
        };

        checkoutValidVariants(repoPath, validCheckouts);

    }

    public List<Path> getRelativePaths(Path folder, String ending) {
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

    private void checkoutValidVariants(Path repoPath, String[] variants) {
        checkoutVariants(repoPath.resolve("intensional_correctness_valid"), variants, "VV");
    }

    private void checkoutInvalidVariants(Path repoPath, String[] variants) {
        checkoutVariants(repoPath.resolve("intensional_correctness_invalid"), variants, "IV");
    }

    private void checkoutVariants(Path repoPath, String[] variants, String shortcut) {
        for (int i = 0; i < variants.length; i++) {
            String name = shortcut + i + "_" + variants[i].replaceAll("[.][0-9]+", "").replaceAll(", ", "_");
            Path compositionPath = repoPath.resolve(name);

            recreateDir(compositionPath);

            service.setBaseDir(compositionPath);
            service.checkout(variants[i]);

            System.out.printf("Valid Checkout %d successful\n", i + 1);
        }
    }

    private void checkExtensionalCorrectness(Path repoPath, String[] commits, String ending) {
        int k = 1;
        for (Commit c : service.getCommits()) {
            System.out.println(c.getConfiguration().toString());

            Path compositionPath = repoPath.resolve("extensional_correctness_check/Commit" + k);

            recreateDir(compositionPath);

            service.setBaseDir(compositionPath);
            service.checkout(c.getConfiguration().toString());
            System.out.printf("Checkout of Commit %d successful\n", k);

            // check all jupyter-files
            List<Path> relPaths = getRelativePaths(compositionPath, ending);
            for (Path relPath : relPaths) {
                assertTrue(
                        compareFiles(compositionPath.resolve(relPath), repoPath.resolve(commits[k - 1]).resolve(relPath)), "Commit " + k + " intentional Correctness test failed for " + relPath);
            }
            k++;
        }
    }
}
