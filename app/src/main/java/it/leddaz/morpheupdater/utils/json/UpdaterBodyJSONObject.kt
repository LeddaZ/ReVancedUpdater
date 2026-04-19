package it.leddaz.morpheupdater.utils.json

import com.google.gson.annotations.SerializedName

data class UpdaterBodyJSONObject(
    @SerializedName("body")
    val latestUpdaterBody: String
)
