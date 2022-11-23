package at.jku.isse.ecco.adapter.python.parse.py4j.ast.writer;

import at.jku.isse.ecco.adapter.python.data.*;
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

    public Node getRoot(){
        return root;
    }
    public int getFilesCount() {
        return nFiles;
    }

    public List<? extends Node> getChildren(Node node){
        return root.getChildren();
    }

    public boolean isOrdered(Node node){
        return node.getArtifact().isOrdered();
    }

    public boolean isField(Node node){
        try {
            Artifact<FieldArtifactData> a = (Artifact<FieldArtifactData>) node.getArtifact();
            return true;
        }catch (ClassCastException e) {
            return false;
        }
    }

    public boolean isClass(Node node){
        try {
            ClassArtifactData a = ((Artifact<ClassArtifactData>) node.getArtifact()).getData();
            return true;
        }catch (ClassCastException e) {
            return false;
        }
    }

    public boolean isDump(Node node){
        try {
            DumpArtifactData a = ((Artifact<DumpArtifactData>) node.getArtifact()).getData();
        }catch (ClassCastException e) {
            return false;
        }
        return true;
    }

    public boolean isType(Node node){
        try {
            TypeArtifactData a = ((Artifact<TypeArtifactData>) node.getArtifact()).getData();
        }catch (ClassCastException e) {
            return false;
        }
        return true;
    }

    public byte[] geTypeArtifactBytes(Node node){
        Artifact<TypeArtifactData> a = (Artifact<TypeArtifactData>) node.getArtifact();
        return a.getData().getBytes();
    }


    public boolean isString(Node node){
        try {
            StringArtifactData a = ((Artifact<StringArtifactData>) node.getArtifact()).getData();
        }catch (ClassCastException e) {
            return false;
        }
        return true;
    }

    public boolean isInt(Node node){
        try {
            IntArtifactData a = ((Artifact<IntArtifactData>) node.getArtifact()).getData();
        }catch (ClassCastException e) {
            return false;
        }
        return true;
    }

    public int getIntArtifactValue(Node node){
        Artifact<IntArtifactData> a = (Artifact<IntArtifactData>) node.getArtifact();
        return a.getData().getValue();
    }

    public String getStringArtifactString(Node node){
        Artifact<StringArtifactData> a = (Artifact<StringArtifactData>) node.getArtifact();
        return a.getData().getString();
    }

    public String getClassArtifactName(Node node){
        Artifact<ClassArtifactData> a = (Artifact<ClassArtifactData>) node.getArtifact();
        return a.getData().getClassName();
    }

    public String getFieldArtifactName(Node node){
        Artifact<FieldArtifactData> a = (Artifact<FieldArtifactData>) node.getArtifact();
        return a.getData().getFieldName();
    }

    public byte[] getDumpArtifactBytes(Node node){
        Artifact<DumpArtifactData> a = (Artifact<DumpArtifactData>) node.getArtifact();
        return a.getData().getBytes();
    }
}