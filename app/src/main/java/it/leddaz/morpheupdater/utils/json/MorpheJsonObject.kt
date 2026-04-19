package it.leddaz.morpheupdater.utils.json

import com.google.gson.annotations.SerializedName

data class MorpheJsonObject(
    @SerializedName("latestYtVersion")
    val latestYtVersion: String,

    @SerializedName("latestYtmVersion")
    val latestYtmVersion: String,

    @SerializedName("latestYtHash")
    val latestYtHash: String,

    @SerializedName("latestYtDate")
    val latestYtDate: String,

    @SerializedName("latestYtmDate")
    val latestYtmDate: String,

    @SerializedName("latestYtmHashArm")
    val latestYtmHashArm: String,

    @SerializedName("latestYtmHashArm64")
    val latestYtmHashArm64: String,

    @SerializedName("latestYtmHashX86")
    val latestYtmHashX86: String,

    @SerializedName("latestYtmHashX86_64")
    val latestYtmHashX64: String
)
