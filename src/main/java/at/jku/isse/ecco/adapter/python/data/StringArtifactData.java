package at.jku.isse.ecco.adapter.python.data;

import at.jku.isse.ecco.artifact.ArtifactData;

import java.util.Objects;

public class StringArtifactData implements ArtifactData {

    private String string;
    public StringArtifactData(String string) {
        this.string = string;
    }

    public void setString(String string) {
        this.string = string;
    }

    public String getString() {
        return this.string;
    }

    @Override
    public String toString() {
        return this.string;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.string);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StringArtifactData other = (StringArtifactData) obj;
        if (string == null) {
            if (other.string != null)
                return false;
        } else if (!string.equals(other.string))
            return false;
        return true;
    }
}