package it.leddaz.revancedupdater

import android.content.Intent
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
import androidx.core.view.isVisible
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.Request.Method.GET
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.DynamicColors
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textview.MaterialTextView
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import it.leddaz.revancedupdater.dialogs.ChangelogDialog
import it.leddaz.revancedupdater.utils.json.GmsCoreJSONObject
import it.leddaz.revancedupdater.utils.json.ReVancedJSONObject
import it.leddaz.revancedupdater.utils.json.UpdaterBodyJSONObject
import it.leddaz.revancedupdater.utils.json.UpdaterDebugJSONObject
import it.leddaz.revancedupdater.utils.json.UpdaterReleaseJSONObject
import it.leddaz.revancedupdater.utils.misc.AppInstaller
import it.leddaz.revancedupdater.utils.misc.CommonStuff.APP_VERSION
import it.leddaz.revancedupdater.utils.misc.CommonStuff.GMSCORE_PACKAGE
import it.leddaz.revancedupdater.utils.misc.CommonStuff.GMS_PACKAGE
import it.leddaz.revancedupdater.utils.misc.CommonStuff.HMS_PACKAGE
import it.leddaz.revancedupdater.utils.misc.CommonStuff.IS_DEBUG
import it.leddaz.revancedupdater.utils.misc.CommonStuff.KEY_X
import it.leddaz.revancedupdater.utils.misc.CommonStuff.KEY_YT
import it.leddaz.revancedupdater.utils.misc.CommonStuff.KEY_YTM
import it.leddaz.revancedupdater.utils.misc.CommonStuff.LOG_TAG
import it.leddaz.revancedupdater.utils.misc.CommonStuff.MUSIC_PACKAGE
import it.leddaz.revancedupdater.utils.misc.CommonStuff.PREFS_NAME
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
    private var reVancedCl = ""
    private var installedReVancedMusicVersion = Version("99.99")
    private var latestReVancedMusicVersion = Version("0.0")
    private var musicDownloadUrl = ""
    private var musicCl = ""
    private var installedUpdaterVersion = Version("99.99")
    private var latestUpdaterVersion = Version("0.0")
    private var latestUpdaterCommit = ""
    private var updaterDownloadUrl = ""
    private var updaterCl = ""
    private var installedGmsCoreVersion = Version("99.99")
    private var latestGmsCoreVersion = Version("0.0")
    private var gmsCoreDownloadUrl = ""
    private var installedXVersion = Version("99.99")
    private var latestXVersion = Version("0.0")
    private var xDownloadUrl = ""
    private var xCl = ""
    private lateinit var revancedIndicator: LinearProgressIndicator
    private lateinit var musicIndicator: LinearProgressIndicator
    private lateinit var gmsCoreIndicator: LinearProgressIndicator
    private lateinit var xIndicator: LinearProgressIndicator
    private lateinit var updaterIndicator: LinearProgressIndicator
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

        revancedIndicator = findViewById(R.id.revanced_download_progress)
        musicIndicator = findViewById(R.id.music_download_progress)
        gmsCoreIndicator = findViewById(R.id.microg_download_progress)
        xIndicator = findViewById(R.id.x_download_progress)
        updaterIndicator = findViewById(R.id.updater_download_progress)

        val updaterCardTitle = findViewById<MaterialTextView>(R.id.updater_title)
        updaterCardTitle.text = getString(R.string.app_name)
        Log.i(LOG_TAG, "Device fingerprint: ${Build.FINGERPRINT}")
        refresh()

        val reVancedCard = findViewById<MaterialCardView>(R.id.revanced_info_card)
        reVancedCard.setOnLongClickListener {
            val dialogFragment = ChangelogDialog(reVancedCl, false)
            dialogFragment.show(supportFragmentManager, "ChangelogDialog")
            true
        }

        val reVancedMusicCard = findViewById<MaterialCardView>(R.id.music_info_card)
        reVancedMusicCard.setOnLongClickListener {
            val dialogFragment = ChangelogDialog(musicCl, false)
            dialogFragment.show(supportFragmentManager, "ChangelogDialog")
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
            val dialogFragment = ChangelogDialog(xCl, false)
            dialogFragment.show(supportFragmentManager, "ChangelogDialog")
            true
        }

        val updaterCard = findViewById<MaterialCardView>(R.id.updater_info_card)
        if (IS_DEBUG) {
            updaterCard.setOnLongClickListener {
                val dialogFragment = ChangelogDialog(
                    updaterCl, true,
                    latestUpdaterCommit
                )
                dialogFragment.show(supportFragmentManager, "ChangelogDialog")
                true
            }
        } else {
            updaterCard.setOnLongClickListener {
                val dialogFragment = ChangelogDialog(
                    updaterCl, true,
                    latestUpdaterVersion.toString()
                )
                dialogFragment.show(supportFragmentManager, "ChangelogDialog")
                true
            }
        }

        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        reVancedCard.isVisible = prefs.getBoolean(KEY_YT, true)
        reVancedMusicCard.isVisible = prefs.getBoolean(KEY_YTM, true)
        microGCard.isVisible = reVancedCard.isVisible || reVancedMusicCard.isVisible
        xCard.isVisible = prefs.getBoolean(KEY_X, true)

        val topAppBar = findViewById<MaterialToolbar>(R.id.topAppBar)
        topAppBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.about -> {
                    openLink(
                        "https://github.com/LeddaZ/RevancedUpdater",
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
     * Gets the installed and latest versions of YouTube ReVanced,
     * ReVanced Music, ReVanced GmsCore and ReVanced Updater.
     * @property callback callback used to detect if the download was
     *                    successful
     */
    private fun getVersionsAndChangelogs(callback: VolleyCallBack) {
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

        // Latest versions, hashes and changelogs
        val queue = Volley.newRequestQueue(this)
        val reVancedJSONUrl =
            "https://raw.githubusercontent.com/LeddaZ/revanced-repo/main/updater.json"
        val updaterAPIUrl = "https://api.github.com/repos/LeddaZ/ReVancedUpdater/releases/latest"
        val updaterCommitUrl = "https://api.github.com/repos/LeddaZ/ReVancedUpdater/commits/master"
        val updaterDebugAPIUrl =
            "https://api.github.com/repos/LeddaZ/ReVancedUpdater/releases/tags/dev"
        val gmsCoreAPIUrl = "https://api.github.com/repos/ReVanced/GmsCore/releases/latest"
        val ytClUrl =
            "https://raw.githubusercontent.com/LeddaZ/revanced-repo/refs/heads/main/changelogs/revanced.md"
        val ytmClUrl =
            "https://raw.githubusercontent.com/LeddaZ/revanced-repo/refs/heads/main/changelogs/music.md"
        val xClUrl =
            "https://raw.githubusercontent.com/LeddaZ/revanced-repo/refs/heads/main/changelogs/x.md"
        var reVancedReply: ReVancedJSONObject
        var updaterReleaseReply: UpdaterReleaseJSONObject
        var updaterDebugReply: UpdaterDebugJSONObject
        var updaterBodyReply: UpdaterBodyJSONObject
        var gmsCoreReply: GmsCoreJSONObject

        val urlPrefix = "https://github.com/LeddaZ/revanced-repo/releases/download/"

        val reVancedRequest = StringRequest(GET, reVancedJSONUrl, { response ->
            try {
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
                    "armeabi-v7a" -> latestReVancedMusicHash =
                        reVancedReply.latestReVancedMusicHashArm

                    "arm64-v8a" -> latestReVancedMusicHash =
                        reVancedReply.latestReVancedMusicHashArm64

                    "x86" -> latestReVancedMusicHash = reVancedReply.latestReVancedMusicHashX86
                    "x86_64" -> latestReVancedMusicHash = reVancedReply.latestReVancedMusicHashX64
                }
                musicDownloadUrl = urlPrefix + reVancedReply.latestReVancedMusicDate +
                        "-ytm/ytm-$preferredABI-signed.apk"
                latestXVersion = Version(reVancedReply.latestXVersion)
                xDownloadUrl = urlPrefix + reVancedReply.latestXDate + "-x/x-signed.apk"
                latestXHash = reVancedReply.latestXHash
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

        val updaterReleaseRequest = StringRequest(GET, updaterAPIUrl, { response ->
            try {
                updaterReleaseReply =
                    Gson().fromJson(
                        response,
                        object : TypeToken<UpdaterReleaseJSONObject>() {}.type
                    )
                updaterBodyReply =
                    Gson().fromJson(response, object : TypeToken<UpdaterBodyJSONObject>() {}.type)
                latestUpdaterVersion = Version(updaterReleaseReply.latestUpdaterVersion)
                updaterCl = updaterBodyReply.latestUpdaterBody
                updaterDownloadUrl =
                    "https://github.com/LeddaZ/ReVancedUpdater/releases/download/" +
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
                latestUpdaterCommit = updaterDebugReply.latestUpdaterCommit.substring(0, 7)
                updaterDownloadUrl =
                    "https://github.com/LeddaZ/ReVancedUpdater/releases/download/dev/app-debug-signed.apk"
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

        val updaterDebugClBodyRequest = StringRequest(GET, updaterDebugAPIUrl, { response ->
            try {
                updaterBodyReply =
                    Gson().fromJson(response, object : TypeToken<UpdaterBodyJSONObject>() {}.type)
                updaterCl = updaterBodyReply.latestUpdaterBody
                updaterDownloadUrl =
                    "https://github.com/LeddaZ/ReVancedUpdater/releases/download/dev/app-debug-signed.apk"
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

        val gmsCoreRequest = StringRequest(GET, gmsCoreAPIUrl, { response ->
            try {
                gmsCoreReply =
                    Gson().fromJson(response, object : TypeToken<GmsCoreJSONObject>() {}.type)
                latestGmsCoreVersion = Version(gmsCoreReply.latestGmsCoreVersion.substring(1))
                gmsCoreDownloadUrl =
                    if (isAppInstalled(this, HMS_PACKAGE) && !isAppInstalled(this, GMS_PACKAGE))
                        gmsCoreReply.assets[0].latestGmsCoreUrl
                    else
                        gmsCoreReply.assets[1].latestGmsCoreUrl
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

        val reVancedClRequest = StringRequest(GET, ytClUrl, { response ->
            try {
                reVancedCl = response
                callback.onSuccess()
            } catch (_: JsonSyntaxException) {
                Toast.makeText(this, R.string.json_error, Toast.LENGTH_SHORT).show()
            }
        }, {})

        val musicClRequest = StringRequest(GET, ytmClUrl, { response ->
            try {
                musicCl = response
                callback.onSuccess()
            } catch (_: JsonSyntaxException) {
                Toast.makeText(this, R.string.json_error, Toast.LENGTH_SHORT).show()
            }
        }, {})

        val xClRequest = StringRequest(GET, xClUrl, { response ->
            try {
                xCl = response
                callback.onSuccess()
            } catch (_: JsonSyntaxException) {
                Toast.makeText(this, R.string.json_error, Toast.LENGTH_SHORT).show()
            }
        }, {})

        queue.add(reVancedRequest)
        queue.add(gmsCoreRequest)
        queue.add(reVancedClRequest)
        queue.add(musicClRequest)
        queue.add(xClRequest)
        if (IS_DEBUG) {
            queue.add(updaterDevRequest)
            queue.add(updaterDebugClBodyRequest)
        } else
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
            window,
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
            window,
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
            window,
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
            this, window, xDownloadUrl, "x.apk", xIndicator, findViewById(R.id.x_download_button)
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
            window,
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
                    when (packageName) {
                        REVANCED_PACKAGE -> latestHash = getLatestReVancedHash()
                        MUSIC_PACKAGE -> latestHash = getLatestReVancedMusicHash()
                        X_PACKAGE -> latestHash = getLatestReVancedXHash()
                    }
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
     * Refreshes the versions.
     */
    private fun refresh() {
        getVersionsAndChangelogs(object : VolleyCallBack {
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

        val reVancedCard = findViewById<MaterialCardView>(R.id.revanced_info_card)
        val reVancedMusicCard = findViewById<MaterialCardView>(R.id.music_info_card)
        val microGCard = findViewById<MaterialCardView>(R.id.microg_info_card)
        val xCard = findViewById<MaterialCardView>(R.id.x_info_card)

        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        reVancedCard.isVisible = prefs.getBoolean(KEY_YT, true)
        reVancedMusicCard.isVisible = prefs.getBoolean(KEY_YTM, true)
        microGCard.isVisible = reVancedCard.isVisible || reVancedMusicCard.isVisible
        xCard.isVisible = prefs.getBoolean(KEY_X, true)

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
