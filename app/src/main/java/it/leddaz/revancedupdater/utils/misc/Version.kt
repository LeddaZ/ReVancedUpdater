package it.leddaz.revancedupdater.utils.misc;

import androidx.annotation.NonNull;

/**
 * Class that represents a version.
 */
public class Version implements Comparable<Version> {

    private String version;

    /**
     * Constructor method.
     *
     * @param version version string
     */
    public Version(String version) {
        setVersion(version);
    }

    /**
     * Returns the version
     *
     * @return Version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the version.
     *
     * @param version version string
     */
    public void setVersion(String version) {
        if (version == null)
            throw new IllegalArgumentException("Version can not be null");
        if (!version.matches("\\d+(\\.\\d+)*"))
            throw new IllegalArgumentException("Invalid version format");
        this.version = version;
    }

    /**
     * Compare two versions.
     *
     * @param other the other version
     * @return 0 if the versions are equal, 1 if the first version is newer,
     * -1 if the second one is newer
     */
    @Override
    public int compareTo(Version other) {
        if (other == null)
            return 1;
        String[] thisParts = this.getVersion().split("\\.");
        String[] thatParts = other.getVersion().split("\\.");
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
     * @param other the other version
     * @return true if the versions are equal, false otherwise
     */
    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other == null)
            return false;
        if (this.getClass() != other.getClass())
            return false;
        return this.compareTo((Version) other) == 0;
    }

    /**
     * @return A string representation of the version
     */
    @NonNull
    public String toString() {
        return version;
    }

}
