package com.baebae.receipt.domain

data class ChosenPhrases(
    val cautions: List<Phrase>,
    val storage: Phrase?,
    val expiry: Phrase?,
)

/** 원자료 → 카드. 순수 함수라 실데이터, 질문형 체험, 보관함 재생성 모두 같은 경로를 탄다. */
object LabelBuilder {

    const val MAX_APPS = 4

    /** 걸음 → 걷기 시간 환산. 분당 약 100보. */
    private const val MS_PER_STEP = 600L

    const val TOP_APP_PLACEHOLDER = "{topApp}"

    fun build(
        stats: WeeklyStats,
        productName: String,
        phrases: ChosenPhrases,
        hiddenPackages: Set<String> = emptySet(),
        hiddenItems: Set<String> = emptySet(),
        topAppFallback: String = "",
    ): LabelCard {
        // 문구 속 앱 이름은 그릴 때 채운다. 나중에 숨긴 앱이 지난 카드 문구에 남지 않게 하기 위해서다.
        val topApp = stats.apps.firstOrNull { it.packageName !in hiddenPackages && it.foregroundMs > 0 }?.label ?: topAppFallback
        fun Phrase.fill() = copy(text = text.replace(TOP_APP_PLACEHOLDER, topApp))
        return LabelCard(
            productName = productName,
            periodStart = stats.weekStart,
            periodEnd = stats.weekEnd,
            ingredients = ingredients(stats, hiddenPackages),
            nutrition = NutritionFacts(
                days = stats.days,
                screenMs = stats.screenMs,
                unlocks = stats.unlocks,
                steps = stats.steps,
                avgSleepMs = stats.avgSleepMs,
            ),
            cautions = phrases.cautions.map { it.fill() },
            storage = phrases.storage?.fill(),
            expiry = phrases.expiry?.fill(),
            hidden = hiddenItems,
        )
    }

    /**
     * 원재료명 및 함량. 분모는 측정된 활동 전체(화면 + 수면 + 걷기)이고,
     * 숨긴 앱은 분모에는 남기되 이름을 올리지 않는다. 실제 라벨처럼 함량 내림차순.
     */
    fun ingredients(stats: WeeklyStats, hiddenPackages: Set<String>): List<Ingredient> {
        val walkMs = stats.steps * MS_PER_STEP
        val pool = (stats.screenMs + stats.sleepTotalMs + walkMs).toDouble()
        if (pool <= 0.0) return emptyList()

        val items = buildList {
            stats.apps
                .filter { it.packageName !in hiddenPackages && it.foregroundMs > 0 }
                .take(MAX_APPS)
                .forEach { add(Ingredient(ItemKeys.ingredientApp(it.packageName), IngredientKind.APP, it.label, it.foregroundMs / pool * 100)) }
            if (stats.sleepTotalMs > 0) add(Ingredient(ItemKeys.INGREDIENT_SLEEP, IngredientKind.SLEEP, "", stats.sleepTotalMs / pool * 100))
            if (walkMs > 0) add(Ingredient(ItemKeys.INGREDIENT_WALK, IngredientKind.WALK, "", walkMs / pool * 100))
        }
        // 0.1% 미만은 표기하면 0.0%가 되므로 뺀다.
        return items.filter { it.percent >= 0.05 }.sortedByDescending { it.percent }
    }
}
