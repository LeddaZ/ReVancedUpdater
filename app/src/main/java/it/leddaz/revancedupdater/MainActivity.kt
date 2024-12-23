package it.leddaz.revancedupdater

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request.Method.GET
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.DynamicColors
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textview.MaterialTextView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import it.leddaz.revancedupdater.dialogs.AboutDialog
import it.leddaz.revancedupdater.utils.json.GmsCoreJSONObject
import it.leddaz.revancedupdater.utils.json.ReVancedJSONObject
import it.leddaz.revancedupdater.utils.json.UpdaterDebugJSONObject
import it.leddaz.revancedupdater.utils.json.UpdaterReleaseJSONObject
import it.leddaz.revancedupdater.utils.misc.AppInstaller
import it.leddaz.revancedupdater.utils.misc.CommonStuff.APP_VERSION
import it.leddaz.revancedupdater.utils.misc.CommonStuff.GMSCORE_PACKAGE
import it.leddaz.revancedupdater.utils.misc.CommonStuff.GMS_PACKAGE
import it.leddaz.revancedupdater.utils.misc.CommonStuff.HMS_PACKAGE
import it.leddaz.revancedupdater.utils.misc.CommonStuff.IS_DEBUG
import it.leddaz.revancedupdater.utils.misc.CommonStuff.LOG_TAG
import it.leddaz.revancedupdater.utils.misc.CommonStuff.MUSIC_PACKAGE
import it.leddaz.revancedupdater.utils.misc.CommonStuff.REVANCED_PACKAGE
import it.leddaz.revancedupdater.utils.misc.CommonStuff.UPDATER_PACKAGE
import it.leddaz.revancedupdater.utils.misc.CommonStuff.X_PACKAGE
import it.leddaz.revancedupdater.utils.misc.CommonStuff.isAppInstalled
import it.leddaz.revancedupdater.utils.misc.CommonStuff.openLink
import it.leddaz.revancedupdater.utils.misc.CommonStuff.requestInstallPermission
import it.leddaz.revancedupdater.utils.misc.Version
import it.leddaz.revancedupdater.utils.misc.VolleyCallBack
import org.apache.commons.codec.binary.Hex
import org.apache.commons.codec.digest.DigestUtils
import java.io.File
import java.io.FileInputStream
import kotlin.concurrent.thread

private var latestReVancedHash = ""
private var latestReVancedMusicHash = ""
private var latestXHash = ""

/**
 * The app's main activity, started at launch.
 * @return The activity
 * @author Leonardo Ledda (LeddaZ)
 */
class MainActivity : AppCompatActivity() {

    private var installedReVancedVersion = Version("99.99")
    private var latestReVancedVersion = Version("0.0")
    private var reVancedDownloadUrl = ""
    private var installedReVancedMusicVersion = Version("99.99")
    private var latestReVancedMusicVersion = Version("0.0")
    private var musicDownloadUrl = ""
    private var installedUpdaterVersion = Version("99.99")
    private var latestUpdaterVersion = Version("0.0")
    private var latestUpdaterCommit = ""
    private var updaterDownloadUrl = ""
    private var installedGmsCoreVersion = Version("99.99")
    private var latestGmsCoreVersion = Version("0.0")
    private var gmsCoreDownloadUrl = ""
    private var installedXVersion = Version("99.99")
    private var latestXVersion = Version("0.0")
    private var xDownloadUrl = ""
    private lateinit var revancedIndicator: LinearProgressIndicator
    private lateinit var musicIndicator: LinearProgressIndicator
    private lateinit var gmsCoreIndicator: LinearProgressIndicator
    private lateinit var xIndicator: LinearProgressIndicator
    private lateinit var updaterIndicator: LinearProgressIndicator


    /**
     * Actions executed when the activity is created at runtime.
     * @property savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        DynamicColors.applyToActivityIfAvailable(this)
        setContentView(R.layout.activity_main)

        revancedIndicator = findViewById(R.id.revanced_download_progress)
        musicIndicator = findViewById(R.id.music_download_progress)
        gmsCoreIndicator = findViewById(R.id.microg_download_progress)
        xIndicator = findViewById(R.id.x_download_progress)
        updaterIndicator = findViewById(R.id.updater_download_progress)

        val updaterCardTitle = findViewById<MaterialTextView>(R.id.updater_title)
        updaterCardTitle.text = getString(R.string.app_name)
        Log.i(LOG_TAG, "Device fingerprint: ${Build.FINGERPRINT}")
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

        val microGCard = findViewById<MaterialCardView>(R.id.microg_info_card)
        microGCard.setOnLongClickListener {
            openLink(
                "https://github.com/ReVanced/GmsCore/releases/tag/v${latestGmsCoreVersion}",
                this
            )
            true
        }

        val xCard = findViewById<MaterialCardView>(R.id.x_info_card)
        xCard.setOnLongClickListener {
            openLink(
                "https://github.com/LeddaZ/revanced-repo/blob/main/changelogs/x.md",
                this
            )
            true
        }

        val updaterCard = findViewById<MaterialCardView>(R.id.updater_info_card)
        if (!IS_DEBUG)
            updaterCard.setOnLongClickListener {
                openLink(
                    "https://github.com/LeddaZ/ReVancedUpdater/releases/tag/${
                        APP_VERSION.substring(
                            0,
                            APP_VERSION.indexOf(' ')
                        )
                    }",
                    this
                )
                true
            }
        else
            updaterCard.setOnLongClickListener {
                openLink(
                    "https://github.com/LeddaZ/ReVancedUpdater/releases/tag/dev",
                    this
                )
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

        requestInstallPermission(this)
    }

    /**
     * Gets the installed and latest versions of YouTube ReVanced,
     * ReVanced Music, ReVanced GmsCore and ReVanced Updater.
     * @property callback callback used to detect if the download was
     *                    successful
     */
    private fun getVersions(callback: VolleyCallBack) {
        // Installed versions
        getAppVersion(
            GMSCORE_PACKAGE,
            findViewById(R.id.installed_microg_version),
            installedGmsCoreVersion,
            findViewById(R.id.microg_download_button)
        )

        getAppVersion(
            X_PACKAGE,
            findViewById(R.id.installed_x_version),
            installedXVersion,
            findViewById(R.id.x_download_button)
        )

        if (isAppInstalled(this, GMSCORE_PACKAGE)) {
            getAppVersion(
                REVANCED_PACKAGE,
                findViewById(R.id.installed_revanced_version),
                installedReVancedVersion,
                findViewById(R.id.revanced_download_button)
            )

            getAppVersion(
                MUSIC_PACKAGE,
                findViewById(R.id.installed_music_version),
                installedReVancedMusicVersion,
                findViewById(R.id.music_download_button)
            )
        }

        getAppVersion(
            BuildConfig.APPLICATION_ID,
            findViewById(R.id.installed_updater_version),
            installedUpdaterVersion,
            findViewById(R.id.updater_download_button)
        )

        // Latest versions and ReVanced hashes
        val queue = Volley.newRequestQueue(this)
        val reVancedJSONUrl =
            "https://raw.githubusercontent.com/LeddaZ/revanced-repo/main/updater.json"
        val updaterAPIUrl = "https://api.github.com/repos/LeddaZ/ReVancedUpdater/releases/latest"
        val updaterCommitUrl = "https://api.github.com/repos/LeddaZ/ReVancedUpdater/commits/master"
        val gmsCoreAPIUrl = "https://api.github.com/repos/ReVanced/GmsCore/releases/latest"
        var reVancedReply: ReVancedJSONObject
        var updaterReleaseReply: UpdaterReleaseJSONObject
        var updaterDebugReply: UpdaterDebugJSONObject
        var gmsCoreReply: GmsCoreJSONObject

        val urlPrefix = "https://github.com/LeddaZ/revanced-repo/releases/download/"

        val reVancedRequest = StringRequest(GET, reVancedJSONUrl, { response ->
            reVancedReply =
                Gson().fromJson(response, object : TypeToken<ReVancedJSONObject>() {}.type)
            latestReVancedVersion = Version(reVancedReply.latestReVancedVersion)
            latestReVancedHash = reVancedReply.latestReVancedHash
            latestReVancedMusicVersion = Version(reVancedReply.latestReVancedMusicVersion)
            reVancedDownloadUrl =
                urlPrefix + reVancedReply.latestReVancedDate + "-yt/yt-signed.apk"
            val preferredABI: String = Build.SUPPORTED_ABIS[0]
            Log.i(LOG_TAG, "Preferred ABI: $preferredABI")
            when (preferredABI) {
                "armeabi-v7a" -> latestReVancedMusicHash = reVancedReply.latestReVancedMusicHashArm
                "arm64-v8a" -> latestReVancedMusicHash = reVancedReply.latestReVancedMusicHashArm64
                "x86" -> latestReVancedMusicHash = reVancedReply.latestReVancedMusicHashX86
                "x86_64" -> latestReVancedMusicHash = reVancedReply.latestReVancedMusicHashX64
            }
            musicDownloadUrl = urlPrefix + reVancedReply.latestReVancedMusicDate +
                    "-ytm/ytm-$preferredABI-signed.apk"
            latestXVersion = Version(reVancedReply.latestXVersion)
            xDownloadUrl = urlPrefix + reVancedReply.latestXDate + "-x/x-signed.apk"
            latestXHash = reVancedReply.latestXHash
            callback.onSuccess()
        }, {})

        val updaterReleaseRequest = StringRequest(GET, updaterAPIUrl, { response ->
            updaterReleaseReply =
                Gson().fromJson(response, object : TypeToken<UpdaterReleaseJSONObject>() {}.type)
            latestUpdaterVersion = Version(updaterReleaseReply.latestUpdaterVersion)
            updaterDownloadUrl = "https://github.com/LeddaZ/ReVancedUpdater/releases/download/" +
                    latestUpdaterVersion + "/app-release.apk"
            callback.onSuccess()
        }, {})

        val updaterDevRequest = StringRequest(GET, updaterCommitUrl, { response ->
            updaterDebugReply =
                Gson().fromJson(response, object : TypeToken<UpdaterDebugJSONObject>() {}.type)
            latestUpdaterCommit = updaterDebugReply.latestUpdaterCommit.substring(0, 7)
            updaterDownloadUrl =
                "https://github.com/LeddaZ/ReVancedUpdater/releases/download/dev/app-debug-signed.apk"
            callback.onSuccess()
        }, {})

        val gmsCoreRequest = StringRequest(GET, gmsCoreAPIUrl, { response ->
            gmsCoreReply =
                Gson().fromJson(response, object : TypeToken<GmsCoreJSONObject>() {}.type)
            latestGmsCoreVersion = Version(gmsCoreReply.latestGmsCoreVersion.substring(1))
            gmsCoreDownloadUrl =
                if (isAppInstalled(this, HMS_PACKAGE) && !isAppInstalled(this, GMS_PACKAGE))
                    gmsCoreReply.assets[0].latestGmsCoreUrl
                else
                    gmsCoreReply.assets[1].latestGmsCoreUrl
            callback.onSuccess()
        }, {})

        queue.add(reVancedRequest)
        queue.add(gmsCoreRequest)
        if (IS_DEBUG)
            queue.add(updaterDevRequest)
        else
            queue.add(updaterReleaseRequest)
    }


    /**
     * Compares versions.
     */
    private fun compareVersions() {
        compareAppVersion(
            GMSCORE_PACKAGE, installedGmsCoreVersion,
            latestGmsCoreVersion, findViewById(R.id.microg_update_status),
            findViewById(R.id.microg_download_button)
        )

        compareAppVersion(
            X_PACKAGE, installedXVersion,
            latestXVersion, findViewById(R.id.x_update_status),
            findViewById(R.id.x_download_button)
        )

        if (isAppInstalled(this, GMSCORE_PACKAGE)) {
            compareAppVersion(
                REVANCED_PACKAGE, installedReVancedVersion,
                latestReVancedVersion, findViewById(R.id.revanced_update_status),
                findViewById(R.id.revanced_download_button)
            )

            compareAppVersion(
                MUSIC_PACKAGE, installedReVancedMusicVersion,
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
            BuildConfig.APPLICATION_ID, installedUpdaterVersion,
            latestUpdaterVersion, findViewById(R.id.updater_update_status),
            findViewById(R.id.updater_download_button)
        )
    }

    /**
     * Downloads YouTube ReVanced when the button is clicked.
     * @property view the view which contains the button.
     */
    fun downloadReVanced(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
        AppInstaller(
            this,
            reVancedDownloadUrl,
            "revanced-nonroot-signed.apk",
            revancedIndicator,
            findViewById(R.id.revanced_download_button)
        )
    }

    /**
     * Downloads ReVanced Music when the button is clicked.
     * @property view the view which contains the button.
     */
    fun downloadReVancedMusic(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
        AppInstaller(
            this,
            musicDownloadUrl,
            "revanced-music-nonroot-signed.apk",
            musicIndicator,
            findViewById(R.id.music_download_button)
        )
    }

    /**
     * Downloads ReVanced GmsCore when the button is clicked.
     * @property view the view which contains the button.
     */
    fun downloadGmsCore(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
        AppInstaller(
            this,
            gmsCoreDownloadUrl,
            "microg.apk",
            gmsCoreIndicator,
            findViewById(R.id.microg_download_button)
        )
    }

    /**
     * Downloads ReVanced GmsCore when the button is clicked.
     * @property view the view which contains the button.
     */
    fun downloadX(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
        AppInstaller(
            this, xDownloadUrl, "x.apk", xIndicator, findViewById(R.id.x_download_button)
        )
    }

    /**
     * Downloads ReVanced Updater when the button is clicked.
     * @property view the view which contains the button.
     */
    fun downloadUpdater(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
        AppInstaller(
            this,
            updaterDownloadUrl,
            "app-release.apk",
            updaterIndicator,
            findViewById(R.id.updater_download_button)
        )
    }

    /**
     * Gets an app's installed version.
     * @property packageName package name
     * @property installedTextView the TextView with the currently installed version
     * @property installedVersion the installed app's version
     * @property button the app's install/update button
     */
    private fun getAppVersion(
        packageName: String, installedTextView: TextView, installedVersion: Version,
        button: Button
    ) {
        try {
            val pInfo: PackageInfo =
                packageManager.getPackageInfo(packageName, 0)
            if (packageName.startsWith(UPDATER_PACKAGE)) {
                if (!IS_DEBUG) {
                    installedVersion.version =
                        APP_VERSION.substring(0, APP_VERSION.indexOf(' '))
                    installedTextView.text =
                        getString(R.string.installed_app_version, installedVersion.version)
                } else {
                    installedTextView.text =
                        getString(R.string.installed_app_version, APP_VERSION)
                }
            } else if (packageName == GMSCORE_PACKAGE) {
                if (isAppInstalled(this, HMS_PACKAGE) && !isAppInstalled(this, GMS_PACKAGE))
                    installedVersion.version =
                        pInfo.versionName?.substring(0, pInfo.versionName!!.length - 3)
                else
                    installedVersion.version = pInfo.versionName
                installedTextView.text =
                    getString(R.string.installed_app_version, installedVersion.version)
            } else if (packageName == X_PACKAGE) {
                installedVersion.version = pInfo.versionName?.substringBefore('-')
                installedTextView.text =
                    getString(R.string.installed_app_version, installedVersion.version)
            } else {
                installedVersion.version = pInfo.versionName
                installedTextView.text =
                    getString(R.string.installed_app_version, installedVersion.version)
            }
        } catch (_: PackageManager.NameNotFoundException) {
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
     * @property packageName package name
     * @property installedVersion the installed app's version
     * @property latestVersion the app's latest version
     * @property updateStatusTextView the TextView with the app update status
     * @property button the app's install/update button
     */
    private fun compareAppVersion(
        packageName: String, installedVersion: Version,
        latestVersion: Version, updateStatusTextView: TextView,
        button: Button
    ) {
        if (packageName == "$UPDATER_PACKAGE.dev") {
            val currentCommit = APP_VERSION.substring(7, 14)
            if (currentCommit == latestUpdaterCommit) {
                updateStatusTextView.text = getString(R.string.no_update_available)
                button.isEnabled = false
            } else {
                updateStatusTextView.text = getString(R.string.update_available)
                button.isEnabled = true
            }
        } else {
            if (installedVersion.compareTo(latestVersion) == -1) {
                updateStatusTextView.text = getString(R.string.update_available)
                button.isEnabled = true
            } else if (installedVersion.compareTo(latestVersion) == 0) {
                if (packageName != GMSCORE_PACKAGE && packageName != UPDATER_PACKAGE) {
                    var latestHash = ""
                    if (packageName == REVANCED_PACKAGE)
                        latestHash = getLatestReVancedHash()
                    else if (packageName == MUSIC_PACKAGE)
                        latestHash = getLatestReVancedMusicHash()
                    else if (packageName == X_PACKAGE)
                        latestHash = getLatestReVancedXHash()
                    thread {
                        compareHashes(latestHash, updateStatusTextView, packageName, button)
                    }
                } else {
                    updateStatusTextView.text = getString(R.string.no_update_available)
                    button.isEnabled = false
                }
            } else {
                updateStatusTextView.text = getString(R.string.app_not_installed)
                button.isEnabled = true
            }
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
        val file = pInfo.applicationInfo?.sourceDir?.let { File(it) }
        val installedAppHash = String(Hex.encodeHex(DigestUtils.sha256(FileInputStream(file))))
        if (installedAppHash == latestHash) {
            runOnUiThread {
                updateStatusTextView.text = getString(R.string.no_update_available)
                button.isEnabled = false
            }
        } else {
            runOnUiThread {
                updateStatusTextView.text = getString(R.string.update_available)
                button.isEnabled = true
            }
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
            "app-release.apk",
            "microg.apk",
            "x.apk"
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
                val latestReVancedTextView: TextView =
                    findViewById(R.id.latest_revanced_version)
                latestReVancedTextView.text =
                    getString(R.string.latest_app_version, latestReVancedVersion)

                val latestReVancedMusicTextView: TextView =
                    findViewById(R.id.latest_music_version)
                latestReVancedMusicTextView.text =
                    getString(R.string.latest_app_version, latestReVancedMusicVersion)

                val latestGmsCoreTextView: TextView =
                    findViewById(R.id.latest_microg_version)
                latestGmsCoreTextView.text =
                    getString(R.string.latest_app_version, latestGmsCoreVersion)

                val latestXTextView: TextView =
                    findViewById(R.id.latest_x_version)
                latestXTextView.text =
                    getString(R.string.latest_app_version, latestXVersion)

                val latestAppTextView: TextView = findViewById(R.id.latest_updater_version)
                if (!IS_DEBUG)
                    latestAppTextView.text =
                        getString(R.string.latest_app_version, latestUpdaterVersion)
                else
                    latestAppTextView.text =
                        getString(R.string.latest_app_version, latestUpdaterCommit)
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

        /**
         * Returns the latest ReVanced X hash.
         * @return Latest ReVanced X hash.
         */
        fun getLatestReVancedXHash(): String {
            return latestXHash
        }
    }

}
