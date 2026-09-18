package com.mohithash.mindstream.ai

import com.mohithash.mindstream.data.Entry
import com.mohithash.mindstream.domain.Reflection
import com.mohithash.mindstream.domain.Settings
import com.mohithash.mindstream.domain.WeeklyInsight
import com.mohithash.mindstream.domain.MOODS

class JournalAi(private val client: AiClient) {
    private val reflectionSchema = Schema.obj(
        "summary" to Schema.str, "themes" to Schema.arr(Schema.str), "mood_word" to Schema.str, "sentiment" to Schema.int,
        "question" to Schema.str, "reframe" to Schema.str, "tiny_step" to Schema.str,
    )
    private val weeklySchema = Schema.obj(
        "headline" to Schema.str, "patterns" to Schema.arr(Schema.str), "wins" to Schema.arr(Schema.str), "watch_out" to Schema.str, "suggestion" to Schema.str,
    )

    private fun persona(s: Settings) = """You are a thoughtful journaling companion${if (s.name.isNotBlank()) " for ${s.name}" else ""}.
        |Tone: ${s.tone}. Never diagnose, never lecture, never moralise. Be specific to what was written; quote short phrases back when useful.
        |If the entry mentions self-harm or crisis, set summary to a caring note that they deserve support and suggest contacting local emergency services or a crisis line.""".trimMargin()

    suspend fun reflect(ai: AiSettings, s: Settings, e: Entry): Reflection {
        val system = persona(s) + "\nReflect on one journal entry. summary: 2 sentences max. themes: 1-4 short tags. sentiment: -2 (very negative) to 2 (very positive). " +
            "question: one open, gentle follow-up. reframe: only if something hard was written, otherwise empty string. tiny_step: one concrete action under 10 minutes."
        val user = "Mood self-rating: ${MOODS.getOrNull(e.mood - 1)?.second ?: e.mood}/5. Tags: ${e.tags.ifBlank { "none" }}.\n\nEntry:\n${e.text}"
        return client.ask(ai, system, user, reflectionSchema, maxTokens = 1500)
    }

    suspend fun weekly(ai: AiSettings, s: Settings, entries: List<Entry>): WeeklyInsight {
        val system = persona(s) + "\nYou are reviewing the last week of entries. headline: one sentence capturing the week. patterns: 2-4 recurring threads (moods, triggers, times). " +
            "wins: 1-3 things that went well, quoted or paraphrased. watch_out: one thing to be gentle about. suggestion: one experiment for next week."
        val user = entries.sortedBy { it.timestamp }.joinToString("\n\n") { "[${it.date} · mood ${it.mood}/5 · ${it.tags}]\n${it.text}" }
        val w: WeeklyInsight = client.ask(ai, system, user, weeklySchema, maxTokens = 2000)
        return w.copy(generatedAt = System.currentTimeMillis(), entryCount = entries.size)
    }

    suspend fun prompt(ai: AiSettings, s: Settings, recent: List<Entry>): String =
        client.chat(ai, persona(s) + "\nWrite ONE journaling prompt (a single question, under 20 words) tailored to what they've been writing about lately. Output only the question.",
            listOf(ChatMsg("user", recent.take(5).joinToString("\n") { "${it.date}: ${it.text.take(200)}" }.ifBlank { "No entries yet." })), maxTokens = 100).trim().trim('"')
}
