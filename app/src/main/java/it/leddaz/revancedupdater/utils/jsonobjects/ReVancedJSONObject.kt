package it.leddaz.revancedupdater.utils.jsonobjects

import com.google.gson.annotations.SerializedName

data class ReVancedJSONObject(
    @SerializedName("latestReVancedVersion")
    val latestReVancedVersion: String,
    @SerializedName("latestReVancedHash")
    val latestReVancedHash: String,
    @SerializedName("latestReVancedMusicVersion")
    val latestReVancedMusicVersion: String,
    @SerializedName("latestReVancedMusicHashArm")
    val latestReVancedMusicHashArm: String,
    @SerializedName("latestReVancedMusicHashArm64")
    val latestReVancedMusicHashArm64: String,
    @SerializedName("latestReVancedMusicHashX86")
    val latestReVancedMusicHashX86: String,
    @SerializedName("latestReVancedMusicHashX86_64")
    val latestReVancedMusicHashX86_64: String,
    @SerializedName("latestMicroGVersion")
    val latestMicroGVersion: String,
    @SerializedName("latestDate")
    val latestDate: String
)
