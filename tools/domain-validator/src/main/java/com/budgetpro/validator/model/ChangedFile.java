package com.budgetpro.validator.model;

import java.util.List;
import java.util.Objects;

/**
 * Representa un archivo que ha cambiado en Git.
 */
public class ChangedFile {

    public enum ChangeType {
        MODIFIED, ADDED, DELETED
    }

    private final String filePath;
    private final ChangeType changeType;
    private final List<LineRange> changedLineRanges;

    public ChangedFile(String filePath, ChangeType changeType, List<LineRange> changedLineRanges) {
        this.filePath = filePath;
        this.changeType = changeType;
        this.changedLineRanges = List.copyOf(changedLineRanges);
    }

    public String getFilePath() {
        return filePath;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public List<LineRange> getChangedLineRanges() {
        return changedLineRanges;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ChangedFile that = (ChangedFile) o;
        return Objects.equals(filePath, that.filePath) && changeType == that.changeType
                && Objects.equals(changedLineRanges, that.changedLineRanges);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filePath, changeType, changedLineRanges);
    }

    @Override
    public String toString() {
        return "ChangedFile{" + "filePath='" + filePath + '\'' + ", changeType=" + changeType + ", changedLineRanges="
                + changedLineRanges + '}';
    }
}
