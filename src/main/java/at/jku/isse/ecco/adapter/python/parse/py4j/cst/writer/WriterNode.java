package at.jku.isse.ecco.adapter.python.parse.py4j.cst.writer;

import at.jku.isse.ecco.adapter.python.data.FieldArtifactData;
import at.jku.isse.ecco.adapter.python.data.TypeArtifactData;
import at.jku.isse.ecco.adapter.python.data.jupyter.JupyterCellArtifactData;
import at.jku.isse.ecco.adapter.python.data.jupyter.JupyterNotebookArtifactData;
import at.jku.isse.ecco.adapter.python.data.jupyter.JupyterLineArtifactData;
import at.jku.isse.ecco.tree.Node;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
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
        TypeArtifactData a = (TypeArtifactData) node.getArtifact().getData();
        return a.getBytes();
    }

    public WriterNode getField(String fieldName) throws Exception {
        for (WriterNode n : getChildren()) {
            if (n.getFieldArtifactName().equals(fieldName)) {
                return n;
            }
        }
        throw new NoSuchFieldException("Field with name " + fieldName + "not found!");
    }

    // FieldArtifact ---------------------------------------------------------------------------------------------------
    public boolean isField() {
        return node != null && node.getArtifact() != null && node.getArtifact().getData() != null &&
                node.getArtifact().getData() instanceof FieldArtifactData;
    }

    public String getFieldArtifactName() {
        FieldArtifactData d = (FieldArtifactData) node.getArtifact().getData();
        return d.getFieldName();
    }

    public String getParentFieldName() {
        FieldArtifactData a = (FieldArtifactData) node.getArtifact().getData();
        return a.getParentFieldName();
    }

    // JupyterNotebookArtifact -----------------------------------------------------------------------------------------
    public int getNbformat() {
        JupyterNotebookArtifactData a = (JupyterNotebookArtifactData) node.getArtifact().getData();
        return a.getNbFormat();
    }

    public int getNbformat_minor() {
        JupyterNotebookArtifactData a = (JupyterNotebookArtifactData) node.getArtifact().getData();
        return a.getNbFormatMinor();
    }

    // JupyterCellArtifact ---------------------------------------------------------------------------------------------
    public String getCellType() {
        JupyterCellArtifactData a = (JupyterCellArtifactData) node.getArtifact().getData();
        return a.getCellType();
    }

    public String getParseType() {
        JupyterCellArtifactData a = (JupyterCellArtifactData) node.getArtifact().getData();
        return a.getParseType();
    }

    // JupyterLineArtifact ----------------------------------------------------------------------------------------------------
    public String getLine() {
        JupyterLineArtifactData a = (JupyterLineArtifactData) node.getArtifact().getData();
        return a.getLine();
    }
}
