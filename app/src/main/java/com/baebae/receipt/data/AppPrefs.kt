package com.baebae.receipt.data

import android.content.Context
import android.content.SharedPreferences

/** 작은 설정값. 사용 기록 같은 원자료는 여기 두지 않고 Room 에 둔다. */
class AppPrefs(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    var onboardingDone: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_DONE, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDING_DONE, value).apply()

    /** 카드 제품명에 들어갈 이름. 비어 있으면 기본값(나/Me)을 쓴다. */
    var nickname: String
        get() = prefs.getString(KEY_NICKNAME, "").orEmpty()
        set(value) = prefs.edit().putString(KEY_NICKNAME, value.trim().take(MAX_NICKNAME)).apply()

    /** 카드 원재료에 이름을 올리지 않을 앱. 화면 시간 합계에는 그대로 포함된다. */
    var hiddenPackages: Set<String>
        get() = prefs.getStringSet(KEY_HIDDEN_PACKAGES, emptySet()).orEmpty().toSet()
        set(value) = prefs.edit().putStringSet(KEY_HIDDEN_PACKAGES, value).apply()

    companion object {
        const val MAX_NICKNAME = 12
        private const val KEY_ONBOARDING_DONE = "onboarding_done"
        private const val KEY_NICKNAME = "nickname"
        private const val KEY_HIDDEN_PACKAGES = "hidden_packages"
    }
}
