package games.indiegesindel.universalarcontroller

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Config
import com.google.ar.core.Session

class ARManager(private val context: Context) {
    var session: Session? = null
        private set

    fun createSession(): Session? {
        if (session == null) {
            try {
                val arCoreApk = ArCoreApk.getInstance()
                if (arCoreApk.requestInstall(context as android.app.Activity, true) == ArCoreApk.InstallStatus.INSTALLED) {
                    session = Session(context).apply {
                        val config = Config(this)
                        config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
                        config.focusMode = Config.FocusMode.AUTO
                        configure(config)
                        resume()
                    }
                }
            } catch (e: Exception) {
                Log.e("ARManager", "Failed to create ARCore session", e)
                Toast.makeText(context, "ARCore session failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
        return session
    }

    fun resume() {
        try {
            session?.resume()
        } catch (e: Exception) {
            Log.e("ARManager", "Failed to resume ARCore session", e)
        }
    }

    fun pause() {
        try {
            session?.pause()
        } catch (e: Exception) {
            Log.e("ARManager", "Failed to pause ARCore session", e)
        }
    }

    fun close() {
        session?.close()
        session = null
    }
}
