package com.baebae.receipt.data.usage

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

/**
 * 런처에 아이콘이 있는 앱 목록. QUERY_ALL_PACKAGES 대신 manifest <queries> 의 MAIN/LAUNCHER 로 조회한다.
 * 화면 시간은 이 목록에 있는 앱만 센다(시스템 UI, 홈 런처, 이 앱 자신 제외).
 */
class InstalledApps(private val context: Context) {

    data class App(val packageName: String, val label: String)

    fun launcherApps(): List<App> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val excluded = homePackages(pm) + context.packageName
        return pm.queryIntentActivities(intent, 0)
            .asSequence()
            .map { it.activityInfo.packageName to it.loadLabel(pm).toString() }
            .filter { (pkg, _) -> pkg !in excluded }
            .distinctBy { it.first }
            .map { (pkg, label) -> App(pkg, label) }
            .sortedBy { it.label.lowercase() }
            .toList()
    }

    fun labelMap(): Map<String, String> = launcherApps().associate { it.packageName to it.label }

    private fun homePackages(pm: PackageManager): Set<String> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        return pm.queryIntentActivities(intent, 0).map { it.activityInfo.packageName }.toSet()
    }
}
