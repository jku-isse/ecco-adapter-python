package at.jku.isse.ecco.adapter.python.data.ast;

import at.jku.isse.ecco.artifact.ArtifactData;

import java.util.Objects;

public class IntArtifactData implements ArtifactData {

    private int value;

    public IntArtifactData(int value) {
        this.value = value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return String.valueOf(this.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        IntArtifactData other = (IntArtifactData) obj;
        return value == other.value;
//        if (value == null) {
//            if (other.value != null)
//                return false;
//        } else if (value != other.value))
//            return false;
//        return true;
    }
}