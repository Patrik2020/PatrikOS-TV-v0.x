package hu.patrikos.tv.apps

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import java.text.Collator
import java.util.Locale

class AppRepository(private val context: Context) {

    private val packageManager = context.packageManager

    fun loadTvApps(): List<AppEntry> {
        val tvIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        }

        val collator = Collator.getInstance(Locale.getDefault())

        return packageManager
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
            .sortedWith { left, right -> collator.compare(left.label, right.label) }
            .toList()
    }
}
