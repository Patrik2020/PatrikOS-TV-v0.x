package hu.patrikos.tv

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Base64
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import java.io.File
import kotlin.math.roundToInt

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        applyImmersiveMode()

        loadSelectedBackground()
        configureHotspots()

        bindApp(
            R.id.youtubeButton,
            label = "YouTube",
            packageNames = listOf("com.google.android.youtube.tv", "com.google.android.youtube"),
            labelKeywords = listOf("youtube"),
            marketPackage = "com.google.android.youtube.tv"
        )
        bindApp(
            R.id.disneyButton,
            label = "Disney+",
            packageNames = listOf("com.disney.disneyplus"),
            labelKeywords = listOf("disney", "disney+"),
            marketPackage = "com.disney.disneyplus"
        )
        bindApp(
            R.id.netflixButton,
            label = "Netflix",
            packageNames = listOf("com.netflix.ninja"),
            labelKeywords = listOf("netflix"),
            marketPackage = "com.netflix.ninja"
        )
        bindApp(
            R.id.maxButton,
            label = "Max",
            packageNames = listOf(
                "com.wbd.stream",
                "com.hbo.hbomax",
                "com.hbo.max",
                "com.hbo.hbonow",
                "eu.hbogo.androidtv",
                "com.hbo.hbogo"
            ),
            labelKeywords = listOf("max", "hbo max", "hbo go", "hbo"),
            marketPackage = "com.wbd.stream"
        )

        findViewById<View>(R.id.settingsButton).setOnClickListener {
            openSystemSettings()
        }

        findViewById<View>(R.id.youtubeButton).requestFocus()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) applyImmersiveMode()
    }

    private fun configureHotspots() {
        val root = findViewById<FrameLayout>(R.id.rootLayout)
        root.post {
            // Coordinates are measured from the exact 1536x864 image selected for Elena TV.
            // Because the background uses fitXY and the TV is 16:9, normalized placement keeps
            // the focus outline directly on the visible card at every Android TV density.
            placeNormalized(R.id.youtubeButton, 175f, 304f, 463f, 558f)
            placeNormalized(R.id.disneyButton, 485f, 304f, 757f, 558f)
            placeNormalized(R.id.netflixButton, 782f, 304f, 1054f, 558f)
            placeNormalized(R.id.maxButton, 1080f, 304f, 1354f, 558f)

            // Cover the sample clock baked into the mock-up with the real live clock/date.
            placeNormalized(R.id.liveClockPanel, 984f, 28f, 1368f, 145f)
            placeNormalized(R.id.settingsButton, 1384f, 43f, 1482f, 141f)

            val youtube = findViewById<View>(R.id.youtubeButton)
            val disney = findViewById<View>(R.id.disneyButton)
            val netflix = findViewById<View>(R.id.netflixButton)
            val max = findViewById<View>(R.id.maxButton)
            val settings = findViewById<View>(R.id.settingsButton)

            youtube.nextFocusRightId = R.id.disneyButton
            disney.nextFocusLeftId = R.id.youtubeButton
            disney.nextFocusRightId = R.id.netflixButton
            netflix.nextFocusLeftId = R.id.disneyButton
            netflix.nextFocusRightId = R.id.maxButton
            max.nextFocusLeftId = R.id.netflixButton
            max.nextFocusUpId = R.id.settingsButton
            settings.nextFocusDownId = R.id.maxButton
        }
    }

    private fun placeNormalized(viewId: Int, left: Float, top: Float, right: Float, bottom: Float) {
        val root = findViewById<FrameLayout>(R.id.rootLayout)
        val width = root.width
        val height = root.height
        if (width <= 0 || height <= 0) return

        val l = (width * left / SOURCE_WIDTH).roundToInt()
        val t = (height * top / SOURCE_HEIGHT).roundToInt()
        val r = (width * right / SOURCE_WIDTH).roundToInt()
        val b = (height * bottom / SOURCE_HEIGHT).roundToInt()

        findViewById<View>(viewId).layoutParams = FrameLayout.LayoutParams(
            (r - l).coerceAtLeast(1),
            (b - t).coerceAtLeast(1)
        ).apply {
            leftMargin = l
            topMargin = t
        }
    }

    private fun loadSelectedBackground() {
        val imageView = findViewById<ImageView>(R.id.backgroundImage)

        // A lossless PNG pushed with ADB is preferred. This avoids the decoder corruption seen
        // on the TCL while keeping the launcher itself tiny and fast.
        val external = getExternalFilesDir(null)?.let { File(it, BACKGROUND_FILE) }
        val internal = File(filesDir, BACKGROUND_FILE)
        val overrideFile = listOfNotNull(external, internal).firstOrNull { it.isFile }

        if (overrideFile != null) {
            val bitmap = BitmapFactory.decodeFile(overrideFile.absolutePath)
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap)
                return
            }
        }

        // Bundled fallback for normal installs.
        val bundledBitmap = runCatching {
            val encoded = SELECTED_BACKGROUND_ASSETS.joinToString(separator = "") { assetName ->
                assets.open(assetName).bufferedReader().use { reader -> reader.readText().trim() }
            }
            val bytes = Base64.decode(encoded, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }.getOrNull()

        if (bundledBitmap != null) {
            imageView.setImageBitmap(bundledBitmap)
        } else {
            Toast.makeText(this, "Az Elena TV háttér nem tölthető be.", Toast.LENGTH_LONG).show()
        }
    }

    private fun bindApp(
        viewId: Int,
        label: String,
        packageNames: List<String>,
        labelKeywords: List<String>,
        marketPackage: String
    ) {
        findViewById<View>(viewId).setOnClickListener {
            launchPreferredApp(label, packageNames, labelKeywords, marketPackage)
        }
    }

    private fun launchPreferredApp(
        label: String,
        packageNames: List<String>,
        labelKeywords: List<String>,
        marketPackage: String
    ) {
        val directIntent = packageNames.asSequence().mapNotNull { packageName ->
            packageManager.getLeanbackLaunchIntentForPackage(packageName)
                ?: packageManager.getLaunchIntentForPackage(packageName)
        }.firstOrNull()

        val launchIntent = directIntent ?: findByLauncherLabel(labelKeywords)
        if (launchIntent != null) {
            runCatching {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(launchIntent)
            }.onFailure {
                Toast.makeText(this, "$label nem indítható.", Toast.LENGTH_SHORT).show()
            }
            return
        }

        openPlayStore(marketPackage, label)
    }

    private fun findByLauncherLabel(labelKeywords: List<String>): Intent? {
        val tvIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        }

        val match = packageManager
            .queryIntentActivities(tvIntent, PackageManager.MATCH_ALL)
            .firstOrNull { resolveInfo ->
                val label = resolveInfo.loadLabel(packageManager).toString()
                labelKeywords.any { keyword -> label.contains(keyword, ignoreCase = true) }
            } ?: return null

        val packageName = match.activityInfo.packageName
        return packageManager.getLeanbackLaunchIntentForPackage(packageName)
            ?: packageManager.getLaunchIntentForPackage(packageName)
    }

    private fun openPlayStore(packageName: String, label: String) {
        val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        runCatching { startActivity(marketIntent) }.onFailure {
            Toast.makeText(
                this,
                "$label nincs telepítve, és a Play Áruház nem nyitható meg.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun openSystemSettings() {
        runCatching {
            startActivity(Intent(Settings.ACTION_SETTINGS))
        }.onFailure {
            Toast.makeText(this, R.string.settings_unavailable, Toast.LENGTH_SHORT).show()
        }
    }

    @Suppress("DEPRECATION")
    private fun applyImmersiveMode() {
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        // HOME launcher: Back should not leave an empty task behind.
    }

    companion object {
        private const val SOURCE_WIDTH = 1536f
        private const val SOURCE_HEIGHT = 864f
        private const val BACKGROUND_FILE = "elena_bluey.png"
        private val SELECTED_BACKGROUND_ASSETS = listOf(
            "elena_selected_bg_1.b64",
            "elena_selected_bg_2.b64",
            "elena_selected_bg_3.b64"
        )
    }
}
