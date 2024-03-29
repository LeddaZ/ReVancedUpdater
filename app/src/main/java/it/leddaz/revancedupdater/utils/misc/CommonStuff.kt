package it.leddaz.revancedupdater.utils.misc

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat.startActivity
import it.leddaz.revancedupdater.BuildConfig

/**
 * Constants and functions used in multiple occasions.
 * @author Leonardo Ledda (LeddaZ)
 */
object CommonStuff {

    // Common variables
    const val APP_VERSION = BuildConfig.VERSION_NAME
    const val LOG_TAG = "ReVanced Updater"
    val IS_DEBUG = BuildConfig.DEBUG

    // Package names
    const val REVANCED_PACKAGE = "app.revanced.android.youtube"
    const val MUSIC_PACKAGE = "app.revanced.android.apps.youtube.music"
    const val MICROG_PACKAGE = "app.revanced.android.gms"
    const val UPDATER_PACKAGE = "it.leddaz.revancedupdater"

    /**
     * Opens a link using the default browser.
     * @property url link
     * @property context the activity's context
     */
    fun openLink(url: String, context: Context) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(context, browserIntent, null)
    }

}
