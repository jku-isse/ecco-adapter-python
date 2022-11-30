package at.jku.isse.ecco.adapter.python.data.jupyter;

import at.jku.isse.ecco.artifact.ArtifactData;

import java.util.Objects;

public class JupyterNotebookArtifactData implements ArtifactData {

    private int nbformat;
    private int nbformat_minor;

    public JupyterNotebookArtifactData(int nbformat, int nbformat_minor) {
        this.nbformat = nbformat;
        this.nbformat_minor = nbformat_minor;
    }

    public void setNbformat(int nbformat) {
        this.nbformat = nbformat;
    }

    public int getNbformat() {
        return this.nbformat;
    }

    public int getNbformat_minor() {
        return this.nbformat_minor;
    }

    @Override
    public String toString() {
        return "nbformat: " + this.nbformat + ", nbformat_minor: " + this.nbformat_minor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.nbformat, this.nbformat_minor);
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
        return (nbformat == other.nbformat && nbformat_minor == other.nbformat_minor);
    }
}