package it.leddaz.revancedupdater.utils.misc

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
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
    const val GMSCORE_PACKAGE = "app.revanced.android.gms"
    const val UPDATER_PACKAGE = "it.leddaz.revancedupdater"
    const val HMS_PACKAGE = "com.huawei.hwid"
    const val GMS_PACKAGE = "com.google.android.gms"
    const val X_PACKAGE = "com.twitter.android"

    /**
     * Opens a link using the default browser.
     * @property url link
     * @property context the activity's context
     */
    fun openLink(url: String, context: Context) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
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
                .setData(Uri.parse("package:" + BuildConfig.APPLICATION_ID))
            context.startActivity(intent)
            return
        }
    }

}
