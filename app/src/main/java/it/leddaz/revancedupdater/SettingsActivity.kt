package it.leddaz.revancedupdater

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.edit
import com.google.android.material.color.DynamicColors
import com.google.android.material.materialswitch.MaterialSwitch
import it.leddaz.revancedupdater.utils.misc.CommonStuff.KEY_X
import it.leddaz.revancedupdater.utils.misc.CommonStuff.KEY_YT
import it.leddaz.revancedupdater.utils.misc.CommonStuff.KEY_YTM
import it.leddaz.revancedupdater.utils.misc.CommonStuff.PREFS_NAME
import java.io.File

class SettingsActivity : AppCompatActivity() {
    /**
     * Actions executed when the activity is created at runtime.
     * @property savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        DynamicColors.applyToActivityIfAvailable(this)
        setContentView(R.layout.activity_settings)

        val toolbar = findViewById<Toolbar>(R.id.topAppBar)
        setSupportActionBar(toolbar)

        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed() // Navigates back
        }

        val ytSwitch = findViewById<MaterialSwitch>(R.id.yt_switch)
        val ytmSwitch = findViewById<MaterialSwitch>(R.id.ytm_switch)
        val xSwitch = findViewById<MaterialSwitch>(R.id.x_switch)

        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        ytSwitch.isChecked = prefs.getBoolean(KEY_YT, true)
        ytmSwitch.isChecked = prefs.getBoolean(KEY_YTM, true)
        xSwitch.isChecked = prefs.getBoolean(KEY_X, true)

        ytSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit { putBoolean(KEY_YT, isChecked) }
        }
        ytmSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit { putBoolean(KEY_YTM, isChecked) }
        }
        xSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit { putBoolean(KEY_X, isChecked) }
        }
    }

    /**
     * Deletes downloaded APKs
     */
    @Suppress("UNUSED_PARAMETER")
    fun deleteAPKs(view: View) {
        val filenames = arrayOf(
            "revanced-music-nonroot-signed.apk",
            "revanced-nonroot-signed.apk",
            "app-release.apk",
            "microg.apk",
            "x.apk"
        )
        val appDataDir = this.getExternalFilesDir("/apks/").toString() + "/"
        for (apk in filenames) {
            val path = File(appDataDir + apk)
            if (path.exists()) {
                path.delete()
            }
        }
        Toast.makeText(this, R.string.apks_deleted, Toast.LENGTH_SHORT).show()
    }
}
