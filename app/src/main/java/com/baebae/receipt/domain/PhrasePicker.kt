package com.baebae.receipt.domain

/** 원자료에 맞는 주의사항/보관방법/소비기한 문구를 고른다. */
interface PhrasePicker {
    /** 미리보기용. 같은 [seed]면 같은 문구가 나오고 사용 기록을 남기지 않는다. */
    fun preview(stats: WeeklyStats, seed: Long = stats.weekStart.toEpochDay()): ChosenPhrases

    /** 발행용. 최근에 쓴 문구를 피해서 고르고 사용 기록을 남긴다. */
    fun pickForPublish(stats: WeeklyStats): ChosenPhrases

    /** [category]만 지금 문구와 다른 것으로 다시 뽑는다. [record]면 사용 기록을 남긴다. */
    fun reroll(stats: WeeklyStats, current: ChosenPhrases, category: String, record: Boolean): ChosenPhrases

    /** 저장된 문구 id 를 지금 언어의 문구로 바꾼다(언어를 바꾼 뒤 지난 카드를 열 때). */
    fun localize(phrases: ChosenPhrases): ChosenPhrases

    /** {topApp} 자리에 넣을 앱이 없을 때 쓰는 말. */
    fun topAppFallback(): String
}
