package it.leddaz.revancedupdater.utils.json

import com.google.gson.annotations.SerializedName

data class ReVancedJSONObject(
    @SerializedName("latestReVancedVersion")
    val latestReVancedVersion: String,

    @SerializedName("latestReVancedMusicVersion")
    val latestReVancedMusicVersion: String,

    @SerializedName("latestReVancedHash")
    val latestReVancedHash: String,

    @SerializedName("latestReVancedDate")
    val latestReVancedDate: String,

    @SerializedName("latestReVancedMusicDate")
    val latestReVancedMusicDate: String,

    @SerializedName("latestReVancedMusicHashArm")
    val latestReVancedMusicHashArm: String,

    @SerializedName("latestReVancedMusicHashArm64")
    val latestReVancedMusicHashArm64: String,

    @SerializedName("latestReVancedMusicHashX86")
    val latestReVancedMusicHashX86: String,

    @SerializedName("latestReVancedMusicHashX86_64")
    val latestReVancedMusicHashX64: String
)
