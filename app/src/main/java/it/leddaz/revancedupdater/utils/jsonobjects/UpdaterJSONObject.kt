package it.leddaz.revancedupdater.utils.jsonobjects

import com.google.gson.annotations.SerializedName

data class UpdaterJSONObject(
    @SerializedName("latestAppVersion")
    val latestUpdaterVersion: String
)
