package com.baebae.receipt.data

import com.baebae.receipt.data.usage.DaySummary
import com.baebae.receipt.domain.AppUsage
import com.baebae.receipt.domain.ChosenPhrases
import com.baebae.receipt.domain.Phrase
import com.baebae.receipt.domain.WeeklyStats
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

/** Room 에 넣을 JSON 직렬화. 프레임워크 org.json 만 쓴다. */
object CardJson {

    fun appsToJson(apps: List<AppUsage>): String = JSONArray().apply {
        apps.forEach { put(JSONObject().put("p", it.packageName).put("l", it.label).put("ms", it.foregroundMs)) }
    }.toString()

    fun appsFromJson(json: String): List<AppUsage> {
        val array = JSONArray(json)
        return (0 until array.length()).map {
            val o = array.getJSONObject(it)
            AppUsage(o.getString("p"), o.optString("l", o.getString("p")), o.getLong("ms"))
        }
    }

    fun statsToJson(s: WeeklyStats): String = JSONObject()
        .put("weekStart", s.weekStart.toString())
        .put("days", s.days)
        .put("apps", JSONArray(appsToJson(s.apps)))
        .put("screenMs", s.screenMs)
        .put("unlocks", s.unlocks)
        .put("steps", s.steps)
        .put("sleepTotalMs", s.sleepTotalMs)
        .put("sleepNights", s.sleepNights)
        .put("lateNightMs", s.lateNightMs)
        .toString()

    fun statsFromJson(json: String): WeeklyStats {
        val o = JSONObject(json)
        return WeeklyStats(
            weekStart = LocalDate.parse(o.getString("weekStart")),
            days = o.getInt("days"),
            apps = appsFromJson(o.getJSONArray("apps").toString()),
            screenMs = o.getLong("screenMs"),
            unlocks = o.getInt("unlocks"),
            steps = o.getLong("steps"),
            sleepTotalMs = o.getLong("sleepTotalMs"),
            sleepNights = o.getInt("sleepNights"),
            lateNightMs = o.getLong("lateNightMs"),
        )
    }

    fun phrasesToJson(p: ChosenPhrases): String = JSONObject()
        .put("cautions", JSONArray().apply { p.cautions.forEach { put(phraseToJson(it)) } })
        .put("storage", p.storage?.let(::phraseToJson) ?: JSONObject.NULL)
        .put("expiry", p.expiry?.let(::phraseToJson) ?: JSONObject.NULL)
        .toString()

    fun phrasesFromJson(json: String): ChosenPhrases {
        val o = JSONObject(json)
        val cautions = o.getJSONArray("cautions")
        return ChosenPhrases(
            cautions = (0 until cautions.length()).map { phraseFromJson(cautions.getJSONObject(it)) },
            storage = o.optJSONObject("storage")?.let(::phraseFromJson),
            expiry = o.optJSONObject("expiry")?.let(::phraseFromJson),
        )
    }

    private fun phraseToJson(p: Phrase) = JSONObject().put("id", p.id).put("text", p.text)
    private fun phraseFromJson(o: JSONObject) = Phrase(o.getString("id"), o.getString("text"))

    fun hiddenToString(hidden: Set<String>): String = hidden.sorted().joinToString("\n")
    fun hiddenFromString(value: String): Set<String> = value.split('\n').filter { it.isNotBlank() }.toSet()

    fun daySummaryApps(summary: DaySummary, labels: Map<String, String>): String =
        appsToJson(summary.appMs.map { (pkg, ms) -> AppUsage(pkg, labels[pkg] ?: pkg, ms) })
}
