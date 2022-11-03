package it.leddaz.revancedupdater

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import it.leddaz.revancedupdater.utils.jsonobjects.AppJSONObject
import it.leddaz.revancedupdater.utils.misc.CommonMethods.compareAppVersion
import it.leddaz.revancedupdater.utils.misc.CommonMethods.dlAndInstall
import it.leddaz.revancedupdater.utils.misc.CommonMethods.getAppVersion
import it.leddaz.revancedupdater.utils.misc.CommonMethods.openLink
import it.leddaz.revancedupdater.utils.misc.Version
import it.leddaz.revancedupdater.utils.misc.VolleyCallBack

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
        refresh(this)
    }

    /**
     * Gets the installed and latest versions of ReVanced Updater
     * @property callback callback used to detect if the download was
     *                    successful
     */
    private fun getVersion(callback: VolleyCallBack) {
        // Installed version
        getAppVersion(
            21,
            "5.0",
            "it.leddaz.revancedupdater",
            this,
            this.window.decorView,
            findViewById(R.id.installedAppVersion),
            installedAppVersion,
            findViewById(R.id.appUpdateStatus),
            findViewById(R.id.appButton)
        )

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
     * Download ReVanced Updater when the button is clicked.
     * @property view the view which contains the button.
     */
    fun downloadApp(view: View) {
        dlAndInstall("app-release.apk", appDownloadUrl, this)
    }

    /**
     * Refreshes the versions.
     */
    private fun refresh(context: Context) {
        getVersion(object : VolleyCallBack {
            override fun onSuccess() {
                val latestAppTextView: TextView = findViewById(R.id.latestAppVersion)
                latestAppTextView.text = getString(R.string.latest_app_version, latestAppVersion)
                compareAppVersion(
                    false, "it.leddaz.revancedupdater", installedAppVersion,
                    latestAppVersion, findViewById(R.id.appUpdateStatus),
                    findViewById(R.id.appButton), context
                )
            }
        })
    }

    /**
     * Called when the user presses the Refresh button.
     * @property view the view which contains the button
     */
    fun refreshButton(view: View) {
        refresh(this)
    }

    /**
     * Called when the user presses the Source code button.
     * @property view the view which contains the button
     */
    fun openSource(view: View) {
        openLink("https://github.com/LeddaZ/ReVancedUpdater", this)
    }

    /**
     * Called when the user presses the app changelog button.
     * @property view the view which contains the button
     */
    fun openAppChangelog(view: View) {
        openLink("https://github.com/LeddaZ/ReVancedUpdater/releases/tag/$APP_VERSION", this)
    }

    /**
     * Called when the user presses the Back button.
     * @property view the view which contains the button
     */
    fun goBack(view: View) {
        this.finish()
    }

}
