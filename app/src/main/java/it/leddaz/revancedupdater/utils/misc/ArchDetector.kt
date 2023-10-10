package it.leddaz.revancedupdater.utils.misc

import android.util.Log
import it.leddaz.revancedupdater.utils.misc.CommonMethods.LOG_TAG
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/**
 * Reads the "ro.product.cpu.abilist" property to detect the device's
 * OS architecture.
 * @author Leonardo Ledda (LeddaZ)
 */
object ArchDetector {

    /**
     * Reads the property.
     * @return The ABIs supported by the device
     */
    private fun readABIList(): String {
        var process: Process? = null
        var bufferedReader: BufferedReader? = null

        return try {
            process = ProcessBuilder().command("/system/bin/getprop", "ro.product.cpu.abilist")
                .redirectErrorStream(true).start()
            bufferedReader = BufferedReader(InputStreamReader(process.inputStream))
            var line = bufferedReader.readLine()
            if (line == null)
                line = "" // Prop not set
            line
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Failed to read ro.product.cpu.abilist", e)
            ""
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close()
                } catch (_: IOException) {
                }
            }
            process?.destroy()
        }
    }

    /**
     * Returns the device architecture.
     * @return Device architecture
     */
    fun getArch(): String {
        val abilist: String = readABIList()
        Log.i(LOG_TAG, "Supported ABIs: $abilist")
        return if (abilist.contains("x86_64"))
            "x86_64"
        else if (abilist.contains("x86"))
            "x86"
        else if (abilist.contains("arm64-v8a"))
            "arm64"
        else if (abilist.contains("armeabi-v7a"))
            "arm"
        else {
            Log.e(LOG_TAG, "Unsupported architecture! (this should never appear)")
            ""
        }
    }

}
