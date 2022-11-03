package it.leddaz.revancedupdater

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request.Method.GET
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import it.leddaz.revancedupdater.utils.jsonobjects.ReVancedJSONObject
import it.leddaz.revancedupdater.utils.misc.ArchDetector
import it.leddaz.revancedupdater.utils.misc.CommonMethods.compareAppVersion
import it.leddaz.revancedupdater.utils.misc.CommonMethods.dlAndInstall
import it.leddaz.revancedupdater.utils.misc.CommonMethods.getAppVersion
import it.leddaz.revancedupdater.utils.misc.CommonMethods.openLink
import it.leddaz.revancedupdater.utils.misc.Version
import it.leddaz.revancedupdater.utils.misc.VolleyCallBack


const val APP_VERSION = BuildConfig.VERSION_NAME
const val LOG_TAG = "ReVanced Updater"
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

        Log.i(LOG_TAG, "ReVanced Updater $APP_VERSION is here!")
        val appVersionTextView: TextView = findViewById(R.id.appVersion)
        appVersionTextView.text = getString(R.string.app_version, APP_VERSION)
        Toast.makeText(this, R.string.download_warning, Toast.LENGTH_LONG).show()
        refresh()
    }

    /**
     * Detects if Vanced microG is installed.
     * @return Vanced microG installation status
     */
    private fun isMicroGInstalled(): Boolean {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                this.packageManager.getPackageInfo(
                    "com.mgoogle.android.gms",
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                this.packageManager.getPackageInfo("com.mgoogle.android.gms", 0)
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Log.i(LOG_TAG, "Vanced microG is not installed, blocking ReVanced YT/YTM installation.")
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
                26, "8.0", "app.revanced.android.youtube", this,
                this.window.decorView, findViewById(R.id.installedReVancedVersion),
                installedReVancedVersion, findViewById(R.id.reVancedUpdateStatus),
                findViewById(R.id.reVancedButton)
            )

            getAppVersion(
                21, "5.0", "app.revanced.android.apps.youtube.music", this,
                this.window.decorView, findViewById(R.id.installedReVancedMusicVersion),
                installedReVancedMusicVersion, findViewById(R.id.reVancedMusicUpdateStatus),
                findViewById(R.id.reVancedMusicButton)
            )
        }

        getAppVersion(
            23, "6.0", "com.mgoogle.android.gms", this,
            this.window.decorView, findViewById(R.id.installedMicroGVersion),
            installedMicroGVersion, findViewById(R.id.microGUpdateStatus),
            findViewById(R.id.microGButton)
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
                "x86_64" -> latestReVancedMusicHash = reply.latestReVancedMusicHashX86_64
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
                    latestReVancedVersion, findViewById(R.id.reVancedUpdateStatus),
                    findViewById(R.id.reVancedButton), this
                )
            }

            compareAppVersion(
                true, "app.revanced.android.apps.youtube.music", installedReVancedMusicVersion,
                latestReVancedMusicVersion, findViewById(R.id.reVancedMusicUpdateStatus),
                findViewById(R.id.reVancedMusicButton), this
            )
        }

        compareAppVersion(
            false, "com.mgoogle.android.gms", installedMicroGVersion, latestMicroGVersion,
            findViewById(R.id.microGUpdateStatus), findViewById(R.id.microGButton), this
        )
    }

    /**
     * Downloads YouTube ReVanced when the button is clicked.
     * @property view the view which contains the button.
     */
    fun downloadReVanced(view: View) {
        dlAndInstall("revanced-nonroot-signed.apk", downloadUrl, this)
    }

    /**
     * Downloads ReVanced Music when the button is clicked.
     * @property view the view which contains the button.
     */
    fun downloadReVancedMusic(view: View) {
        dlAndInstall("revanced-music-nonroot-signed.apk", musicDownloadUrl, this)
    }

    /**
     * Downloads Vanced microG when the button is clicked.
     * @property view the view which contains the button.
     */
    fun downloadMicroG(view: View) {
        dlAndInstall("vanced-microG.apk", microGDownloadUrl, this)
    }

    /**
     * Called when the user presses the ReVanced changelog button.
     * @property view the view which contains the button
     */
    fun openReVancedChangelog(view: View) {
        openLink("https://github.com/LeddaZ/revanced-repo/blob/main/changelogs/revanced.md", this)
    }

    /**
     * Called when the user presses the ReVanced Music changelog button.
     * @property view the view which contains the button
     */
    fun openReVancedMusicChangelog(view: View) {
        openLink("https://github.com/LeddaZ/revanced-repo/blob/main/changelogs/music.md", this)
    }

    /**
     * Refreshes the versions.
     */
    private fun refresh() {
        getVersions(object : VolleyCallBack {
            override fun onSuccess() {
                val latestReVancedTextView: TextView = findViewById(R.id.latestReVancedVersion)
                latestReVancedTextView.text =
                    getString(R.string.latest_app_version, latestReVancedVersion)

                val latestReVancedMusicTextView: TextView =
                    findViewById(R.id.latestReVancedMusicVersion)
                latestReVancedMusicTextView.text =
                    getString(R.string.latest_app_version, latestReVancedMusicVersion)

                val latestMicroGTextView: TextView = findViewById(R.id.latestMicroGVersion)
                latestMicroGTextView.text =
                    getString(R.string.latest_app_version, latestMicroGVersion)
                compareVersions()
            }
        })
    }

    /**
     * Called when the user presses the Refresh button.
     * @property view the view which contains the button
     */
    fun refreshButton(view: View) {
        refresh()
    }

    /**
     * Called when the user presses the App info button.
     * @property view the view which contains the button
     */
    fun appInfo(view: View) {
        val intent = Intent(this, AppInfoActivity::class.java).apply {}
        startActivity(intent)
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
