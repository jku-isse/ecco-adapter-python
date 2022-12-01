package at.jku.isse.ecco.adapter.python.parse.py4j.cst.writer;

import at.jku.isse.ecco.adapter.python.data.FieldArtifactData;
import at.jku.isse.ecco.adapter.python.data.TypeArtifactData;
import at.jku.isse.ecco.adapter.python.data.jupyter.JupyterCellArtifactData;
import at.jku.isse.ecco.adapter.python.data.jupyter.LineArtifactData;
import at.jku.isse.ecco.artifact.Artifact;
import at.jku.isse.ecco.tree.Node;

import java.io.InvalidClassException;
import java.util.ArrayList;
import java.util.List;

public record WriterNode(Node node) {

    // provides methods to be called from python writer script

    // Node ------------------------------------------------------------------------------------------------------------
    public boolean isOrdered() {
        return node.getArtifact().isOrdered();
    }

    public List<WriterNode> getChildren() {
        List<WriterNode> list = new ArrayList<>();
        for (Node n : node.getChildren()) {
            list.add(new WriterNode((n)));
        }
        return list;
    }

    // TypeArtifact ----------------------------------------------------------------------------------------------------
    public boolean isType() {
        return node != null && node.getArtifact() != null && node.getArtifact().getData() != null &&
                node.getArtifact().getData() instanceof TypeArtifactData;
    }

    public byte[] getTypeArtifactBytes() {
        Artifact<TypeArtifactData> a = (Artifact<TypeArtifactData>) node.getArtifact();
        return a.getData().getBytes();
    }

    public WriterNode getField(String fieldName) throws InvalidClassException {
        for (WriterNode n : getChildren()) {
            if (n.getFieldArtifactName().equals(fieldName)) {
                return n;
            }
        }
        throw new NullPointerException("Field with name " + fieldName + "not found!");
        //throw new InvalidArtifactTypeException(TypeArtifactData.class);
    }

    // FieldArtifact ---------------------------------------------------------------------------------------------------
    public boolean isField() {
        return node != null && node.getArtifact() != null && node.getArtifact().getData() != null &&
                node.getArtifact().getData() instanceof FieldArtifactData;
    }

    public String getFieldArtifactName() {
        Artifact<FieldArtifactData> a = (Artifact<FieldArtifactData>) node.getArtifact();
        return a.getData().getFieldName();
    }

    public String getParentFieldName() {
        FieldArtifactData a = ((Artifact<FieldArtifactData>) node.getArtifact()).getData();
        return a.getParentFieldName();
    }

    // JupyterCellArtifact ---------------------------------------------------------------------------------------------
    public String getCellType() throws InvalidArtifactTypeException {
        try {
            JupyterCellArtifactData a = ((Artifact<JupyterCellArtifactData>) node.getArtifact()).getData();
            return a.getCellType();
        } catch (ClassCastException e) {
            throw new InvalidArtifactTypeException(JupyterCellArtifactData.class);
        }
    }

    public String getParseType() {
        JupyterCellArtifactData a = ((Artifact<JupyterCellArtifactData>) node.getArtifact()).getData();
        return a.getParseType();
    }

    // LineArtifact ----------------------------------------------------------------------------------------------------
    public String getLine() throws InvalidArtifactTypeException {
        try {
            LineArtifactData a = ((Artifact<LineArtifactData>) node.getArtifact()).getData();
            return a.getLine();
        } catch (ClassCastException e) {
            throw new InvalidArtifactTypeException(LineArtifactData.class);
        }
    }

    private class InvalidArtifactTypeException extends InvalidClassException {
        public <T> InvalidArtifactTypeException(Class<T> expected) {
            super("Invalid Artifact type (expected " + expected.getSimpleName() + ", but got " +
                    node.getArtifact().getData().getClass().getSimpleName() + ")");
        }
    }
}
