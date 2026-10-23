package com.baebae.receipt.domain

import java.time.LocalDate

enum class IngredientKind { APP, SLEEP, WALK }

/** 원재료 1건. [name]은 APP일 때만 쓰고, 수면/걷기는 화면에서 현지화 문자열로 표시한다. */
data class Ingredient(
    val key: String,
    val kind: IngredientKind,
    val name: String,
    val percent: Double,
)

data class NutritionFacts(
    val days: Int,
    val screenMs: Long,
    val unlocks: Int,
    val steps: Long,
    /** 수면 추정이 하루도 안 되면 null. */
    val avgSleepMs: Long?,
)

data class Phrase(val id: String, val text: String)

/** 카드 한 장에 들어갈 내용. 문구는 이미 고른 상태로 들고 있어서 다시 그려도 바뀌지 않는다. */
data class LabelCard(
    val productName: String,
    val periodStart: LocalDate,
    val periodEnd: LocalDate,
    val ingredients: List<Ingredient>,
    val nutrition: NutritionFacts,
    val cautions: List<Phrase>,
    val storage: Phrase?,
    val expiry: Phrase?,
    val hidden: Set<String> = emptySet(),
) {
    fun isShown(key: String) = key !in hidden
    val visibleIngredients: List<Ingredient> get() = ingredients.filter { isShown(it.key) }
}

/** 숨기기 대상 항목 키. 원재료는 [Ingredient.key]를 그대로 쓴다. */
object ItemKeys {
    const val SCREEN = "nut:screen"
    const val UNLOCKS = "nut:unlocks"
    const val STEPS = "nut:steps"
    const val SLEEP = "nut:sleep"
    const val CAUTION = "phrase:caution"
    const val STORAGE = "phrase:storage"
    const val EXPIRY = "phrase:expiry"

    const val INGREDIENT_SLEEP = "ing:sleep"
    const val INGREDIENT_WALK = "ing:walk"
    fun ingredientApp(packageName: String) = "ing:app:$packageName"
}
