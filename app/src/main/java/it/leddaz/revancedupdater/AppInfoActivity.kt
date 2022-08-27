package it.leddaz.revancedupdater

import android.app.DownloadManager
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.paris.extensions.style
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import it.leddaz.revancedupdater.utils.*
import it.leddaz.revancedupdater.utils.jsonobjects.AppJSONObject

private var installedAppVersion = Version("99.99")
private var latestAppVersion = Version("0.0")
private var appDownloadUrl = ""

/**
 * The App info activity, started when the App info button is
 * pressed.
 * @return The activity
 * @author Leonardo Ledda (LeddaZ)
 */
class AppInfoActivity : AppCompatActivity() {

    /**
     * Actions executed when the activity is created at runtime.
     * @property savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_info)
        refresh()
    }

    /**
     * Gets the installed and latest versions of ReVanced Updater
     * @property callback callback used to detect if the download was
     *                    successful
     */
    private fun getVersion(callback: VolleyCallBack) {
        // Installed version
        val installedAppTextView: TextView = findViewById(R.id.installedAppVersion)
        val pInfo: PackageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            this.packageManager.getPackageInfo("it.leddaz.revancedupdater",
                PackageManager.PackageInfoFlags.of(0))
        } else {
            this.packageManager.getPackageInfo("it.leddaz.revancedupdater", 0)
        }
        installedAppVersion = Version(pInfo.versionName)
        installedAppTextView.text = getString(R.string.installed_app_version, installedAppVersion)

        // Latest version
        val queue = Volley.newRequestQueue(this)
        val url = "https://raw.githubusercontent.com/LeddaZ/revanced-repo/main/updater.json"
        var reply: AppJSONObject

        // Request a string response from the provided URL.
        val stringRequest = StringRequest(Request.Method.GET, url, { response ->
            reply = Gson().fromJson(response, object : TypeToken<AppJSONObject>() {}.type)
            latestAppVersion = Version(reply.latestAppVersion)
            appDownloadUrl = "https://github.com/LeddaZ/ReVancedUpdater/releases/download/" +
                    reply.latestAppVersion + "/app-release.apk"
            callback.onSuccess()
        }, {})

        // Add the request to the RequestQueue.
        queue.add(stringRequest)
    }

    /**
     * Compares versions.
     */
    private fun compareVersions() {
        val appUpdateStatusTextView: TextView = findViewById(R.id.appUpdateStatus)
        if (installedAppVersion.compareTo(latestAppVersion) == -1) {
            appUpdateStatusTextView.text = getString(R.string.update_available)
            setButtonProperties(true, R.string.update_button)
        } else if (installedAppVersion.compareTo(latestAppVersion) == 0) {
            appUpdateStatusTextView.text = getString(R.string.no_update_available)
            setButtonProperties(false, R.string.update_button)
        } else {
            appUpdateStatusTextView.text = getString(R.string.app_not_installed)
            setButtonProperties(true, R.string.install)
        }
    }

    /**
     * Download ReVanced Updater when the button is clicked.
     * @property view the view which contains the button.
     */
    fun downloadApp(view: View) {
        val fileName = "app-release.apk"
        Downloader(
            getSystemService(DOWNLOAD_SERVICE) as DownloadManager,
            this,
            Uri.parse(appDownloadUrl),
            fileName)
        AppInstaller(fileName, this)
    }

    /**
     * Sets button properties.
     * @property isEnabled button enabled state
     * @property text button text
     */
    private fun setButtonProperties(isEnabled: Boolean, text: Int) {
        val buttonView: Button = findViewById(R.id.appButton)
        buttonView.isEnabled = isEnabled
        buttonView.text = getString(text)
        if (isEnabled)
            buttonView.style(R.style.button_enabled)
        else
           buttonView.style(R.style.button_disabled)
    }

    /**
     * Refreshes the versions.
     */
    private fun refresh() {
        getVersion(object : VolleyCallBack {
            override fun onSuccess() {
                val latestAppTextView: TextView = findViewById(R.id.latestAppVersion)
                latestAppTextView.text = getString(R.string.latest_app_version, latestAppVersion)
                compareVersions()
            }
        })
    }

    /**
     * Called when the user presses the Refresh button.
     * @property view the view which contains the button
     */
    fun refreshButton (view: View) {
        refresh()
    }

    /**
     * Called when the user presses the Source code button.
     * @property view the view which contains the button
     */
    fun openSource(view: View) {
        val browserIntent = Intent(Intent.ACTION_VIEW,
            Uri.parse("https://github.com/LeddaZ/ReVancedUpdater"))
        startActivity(browserIntent)
    }

    /**
     * Called when the user presses the app changelog button.
     * @property view the view which contains the button
     */
    fun openAppChangelog(view: View) {
        val browserIntent = Intent(Intent.ACTION_VIEW,
            Uri.parse("https://github.com/LeddaZ/ReVancedUpdater/releases/tag/$APP_VERSION"))
        startActivity(browserIntent)
    }

    /**
     * Called when the user presses the Back button.
     * @property view the view which contains the button
     */
    fun goBack(view: View) {
        this.finish()
    }

}
