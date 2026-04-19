package it.leddaz.morpheupdater.utils.misc

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.core.net.toUri
import it.leddaz.morpheupdater.BuildConfig

/**
 * Constants and functions used in multiple occasions.
 * @author Leonardo Ledda (LeddaZ)
 */
object CommonStuff {
    // Common variables
    const val PREFS_NAME = "settings"
    const val KEY_YT = "show_yt"
    const val KEY_YTM = "show_ytm"
    const val APP_VERSION = BuildConfig.VERSION_NAME
    const val LOG_TAG = "Morphe Updater"
    val IS_DEBUG = BuildConfig.DEBUG

    // Package names
    const val YT_PACKAGE = "app.morphe.android.youtube"
    const val YTM_PACKAGE = "app.morphe.android.apps.youtube.music"
    const val MICROG_PACKAGE = "app.revanced.android.gms"
    const val UPDATER_PACKAGE = "it.leddaz.revancedupdater"

    // URLs
    const val APP_REPO = "LeddaZ/MorpheUpdater"
    const val APK_REPO = "LeddaZ/morphe-repo"

    /**
     * Opens a link using the default browser.
     * @property url link
     * @property context the activity's context
     */
    fun openLink(url: String, context: Context) {
        val browserIntent = Intent(Intent.ACTION_VIEW, url.toUri())
        context.startActivity(browserIntent)
    }

    /**
     * Detects if an app is installed.
     * @param context The activity's context
     * @return true if the app is installed, false otherwise
     */
    fun isAppInstalled(context: Context, packageName: String): Boolean {
        try {
            context.packageManager.getPackageInfo(packageName, 0)
        } catch (_: PackageManager.NameNotFoundException) {
            return false
        }
        return true
    }

    /**
     * Prompts the user to grant the permission to install apps.
     * @param context The activity's context
     */
    fun requestInstallPermission(context: Context) {
        val packageManager = context.packageManager
        if (!packageManager.canRequestPackageInstalls()) {
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                .setData(("package:" + BuildConfig.APPLICATION_ID).toUri())
            context.startActivity(intent)
            return
        }
    }
}
