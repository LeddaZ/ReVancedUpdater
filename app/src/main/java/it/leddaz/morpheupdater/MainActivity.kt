package it.leddaz.morpheupdater

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
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
import androidx.core.view.isVisible
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.Request.Method.GET
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.DynamicColors
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import it.leddaz.morpheupdater.dialogs.ChangelogDialog
import it.leddaz.morpheupdater.utils.json.GmsCoreJSONObject
import it.leddaz.morpheupdater.utils.json.MorpheJsonObject
import it.leddaz.morpheupdater.utils.json.UpdaterBodyJSONObject
import it.leddaz.morpheupdater.utils.json.UpdaterDebugJSONObject
import it.leddaz.morpheupdater.utils.json.UpdaterReleaseJSONObject
import it.leddaz.morpheupdater.utils.misc.AppInstaller
import it.leddaz.morpheupdater.utils.misc.CommonStuff.APK_REPO
import it.leddaz.morpheupdater.utils.misc.CommonStuff.APP_REPO
import it.leddaz.morpheupdater.utils.misc.CommonStuff.APP_VERSION
import it.leddaz.morpheupdater.utils.misc.CommonStuff.IS_DEBUG
import it.leddaz.morpheupdater.utils.misc.CommonStuff.KEY_YT
import it.leddaz.morpheupdater.utils.misc.CommonStuff.KEY_YTM
import it.leddaz.morpheupdater.utils.misc.CommonStuff.LOG_TAG
import it.leddaz.morpheupdater.utils.misc.CommonStuff.MICROG_PACKAGE
import it.leddaz.morpheupdater.utils.misc.CommonStuff.PREFS_NAME
import it.leddaz.morpheupdater.utils.misc.CommonStuff.UPDATER_PACKAGE
import it.leddaz.morpheupdater.utils.misc.CommonStuff.YTM_PACKAGE
import it.leddaz.morpheupdater.utils.misc.CommonStuff.YT_PACKAGE
import it.leddaz.morpheupdater.utils.misc.CommonStuff.isAppInstalled
import it.leddaz.morpheupdater.utils.misc.CommonStuff.openLink
import it.leddaz.morpheupdater.utils.misc.CommonStuff.requestInstallPermission
import it.leddaz.morpheupdater.utils.misc.Version
import it.leddaz.morpheupdater.utils.misc.VolleyCallBack
import org.apache.commons.codec.binary.Hex
import org.apache.commons.codec.digest.DigestUtils
import java.io.File
import java.io.FileInputStream
import kotlin.concurrent.thread

private var latestYtHash = ""
private var latestYtmHash = ""

/**
 * The app's main activity, started at launch.
 * @return The activity
 * @author Leonardo Ledda (LeddaZ)
 */
class MainActivity : AppCompatActivity() {
    private var installedYtVersion = Version("99.99")
    private var latestYtVersion = Version("0.0")
    private var ytDownloadUrl = ""
    private var ytChangelog = ""
    private var installedYtmVersion = Version("99.99")
    private var latestYtmVersion = Version("0.0")
    private var ytmDownloadUrl = ""
    private var ytmChangelog = ""
    private var installedUpdaterVersion = Version("99.99")
    private var latestUpdaterVersion = Version("0.0")
    private var latestUpdaterCommit = ""
    private var updaterDownloadUrl = ""
    private var updaterChangelog = ""
    private var installedMicroGVersion = Version("99.99")
    private var latestMicroGVersion = Version("0.0")
    private var microGDownloadUrl = ""
    private lateinit var ytDownloadProgress: LinearProgressIndicator
    private lateinit var ytmDownloadProgress: LinearProgressIndicator
    private lateinit var microGDownloadProgress: LinearProgressIndicator
    private lateinit var updaterDownloadProgress: LinearProgressIndicator
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    /**
     * Actions executed when the activity is created at runtime.
     * @property savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        DynamicColors.applyToActivityIfAvailable(this)
        setContentView(R.layout.activity_main)

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)

        swipeRefreshLayout.setOnRefreshListener {
            refresh()
        }

        ytDownloadProgress = findViewById(R.id.yt_download_progress)
        ytmDownloadProgress = findViewById(R.id.ytm_download_progress)
        microGDownloadProgress = findViewById(R.id.microg_download_progress)
        updaterDownloadProgress = findViewById(R.id.updater_download_progress)

        val updaterCardTitle = findViewById<TextView>(R.id.updater_title)
        updaterCardTitle.text = getString(R.string.app_name)
        Log.i(LOG_TAG, "Device fingerprint: ${Build.FINGERPRINT}")
        refresh()

        val ytCard = findViewById<MaterialCardView>(R.id.yt_info_card)
        ytCard.setOnLongClickListener {
            val dialogFragment = ChangelogDialog(ytChangelog, false)
            dialogFragment.show(supportFragmentManager, "ChangelogDialog")
            true
        }

        val ytmCard = findViewById<MaterialCardView>(R.id.ytm_info_card)
        ytmCard.setOnLongClickListener {
            val dialogFragment = ChangelogDialog(ytmChangelog, false)
            dialogFragment.show(supportFragmentManager, "ChangelogDialog")
            true
        }

        val microGCard = findViewById<MaterialCardView>(R.id.microg_info_card)
        microGCard.setOnLongClickListener {
            openLink(
                "https://github.com/MorpheApp/MicroG-RE/releases/tag/${latestMicroGVersion}",
                this
            )
            true
        }

        val updaterCard = findViewById<MaterialCardView>(R.id.updater_info_card)
        if (IS_DEBUG) {
            updaterCard.setOnLongClickListener {
                val dialogFragment = ChangelogDialog(
                    updaterChangelog, true,
                    latestUpdaterCommit
                )
                dialogFragment.show(supportFragmentManager, "ChangelogDialog")
                true
            }
        } else {
            updaterCard.setOnLongClickListener {
                val dialogFragment = ChangelogDialog(
                    updaterChangelog, true,
                    latestUpdaterVersion.toString()
                )
                dialogFragment.show(supportFragmentManager, "ChangelogDialog")
                true
            }
        }

        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        ytCard.isVisible = prefs.getBoolean(KEY_YT, true)
        ytmCard.isVisible = prefs.getBoolean(KEY_YTM, true)
        microGCard.isVisible = ytCard.isVisible || ytmCard.isVisible

        val topAppBar = findViewById<MaterialToolbar>(R.id.topAppBar)
        topAppBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.about -> {
                    openLink(
                        "https://github.com/$APP_REPO",
                        this
                    )
                }

                R.id.settings -> {
                    val intent = Intent(this@MainActivity, SettingsActivity::class.java)
                    this@MainActivity.startActivity(intent)
                }
            }
            false
        }

        requestInstallPermission(this)
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    /**
     * Gets the installed and latest versions of Morphe YouTube,
     * YouTube Music, MicroG-RE and Morphe Updater.
     * @property callback callback used to detect if the download was
     *                    successful
     */
    private fun getVersionsAndChangelogs(callback: VolleyCallBack) {
        // Installed versions
        getAppVersion(
            MICROG_PACKAGE,
            findViewById(R.id.installed_microg_version),
            installedMicroGVersion,
            findViewById(R.id.microg_download_button)
        )

        if (isAppInstalled(this, MICROG_PACKAGE)) {
            getAppVersion(
                YT_PACKAGE,
                findViewById(R.id.installed_yt_version),
                installedYtVersion,
                findViewById(R.id.yt_download_button)
            )

            getAppVersion(
                YTM_PACKAGE,
                findViewById(R.id.installed_ytm_version),
                installedYtmVersion,
                findViewById(R.id.ytm_download_button)
            )
        }

        getAppVersion(
            BuildConfig.APPLICATION_ID,
            findViewById(R.id.installed_updater_version),
            installedUpdaterVersion,
            findViewById(R.id.updater_download_button)
        )

        // Latest versions, hashes and changelogs
        val queue = Volley.newRequestQueue(this)
        val morpheJsonUrl =
            "https://raw.githubusercontent.com/$APK_REPO/main/updater.json"
        val updaterApiUrl = "https://api.github.com/repos/$APP_REPO/releases/latest"
        val updaterCommitUrl = "https://api.github.com/repos/$APP_REPO/commits/master"
        val updaterDebugApiUrl =
            "https://api.github.com/repos/$APP_REPO/releases/tags/dev"
        val microGApiUrl = "https://api.github.com/repos/MorpheApp/MicroG-RE/releases/latest"
        val ytChangelogUrl =
            "https://raw.githubusercontent.com/$APK_REPO/refs/heads/main/changelogs/yt.md"
        val ytmChangelogUrl =
            "https://raw.githubusercontent.com/$APK_REPO/refs/heads/main/changelogs/ytm.md"
        var morpheReply: MorpheJsonObject
        var updaterReleaseReply: UpdaterReleaseJSONObject
        var updaterDebugReply: UpdaterDebugJSONObject
        var updaterBodyReply: UpdaterBodyJSONObject
        var microGReply: GmsCoreJSONObject

        val urlPrefix = "https://github.com/$APK_REPO/releases/download/"

        val morpheRequest = StringRequest(GET, morpheJsonUrl, { response ->
            try {
                morpheReply =
                    Gson().fromJson(response, object : TypeToken<MorpheJsonObject>() {}.type)
                latestYtVersion = Version(morpheReply.latestYtVersion)
                latestYtHash = morpheReply.latestYtHash
                latestYtmVersion = Version(morpheReply.latestYtmVersion)
                ytDownloadUrl =
                    urlPrefix + morpheReply.latestYtDate + "-yt/yt-signed.apk"
                val preferredABI: String = Build.SUPPORTED_ABIS[0]
                Log.i(LOG_TAG, "Preferred ABI: $preferredABI")
                when (preferredABI) {
                    "armeabi-v7a" -> latestYtmHash =
                        morpheReply.latestYtmHashArm

                    "arm64-v8a" -> latestYtmHash =
                        morpheReply.latestYtmHashArm64

                    "x86" -> latestYtmHash = morpheReply.latestYtmHashX86
                    "x86_64" -> latestYtmHash = morpheReply.latestYtmHashX64
                }
                ytmDownloadUrl = urlPrefix + morpheReply.latestYtmDate +
                        "-ytm/ytm-$preferredABI-signed.apk"
                callback.onSuccess()
            } catch (_: JsonSyntaxException) {
                Toast.makeText(this, R.string.json_error, Toast.LENGTH_SHORT).show()
            }
        }, { error ->
            when (error.networkResponse?.statusCode) {
                403 -> {
                    Toast.makeText(this, R.string.rate_limit, Toast.LENGTH_LONG).show()
                    Log.e(LOG_TAG, "GitHub rate limit")
                }

                else -> {
                    Log.e(LOG_TAG, "Volley Error: ${error.message}")
                }
            }
        })

        val updaterReleaseRequest = StringRequest(GET, updaterApiUrl, { response ->
            try {
                updaterReleaseReply =
                    Gson().fromJson(
                        response,
                        object : TypeToken<UpdaterReleaseJSONObject>() {}.type
                    )
                updaterBodyReply =
                    Gson().fromJson(response, object : TypeToken<UpdaterBodyJSONObject>() {}.type)
                latestUpdaterVersion = Version(updaterReleaseReply.latestUpdaterVersion)
                updaterChangelog = updaterBodyReply.latestUpdaterBody
                updaterDownloadUrl =
                    "https://github.com/$APP_REPO/releases/download/" +
                            latestUpdaterVersion + "/app-release.apk"
                callback.onSuccess()
            } catch (_: JsonSyntaxException) {
                Toast.makeText(this, R.string.json_error, Toast.LENGTH_SHORT).show()
            }
        }, { error ->
            when (error.networkResponse?.statusCode) {
                403 -> {
                    Toast.makeText(this, R.string.rate_limit, Toast.LENGTH_LONG).show()
                    Log.e(LOG_TAG, "GitHub rate limit")
                }

                else -> {
                    Log.e(LOG_TAG, "Volley Error: ${error.message}")
                }
            }
        })

        val updaterDevRequest = StringRequest(GET, updaterCommitUrl, { response ->
            try {
                updaterDebugReply =
                    Gson().fromJson(response, object : TypeToken<UpdaterDebugJSONObject>() {}.type)
                latestUpdaterCommit = updaterDebugReply.latestUpdaterCommit.take(7)
                updaterDownloadUrl =
                    "https://github.com/$APP_REPO/releases/download/dev/app-debug-signed.apk"
                callback.onSuccess()
            } catch (_: JsonSyntaxException) {
                Toast.makeText(this, R.string.json_error, Toast.LENGTH_SHORT).show()
            }
        }, { error ->
            when (error.networkResponse?.statusCode) {
                403 -> {
                    Toast.makeText(this, R.string.rate_limit, Toast.LENGTH_LONG).show()
                    Log.e(LOG_TAG, "GitHub rate limit")
                }

                else -> {
                    Log.e(LOG_TAG, "Volley Error: ${error.message}")
                }
            }
        })

        val updaterDevChangelogRequest = StringRequest(GET, updaterDebugApiUrl, { response ->
            try {
                updaterBodyReply =
                    Gson().fromJson(response, object : TypeToken<UpdaterBodyJSONObject>() {}.type)
                updaterChangelog = updaterBodyReply.latestUpdaterBody
                callback.onSuccess()
            } catch (_: JsonSyntaxException) {
                Toast.makeText(this, R.string.json_error, Toast.LENGTH_SHORT).show()
            }
        }, { error ->
            when (error.networkResponse?.statusCode) {
                403 -> {
                    Toast.makeText(this, R.string.rate_limit, Toast.LENGTH_LONG).show()
                    Log.e(LOG_TAG, "GitHub rate limit")
                }

                else -> {
                    Log.e(LOG_TAG, "Volley Error: ${error.message}")
                }
            }
        })

        val microGRequest = StringRequest(GET, microGApiUrl, { response ->
            try {
                microGReply =
                    Gson().fromJson(response, object : TypeToken<GmsCoreJSONObject>() {}.type)
                latestMicroGVersion = Version(microGReply.latestGmsCoreVersion)
                microGDownloadUrl = microGReply.assets[0].latestGmsCoreUrl
                callback.onSuccess()
            } catch (_: JsonSyntaxException) {
                Toast.makeText(this, R.string.json_error, Toast.LENGTH_SHORT).show()
            }
        }, { error ->
            when (error.networkResponse?.statusCode) {
                403 -> {
                    Toast.makeText(this, R.string.rate_limit, Toast.LENGTH_LONG).show()
                    Log.e(LOG_TAG, "GitHub rate limit")
                }

                else -> {
                    Log.e(LOG_TAG, "Volley Error: ${error.message}")
                }
            }
        })

        val ytChangelogRequest = StringRequest(GET, ytChangelogUrl, { response ->
            try {
                ytChangelog = response
                callback.onSuccess()
            } catch (_: JsonSyntaxException) {
                Toast.makeText(this, R.string.json_error, Toast.LENGTH_SHORT).show()
            }
        }, { error ->
            when (error.networkResponse?.statusCode) {
                403 -> {
                    Toast.makeText(this, R.string.rate_limit, Toast.LENGTH_LONG).show()
                    Log.e(LOG_TAG, "GitHub rate limit")
                }

                else -> {
                    Log.e(LOG_TAG, "Volley Error: ${error.message}")
                }
            }
        })

        val ytmChangelogRequest = StringRequest(GET, ytmChangelogUrl, { response ->
            try {
                ytmChangelog = response
                callback.onSuccess()
            } catch (_: JsonSyntaxException) {
                Toast.makeText(this, R.string.json_error, Toast.LENGTH_SHORT).show()
            }
        }, { error ->
            when (error.networkResponse?.statusCode) {
                403 -> {
                    Toast.makeText(this, R.string.rate_limit, Toast.LENGTH_LONG).show()
                    Log.e(LOG_TAG, "GitHub rate limit")
                }

                else -> {
                    Log.e(LOG_TAG, "Volley Error: ${error.message}")
                }
            }
        })

        queue.add(morpheRequest)
        queue.add(microGRequest)
        queue.add(ytChangelogRequest)
        queue.add(ytmChangelogRequest)
        if (IS_DEBUG) {
            queue.add(updaterDevRequest)
            queue.add(updaterDevChangelogRequest)
        } else
            queue.add(updaterReleaseRequest)
    }

    /**
     * Compares versions.
     */
    private fun compareVersions() {
        compareAppVersion(
            MICROG_PACKAGE, installedMicroGVersion,
            latestMicroGVersion, findViewById(R.id.microg_update_status),
            findViewById(R.id.microg_download_button),
            findViewById(R.id.microg_uninstall_button)
        )

        if (isAppInstalled(this, MICROG_PACKAGE)) {
            compareAppVersion(
                YT_PACKAGE, installedYtVersion,
                latestYtVersion, findViewById(R.id.yt_update_status),
                findViewById(R.id.yt_download_button),
                findViewById(R.id.yt_uninstall_button)
            )

            compareAppVersion(
                YTM_PACKAGE, installedYtmVersion,
                latestYtmVersion, findViewById(R.id.ytm_update_status),
                findViewById(R.id.ytm_download_button),
                findViewById(R.id.ytm_uninstall_button)
            )
        } else {
            val reVancedTextView = findViewById<TextView>(R.id.yt_update_status)
            reVancedTextView.text = getString(R.string.microg_dialog_title)
            val reVancedMusicTextView = findViewById<TextView>(R.id.ytm_update_status)
            reVancedMusicTextView.text = getString(R.string.microg_dialog_title)
        }
        compareAppVersion(
            BuildConfig.APPLICATION_ID, installedUpdaterVersion,
            latestUpdaterVersion, findViewById(R.id.updater_update_status),
            findViewById(R.id.updater_download_button),
            null
        )
    }

    /**
     * Downloads Morphe YT when the button is clicked.
     * @property view the view which contains the button.
     */
    fun downloadYt(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
        AppInstaller(
            this,
            window,
            ytDownloadUrl,
            "revanced-nonroot-signed.apk",
            ytDownloadProgress,
            findViewById(R.id.yt_download_button)
        )
    }

    /**
     * Downloads Morphe YTM when the button is clicked.
     * @property view the view which contains the button.
     */
    fun downloadYtm(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
        AppInstaller(
            this,
            window,
            ytmDownloadUrl,
            "revanced-music-nonroot-signed.apk",
            ytmDownloadProgress,
            findViewById(R.id.ytm_download_button)
        )
    }

    /**
     * Downloads MicroG-RE when the button is clicked.
     * @property view the view which contains the button.
     */
    fun downloadMicroG(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
        AppInstaller(
            this,
            window,
            microGDownloadUrl,
            "microg.apk",
            microGDownloadProgress,
            findViewById(R.id.microg_download_button)
        )
    }

    /**
     * Downloads Morphe Updater when the button is clicked.
     * @property view the view which contains the button.
     */
    fun downloadUpdater(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
        AppInstaller(
            this,
            window,
            updaterDownloadUrl,
            "app-release.apk",
            updaterDownloadProgress,
            findViewById(R.id.updater_download_button)
        )
    }

    /**
     * Shows the uninstall prompt for an app.
     * @property packageName the app to uninstall
     */
    private fun showUninstallPrompt(packageName: String) {
        val intent = Intent(Intent.ACTION_DELETE)
        intent.data = Uri.fromParts("package", packageName, null)
        startActivity(intent)
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
                        APP_VERSION.substringBefore(' ')
                    installedTextView.text =
                        getString(R.string.installed_app_version, installedVersion.version)
                } else {
                    installedTextView.text =
                        getString(R.string.installed_app_version, APP_VERSION)
                }
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
     * @property installButton the app's install/update button
     * @property uninstallButton the app's uninstall button (if present)
     */
    private fun compareAppVersion(
        packageName: String, installedVersion: Version,
        latestVersion: Version, updateStatusTextView: TextView,
        installButton: Button, uninstallButton: Button?
    ) {
        if (packageName == "$UPDATER_PACKAGE.dev") {
            val currentCommit = APP_VERSION.substring(7, 14)
            if (currentCommit == latestUpdaterCommit) {
                updateStatusTextView.text = getString(R.string.no_update_available)
                installButton.isEnabled = false
                installButton.visibility = View.VISIBLE
                uninstallButton?.isEnabled = false
                uninstallButton?.visibility = View.GONE
            } else {
                updateStatusTextView.text = getString(R.string.update_available)
                installButton.isEnabled = true
                installButton.visibility = View.VISIBLE
                uninstallButton?.isEnabled = false
                uninstallButton?.visibility = View.GONE
            }
        } else {
            if (installedVersion.compareTo(latestVersion) == -1) {
                updateStatusTextView.text = getString(R.string.update_available)
                installButton.isEnabled = true
                installButton.visibility = View.VISIBLE
                uninstallButton?.isEnabled = false
                uninstallButton?.visibility = View.GONE
            } else if (installedVersion.compareTo(latestVersion) == 0) {
                if (packageName != MICROG_PACKAGE && packageName != UPDATER_PACKAGE) {
                    var latestHash = ""
                    when (packageName) {
                        YT_PACKAGE -> latestHash = getLatestYtHash()
                        YTM_PACKAGE -> latestHash = getLatestYtmHash()
                    }
                    thread {
                        compareHashes(
                            latestHash,
                            updateStatusTextView,
                            packageName,
                            installButton,
                            uninstallButton
                        )
                    }
                } else {
                    updateStatusTextView.text = getString(R.string.no_update_available)
                    installButton.isEnabled = false
                    installButton.visibility = View.VISIBLE
                    uninstallButton?.isEnabled = false
                    uninstallButton?.visibility = View.GONE
                }
            } else if (installedVersion.version != "99.99") {
                updateStatusTextView.text = getString(R.string.newer_version_installed)
                installButton.isEnabled = false
                installButton.visibility = View.GONE
                uninstallButton?.isEnabled = true
                uninstallButton?.visibility = View.VISIBLE
            } else {
                updateStatusTextView.text = getString(R.string.app_not_installed)
                installButton.isEnabled = true
                installButton.visibility = View.VISIBLE
                uninstallButton?.isEnabled = false
                uninstallButton?.visibility = View.GONE
            }
        }
        uninstallButton?.setOnClickListener {
            showUninstallPrompt(packageName)
        }
    }

    /**
     * Compares hashes.
     * @property latestHash the latest app version's hash
     * @property updateStatusTextView the TextView with the app update status
     * @property packageName package name
     * @property installButton the app's install/update button
     * @property uninstallButton the app's uninstall button (if present)
     */
    private fun compareHashes(
        latestHash: String, updateStatusTextView: TextView, packageName: String,
        installButton: Button, uninstallButton: Button?
    ) {
        val pInfo: PackageInfo = packageManager.getPackageInfo(packageName, 0)
        val file = pInfo.applicationInfo?.sourceDir?.let { File(it) }
        val installedAppHash = String(Hex.encodeHex(DigestUtils.sha256(FileInputStream(file))))
        if (installedAppHash == latestHash) {
            runOnUiThread {
                updateStatusTextView.text = getString(R.string.no_update_available)
                installButton.isEnabled = false
                installButton.visibility = View.VISIBLE
                uninstallButton?.isEnabled = false
                uninstallButton?.visibility = View.GONE
            }
        } else {
            runOnUiThread {
                updateStatusTextView.text = getString(R.string.update_available)
                installButton.isEnabled = true
                installButton.visibility = View.VISIBLE
                uninstallButton?.isEnabled = false
                uninstallButton?.visibility = View.GONE
            }
        }
    }

    /**
     * Refreshes the versions.
     */
    private fun refresh() {
        getVersionsAndChangelogs(object : VolleyCallBack {
            override fun onSuccess() {
                val latestYtTextView: TextView =
                    findViewById(R.id.latest_yt_version)
                latestYtTextView.text =
                    getString(R.string.latest_app_version, latestYtVersion)

                val latestYtmTextView: TextView =
                    findViewById(R.id.latest_ytm_version)
                latestYtmTextView.text =
                    getString(R.string.latest_app_version, latestYtmVersion)

                val latestMicroGTextView: TextView =
                    findViewById(R.id.latest_microg_version)
                latestMicroGTextView.text =
                    getString(R.string.latest_app_version, latestMicroGVersion)

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

        val ytCard = findViewById<MaterialCardView>(R.id.yt_info_card)
        val ytmCard = findViewById<MaterialCardView>(R.id.ytm_info_card)
        val microGCard = findViewById<MaterialCardView>(R.id.microg_info_card)

        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        ytCard.isVisible = prefs.getBoolean(KEY_YT, true)
        ytmCard.isVisible = prefs.getBoolean(KEY_YTM, true)
        microGCard.isVisible = ytCard.isVisible || ytmCard.isVisible

        swipeRefreshLayout.isRefreshing = false
    }

    /**
     * Companion object
     */
    companion object {
        /**
         * Returns the latest ReVanced hash.
         * @return Latest ReVanced hash.
         */
        fun getLatestYtHash(): String {
            return latestYtHash
        }

        /**
         * Returns the latest YouTube Music hash.
         * @return Latest YouTube Music hash.
         */
        fun getLatestYtmHash(): String {
            return latestYtmHash
        }
    }
}
