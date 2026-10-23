package com.baebae.receipt.domain.phrase

import org.json.JSONObject

object PhraseCategory {
    const val CAUTION = "caution"
    const val STORAGE = "storage"
    const val EXPIRY = "expiry"
}

data class Condition(val metric: String, val op: String, val value: Double) {
    /** 지표가 없거나(NaN) 연산자를 모르면 거짓. */
    fun test(metrics: Map<String, Double>): Boolean {
        val m = metrics[metric] ?: return false
        if (m.isNaN()) return false
        return when (op) {
            ">" -> m > value
            ">=" -> m >= value
            "<" -> m < value
            "<=" -> m <= value
            else -> false
        }
    }
}

data class PhraseGroup(
    val id: String,
    val priority: Int,
    val conditions: List<Condition>,
    val phrases: List<String>,
) {
    val isDefault: Boolean get() = conditions.isEmpty()
    fun matches(metrics: Map<String, Double>) = conditions.all { it.test(metrics) }
}

data class CategoryDef(val pick: Int, val groups: List<PhraseGroup>)

/**
 * assets/phrases_{lang}.json. 문구 id 는 "카테고리.그룹.순번"이고 언어가 달라도 같은 자리를 가리킨다.
 */
class PhraseBank(
    val fallbackTopApp: String,
    val categories: Map<String, CategoryDef>,
) {
    fun text(id: String): String? {
        val parts = id.split('.')
        if (parts.size != 3) return null
        val group = categories[parts[0]]?.groups?.firstOrNull { it.id == parts[1] } ?: return null
        return group.phrases.getOrNull(parts[2].toIntOrNull() ?: return null)
    }

    companion object {
        fun id(category: String, group: String, index: Int) = "$category.$group.$index"

        fun parse(json: String): PhraseBank {
            val root = JSONObject(json)
            val cats = root.getJSONObject("categories")
            val categories = cats.keys().asSequence().associateWith { name ->
                val c = cats.getJSONObject(name)
                val groups = c.getJSONArray("groups")
                CategoryDef(
                    pick = c.optInt("pick", 1),
                    groups = (0 until groups.length()).map { i ->
                        val g = groups.getJSONObject(i)
                        val whens = g.optJSONArray("when")
                        val phrases = g.getJSONArray("phrases")
                        PhraseGroup(
                            id = g.getString("id"),
                            priority = g.optInt("priority", 0),
                            conditions = (0 until (whens?.length() ?: 0)).map { j ->
                                val w = whens!!.getJSONObject(j)
                                Condition(w.getString("metric"), w.getString("op"), w.getDouble("value"))
                            },
                            phrases = (0 until phrases.length()).map { phrases.getString(it) },
                        )
                    },
                )
            }
            return PhraseBank(root.optString("fallbackTopApp", ""), categories)
        }
    }
}
