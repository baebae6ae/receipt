package com.baebae.receipt.domain

/** 원자료에 맞는 주의사항/보관방법/소비기한 문구를 고른다. */
interface PhrasePicker {
    /** 진행 중인 주 미리보기용. 같은 주에는 같은 문구가 나오도록 결정적으로 고르고 기록을 남기지 않는다. */
    fun preview(stats: WeeklyStats): ChosenPhrases

    /** 발행용. 최근에 쓴 문구를 피해서 고르고 사용 기록을 남긴다. */
    fun pickForPublish(stats: WeeklyStats): ChosenPhrases
}
