package com.baebae.receipt.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.baebae.receipt.BuildConfig
import com.baebae.receipt.data.AppPrefs
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import java.time.LocalDate

/**
 * 광고 노출 규칙을 한곳에 모은다.
 * - 전면: 새로 발행된 카드를 처음 열기 직전에만, 하루 [INTERSTITIAL_DAILY_CAP]회까지.
 * - 보상형: 사용자가 직접 고른 경우에만(템플릿 잠금 해제, 워터마크 제거 1회).
 * - 배너: 홈/설정 하단에만. 결과 카드 화면과 카드 이미지에는 넣지 않는다.
 * 광고 요청에는 사용 기록이나 카드 내용을 담지 않는다(타기팅 키워드 없음).
 */
class AdsManager(private val prefs: AppPrefs) {

    private var initialized = false
    private var interstitial: InterstitialAd? = null
    private var interstitialLoading = false

    /** 구글 권장대로 초기화는 백그라운드 스레드에서, 광고 로드는 메인 스레드에서 한다. */
    fun initialize(context: Context) {
        if (initialized) return
        initialized = true
        val app = context.applicationContext
        val main = Handler(Looper.getMainLooper())
        Thread { MobileAds.initialize(app) { main.post { preloadInterstitial(app) } } }.start()
    }

    private fun request(): AdRequest = AdRequest.Builder().build()

    fun preloadInterstitial(context: Context) {
        if (interstitial != null || interstitialLoading || !canShowInterstitialToday()) return
        interstitialLoading = true
        InterstitialAd.load(
            context.applicationContext,
            BuildConfig.AD_UNIT_INTERSTITIAL,
            request(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitial = ad
                    interstitialLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialLoading = false
                }
            },
        )
    }

    fun canShowInterstitialToday(today: LocalDate = LocalDate.now()): Boolean =
        prefs.interstitialCount(today) < INTERSTITIAL_DAILY_CAP

    /** 보여줄 수 있으면 전면 광고를 띄우고, 닫히거나 못 띄우면 [onDone]. */
    fun showInterstitialThen(activity: Activity, onDone: () -> Unit) {
        val ad = interstitial
        if (ad == null || !canShowInterstitialToday()) {
            onDone()
            preloadInterstitial(activity)
            return
        }
        interstitial = null
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                prefs.recordInterstitial(LocalDate.now())
            }

            override fun onAdDismissedFullScreenContent() {
                onDone()
                preloadInterstitial(activity)
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                onDone()
                preloadInterstitial(activity)
            }
        }
        ad.show(activity)
    }

    /**
     * 보상형 광고를 불러와 바로 보여준다. 끝까지 보면 [onRewarded], 어떤 경우든 마지막에 [onFinished].
     */
    fun showRewarded(activity: Activity, onRewarded: () -> Unit, onFinished: (rewarded: Boolean) -> Unit) {
        RewardedAd.load(
            activity,
            BuildConfig.AD_UNIT_REWARDED,
            request(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    var earned = false
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() = onFinished(earned)
                        override fun onAdFailedToShowFullScreenContent(error: AdError) = onFinished(false)
                    }
                    ad.show(activity) {
                        earned = true
                        onRewarded()
                    }
                }

                override fun onAdFailedToLoad(error: LoadAdError) = onFinished(false)
            },
        )
    }

    companion object {
        const val INTERSTITIAL_DAILY_CAP = 2
    }
}
