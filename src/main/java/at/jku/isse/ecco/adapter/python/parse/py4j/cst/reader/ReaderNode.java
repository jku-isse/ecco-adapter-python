package at.jku.isse.ecco.adapter.python.parse.py4j.cst.reader;

import at.jku.isse.ecco.adapter.python.data.FieldArtifactData;
import at.jku.isse.ecco.adapter.python.data.TypeArtifactData;
import at.jku.isse.ecco.adapter.python.data.jupyter.JupyterCellArtifactData;
import at.jku.isse.ecco.adapter.python.data.jupyter.JupyterNotebookArtifactData;
import at.jku.isse.ecco.adapter.python.data.jupyter.JupyterLineArtifactData;
import at.jku.isse.ecco.artifact.Artifact;
import at.jku.isse.ecco.artifact.ArtifactData;
import at.jku.isse.ecco.dao.EntityFactory;
import at.jku.isse.ecco.tree.Node;

@SuppressWarnings("unused")
public record ReaderNode(Node.Op node) {

    // provides methods to be called from python reader script
    private static EntityFactory entityFactory;
    private static ReaderEntryPoint readerEntryPoint;

    public static void reset(EntityFactory entityFactory, ReaderEntryPoint readerEntryPoint) {
        ReaderNode.entityFactory = entityFactory;
        ReaderNode.readerEntryPoint = readerEntryPoint;
    }

    // Node ------------------------------------------------------------------------------------------------------------
    private ReaderNode addNode(ArtifactData artifactData) {
        Artifact.Op<?> artifact = entityFactory.createArtifact(artifactData);
        Node.Op child = entityFactory.createNode(artifact);
        readerEntryPoint.nNodes++;

        if (readerEntryPoint.root == null || node == null) {
            readerEntryPoint.root = child;
        } else {
            node.addChild(child);
        }

        return new ReaderNode(child);
    }

    public void makeOrdered() {
        node.getArtifact().setOrdered(true);
    }

    // TypeArtifact ----------------------------------------------------------------------------------------------------
    public ReaderNode addTypeNode() {
        return addNode(new TypeArtifactData());
    }

    public void setBytes(byte[] bytes) throws ClassCastException {
        TypeArtifactData a = (TypeArtifactData) node.getArtifact().getData();
        a.setBytes(bytes);
    }

    // FieldArtifact ---------------------------------------------------------------------------------------------------
    public ReaderNode addFieldNode(String fieldName) {
        return addNode(new FieldArtifactData(fieldName));
    }

    public void setParentFieldName(String parentFieldName) throws ClassCastException {
        FieldArtifactData a = (FieldArtifactData) node.getArtifact().getData();
        a.setParentFieldName(parentFieldName);
    }

    // JupyterNotebookArtifact -----------------------------------------------------------------------------------------
    public ReaderNode addJupyterNotebookNode(int nbformat, int nbformat_minor) {
        return addNode(new JupyterNotebookArtifactData(nbformat, nbformat_minor));
    }

    // JupyterCellArtifact ---------------------------------------------------------------------------------------------
    public ReaderNode addJupyterCellNode(String cell_type) {
        return addNode(new JupyterCellArtifactData(cell_type));
    }

    public void setParseType(String parseType) throws ClassCastException {
        JupyterCellArtifactData a = (JupyterCellArtifactData) node.getArtifact().getData();
        a.setParseType(parseType);
    }

    // JupyterLineArtifact ----------------------------------------------------------------------------------------------------
    public ReaderNode addJupyterLineNode(String line) {
        return addNode(new JupyterLineArtifactData(line));
    }
}
