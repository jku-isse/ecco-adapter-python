package at.jku.isse.ecco.adapter.python.parse.py4j.ast.reader;


import at.jku.isse.ecco.adapter.python.PythonParser;
import at.jku.isse.ecco.adapter.python.parse.py4j.PY4JParser;
import at.jku.isse.ecco.dao.EntityFactory;
import at.jku.isse.ecco.tree.Node;

import java.io.*;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;


public class PY4JASTReadParser extends PY4JParser implements PythonParser.Reader {

    public PY4JASTReadParser() {
        PARSER_SCRIPT_NAME = "python_ast_reader.py";
        gateway = new ReaderGateway();
    }

    @Override
    public Node.Op parse(Path path, EntityFactory entityFactory) {
        LOGGER.setLevel(Level.ALL); // TODO remove
        LOGGER.log(Level.INFO, "start parsing {0}", path);
        ReaderGateway readerGateway = (ReaderGateway) gateway;
        readerGateway.reset(path, entityFactory);

        ProcessBuilder parsePython = new ProcessBuilder("python", pythonScript, path.toString());
        Process process = null;

        try {
            process = parsePython.start();
            long tm = System.nanoTime();
            logOutput(process);

            int exitCode = -1;
            if (process.waitFor(MAX_SCRIPT_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                exitCode = process.exitValue();
            } else {
                LOGGER.severe("parsing process timed out after " + MAX_SCRIPT_TIMEOUT_SECONDS + " seconds");
            }

            if (exitCode == 0) {
                LOGGER.log(Level.INFO, "Parce exited normal, code: {0}, {1}ms", new Object[]{exitCode, (System.nanoTime() - tm) / 1000000});
                LOGGER.log(Level.INFO, "created {0} nodes", readerGateway.getNodesCount());

                return readerGateway.getRoot();

            } else {
                LOGGER.severe("Parce exited with code " + exitCode + ":\n" + getStackTrace(process));
            }

        } catch (IOException | InterruptedException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);

        } finally {
            if (process != null) process.destroy();
        }

        return null;
    }
}
