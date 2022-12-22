package at.jku.isse.ecco.adapter.python.data.jupyter;

import at.jku.isse.ecco.artifact.ArtifactData;

import java.util.Objects;

public class JupyterNotebookArtifactData implements ArtifactData {

    private int nbFormat;
    private int nbFormatMinor;

    public JupyterNotebookArtifactData(int nbFormat, int nbFormatMinor) {
        this.nbFormat = nbFormat;
        this.nbFormatMinor = nbFormatMinor;
    }

    public void setNbFormat(int nbFormat) {
        this.nbFormat = nbFormat;
    }

    public int getNbFormat() {
        return this.nbFormat;
    }

    public void setNbFormatMinor(int nbFormatMinor) {
        this.nbFormatMinor = nbFormatMinor;
    }

    public int getNbFormatMinor() {
        return this.nbFormatMinor;
    }

    @Override
    public String toString() {
        return "JupyterNotebookArtifactData(nbformat: " + this.nbFormat + ", nbformat_minor: " + this.nbFormatMinor + ")";
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.nbFormat, this.nbFormatMinor);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        JupyterNotebookArtifactData other = (JupyterNotebookArtifactData) obj;
        return (nbFormat == other.nbFormat && nbFormatMinor == other.nbFormatMinor);
    }
}