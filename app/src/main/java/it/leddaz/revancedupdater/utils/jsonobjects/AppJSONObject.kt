package it.leddaz.revancedupdater.utils.jsonobjects

import com.google.gson.annotations.SerializedName

data class AppJSONObject(
    @SerializedName("latestAppVersion")
    val latestAppVersion: String
)
