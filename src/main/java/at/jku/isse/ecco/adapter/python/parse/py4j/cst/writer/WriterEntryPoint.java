package at.jku.isse.ecco.adapter.python.parse.py4j.cst.writer;

import at.jku.isse.ecco.tree.Node;

import java.nio.file.Path;

public class WriterEntryPoint {
    private Node root;
    private Path path;

    public void reset(Path path, Node root) {
        this.root = root;
        this.path = path;
    }

    public WriterNode getRoot() {
        return new WriterNode(root);
    }
}