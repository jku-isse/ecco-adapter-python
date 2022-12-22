package at.jku.isse.ecco.adapter.python.data.jupyter;

import at.jku.isse.ecco.artifact.ArtifactData;

import java.util.Objects;

public class JupyterLineArtifactData implements ArtifactData {

    private String line;
    public JupyterLineArtifactData(String line) {
        this.line = line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public String getLine() {
        return this.line;
    }

    @Override
    public String toString() {
        return this.line;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.line);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        JupyterLineArtifactData other = (JupyterLineArtifactData) obj;
        if (line == null) {
            return other.line == null;
        } else return line.equals(other.line);
    }
}