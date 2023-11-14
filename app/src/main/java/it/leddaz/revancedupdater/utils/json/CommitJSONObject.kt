package it.leddaz.revancedupdater.utils.json

import com.google.gson.annotations.SerializedName

data class CommitJSONObject(
    @SerializedName("sha")
    val latestUpdaterCommit: String,
)
