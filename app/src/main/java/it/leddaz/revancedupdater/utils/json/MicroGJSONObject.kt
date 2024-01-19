package it.leddaz.revancedupdater.utils.json

import com.google.gson.annotations.SerializedName

data class MicroGJSONObject(
    @SerializedName("tag_name")
    val latestMicroGVersion: String
)
