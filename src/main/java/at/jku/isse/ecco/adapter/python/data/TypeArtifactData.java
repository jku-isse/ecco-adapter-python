package at.jku.isse.ecco.adapter.python.data;

import at.jku.isse.ecco.artifact.ArtifactData;

import java.util.Arrays;

public class TypeArtifactData implements ArtifactData {

    private byte[] bytes;

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte[] getBytes() {
        return this.bytes;
    }

    @Override
    public String toString() {
        return Arrays.toString(this.bytes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.bytes);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TypeArtifactData other = (TypeArtifactData) obj;
        if (bytes == null) {
            return other.bytes == null;
        } else return Arrays.equals(bytes, other.bytes);
    }
}