package it.leddaz.revancedupdater.utils.misc;

import androidx.annotation.NonNull;

/**
 * Class that represents a version.
 */
public class Version implements Comparable<Version> {

    private final String version;

    /**
     * Returns the version
     *
     * @return Version
     */
    public final String get() {
        return this.version;
    }

    /**
     * Constructor method.
     *
     * @param version version string
     */
    public Version(String version) {
        if (version == null)
            throw new IllegalArgumentException("Version can not be null");
        if (!version.matches("\\d+(\\.\\d+)*"))
            throw new IllegalArgumentException("Invalid version format");
        this.version = version;
    }

    /**
     * Compare two versions.
     *
     * @param that the other version
     * @return 0 if the versions are equal, 1 if the first version is newer,
     * -1 if the second one is newer
     */
    @Override
    public int compareTo(Version that) {
        if (that == null)
            return 1;
        String[] thisParts = this.get().split("\\.");
        String[] thatParts = that.get().split("\\.");
        int length = Math.max(thisParts.length, thatParts.length);
        for (int i = 0; i < length; i++) {
            int thisPart = i < thisParts.length ?
                    Integer.parseInt(thisParts[i]) : 0;
            int thatPart = i < thatParts.length ?
                    Integer.parseInt(thatParts[i]) : 0;
            if (thisPart < thatPart)
                return -1;
            if (thisPart > thatPart)
                return 1;
        }
        return 0;
    }

    /**
     * Declares if two versions are equal
     *
     * @param that misc version
     * @return true if the versions are equal, false otherwise
     */
    @Override
    public boolean equals(Object that) {
        if (this == that)
            return true;
        if (that == null)
            return false;
        if (this.getClass() != that.getClass())
            return false;
        return this.compareTo((Version) that) == 0;
    }

    /**
     * @return A string representation of the version
     */
    @NonNull
    public String toString() {
        return version;
    }

}
