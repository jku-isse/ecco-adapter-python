package at.jku.isse.ecco.adapter.python.parse.py4j.cst.writer;

import at.jku.isse.ecco.adapter.python.data.*;
import at.jku.isse.ecco.adapter.python.data.jupyter.JupyterCellArtifactData;
import at.jku.isse.ecco.adapter.python.data.jupyter.LineArtifactData;
import at.jku.isse.ecco.artifact.Artifact;
import at.jku.isse.ecco.tree.Node;

import java.nio.file.Path;
import java.util.List;

public class WriterEntryPoint {
    private Node root;
    private Path path;
    private int nFiles;

    public void reset(Path path, Node root) {
        this.root = root;
        this.path = path;
        this.nFiles = 0;
    }

    public Node getRoot() {
        return root;
    }

    public int getFilesCount() {
        return nFiles;
    }

    public boolean isOrdered(Node node) {
        return node.getArtifact().isOrdered();
    }

    public boolean isField(Node node) {
        try {
            Artifact<FieldArtifactData> a = (Artifact<FieldArtifactData>) node.getArtifact();
            return true;
        } catch (ClassCastException e) {
            return false;
        }
    }

    public boolean isType(Node node) {
        try {
            TypeArtifactData a = ((Artifact<TypeArtifactData>) node.getArtifact()).getData();
        } catch (ClassCastException e) {
            return false;
        }
        return true;
    }

    public String getCellType(Node node) {
        try {
            JupyterCellArtifactData a = ((Artifact<JupyterCellArtifactData>) node.getArtifact()).getData();
            return a.getCellType();
        } catch (ClassCastException e) {
            return "invalid node type";
        }
    }

    public String getParseType(Node node) {
        JupyterCellArtifactData a = ((Artifact<JupyterCellArtifactData>) node.getArtifact()).getData();
        return a.getParseType();
    }

    public String getLine(Node node){
        try {
            LineArtifactData a = ((Artifact<LineArtifactData>) node.getArtifact()).getData();
            return a.getLine();
        } catch (ClassCastException e) {
            //return false;
            return "not line type";
        }
       // return "";
    }

    public Node getField(String fieldName, Node typeNode) {
        for (Node n : typeNode.getChildren()) {
            if (getFieldArtifactName(n).equals(fieldName)) {
                return n;
            }
        }
        return null;
    }

    public List<? extends Node> getChildren(Node node) {
        return node.getChildren();
    }

    public byte[] getTypeArtifactBytes(Node node) {
        Artifact<TypeArtifactData> a = (Artifact<TypeArtifactData>) node.getArtifact();
        return a.getData().getBytes();
    }

    public String getFieldArtifactName(Node node) {
        Artifact<FieldArtifactData> a = (Artifact<FieldArtifactData>) node.getArtifact();
        return a.getData().getFieldName();
    }
}