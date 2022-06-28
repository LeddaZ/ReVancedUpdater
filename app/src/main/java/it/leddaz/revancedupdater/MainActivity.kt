package it.leddaz.revancedupdater

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInfo
import android.content.pm.PackageManager.NameNotFoundException
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.android.volley.Request.Method.GET
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File


const val LOG_TAG = "ReVanced Updater"
var installedReVancedVersion = Version("99.99")
var latestReVancedVersion = Version("0.0")
var installedMicroGVersion = Version("99.99")
var latestMicroGVersion = Version("0.0")
var downloadUrl = ""
var microGDownloadUrl = ""

class MainActivity : AppCompatActivity() {

    private lateinit var destination: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        refresh()
        }

    /** Gets the installed and latest versions of YouTube Revanced and Vanced microG. **/
    private fun getVersions(callback: VolleyCallBack) {
        // Installed versions
        val installedReVancedTextView: TextView = findViewById(R.id.installedReVancedVersion)
        try {
            val pInfo: PackageInfo =
                this.packageManager.getPackageInfo("app.revanced.android.youtube", 0)
            installedReVancedVersion = Version(pInfo.versionName)
            installedReVancedTextView.text = getString(R.string.installed_app_version, installedReVancedVersion)
        } catch(e: NameNotFoundException) {
            installedReVancedTextView.text = getString(R.string.installed_app_version, "none")
            installedReVancedVersion = Version("99.99")
            setButtonProperties(getButtons()[0], true, R.string.install, R.attr.button_content_color)
        }

        val installedMicroGTextView: TextView = findViewById(R.id.installedMicroGVersion)
        try {
            val pInfo: PackageInfo =
                this.packageManager.getPackageInfo("com.mgoogle.android.gms", 0)
            installedMicroGVersion = Version(pInfo.versionName)
            installedMicroGTextView.text = getString(R.string.installed_app_version, installedMicroGVersion)
        } catch(e: NameNotFoundException) {
            installedMicroGTextView.text = getString(R.string.installed_app_version, "none")
            setButtonProperties(getButtons()[1], true, R.string.install, R.attr.button_content_color)
        }

        // Latest versions
        val queue = Volley.newRequestQueue(this)
        val url = "https://raw.githubusercontent.com/LeddaZ/revanced-repo/main/updater.json"
        var reply: ReVancedJSONObject

        // Request a string response from the provided URL.
        val stringRequest = StringRequest(GET, url, { response ->
            reply = Gson().fromJson(response, object : TypeToken<ReVancedJSONObject>() {}.type)
            latestReVancedVersion = Version(reply.latestReVancedVersion)
            latestMicroGVersion = Version(reply.latestMicroGVersion)
            downloadUrl = reply.downloadUrl
            microGDownloadUrl = reply.microGDownloadUrl
            callback.onSuccess()
        }, {})

        // Add the request to the RequestQueue.
        queue.add(stringRequest)
    }

    /** Compares versions. **/
    private fun compareVersions() {
        val reVancedUpdateStatusTextView: TextView = findViewById(R.id.reVancedUpdateStatus)
        if (installedReVancedVersion.compareTo(latestReVancedVersion) == -1) {
            reVancedUpdateStatusTextView.text = getString(R.string.update_available)
            setButtonProperties(getButtons()[0], true, R.string.update_button, R.attr.button_content_color)
        } else if (installedReVancedVersion.compareTo(latestReVancedVersion) == 0) {
            reVancedUpdateStatusTextView.text = getString(R.string.no_update_available)
            setButtonProperties(getButtons()[0], false, R.string.update_button, R.color.dark_gray)
        } else {
            reVancedUpdateStatusTextView.text = getString(R.string.app_not_installed)
            setButtonProperties(getButtons()[0], true, R.string.install, R.attr.button_content_color)
        }

        val microGUpdateStatusTextView: TextView = findViewById(R.id.microGUpdateStatus)
        if (installedMicroGVersion.compareTo(latestMicroGVersion) == -1) {
            microGUpdateStatusTextView.text = getString(R.string.update_available)
            setButtonProperties(getButtons()[1], true, R.string.update_button, R.attr.button_content_color)
        } else if (installedMicroGVersion.compareTo(installedMicroGVersion) == 0) {
            microGUpdateStatusTextView.text = getString(R.string.no_update_available)
            setButtonProperties(getButtons()[1], false, R.string.update_button, R.color.dark_gray)
        } else {
            microGUpdateStatusTextView.text = getString(R.string.app_not_installed)
            setButtonProperties(getButtons()[1], true, R.string.install, R.attr.button_content_color)
        }
    }

    /** Downloads a file. **/
    private fun downloadFile(uri: Uri, fileName: String): Long {
        var downloadReference: Long = 0
        val downloadManager: DownloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        try {
            val request = DownloadManager.Request(uri)

            // Set title of request
            request.setTitle(fileName)

            // Set notification when download completed
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

            // Set the local destination for the downloaded file to a path within the application's external files directory
            destination = this.getExternalFilesDir("/apks/").toString() + "/"
            destination += fileName
            request.setDestinationInExternalFilesDir(this, "/apks/", fileName)

            //Enqueue download and save the referenceId
            downloadReference = downloadManager.enqueue(request)
            Toast.makeText(this, R.string.download_started, Toast.LENGTH_LONG).show()
        } catch (e: IllegalArgumentException) {
            Toast.makeText(this, R.string.download_error, Toast.LENGTH_LONG).show()
            Log.e(LOG_TAG, e.printStackTrace().toString())
        }
        return downloadReference
    }

    /** Download YouTube ReVanced when the button is clicked. **/
    fun downloadReVanced(view: View) {
        downloadFile(
            Uri.parse(downloadUrl),
            "revanced-nonroot-signed.apk")
        showInstallOption()
    }

    /** Download Vanced microG when the button is clicked. **/
    fun downloadMicroG(view: View) {
        downloadFile(
            Uri.parse(microGDownloadUrl),
            "vanced-microG.apk")
        showInstallOption()
    }

    private fun getButtons(): Array<Button> {
        return arrayOf(findViewById(R.id.reVancedButton), findViewById(R.id.microGButton))
    }

    private fun showInstallOption() {
        // Set BroadcastReceiver to install app when .apk is downloaded
        val onComplete = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val contentUri = FileProvider.getUriForFile(
                    context,
                    BuildConfig.APPLICATION_ID + ".provider",
                    File(destination)
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    val install = Intent(Intent.ACTION_VIEW)
                    install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    install.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    install.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
                    install.data = contentUri
                    context.startActivity(install)
                    context.unregisterReceiver(this)
                    // finish()
                } else {
                    val install = Intent(Intent.ACTION_VIEW)
                    install.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    install.setDataAndType(
                        contentUri,
                        "\"application/vnd.android.package-archive\""
                    )
                    context.startActivity(install)
                    context.unregisterReceiver(this)
                    refresh()
                }
            }
        }
        this.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        val test = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                refresh()
            }
        }

        this.registerReceiver(test, IntentFilter(Intent.ACTION_PACKAGE_ADDED))
    }

    private fun setButtonProperties(button: Button, isEnabled: Boolean, text: Int, textColor: Int) {
        button.isEnabled = isEnabled
        button.text = getString(text)
        button.setTextColor(textColor)
    }

    private fun refresh() {
        getVersions(object : VolleyCallBack {
            override fun onSuccess() {
                val latestReVancedTextView: TextView = findViewById(R.id.latestReVancedVersion)
                latestReVancedTextView.text = getString(R.string.latest_app_version, latestReVancedVersion)

                val latestMicroGTextView: TextView = findViewById(R.id.latestMicroGVersion)
                latestMicroGTextView.text = getString(R.string.latest_app_version, latestMicroGVersion)
                compareVersions()
            }
        })
    }

    /** Called when the user presses the Refresh button. **/
    fun refreshButton (view: View) {
        refresh()
    }

}
