package it.leddaz.revancedupdater.utils.jsonobjects

import com.google.gson.annotations.SerializedName

data class ReVancedJSONObject(
    @SerializedName("latestReVancedVersion")
    val latestReVancedVersion: String,
    @SerializedName("latestReVancedHash")
    val latestReVancedHash: String,
    @SerializedName("latestReVancedMusicVersion")
    val latestReVancedMusicVersion: String,
    @SerializedName("latestReVancedMusicHash")
    val latestReVancedMusicHash: String,
    @SerializedName("latestMicroGVersion")
    val latestMicroGVersion: String,
    @SerializedName("downloadUrl")
    val downloadUrl: String,
    @SerializedName("musicDownloadUrl")
    val musicDownloadUrl: String,
    @SerializedName("microGDownloadUrl")
    val microGDownloadUrl: String
)
