package com.baebae.receipt.ui.card

import com.baebae.receipt.domain.Ingredient
import com.baebae.receipt.domain.IngredientKind
import com.baebae.receipt.domain.ItemKeys
import com.baebae.receipt.domain.LabelCard
import com.baebae.receipt.domain.NutritionFacts
import com.baebae.receipt.domain.Phrase
import com.baebae.receipt.util.WeekMath
import java.time.LocalDate

/** 디자인 확인용 더미 카드. 실제 데이터가 없을 때(권한 전 미리보기 등)에도 쓴다. */
object SampleCard {
    fun create(
        productName: String,
        cautions: List<String>,
        storage: String,
        expiry: String,
        today: LocalDate = LocalDate.now(),
    ): LabelCard {
        val start = WeekMath.weekStartOf(today)
        return LabelCard(
            productName = productName,
            periodStart = start,
            periodEnd = start.plusDays(6),
            ingredients = listOf(
                Ingredient(ItemKeys.ingredientApp("com.google.android.youtube"), IngredientKind.APP, "YouTube", 31.0),
                Ingredient(ItemKeys.INGREDIENT_SLEEP, IngredientKind.SLEEP, "", 24.0),
                Ingredient(ItemKeys.ingredientApp("com.instagram.android"), IngredientKind.APP, "Instagram", 12.0),
                Ingredient(ItemKeys.ingredientApp("com.kakao.talk"), IngredientKind.APP, "KakaoTalk", 9.0),
                Ingredient(ItemKeys.INGREDIENT_WALK, IngredientKind.WALK, "", 3.0),
            ),
            nutrition = NutritionFacts(
                days = 7,
                screenMs = (32 * 60 + 15) * 60_000L,
                unlocks = 612,
                steps = 41_203,
                avgSleepMs = (6 * 60 + 12) * 60_000L,
            ),
            cautions = cautions.mapIndexed { i, text -> Phrase("sample.caution.$i", text) },
            storage = Phrase("sample.storage", storage),
            expiry = Phrase("sample.expiry", expiry),
        )
    }
}
