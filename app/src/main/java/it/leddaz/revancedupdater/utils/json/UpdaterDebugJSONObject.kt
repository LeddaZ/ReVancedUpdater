package it.leddaz.revancedupdater.utils.json

import com.google.gson.annotations.SerializedName

data class UpdaterDebugJSONObject(
    @SerializedName("sha")
    val latestUpdaterCommit: String
)
