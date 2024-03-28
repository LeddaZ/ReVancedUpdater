package it.leddaz.revancedupdater.utils.json

import com.google.gson.annotations.SerializedName

data class MicroGJSONObject(
    @SerializedName("tag_name")
    val latestMicroGVersion: String,

    @SerializedName("assets")
    val assets: List<Asset>,
)

data class Asset(
    @SerializedName("browser_download_url")
    val latestMicroGUrl: String
)
