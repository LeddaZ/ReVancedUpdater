package it.leddaz.revancedupdater.utils.misc

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import it.leddaz.revancedupdater.BuildConfig
import it.leddaz.revancedupdater.utils.apputils.AppInstaller
import it.leddaz.revancedupdater.utils.apputils.Downloader

/**
 * Constants and functions used in multiple occasions.
 * @author Leonardo Ledda (LeddaZ)
 */
object CommonStuff {

    // Common variables
    const val APP_VERSION = BuildConfig.VERSION_NAME
    const val LOG_TAG = "ReVanced Updater"
    val IS_DEBUG = BuildConfig.DEBUG

    /**
     * Opens a link using the default browser.
     * @property url link
     * @property context the activity's context
     */
    fun openLink(url: String, context: Context) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(context, browserIntent, null)
    }

    /**
     * Downloads and installs an app when the corresponding button is clicked.
     * @property fileName the APK filename
     * @property url link
     * @property context the activity's context
     */
    fun dlAndInstall(fileName: String, url: String, context: Context) {
        Downloader(
            context.getSystemService(AppCompatActivity.DOWNLOAD_SERVICE) as DownloadManager,
            context, Uri.parse(url), fileName
        )
        AppInstaller(fileName, context)
    }

}
