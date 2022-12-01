package at.jku.isse.ecco.adapter.python.parse.py4j.cst.reader;

import at.jku.isse.ecco.adapter.python.data.*;
import at.jku.isse.ecco.adapter.python.data.jupyter.*;
import at.jku.isse.ecco.artifact.Artifact;
import at.jku.isse.ecco.dao.EntityFactory;
import at.jku.isse.ecco.tree.Node;

import java.nio.file.Path;

public class ReaderEntryPoint {

    private Node.Op root;
    private EntityFactory entityFactory;
    private Path path;
    private int nNodes = 0;

    private Node.Op addNode(Node.Op parent, Artifact.Op<?> artifact){
        Node.Op node = this.entityFactory.createNode(artifact);
        nNodes++;

        if (root == null || parent == null) {
            root = node;
        } else {
            parent.addChild(node);
        }

        return node;
    }

    public Node.Op AddFieldNode(String fieldName, Node.Op parent){
        Artifact.Op<FieldArtifactData> fieldArtifact = this.entityFactory.createArtifact(new FieldArtifactData(fieldName));
        return addNode(parent, fieldArtifact);
    }

    public Node.Op AddTypeNode(Node.Op parent){
        Artifact.Op<TypeArtifactData> typeArtifact = this.entityFactory.createArtifact(new TypeArtifactData());
        return addNode(parent, typeArtifact);
    }

    public Node.Op AddJupyterArtifactNode(Node.Op parent, int nbformat, int nbformat_minor){
        Artifact.Op<JupyterNotebookArtifactData> typeArtifact = this.entityFactory.createArtifact(new JupyterNotebookArtifactData(nbformat, nbformat_minor));
        return addNode(parent, typeArtifact);
    }

    public Node.Op AddJupyterCellNode(Node.Op parent, String cell_type){
        Artifact.Op<JupyterCellArtifactData> typeArtifact = this.entityFactory.createArtifact(new JupyterCellArtifactData(cell_type));
        return addNode(parent, typeArtifact);
    }

    public void setParseType(Node.Op node, String parseType){
        JupyterCellArtifactData a = ((Artifact<JupyterCellArtifactData>) node.getArtifact()).getData();
        a.setParseType(parseType);
    }

    public Node.Op AddLineNode(Node.Op parent, String fieldName){ // TODO type to line?
        Artifact.Op<LineArtifactData> fieldArtifact = this.entityFactory.createArtifact(new LineArtifactData(fieldName));
        return addNode(parent, fieldArtifact);
    }

    public void SetTypeNodeBytes(byte[] bytes,Node.Op node) throws ClassCastException {
        TypeArtifactData a = ((Artifact<TypeArtifactData>) node.getArtifact()).getData();
        a.setBytes(bytes);
    }

    public void setParentFieldName(Node.Op node, String parentFieldName){
        FieldArtifactData a = ((Artifact<FieldArtifactData>) node.getArtifact()).getData();
        a.setParentFieldName(parentFieldName);
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