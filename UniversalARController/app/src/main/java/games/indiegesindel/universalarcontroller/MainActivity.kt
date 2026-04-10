package games.indiegesindel.universalarcontroller

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.ar.core.Frame
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import android.opengl.GLSurfaceView
import games.indiegesindel.universalarcontroller.ui.theme.UniversalARControllerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UniversalARControllerTheme {
                ARScreen()
            }
        }
    }
}

@Composable
fun ARScreen() {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(key1 = true) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    if (hasCameraPermission) {
        ARViewContainer()
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Camera permission is required to use AR.")
        }
    }
}

@Composable
fun ARViewContainer() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val arManager = remember { ARManager(context) }
    var session by remember { mutableStateOf<Session?>(null) }
    var poseString by remember { mutableStateOf("Initializing tracking...") }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    session = arManager.createSession()
                    arManager.resume()
                }
                Lifecycle.Event.ON_PAUSE -> {
                    arManager.pause()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            arManager.close()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            val currentSession = session
            if (currentSession != null) {
                ARCameraFeed(currentSession) { frame ->
                    val camera = frame.camera
                    if (camera.trackingState == TrackingState.TRACKING) {
                        val pose = camera.displayOrientedPose
                        val pos = pose.translation
                        val rot = pose.rotationQuaternion
                        poseString = "Tracking Status: TRACKING\n\nPosition:\nX: ${"%.3f".format(pos[0])}, Y: ${"%.3f".format(pos[1])}, Z: ${"%.3f".format(pos[2])}\n\nRotation:\nX: ${"%.3f".format(rot[0])}, Y: ${"%.3f".format(rot[1])}, Z: ${"%.3f".format(rot[2])}, W: ${"%.3f".format(rot[3])}"
                    } else {
                        poseString = "Tracking Status: ${camera.trackingState}\n\nMove the device around to initialize tracking."
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Initializing AR...")
                }
            }
        }
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Pose Information",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = poseString,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun ARCameraFeed(session: Session, onFrameUpdate: (Frame) -> Unit) {
    AndroidView(
        factory = { context ->
            GLSurfaceView(context).apply {
                preserveEGLContextOnPause = true
                setEGLContextClientVersion(2)
                setRenderer(ARRenderer(session, onFrameUpdate))
                renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}