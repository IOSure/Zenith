package com.etrisad.zenith.ui.components.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.etrisad.zenith.R
import com.etrisad.zenith.data.preferences.FontOption
import com.etrisad.zenith.ui.theme.GSFlexSettings
import com.etrisad.zenith.ui.theme.ZenithTheme
import kotlinx.coroutines.delay
import java.util.Locale

/**
 * Floating "screen-on time" pill.
 *
 * The companion service decides when the pill should be visible (after the
 * configured screen-on duration); this manager only renders it and reports a
 * double tap so the service can hide it for the configured cool-down.
 */
class AwakeTimeOverlayManager(
    private val context: Context,
    private val onDoubleTap: () -> Unit
) {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val mainHandler = Handler(Looper.getMainLooper())

    private var overlayView: ComposeView? = null
    private var lifecycleOwner: MyLifecycleOwner? = null
    private var viewModelStore: ViewModelStore? = null

    @Volatile
    var isShowing = false
        private set

    fun show(
        sessionStartMillis: Long,
        isDark: Boolean,
        fontOption: FontOption = FontOption.SYSTEM,
        dynamicColor: Boolean = true,
        expressiveColors: Boolean = false,
        gsFlexSettings: GSFlexSettings = GSFlexSettings(),
        opacityPercent: Int = 92
    ) {
        if (isShowing) return
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post {
                show(sessionStartMillis, isDark, fontOption, dynamicColor, expressiveColors, gsFlexSettings, opacityPercent)
            }
            return
        }

        val store = ViewModelStore()
        viewModelStore = store
        val owner = MyLifecycleOwner()
        owner.performRestore(null)
        owner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleOwner = owner

        val alpha = (opacityPercent / 100f).coerceIn(0.2f, 1f)
        val uiContext = localizedUiContext()
        val view = ComposeView(uiContext).apply {
            setViewTreeLifecycleOwner(owner)
            setViewTreeViewModelStoreOwner(object : ViewModelStoreOwner {
                override val viewModelStore: ViewModelStore = store
            })
            setViewTreeSavedStateRegistryOwner(owner)
            setContent {
                ZenithTheme(
                    darkTheme = isDark,
                    fontOption = fontOption,
                    dynamicColor = dynamicColor,
                    expressiveColors = expressiveColors,
                    gsFlexSettings = gsFlexSettings
                ) {
                    AwakeTimeHUD(
                        sessionStartMillis = sessionStartMillis,
                        surfaceAlpha = alpha,
                        onDoubleTap = onDoubleTap
                    )
                }
            }
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 32
            y = 190
        }

        try {
            windowManager.addView(view, params)
            overlayView = view
            isShowing = true
            owner.handleLifecycleEvent(Lifecycle.Event.ON_START)
            owner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        } catch (e: Exception) {
            e.printStackTrace()
            isShowing = false
        }
    }

    fun hide() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post { hide() }
            return
        }
        destroyView()
    }

    /**
     * The application resources of a service do not always pick up the Android 13
     * per-app locale, which made this overlay fall back to English inside a
     * Chinese UI. Compose resources are resolved from the view context, so build
     * one with the process locale (kept in sync by AppLanguage).
     */
    private fun localizedUiContext(): Context {
        return try {
            val configuration = android.content.res.Configuration(context.resources.configuration)
            configuration.setLocale(com.etrisad.zenith.util.AppLanguage.currentLocale(context))
            context.createConfigurationContext(configuration)
        } catch (_: Exception) {
            context
        }
    }

    private fun destroyView() {
        val view = overlayView
        val owner = lifecycleOwner
        val store = viewModelStore
        overlayView = null
        lifecycleOwner = null
        viewModelStore = null
        isShowing = false
        if (view == null) return
        try {
            owner?.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            owner?.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
            owner?.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            view.disposeComposition()
            windowManager.removeViewImmediate(view)
            store?.clear()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private class MyLifecycleOwner : LifecycleOwner, SavedStateRegistryOwner {
        private val lifecycleRegistry = LifecycleRegistry(this)
        private val savedStateRegistryController = SavedStateRegistryController.create(this)

        override val lifecycle: Lifecycle = lifecycleRegistry
        override val savedStateRegistry: SavedStateRegistry = savedStateRegistryController.savedStateRegistry

        fun handleLifecycleEvent(event: Lifecycle.Event) {
            lifecycleRegistry.handleLifecycleEvent(event)
        }

        fun performRestore(savedState: Bundle?) {
            savedStateRegistryController.performRestore(savedState)
        }
    }
}

@Composable
fun AwakeTimeHUD(
    sessionStartMillis: Long,
    surfaceAlpha: Float,
    onDoubleTap: () -> Unit
) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(sessionStartMillis) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1000)
        }
    }
    val elapsed = (now - sessionStartMillis).coerceAtLeast(0L)

    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surface.copy(alpha = surfaceAlpha),
        shadowElevation = 8.dp,
        modifier = Modifier.pointerInput(Unit) {
            detectTapGestures(onDoubleTap = { onDoubleTap() })
        }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Timer,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = stringResource(R.string.awake_overlay_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
                Text(
                    text = formatElapsed(elapsed),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

private fun formatElapsed(millis: Long): String {
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }
}
