package it.leddaz.revancedupdater

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request.Method.GET
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import it.leddaz.revancedupdater.utils.jsonobjects.ReVancedJSONObject
import it.leddaz.revancedupdater.utils.misc.ArchDetector
import it.leddaz.revancedupdater.utils.misc.Utils.LOG_TAG
import it.leddaz.revancedupdater.utils.misc.Utils.compareAppVersion
import it.leddaz.revancedupdater.utils.misc.Utils.dlAndInstall
import it.leddaz.revancedupdater.utils.misc.Utils.getAppVersion
import it.leddaz.revancedupdater.utils.misc.Utils.openLink
import it.leddaz.revancedupdater.utils.misc.Version
import it.leddaz.revancedupdater.utils.misc.VolleyCallBack
import java.io.File


private var installedReVancedVersion = Version("99.99")
private var latestReVancedVersion = Version("0.0")
private var latestReVancedHash = ""
private var installedReVancedMusicVersion = Version("99.99")
private var latestReVancedMusicVersion = Version("0.0")
private var latestReVancedMusicHash = ""
private var installedMicroGVersion = Version("99.99")
private var latestMicroGVersion = Version("0.0")
private var downloadUrl = ""
private var musicDownloadUrl = ""
private var microGDownloadUrl = ""

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
        setContentView(R.layout.activity_main)
        refresh(this)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    finishAffinity()
                    finish()
                }
            }
        )

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
            openLink(
                "https://github.com/LeddaZ/revanced-repo/blob/main/changelogs/music.md",
                this
            )
            true
        }

        bottomNavigationView.selectedItemId = R.id.revanced
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.revanced -> {
                    startActivity(Intent(applicationContext, MainActivity::class.java))
                    return@setOnItemSelectedListener true
                }

                R.id.updater -> {
                    startActivity(Intent(applicationContext, UpdaterActivity::class.java))
                    return@setOnItemSelectedListener true
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
                "app.revanced.android.youtube", this,
                findViewById(R.id.installed_revanced_version),
                installedReVancedVersion, findViewById(R.id.revanced_update_status),
                findViewById(R.id.revanced_download_button)
            )

            getAppVersion(
                "app.revanced.android.apps.youtube.music", this,
                findViewById(R.id.installed_music_version),
                installedReVancedMusicVersion, findViewById(R.id.music_update_status),
                findViewById(R.id.music_download_button)
            )
        }

        getAppVersion(
            "com.mgoogle.android.gms", this,
            findViewById(R.id.installed_microg_version),
            installedMicroGVersion, findViewById(R.id.microg_update_status),
            findViewById(R.id.microg_download_button)
        )

        // Latest versions and ReVanced hashes
        val queue = Volley.newRequestQueue(this)
        val url = "https://raw.githubusercontent.com/LeddaZ/revanced-repo/main/updater.json"
        var reply: ReVancedJSONObject

        val urlPrefix = "https://github.com/LeddaZ/revanced-repo/releases/download/"

        // Request a string response from the provided URL.
        val stringRequest = StringRequest(GET, url, { response ->
            reply = Gson().fromJson(response, object : TypeToken<ReVancedJSONObject>() {}.type)
            latestReVancedVersion = Version(reply.latestReVancedVersion)
            latestReVancedHash = reply.latestReVancedHash
            latestReVancedMusicVersion = Version(reply.latestReVancedMusicVersion)
            latestMicroGVersion = Version(reply.latestMicroGVersion)
            downloadUrl = urlPrefix + reply.latestReVancedDate + "-yt/revanced-nonroot-signed.apk"
            microGDownloadUrl = urlPrefix + reply.latestReVancedDate + "-yt/vanced-microG.apk"
            val arch: String = ArchDetector.getArch()
            Log.i(LOG_TAG, "OS architecture: $arch")
            when (arch) {
                "arm" -> latestReVancedMusicHash = reply.latestReVancedMusicHashArm
                "arm64" -> latestReVancedMusicHash = reply.latestReVancedMusicHashArm64
                "x86" -> latestReVancedMusicHash = reply.latestReVancedMusicHashX86
                "x86_64" -> latestReVancedMusicHash = reply.latestReVancedMusicHashX64
            }
            musicDownloadUrl = urlPrefix + reply.latestReVancedMusicDate +
                    "-ytm/revanced-music-nonroot-$arch-signed.apk"
            callback.onSuccess()
        }, {})

        // Add the request to the RequestQueue.
        queue.add(stringRequest)
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
                    findViewById(R.id.revanced_download_button), this
                )
            }

            compareAppVersion(
                true, "app.revanced.android.apps.youtube.music", installedReVancedMusicVersion,
                latestReVancedMusicVersion, findViewById(R.id.music_update_status),
                findViewById(R.id.music_download_button), this
            )
        }


        compareAppVersion(
            false, "com.mgoogle.android.gms", installedMicroGVersion, latestMicroGVersion,
            findViewById(R.id.microg_update_status), findViewById(R.id.microg_download_button), this
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
     * Downloads Vanced microG when the button is clicked.
     * @property view the view which contains the button.
     */
    @Suppress("UNUSED_PARAMETER")
    fun downloadMicroG(view: View) {
        dlAndInstall("vanced-microG.apk", microGDownloadUrl, this)
    }

    /**
     * Refreshes the versions and deletes existing APKs.
     * @param context the app's context.
     */
    private fun refresh(context: Context) {
        val filenames = arrayOf(
            "revanced-music-nonroot-arm-signed.apk",
            "revanced-music-nonroot-arm64-signed.apk",
            "revanced-music-nonroot-x86-signed.apk",
            "revanced-music-nonroot-x86_64-signed.apk",
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

                val latestMicroGTextView: TextView = findViewById(R.id.latest_microg_version)
                latestMicroGTextView.text =
                    getString(R.string.latest_app_version, latestMicroGVersion)
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
