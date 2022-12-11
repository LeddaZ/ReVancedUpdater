package it.leddaz.revancedupdater.utils.misc

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import com.airbnb.paris.extensions.style
import it.leddaz.revancedupdater.MainActivity
import it.leddaz.revancedupdater.R
import it.leddaz.revancedupdater.utils.apputils.AppInstaller
import it.leddaz.revancedupdater.utils.apputils.Downloader
import org.apache.commons.codec.binary.Hex
import org.apache.commons.codec.digest.DigestUtils
import java.io.File
import java.io.FileInputStream

/**
 * Common methods used in multiple activities.
 * @author Leonardo Ledda (LeddaZ)
 */
object CommonMethods {

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
     * Sets button properties.
     * @property button the button to set the properties for
     * @property isEnabled button enabled state
     * @property text button text
     * @property context the activity's context
     */
    private fun setButtonProperties(button: Button, isEnabled: Boolean, text: Int, context: Context) {
        button.isEnabled = isEnabled
        button.text = context.getString(text)
        if (isEnabled)
            button.style(R.style.button_enabled)
        else
            button.style(R.style.button_disabled)
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
     * @property minSdk lowest supported SDK version
     * @property minVer lowest supported Android version
     * @property packageName package name
     * @property context the activity's context
     * @property view the activity's view
     * @property installedTextView the TextView with the currently installed version
     * @property installedVersion the installed app's version
     * @property updateStatusTextView the TextView with the app update status
     * @property button the app's install/update button
     */
    fun getAppVersion(
        minSdk: Int, minVer: String, packageName: String, context: Context, view: View,
        installedTextView: TextView, installedVersion: Version, updateStatusTextView: TextView,
        button: Button
    ) {
        try {
            if (Build.VERSION.SDK_INT >= minSdk) {
                val pInfo: PackageInfo =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        context.packageManager.getPackageInfo(
                            packageName, PackageManager.PackageInfoFlags.of(0)
                        )
                    } else {
                        context.packageManager.getPackageInfo(packageName, 0)
                    }
                installedVersion.version = pInfo.versionName
                installedTextView.text =
                    context.getString(R.string.installed_app_version, installedVersion.version)
            } else {
                updateStatusTextView.text =
                    context.getString(R.string.old_android_version, minVer)
                setButtonProperties(button, false, R.string.install, context)
            }
        } catch (e: PackageManager.NameNotFoundException) {
            installedTextView.text = context.getString(R.string.installed_app_version, "none")
            installedVersion.version = "99.99"
            setButtonProperties(button, true, R.string.install, context)
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
        latestVersion: Version, updateStatusTextView: TextView, button: Button, context: Context
    ) {
        if (installedVersion.compareTo(latestVersion) == -1) {
            updateStatusTextView.text = context.getString(R.string.update_available)
            setButtonProperties(button, true, R.string.update_button, context)
        } else if (installedVersion.compareTo(latestVersion) == 0) {
            if (hashCheck) {
                var latestHash = MainActivity.getLatestReVancedHash()
                if (packageName.equals("app.revanced.android.apps.youtube.music"))
                    latestHash = MainActivity.getLatestReVancedMusicHash()
                compareHashes(latestHash, updateStatusTextView, packageName, context, button)
            } else {
                updateStatusTextView.text = context.getString(R.string.no_update_available)
                setButtonProperties(button, false, R.string.update_button, context)
            }
        } else {
            updateStatusTextView.text = context.getString(R.string.app_not_installed)
            setButtonProperties(button, true, R.string.install, context)
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
        val pInfo: PackageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(
                packageName,
                PackageManager.PackageInfoFlags.of(0)
            )
        } else {
            context.packageManager.getPackageInfo(packageName, 0)
        }
        val file = File(pInfo.applicationInfo.sourceDir)
        val installedAppHash = String(Hex.encodeHex(DigestUtils.sha256(FileInputStream(file))))
        if (installedAppHash == latestHash) {
            updateStatusTextView.text = context.getString(R.string.no_update_available)
            setButtonProperties(button, false, R.string.update_button, context)
        } else {
            updateStatusTextView.text = context.getString(R.string.update_available)
            setButtonProperties(button, true, R.string.update_button, context)
        }
    }

}
