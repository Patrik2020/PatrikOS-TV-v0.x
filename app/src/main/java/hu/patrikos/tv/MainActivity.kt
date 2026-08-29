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
import android.widget.ImageView
import android.widget.Toast
import java.io.File

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        applyImmersiveMode()

        loadSelectedBackground()

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

    private fun loadSelectedBackground() {
        val imageView = findViewById<ImageView>(R.id.backgroundImage)

        // Optional developer override: if a newer background was pushed to the app's files,
        // prefer it. Normal installs use the exact selected Elena TV image bundled below.
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
            Toast.makeText(
                this,
                "Az Elena TV háttér nem tölthető be.",
                Toast.LENGTH_LONG
            ).show()
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
        val marketIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("market://details?id=$packageName")
        ).apply {
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
        private const val BACKGROUND_FILE = "elena_bluey.webp"
        private val SELECTED_BACKGROUND_ASSETS = listOf(
            "elena_selected_bg_1.b64",
            "elena_selected_bg_2.b64",
            "elena_selected_bg_3.b64"
        )
    }
}
