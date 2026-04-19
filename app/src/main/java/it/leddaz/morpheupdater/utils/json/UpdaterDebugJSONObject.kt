package it.leddaz.morpheupdater.utils.json

import com.google.gson.annotations.SerializedName

data class UpdaterDebugJSONObject(
    @SerializedName("sha")
    val latestUpdaterCommit: String
)
