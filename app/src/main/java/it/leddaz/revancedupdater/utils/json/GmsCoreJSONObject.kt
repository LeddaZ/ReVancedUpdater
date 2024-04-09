package it.leddaz.revancedupdater.utils.json

import com.google.gson.annotations.SerializedName

data class GmsCoreJSONObject(
    @SerializedName("tag_name")
    val latestGmsCoreVersion: String,

    @SerializedName("assets")
    val assets: List<Asset>,
)

data class Asset(
    @SerializedName("browser_download_url")
    val latestGmsCoreUrl: String
)
