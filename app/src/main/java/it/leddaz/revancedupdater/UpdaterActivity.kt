package it.leddaz.revancedupdater

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import it.leddaz.revancedupdater.utils.jsonobjects.UpdaterJSONObject
import it.leddaz.revancedupdater.utils.misc.Utils.APP_VERSION
import it.leddaz.revancedupdater.utils.misc.Utils.compareAppVersion
import it.leddaz.revancedupdater.utils.misc.Utils.dlAndInstall
import it.leddaz.revancedupdater.utils.misc.Utils.getAppVersion
import it.leddaz.revancedupdater.utils.misc.Utils.openLink
import it.leddaz.revancedupdater.utils.misc.Version
import it.leddaz.revancedupdater.utils.misc.VolleyCallBack

private var installedUpdaterVersion = Version("99.99")
private var latestUpdaterVersion = Version("0.0")
private var updaterDownloadUrl = ""

/**
 * The App info activity, started when the App info button is
 * pressed.
 * @return The activity
 * @author Leonardo Ledda (LeddaZ)
 */
class UpdaterActivity : AppCompatActivity() {

    /**
     * Actions executed when the activity is created at runtime.
     * @property savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_updater)
        refresh(this)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    bottomNavigationView.selectedItemId = R.id.revanced
                    finish()
                }
            }
        )

        val updaterCard = findViewById<MaterialCardView>(R.id.updater_info_card)
        updaterCard.setOnLongClickListener {
            openLink(
                "https://github.com/LeddaZ/ReVancedUpdater/releases/tag/$APP_VERSION", this
            )
            true
        }

        bottomNavigationView.selectedItemId = R.id.updater
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
     * Gets the installed and latest versions of ReVanced Updater
     * @property callback callback used to detect if the download was
     *                    successful
     */
    private fun getVersion(callback: VolleyCallBack) {
        // Installed version
        getAppVersion(
            "it.leddaz.revancedupdater",
            this,
            findViewById(R.id.installed_updater_version),
            installedUpdaterVersion,
            findViewById(R.id.updater_update_status),
            findViewById(R.id.updater_download_button)
        )

        // Latest version
        val queue = Volley.newRequestQueue(this)
        val url = "https://raw.githubusercontent.com/LeddaZ/revanced-repo/main/updater.json"
        var reply: UpdaterJSONObject

        // Request a string response from the provided URL.
        val stringRequest = StringRequest(Request.Method.GET, url, { response ->
            reply = Gson().fromJson(response, object : TypeToken<UpdaterJSONObject>() {}.type)
            latestUpdaterVersion = Version(reply.latestUpdaterVersion)
            updaterDownloadUrl = "https://github.com/LeddaZ/ReVancedUpdater/releases/download/" +
                    reply.latestUpdaterVersion + "/app-release.apk"
            callback.onSuccess()
        }, {})

        // Add the request to the RequestQueue.
        queue.add(stringRequest)
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
     * Refreshes the versions.
     */
    private fun refresh(context: Context) {
        getVersion(object : VolleyCallBack {
            override fun onSuccess() {
                val latestAppTextView: TextView = findViewById(R.id.latest_updater_version)
                latestAppTextView.text =
                    getString(R.string.latest_app_version, latestUpdaterVersion)
                compareAppVersion(
                    false, "it.leddaz.revancedupdater", installedUpdaterVersion,
                    latestUpdaterVersion, findViewById(R.id.updater_update_status),
                    findViewById(R.id.updater_download_button), context
                )
            }
        })
    }

    /**
     * Called when the user presses the Source code button.
     * @property view the view which contains the button
     */
    @Suppress("UNUSED_PARAMETER")
    fun openGitHub(view: View) {
        openLink("https://github.com/LeddaZ/ReVancedUpdater", this)
    }

}
