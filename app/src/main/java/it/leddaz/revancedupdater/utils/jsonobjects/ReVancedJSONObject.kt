package it.leddaz.revancedupdater.utils.jsonobjects

import com.google.gson.annotations.SerializedName

data class ReVancedJSONObject(
    @SerializedName("latestReVancedVersion")
    val latestReVancedVersion: String,
    @SerializedName("latestMicroGVersion")
    val latestMicroGVersion: String,
    @SerializedName("downloadUrl")
    val downloadUrl: String,
    @SerializedName("microGDownloadUrl")
    val microGDownloadUrl: String
)
