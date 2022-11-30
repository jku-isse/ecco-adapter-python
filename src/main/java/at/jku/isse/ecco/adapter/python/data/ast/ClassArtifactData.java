package at.jku.isse.ecco.adapter.python.data.ast;

import at.jku.isse.ecco.artifact.ArtifactData;

import java.util.Objects;

public class ClassArtifactData implements ArtifactData {
    private String className;
    public ClassArtifactData(String className) {
        this.className = className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassName() {
        return this.className;
    }

    @Override
    public String toString() {
        return this.className;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.className);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ClassArtifactData other = (ClassArtifactData) obj;
        if (className == null) {
            if (other.className != null)
                return false;
        } else if (!className.equals(other.className))
            return false;
        return true;
    }
}