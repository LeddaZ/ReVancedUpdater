package it.leddaz.revancedupdater

import android.app.DownloadManager
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.paris.extensions.style
import com.android.volley.Request.Method.GET
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import it.leddaz.revancedupdater.utils.apputils.AppInstaller
import it.leddaz.revancedupdater.utils.apputils.Downloader
import it.leddaz.revancedupdater.utils.jsonobjects.ReVancedJSONObject
import it.leddaz.revancedupdater.utils.other.ArchDetector
import it.leddaz.revancedupdater.utils.other.Version
import it.leddaz.revancedupdater.utils.other.VolleyCallBack
import org.apache.commons.codec.binary.Hex
import org.apache.commons.codec.digest.DigestUtils
import java.io.File
import java.io.FileInputStream


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
            /**
             * "bro that method is deprecated in A13"
             * ok I'll update
             * "bro you can't because the new method only works on A13 and your app runs on A6+"
             * ok I'll do both with a check
             * "bro that method is deprecated in A13"
             * Thanks Google
             */
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
            val installedReVancedTextView: TextView = findViewById(R.id.installedReVancedVersion)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val pInfo: PackageInfo =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            this.packageManager.getPackageInfo(
                                "app.revanced.android.youtube",
                                PackageManager.PackageInfoFlags.of(0)
                            )
                        } else {
                            this.packageManager.getPackageInfo("app.revanced.android.youtube", 0)
                        }
                    installedReVancedVersion = Version(pInfo.versionName)
                    installedReVancedTextView.text =
                        getString(R.string.installed_app_version, installedReVancedVersion)
                } else {
                    val reVancedUpdateStatusTextView: TextView =
                        findViewById(R.id.reVancedUpdateStatus)
                    reVancedUpdateStatusTextView.text =
                        getString(R.string.incompatible_android_version)
                    setButtonProperties(getButtons()[0], false, R.string.install)
                }
            } catch (e: PackageManager.NameNotFoundException) {
                installedReVancedTextView.text = getString(R.string.installed_app_version, "none")
                installedReVancedVersion = Version("99.99")
                setButtonProperties(getButtons()[0], true, R.string.install)
            }

            val installedReVancedMusicTextView: TextView =
                findViewById(R.id.installedReVancedMusicVersion)
            try {
                val pInfo: PackageInfo =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        this.packageManager.getPackageInfo(
                            "app.revanced.android.apps.youtube.music",
                            PackageManager.PackageInfoFlags.of(0)
                        )
                    } else {
                        this.packageManager.getPackageInfo(
                            "app.revanced.android.apps.youtube.music",
                            0
                        )
                    }
                installedReVancedMusicVersion = Version(pInfo.versionName)
                installedReVancedMusicTextView.text =
                    getString(R.string.installed_app_version, installedReVancedMusicVersion)
            } catch (e: PackageManager.NameNotFoundException) {
                installedReVancedMusicTextView.text =
                    getString(R.string.installed_app_version, "none")
                installedReVancedMusicVersion = Version("99.99")
                setButtonProperties(getButtons()[1], true, R.string.install)
            }
        } else {
            val reVancedUpdateStatusTextView: TextView = findViewById(R.id.reVancedUpdateStatus)
            val reVancedMusicUpdateStatusTextView: TextView =
                findViewById(R.id.reVancedMusicUpdateStatus)
            reVancedUpdateStatusTextView.text = getString(R.string.no_microg)
            reVancedMusicUpdateStatusTextView.text = getString(R.string.no_microg)
        }

        val installedMicroGTextView: TextView = findViewById(R.id.installedMicroGVersion)
        try {
            val pInfo: PackageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                this.packageManager.getPackageInfo(
                    "com.mgoogle.android.gms",
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                this.packageManager.getPackageInfo("com.mgoogle.android.gms", 0)
            }
            installedMicroGVersion = Version(pInfo.versionName)
            installedMicroGTextView.text =
                getString(R.string.installed_app_version, installedMicroGVersion)
        } catch (e: PackageManager.NameNotFoundException) {
            installedMicroGTextView.text = getString(R.string.installed_app_version, "none")
            installedMicroGVersion = Version("99.99")
            setButtonProperties(getButtons()[2], true, R.string.install)
        }

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
                val reVancedUpdateStatusTextView: TextView = findViewById(R.id.reVancedUpdateStatus)
                if (installedReVancedVersion.compareTo(latestReVancedVersion) == -1) {
                    reVancedUpdateStatusTextView.text = getString(R.string.update_available)
                    setButtonProperties(getButtons()[0], true, R.string.update_button)
                } else if (installedReVancedVersion.compareTo(latestReVancedVersion) == 0) {
                    compareHashes(reVancedUpdateStatusTextView, "app.revanced.android.youtube")
                } else {
                    reVancedUpdateStatusTextView.text = getString(R.string.app_not_installed)
                    setButtonProperties(getButtons()[0], true, R.string.install)
                }
            }

            val reVancedMusicUpdateStatusTextView: TextView =
                findViewById(R.id.reVancedMusicUpdateStatus)
            if (installedReVancedMusicVersion.compareTo(latestReVancedMusicVersion) == -1) {
                reVancedMusicUpdateStatusTextView.text = getString(R.string.update_available)
                setButtonProperties(getButtons()[1], true, R.string.update_button)
            } else if (installedReVancedMusicVersion.compareTo(latestReVancedMusicVersion) == 0) {
                compareHashes(
                    reVancedMusicUpdateStatusTextView,
                    "app.revanced.android.apps.youtube.music"
                )
            } else {
                reVancedMusicUpdateStatusTextView.text = getString(R.string.app_not_installed)
                setButtonProperties(getButtons()[1], true, R.string.install)
            }
        }

        val microGUpdateStatusTextView: TextView = findViewById(R.id.microGUpdateStatus)
        if (installedMicroGVersion.compareTo(latestMicroGVersion) == -1) {
            microGUpdateStatusTextView.text = getString(R.string.update_available)
            setButtonProperties(getButtons()[2], true, R.string.update_button)
        } else if (installedMicroGVersion.compareTo(latestMicroGVersion) == 0) {
            microGUpdateStatusTextView.text = getString(R.string.no_update_available)
            setButtonProperties(getButtons()[2], false, R.string.update_button)
        } else {
            microGUpdateStatusTextView.text = getString(R.string.app_not_installed)
            setButtonProperties(getButtons()[2], true, R.string.install)
        }
    }

    /**
     * Compares hashes.
     */
    private fun compareHashes(updateStatusTextView: TextView, packageName: String) {
        var latestAppHash = latestReVancedHash
        var buttonIndex = 0
        if (packageName == "app.revanced.android.apps.youtube.music") {
            latestAppHash = latestReVancedMusicHash
            buttonIndex = 1
        }
        val pInfo: PackageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            this.packageManager.getPackageInfo(
                packageName,
                PackageManager.PackageInfoFlags.of(0)
            )
        } else {
            this.packageManager.getPackageInfo(packageName, 0)
        }
        val file = File(pInfo.applicationInfo.sourceDir)
        val installedAppHash = String(Hex.encodeHex(DigestUtils.sha256(FileInputStream(file))))
        if (installedAppHash == latestAppHash) {
            updateStatusTextView.text = getString(R.string.no_update_available)
            setButtonProperties(getButtons()[buttonIndex], false, R.string.update_button)
        } else {
            updateStatusTextView.text = getString(R.string.update_available)
            setButtonProperties(getButtons()[buttonIndex], true, R.string.update_button)
        }
    }

    /**
     * Downloads YouTube ReVanced when the button is clicked.
     * @property view the view which contains the button.
     */
    fun downloadReVanced(view: View) {
        val fileName = "revanced-nonroot-signed.apk"
        Downloader(
            getSystemService(DOWNLOAD_SERVICE) as DownloadManager,
            this,
            Uri.parse(downloadUrl),
            fileName
        )
        AppInstaller(fileName, this)
    }

    /**
     * Downloads ReVanced Music when the button is clicked.
     * @property view the view which contains the button.
     */
    fun downloadReVancedMusic(view: View) {
        val fileName = "revanced-music-nonroot-signed.apk"
        Downloader(
            getSystemService(DOWNLOAD_SERVICE) as DownloadManager,
            this,
            Uri.parse(musicDownloadUrl),
            fileName
        )
        AppInstaller(fileName, this)
    }

    /**
     * Downloads Vanced microG when the button is clicked.
     * @property view the view which contains the button.
     */
    fun downloadMicroG(view: View) {
        val fileName = "vanced-microG.apk"
        Downloader(
            getSystemService(DOWNLOAD_SERVICE) as DownloadManager,
            this,
            Uri.parse(microGDownloadUrl),
            fileName
        )
        AppInstaller(fileName, this)
    }

    /**
     * Returns all buttons in the view.
     * @return Array of buttons
     */
    private fun getButtons(): Array<Int> {
        return arrayOf(R.id.reVancedButton, R.id.reVancedMusicButton, R.id.microGButton)
    }

    /**
     * Sets button properties.
     * @property button the button to set the properties for
     * @property isEnabled button enabled state
     * @property text button text
     */
    private fun setButtonProperties(button: Int, isEnabled: Boolean, text: Int) {
        val buttonView: Button = findViewById(button)
        buttonView.isEnabled = isEnabled
        buttonView.text = getString(text)
        if (isEnabled)
            buttonView.style(R.style.button_enabled)
        else
            buttonView.style(R.style.button_disabled)
    }

    /**
     * Called when the user presses the ReVanced changelog button.
     * @property view the view which contains the button
     */
    fun openReVancedChangelog(view: View) {
        val browserIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://github.com/LeddaZ/revanced-repo/blob/main/changelogs/revanced.md")
        )
        startActivity(browserIntent)
    }

    /**
     * Called when the user presses the ReVanced Music changelog button.
     * @property view the view which contains the button
     */
    fun openReVancedMusicChangelog(view: View) {
        val browserIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://github.com/LeddaZ/revanced-repo/blob/main/changelogs/music.md")
        )
        startActivity(browserIntent)
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

}
