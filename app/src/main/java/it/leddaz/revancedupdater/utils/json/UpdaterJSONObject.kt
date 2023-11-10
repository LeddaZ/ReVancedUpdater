package it.leddaz.revancedupdater.utils.json

import com.google.gson.annotations.SerializedName

data class UpdaterJSONObject(
    @SerializedName("tag_name")
    val latestUpdaterVersion: String,
)
