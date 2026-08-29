package hu.patrikos.tv

import android.app.Activity
import android.app.ActivityManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import hu.patrikos.tv.apps.AppAdapter
import hu.patrikos.tv.apps.AppEntry
import hu.patrikos.tv.apps.AppRepository
import java.util.Locale
import java.util.concurrent.Executors

class MainActivity : Activity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var statusText: TextView
    private lateinit var deviceInfoText: TextView
    private lateinit var adapter: AppAdapter
    private lateinit var repository: AppRepository

    private val worker = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        applyImmersiveMode()

        recyclerView = findViewById(R.id.appGrid)
        statusText = findViewById(R.id.statusText)
        deviceInfoText = findViewById(R.id.deviceInfoText)

        repository = AppRepository(this)
        adapter = AppAdapter(::launchApp)

        recyclerView.layoutManager = GridLayoutManager(this, 5)
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)

        findViewById<TextView>(R.id.refreshButton).setOnClickListener {
            loadApps(requestFirstItemFocus = true)
        }
        findViewById<TextView>(R.id.settingsButton).setOnClickListener {
            openSystemSettings()
        }

        deviceInfoText.text = buildDeviceSummary()
        loadApps(requestFirstItemFocus = true)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) applyImmersiveMode()
    }

    private fun loadApps(requestFirstItemFocus: Boolean) {
        statusText.visibility = View.VISIBLE
        statusText.text = getString(R.string.loading_apps)

        worker.execute {
            val apps = repository.loadTvApps()
            runOnUiThread {
                if (isFinishing || isDestroyed) return@runOnUiThread

                adapter.submit(apps)
                if (apps.isEmpty()) {
                    statusText.visibility = View.VISIBLE
                    statusText.text = getString(R.string.no_tv_apps)
                } else {
                    statusText.visibility = View.GONE
                    if (requestFirstItemFocus) {
                        recyclerView.post {
                            recyclerView
                                .findViewHolderForAdapterPosition(0)
                                ?.itemView
                                ?.requestFocus()
                        }
                    }
                }
            }
        }
    }

    private fun launchApp(entry: AppEntry) {
        val launchIntent = packageManager.getLeanbackLaunchIntentForPackage(entry.packageName)
            ?: packageManager.getLaunchIntentForPackage(entry.packageName)

        if (launchIntent == null) {
            Toast.makeText(this, getString(R.string.cannot_launch, entry.label), Toast.LENGTH_SHORT).show()
            return
        }

        runCatching {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(launchIntent)
        }.onFailure {
            Toast.makeText(this, getString(R.string.cannot_launch, entry.label), Toast.LENGTH_SHORT).show()
        }
    }

    private fun openSystemSettings() {
        runCatching {
            startActivity(Intent(Settings.ACTION_SETTINGS))
        }.onFailure {
            Toast.makeText(this, R.string.settings_unavailable, Toast.LENGTH_SHORT).show()
        }
    }

    private fun buildDeviceSummary(): String {
        val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        val totalRamGb = memoryInfo.totalMem / 1024.0 / 1024.0 / 1024.0
        val manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }

        return getString(
            R.string.device_summary,
            manufacturer,
            Build.MODEL,
            Build.VERSION.RELEASE,
            Build.VERSION.SDK_INT,
            String.format(Locale.US, "%.1f", totalRamGb)
        )
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
        // A HOME activity should not disappear into an empty task when Back is pressed.
    }

    override fun onDestroy() {
        worker.shutdownNow()
        super.onDestroy()
    }
}
