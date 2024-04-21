package it.leddaz.revancedupdater.utils.misc

import kotlin.math.max

/**
 * Class that represents a version.
 */
class Version(version: String?) : Comparable<Version?> {
    var version: String? = null

    /**
     * Constructor method.
     */
    init {
        setVersion(this, version)
    }

    /**
     * Compare two versions.
     *
     * @param other the other version
     * @return 0 if the versions are equal, 1 if the first version is newer,
     * -1 if the second one is newer
     */
    override fun compareTo(other: Version?): Int {
        if (other == null) return 1
        val thisParts =
            version!!.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val thatParts = other.version!!.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        val length = max(thisParts.size, thatParts.size)
        for (i in 0 until length) {
            val thisPart = if (i < thisParts.size) thisParts[i].toInt() else 0
            val thatPart = if (i < thatParts.size) thatParts[i].toInt() else 0
            if (thisPart < thatPart) return -1
            if (thisPart > thatPart) return 1
        }
        return 0
    }

    /**
     * Declares if two versions are equal
     *
     * @param other the other version
     * @return true if the versions are equal, false otherwise
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        return if (this.javaClass != other.javaClass) false else this.compareTo(other as Version?) == 0
    }

    /**
     * @return A string representation of the version
     */
    override fun toString(): String {
        return version!!
    }

    override fun hashCode(): Int {
        return version?.hashCode() ?: 0
    }

    companion object {
        /**
         * Sets the version.
         *
         * @param obj Version instance to set the version on
         * @param version version string
         */
        fun setVersion(obj: Version, version: String?) {
            requireNotNull(version) { "Version can not be null" }
            require(version.matches("\\d+(\\.\\d+)*".toRegex())) { "Invalid version format" }
            obj.version = version
        }
    }
}
