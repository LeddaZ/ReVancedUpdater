package it.leddaz.revancedupdater

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request.Method.GET
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.DynamicColors
import com.google.android.material.elevation.SurfaceColors
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import it.leddaz.revancedupdater.dialogs.AboutDialog
import it.leddaz.revancedupdater.dialogs.MicroGDialog
import it.leddaz.revancedupdater.utils.json.ReVancedJSONObject
import it.leddaz.revancedupdater.utils.json.UpdaterJSONObject
import it.leddaz.revancedupdater.utils.misc.CommonStuff.APP_VERSION
import it.leddaz.revancedupdater.utils.misc.CommonStuff.LOG_TAG
import it.leddaz.revancedupdater.utils.misc.CommonStuff.dlAndInstall
import it.leddaz.revancedupdater.utils.misc.CommonStuff.openLink
import it.leddaz.revancedupdater.utils.misc.Version
import it.leddaz.revancedupdater.utils.misc.VolleyCallBack
import org.apache.commons.codec.binary.Hex
import org.apache.commons.codec.digest.DigestUtils
import java.io.File
import java.io.FileInputStream


private var installedReVancedVersion = Version("99.99")
private var latestReVancedVersion = Version("0.0")
private var latestReVancedHash = ""
private var installedReVancedMusicVersion = Version("99.99")
private var latestReVancedMusicVersion = Version("0.0")
private var latestReVancedMusicHash = ""
private var installedUpdaterVersion = Version("99.99")
private var latestUpdaterVersion = Version("0.0")
private var updaterDownloadUrl = ""
private var downloadUrl = ""
var microGDownloadUrl = ""
private var musicDownloadUrl = ""

/**
 * The app's main activity, started at launch.
 * @return The activity
 * @author Leonardo Ledda (LeddaZ)
 */
class MainActivity : AppCompatActivity() {

    /**
     * Actions executed when the activity is created at runtime.
     * @property savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DynamicColors.applyToActivityIfAvailable(this)
        window.navigationBarColor = SurfaceColors.SURFACE_0.getColor(this)
        window.statusBarColor = SurfaceColors.SURFACE_0.getColor(this)
        setContentView(R.layout.activity_main)
        refresh(this)

        val reVancedCard = findViewById<MaterialCardView>(R.id.revanced_info_card)
        reVancedCard.setOnLongClickListener {
            openLink(
                "https://github.com/LeddaZ/revanced-repo/blob/main/changelogs/revanced.md",
                this
            )
            true
        }

        val reVancedMusicCard = findViewById<MaterialCardView>(R.id.music_info_card)
        reVancedMusicCard.setOnLongClickListener {
            openLink("https://github.com/LeddaZ/revanced-repo/blob/main/changelogs/music.md", this)
            true
        }

        val updaterCard = findViewById<MaterialCardView>(R.id.updater_info_card)
        updaterCard.setOnLongClickListener {
            openLink("https://github.com/LeddaZ/ReVancedUpdater/releases/tag/${APP_VERSION}", this)
            true
        }

        val topAppBar = findViewById<MaterialToolbar>(R.id.topAppBar)
        topAppBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.about -> {
                    val dialogFragment = AboutDialog()
                    dialogFragment.show(supportFragmentManager, "AboutDialog")
                }

                R.id.refresh -> {
                    refresh(this)
                }
            }
            false
        }
    }

    /**
     * Detects if Vanced microG is installed.
     * @return Vanced microG installation status
     */
    private fun isMicroGInstalled(): Boolean {
        try {
            this.packageManager.getPackageInfo("com.mgoogle.android.gms", 0)
        } catch (e: PackageManager.NameNotFoundException) {
            return false
        }
        return true
    }

    /**
     * Gets the installed and latest versions of YouTube Revanced,
     * ReVanced Music and Vanced microG.
     * @property callback callback used to detect if the download was
     *                    successful
     */
    private fun getVersions(callback: VolleyCallBack) {
        // Installed versions
        if (isMicroGInstalled()) {
            getAppVersion(
                "app.revanced.android.youtube",
                findViewById(R.id.installed_revanced_version),
                installedReVancedVersion, findViewById(R.id.revanced_update_status),
                findViewById(R.id.revanced_download_button)
            )

            getAppVersion(
                "app.revanced.android.apps.youtube.music",
                findViewById(R.id.installed_music_version),
                installedReVancedMusicVersion, findViewById(R.id.music_update_status),
                findViewById(R.id.music_download_button)
            )
        } else {
            val dialogFragment = MicroGDialog()
            dialogFragment.show(supportFragmentManager, "MicroGDialog")
        }
        getAppVersion(
            "it.leddaz.revancedupdater",
            findViewById(R.id.installed_updater_version),
            installedUpdaterVersion,
            findViewById(R.id.updater_update_status),
            findViewById(R.id.updater_download_button)
        )

        // Latest versions and ReVanced hashes
        val queue = Volley.newRequestQueue(this)
        val reVancedJSONUrl =
            "https://raw.githubusercontent.com/LeddaZ/revanced-repo/main/updater.json"
        val updaterAPIUrl = "https://api.github.com/repos/LeddaZ/ReVancedUpdater/releases/latest"
        var reVancedReply: ReVancedJSONObject
        var updaterReply: UpdaterJSONObject

        val urlPrefix = "https://github.com/LeddaZ/revanced-repo/releases/download/"

        val reVancedRequest = StringRequest(GET, reVancedJSONUrl, { response ->
            reVancedReply =
                Gson().fromJson(response, object : TypeToken<ReVancedJSONObject>() {}.type)
            latestReVancedVersion = Version(reVancedReply.latestReVancedVersion)
            latestReVancedHash = reVancedReply.latestReVancedHash
            latestReVancedMusicVersion = Version(reVancedReply.latestReVancedMusicVersion)
            microGDownloadUrl =
                urlPrefix + reVancedReply.latestReVancedDate + "-yt/vanced-microG.apk"
            downloadUrl =
                urlPrefix + reVancedReply.latestReVancedDate + "-yt/revanced-nonroot-signed.apk"
            val preferredABI: String = Build.SUPPORTED_ABIS[0]
            Log.i(LOG_TAG, "Preferred ABI: $preferredABI")
            when (preferredABI) {
                "armeabi-v7a" -> latestReVancedMusicHash = reVancedReply.latestReVancedMusicHashArm
                "arm64-v8a" -> latestReVancedMusicHash = reVancedReply.latestReVancedMusicHashArm64
                "x86" -> latestReVancedMusicHash = reVancedReply.latestReVancedMusicHashX86
                "x86_64" -> latestReVancedMusicHash = reVancedReply.latestReVancedMusicHashX64
            }
            musicDownloadUrl = urlPrefix + reVancedReply.latestReVancedMusicDate +
                    "-ytm/revanced-music-nonroot-$preferredABI-signed.apk"
            callback.onSuccess()
        }, {})

        val updaterRequest = StringRequest(GET, updaterAPIUrl, { response ->
            updaterReply =
                Gson().fromJson(response, object : TypeToken<UpdaterJSONObject>() {}.type)
            latestUpdaterVersion = Version(updaterReply.latestUpdaterVersion)
            updaterDownloadUrl = "https://github.com/LeddaZ/ReVancedUpdater/releases/download/" +
                    latestUpdaterVersion + "/app-release.apk"
            callback.onSuccess()
        }, {})

        queue.add(reVancedRequest)
        queue.add(updaterRequest)
    }


    /**
     * Compares versions.
     */
    private fun compareVersions() {
        if (isMicroGInstalled()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                compareAppVersion(
                    true, "app.revanced.android.youtube", installedReVancedVersion,
                    latestReVancedVersion, findViewById(R.id.revanced_update_status),
                    findViewById(R.id.revanced_download_button)
                )
            }

            compareAppVersion(
                true, "app.revanced.android.apps.youtube.music", installedReVancedMusicVersion,
                latestReVancedMusicVersion, findViewById(R.id.music_update_status),
                findViewById(R.id.music_download_button)
            )
        } else {
            val reVancedTextView = findViewById<TextView>(R.id.revanced_update_status)
            reVancedTextView.text = getString(R.string.microg_dialog_title)
            val reVancedMusicTextView = findViewById<TextView>(R.id.music_update_status)
            reVancedMusicTextView.text = getString(R.string.microg_dialog_title)
        }
        compareAppVersion(
            false, "it.leddaz.revancedupdater", installedUpdaterVersion,
            latestUpdaterVersion, findViewById(R.id.updater_update_status),
            findViewById(R.id.updater_download_button)
        )
    }

    /**
     * Downloads YouTube ReVanced when the button is clicked.
     * @property view the view which contains the button.
     */
    @Suppress("UNUSED_PARAMETER")
    fun downloadReVanced(view: View) {
        dlAndInstall("revanced-nonroot-signed.apk", downloadUrl, this)
    }

    /**
     * Downloads ReVanced Music when the button is clicked.
     * @property view the view which contains the button.
     */
    @Suppress("UNUSED_PARAMETER")
    fun downloadReVancedMusic(view: View) {
        dlAndInstall("revanced-music-nonroot-signed.apk", musicDownloadUrl, this)
    }

    /**
     * Download ReVanced Updater when the button is clicked.
     * @property view the view which contains the button.
     */
    @Suppress("UNUSED_PARAMETER")
    fun downloadUpdater(view: View) {
        dlAndInstall("app-release.apk", updaterDownloadUrl, this)
    }

    /**
     * Gets the installed and latest versions of YouTube Revanced
     * and ReVanced Music.
     * @property packageName package name
     * @property installedTextView the TextView with the currently installed version
     * @property installedVersion the installed app's version
     * @property updateStatusTextView the TextView with the app update status
     * @property button the app's install/update button
     */
    private fun getAppVersion(
        packageName: String, installedTextView: TextView, installedVersion: Version,
        updateStatusTextView: TextView, button: Button
    ) {
        try {
            if (packageName == "app.revanced.android.youtube" && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                updateStatusTextView.text =
                    getString(R.string.old_android_version)
                button.isEnabled = false
            }
            val pInfo: PackageInfo =
                packageManager.getPackageInfo(packageName, 0)
            if (packageName == "it.leddaz.revancedupdater")
                installedVersion.version =
                    pInfo.versionName.substring(0, pInfo.versionName.indexOf(' '))
            else
                installedVersion.version = pInfo.versionName
            installedTextView.text =
                getString(R.string.installed_app_version, installedVersion.version)
        } catch (e: PackageManager.NameNotFoundException) {
            installedTextView.text =
                getString(R.string.installed_app_version, getString(R.string.none))
            installedVersion.version = "99.99"
            button.isEnabled = true
        } catch (e: IllegalArgumentException) {
            installedTextView.text =
                getString(R.string.installed_app_version, getString(R.string.invalid))
            installedVersion.version = "99.99"
            Toast.makeText(this, R.string.invalid_version_detected, Toast.LENGTH_LONG).show()
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
     */
    private fun compareAppVersion(
        hashCheck: Boolean, packageName: String, installedVersion: Version,
        latestVersion: Version, updateStatusTextView: TextView,
        button: Button
    ) {
        if (installedVersion.compareTo(latestVersion) == -1) {
            updateStatusTextView.text = getString(R.string.update_available)
            button.isEnabled = true
        } else if (installedVersion.compareTo(latestVersion) == 0) {
            if (hashCheck) {
                var latestHash = getLatestReVancedHash()
                if (packageName == "app.revanced.android.apps.youtube.music")
                    latestHash = getLatestReVancedMusicHash()
                compareHashes(latestHash, updateStatusTextView, packageName, button)
            } else {
                updateStatusTextView.text = getString(R.string.no_update_available)
                button.isEnabled = false
            }
        } else {
            updateStatusTextView.text = getString(R.string.app_not_installed)
            button.isEnabled = true
        }
    }

    /**
     * Compares hashes.
     * @property latestHash the latest app version's hash
     * @property updateStatusTextView the TextView with the app update status
     * @property packageName package name
     * @property button the app's install/update button
     */
    private fun compareHashes(
        latestHash: String, updateStatusTextView: TextView, packageName: String,
        button: Button
    ) {
        val pInfo: PackageInfo = packageManager.getPackageInfo(packageName, 0)
        val file = File(pInfo.applicationInfo.sourceDir)
        val installedAppHash = String(Hex.encodeHex(DigestUtils.sha256(FileInputStream(file))))
        if (installedAppHash == latestHash) {
            updateStatusTextView.text = getString(R.string.no_update_available)
            button.isEnabled = false
        } else {
            updateStatusTextView.text = getString(R.string.update_available)
            button.isEnabled = true
        }
    }

    /**
     * Refreshes the versions and deletes existing APKs.
     * @param context the app's context.
     */
    private fun refresh(context: Context) {
        val filenames = arrayOf(
            "revanced-music-nonroot-signed.apk",
            "revanced-nonroot-signed.apk",
            "app-release.apk"
        )
        val appDataDir = context.getExternalFilesDir("/apks/").toString() + "/"
        for (apk in filenames) {
            val path = File(appDataDir + apk)
            if (path.exists()) {
                path.delete()
            }
        }
        getVersions(object : VolleyCallBack {
            override fun onSuccess() {
                val latestReVancedTextView: TextView = findViewById(R.id.latest_revanced_version)
                latestReVancedTextView.text =
                    getString(R.string.latest_app_version, latestReVancedVersion)

                val latestReVancedMusicTextView: TextView =
                    findViewById(R.id.latest_music_version)
                latestReVancedMusicTextView.text =
                    getString(R.string.latest_app_version, latestReVancedMusicVersion)

                val latestAppTextView: TextView = findViewById(R.id.latest_updater_version)
                latestAppTextView.text =
                    getString(R.string.latest_app_version, latestUpdaterVersion)
                compareVersions()
            }
        })
    }

    /**
     * Companion object
     */
    companion object {
        /**
         * Returns the latest ReVanced hash.
         * @return Latest ReVanced hash.
         */
        fun getLatestReVancedHash(): String {
            return latestReVancedHash
        }

        /**
         * Returns the latest ReVanced Music hash.
         * @return Latest ReVanced Music hash.
         */
        fun getLatestReVancedMusicHash(): String {
            return latestReVancedMusicHash
        }
    }

}
