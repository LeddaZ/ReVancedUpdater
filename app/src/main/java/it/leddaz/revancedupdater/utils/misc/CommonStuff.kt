package it.leddaz.revancedupdater.utils.misc

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
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
     * Detects if HMS Core is installed.
     * @param context The activity's context
     * @return HMS Core installation status
     */
    fun isHmsInstalled(context: Context): Boolean {
        try {
            context.packageManager.getPackageInfo(HMS_PACKAGE, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            return false
        }
        return true
    }

    /**
     * Detects if GMS is installed.
     * @param context The activity's context
     * @return GMS installation status
     */
    fun isGmsInstalled(context: Context): Boolean {
        try {
            context.packageManager.getPackageInfo(GMS_PACKAGE, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            return false
        }
        return true
    }

    /**
     * Detects if ReVanced GmsCore is installed.
     * @param context The activity's context
     * @return ReVanced GmsCore installation status
     */
    fun isGmsCoreInstalled(context: Context): Boolean {
        try {
            context.packageManager.getPackageInfo(GMSCORE_PACKAGE, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            return false
        }
        return true
    }

}
