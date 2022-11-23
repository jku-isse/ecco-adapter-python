package at.jku.isse.ecco.adapter.python.parse.py4j.ast.reader;

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

    public Node.Op CreateClassNode(String className) {
        Artifact.Op<ClassArtifactData> classArtifact = this.entityFactory.createArtifact(new ClassArtifactData(className));
        Node.Op classNode = this.entityFactory.createNode(classArtifact);
        nNodes++;

        if (root == null) {
            root = classNode;
        }

        return classNode;
    }

    public Node.Op AddFieldNode(String fieldName, Node.Op parent){
        Artifact.Op<FieldArtifactData> fieldArtifact = this.entityFactory.createArtifact(new FieldArtifactData(fieldName));
        Node.Op fieldNode = this.entityFactory.createNode(fieldArtifact);
        parent.addChild(fieldNode);
        nNodes++;
        return fieldNode;
    }

    public Node.Op AddIntNode(int value, Node.Op parent){
        Artifact.Op<IntArtifactData> intArtifact = this.entityFactory.createArtifact(new IntArtifactData(value));
        Node.Op intNode = this.entityFactory.createNode(intArtifact);
        parent.addChild(intNode);
        nNodes++;
        return intNode;
    }

    public Node.Op AddStringNode(String string, Node.Op parent){
        Artifact.Op<StringArtifactData> stringArtifact = this.entityFactory.createArtifact(new StringArtifactData(string));
        Node.Op stringNode = this.entityFactory.createNode(stringArtifact);
        parent.addChild(stringNode);
        nNodes++;
        return stringNode;
    }

    public Node.Op AddDumpNode(byte[] bytes, Node.Op parent){
        Artifact.Op<DumpArtifactData> dumpArtifact = this.entityFactory.createArtifact(new DumpArtifactData(bytes));
        Node.Op dumpNode = this.entityFactory.createNode(dumpArtifact);
        parent.addChild(dumpNode);
        nNodes++;
        return null;
    }

    public Node.Op AddTypeNode(Node.Op parent){
        Artifact.Op<TypeArtifactData> typeArtifact = this.entityFactory.createArtifact(new TypeArtifactData());
        Node.Op typeNode = this.entityFactory.createNode(typeArtifact);
        nNodes++;

        if (root == null) {
            root = typeNode;
        } else {
            parent.addChild(typeNode);
        }

        return typeNode;
    }

    public void SetTypeNodeBytes(byte[] bytes,Node.Op node) throws ClassCastException {
//        try {
            TypeArtifactData a = ((Artifact<TypeArtifactData>) node.getArtifact()).getData();
            a.setBytes(bytes);
//        }catch (ClassCastException e) {
//            return false;
//        }
    }



    public void MakeOrdered(Node.Op node){
        node.getArtifact().setOrdered(true);
    }

    public Node.Op AddClassNode(String className, Node.Op parent){
        Artifact.Op<ClassArtifactData> classArtifact = this.entityFactory.createArtifact(new ClassArtifactData(className));
        Node.Op classNode = this.entityFactory.createNode(classArtifact);
        if (parent == null){
            if(root == null){
                root = classNode;
            } else{
                System.out.println("No parent node passed for add new ClassNode");
                //throw new RuntimeException("No parent node passed for add new ClassNode");
            }
        }else {
            parent.addChild(classNode);
        }
        nNodes++;
        return classNode;
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

//    public int getMaxDepth() {
//        return 0;
//    }

}