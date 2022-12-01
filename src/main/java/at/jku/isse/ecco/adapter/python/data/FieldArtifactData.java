package at.jku.isse.ecco.adapter.python.data;

import at.jku.isse.ecco.artifact.ArtifactData;

import java.util.Objects;

public class FieldArtifactData implements ArtifactData {

    private String parentFieldName;
    private String fieldName;

    public FieldArtifactData(String fieldName) {
        this.fieldName = fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return this.fieldName;
    }

    public void setParentFieldName(String parentFieldName) {
        this.parentFieldName = parentFieldName;
    }

    public String getParentFieldName() {
        return this.parentFieldName;
    }

    @Override
    public String toString() {
        return this.fieldName;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.fieldName);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FieldArtifactData other = (FieldArtifactData) obj;
        if (fieldName == null) {
            if (other.fieldName != null)
                return false;
        } else if (!fieldName.equals(other.fieldName))
            return false;
        return true;
    }
}