package at.jku.isse.ecco.adapter.python.parse.py4j.cst.reader;

import at.jku.isse.ecco.adapter.python.data.*;
import at.jku.isse.ecco.artifact.Artifact;
import at.jku.isse.ecco.dao.EntityFactory;
import at.jku.isse.ecco.tree.Node;

import java.nio.file.Path;

public class ReaderEntryPoint {

    private Node.Op root;
    private EntityFactory entityFactory;
    private Path path;
    private int nNodes = 0;

    public Node.Op AddFieldNode(String fieldName, Node.Op parent){
        Artifact.Op<FieldArtifactData> fieldArtifact = this.entityFactory.createArtifact(new FieldArtifactData(fieldName));
        Node.Op fieldNode = this.entityFactory.createNode(fieldArtifact);
        parent.addChild(fieldNode);
        nNodes++;
        return fieldNode;
    }

    public Node.Op AddTypeNode(Node.Op parent){
        Artifact.Op<TypeArtifactData> typeArtifact = this.entityFactory.createArtifact(new TypeArtifactData());
        Node.Op typeNode = this.entityFactory.createNode(typeArtifact);
        nNodes++;

        if (root == null || parent == null) {
            root = typeNode;
        } else {
            parent.addChild(typeNode);
        }

        return typeNode;
    }

    public void SetTypeNodeBytes(byte[] bytes,Node.Op node) throws ClassCastException {
        TypeArtifactData a = ((Artifact<TypeArtifactData>) node.getArtifact()).getData();
        a.setBytes(bytes);
    }

    public void MakeOrdered(Node.Op node){
        node.getArtifact().setOrdered(true);
    }


    public void reset(Path path, EntityFactory entityFactory) {
        this.entityFactory = entityFactory;
        this.path = path;
        root = null;
        nNodes = 0;
    }

    public boolean test() {
        return true;
    }

    public Node.Op getRoot() {
        return root;
    }
    public int getNodesCount() {
        return nNodes;
    }
}