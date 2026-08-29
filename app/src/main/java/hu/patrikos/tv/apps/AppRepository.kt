package hu.patrikos.tv.apps

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

class AppRepository(private val context: Context) {

    private val packageManager = context.packageManager

    private data class PreferredApp(
        val packageNames: Set<String>,
        val labelKeywords: Set<String>
    )

    private val preferredApps = listOf(
        PreferredApp(
            packageNames = setOf("com.google.android.youtube.tv"),
            labelKeywords = setOf("youtube")
        ),
        PreferredApp(
            packageNames = setOf("com.netflix.ninja"),
            labelKeywords = setOf("netflix")
        ),
        PreferredApp(
            packageNames = setOf("com.disney.disneyplus"),
            labelKeywords = setOf("disney", "disney+")
        )
    )

    fun loadTvApps(): List<AppEntry> {
        val tvIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        }

        val installedApps = packageManager
            .queryIntentActivities(tvIntent, PackageManager.MATCH_ALL)
            .asSequence()
            .filter { it.activityInfo.packageName != context.packageName }
            .distinctBy { it.activityInfo.packageName }
            .mapNotNull { resolveInfo ->
                runCatching {
                    AppEntry(
                        label = resolveInfo.loadLabel(packageManager).toString(),
                        packageName = resolveInfo.activityInfo.packageName,
                        icon = resolveInfo.loadIcon(packageManager)
                    )
                }.getOrNull()
            }
            .toList()

        return preferredApps.mapNotNull { preferred ->
            installedApps.firstOrNull { it.packageName in preferred.packageNames }
                ?: installedApps.firstOrNull { entry ->
                    preferred.labelKeywords.any { keyword ->
                        entry.label.contains(keyword, ignoreCase = true)
                    }
                }
        }.distinctBy { it.packageName }
    }
}
