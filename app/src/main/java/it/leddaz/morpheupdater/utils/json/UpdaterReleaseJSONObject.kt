package it.leddaz.morpheupdater.utils.json

import com.google.gson.annotations.SerializedName

data class UpdaterReleaseJSONObject(
    @SerializedName("tag_name")
    val latestUpdaterVersion: String
)
