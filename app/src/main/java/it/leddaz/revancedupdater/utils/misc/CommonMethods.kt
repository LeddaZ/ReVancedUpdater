package it.leddaz.revancedupdater.utils.misc

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import it.leddaz.revancedupdater.BuildConfig
import it.leddaz.revancedupdater.MainActivity
import it.leddaz.revancedupdater.R
import it.leddaz.revancedupdater.utils.apputils.AppInstaller
import it.leddaz.revancedupdater.utils.apputils.Downloader
import org.apache.commons.codec.binary.Hex
import org.apache.commons.codec.digest.DigestUtils
import java.io.File
import java.io.FileInputStream

/**
 * Common methods used in multiple occasions.
 * @author Leonardo Ledda (LeddaZ)
 */
object CommonMethods {

    // Constants
    const val APP_VERSION = BuildConfig.VERSION_NAME
    const val LOG_TAG = "ReVanced Updater"

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

    /**
     * Gets the installed and latest versions of YouTube Revanced,
     * ReVanced Music and Vanced microG.
     * @property packageName package name
     * @property context the activity's context
     * @property installedTextView the TextView with the currently installed version
     * @property installedVersion the installed app's version
     * @property updateStatusTextView the TextView with the app update status
     * @property button the app's install/update button
     */
    fun getAppVersion(
        packageName: String, context: Context, installedTextView: TextView,
        installedVersion: Version, updateStatusTextView: TextView,
        button: Button
    ) {
        val minSdk: Int
        val minVer: String
        when (packageName) {
            "app.revanced.android.youtube" -> {
                minSdk = 26
                minVer = "8.0"
            }

            else -> {
                minSdk = 24
                minVer = "7.0"
            }
        }
        try {
            if (Build.VERSION.SDK_INT >= minSdk) {
                val pInfo: PackageInfo =
                    context.packageManager.getPackageInfo(packageName, 0)
                installedVersion.version = pInfo.versionName
                installedTextView.text =
                    context.getString(R.string.installed_app_version, installedVersion.version)
            } else {
                updateStatusTextView.text =
                    context.getString(R.string.old_android_version, minVer)
                button.isEnabled = false
            }
        } catch (e: PackageManager.NameNotFoundException) {
            installedTextView.text =
                context.getString(R.string.installed_app_version, context.getString(R.string.none))
            installedVersion.version = "99.99"
            button.isEnabled = true
        } catch (e: IllegalArgumentException) {
            installedTextView.text =
                context.getString(
                    R.string.installed_app_version,
                    context.getString(R.string.invalid)
                )
            installedVersion.version = "99.99"
            Toast.makeText(context, R.string.invalid_version_detected, Toast.LENGTH_LONG).show()
            Log.e(LOG_TAG, e.printStackTrace().toString())
        }
    }

    /**
     * Compares versions.
     * @property hashCheck true if the hash should be checked
     * @property packageName package name
     * @property installedVersion the installed app's version
     * @property latestVersion the app's latest version
     * @property updateStatusTextView the TextView with the app update status
     * @property button the app's install/update button
     * @property context the activity's context
     */
    fun compareAppVersion(
        hashCheck: Boolean, packageName: String, installedVersion: Version,
        latestVersion: Version, updateStatusTextView: TextView,
        button: Button, context: Context
    ) {
        if (installedVersion.compareTo(latestVersion) == -1) {
            updateStatusTextView.text = context.getString(R.string.update_available)
            button.isEnabled = true
        } else if (installedVersion.compareTo(latestVersion) == 0) {
            if (hashCheck) {
                var latestHash = MainActivity.getLatestReVancedHash()
                if (packageName == "app.revanced.android.apps.youtube.music")
                    latestHash = MainActivity.getLatestReVancedMusicHash()
                compareHashes(latestHash, updateStatusTextView, packageName, context, button)
            } else {
                updateStatusTextView.text = context.getString(R.string.no_update_available)
                button.isEnabled = false
            }
        } else {
            updateStatusTextView.text = context.getString(R.string.app_not_installed)
            button.isEnabled = true
        }
    }

    /**
     * Compares hashes.
     * @property latestHash the latest app version's hash
     * @property updateStatusTextView the TextView with the app update status
     * @property packageName package name
     * @property context the activity's context
     * @property button the app's install/update button
     */
    private fun compareHashes(
        latestHash: String, updateStatusTextView: TextView, packageName: String,
        context: Context, button: Button
    ) {
        val pInfo: PackageInfo = context.packageManager.getPackageInfo(packageName, 0)
        val file = File(pInfo.applicationInfo.sourceDir)
        val installedAppHash = String(Hex.encodeHex(DigestUtils.sha256(FileInputStream(file))))
        if (installedAppHash == latestHash) {
            updateStatusTextView.text = context.getString(R.string.no_update_available)
            button.isEnabled = false
        } else {
            updateStatusTextView.text = context.getString(R.string.update_available)
            button.isEnabled = true
        }
    }

}
