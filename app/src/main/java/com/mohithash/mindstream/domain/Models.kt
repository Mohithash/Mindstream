package com.mohithash.mindstream.domain

import kotlinx.serialization.Serializable

@Serializable
data class Reflection(
    val summary: String = "",
    val themes: List<String> = emptyList(),
    val mood_word: String = "",
    val sentiment: Int = 0,          // -2..2
    val question: String = "",       // one gentle follow-up question
    val reframe: String = "",        // a kinder way to see one hard thing, if any
    val tiny_step: String = "",      // one concrete small action for tomorrow
)

@Serializable
data class WeeklyInsight(
    val headline: String = "",
    val patterns: List<String> = emptyList(),
    val wins: List<String> = emptyList(),
    val watch_out: String = "",
    val suggestion: String = "",
    val generatedAt: Long = 0,
    val entryCount: Int = 0,
)

@Serializable
data class Settings(
    val name: String = "",
    val tone: String = "warm",       // warm | direct | playful
    val onboarded: Boolean = false,
)

val MOODS = listOf("😞" to "Rough", "😕" to "Low", "😐" to "Okay", "🙂" to "Good", "😄" to "Great")
val TAGS = listOf("work", "family", "friends", "health", "sleep", "exercise", "money", "creative", "love", "anxious", "grateful", "tired")
val PROMPTS = listOf(
    "What took up most of your headspace today?",
    "Name one thing that went better than expected.",
    "What would you tell a friend who had your day?",
    "What drained you, and what refilled you?",
    "What are you avoiding right now?",
    "Describe today in three words, then explain one.",
    "What did you do today that your future self will thank you for?",
    "Who made today easier? Did you tell them?",
    "What's one small thing you're looking forward to?",
    "If today had a soundtrack, what would it be and why?",
)
