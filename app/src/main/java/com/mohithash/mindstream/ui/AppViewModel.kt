package com.mohithash.mindstream.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mohithash.mindstream.App
import com.mohithash.mindstream.ai.AiSettings
import com.mohithash.mindstream.data.Entry
import com.mohithash.mindstream.domain.PROMPTS
import com.mohithash.mindstream.domain.Reflection
import com.mohithash.mindstream.domain.Settings
import com.mohithash.mindstream.domain.WeeklyInsight
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.time.LocalDate

sealed interface Job<out T> {
    data object Idle : Job<Nothing>
    data object Loading : Job<Nothing>
    data class Done<T>(val value: T) : Job<T>
    data class Failed(val message: String) : Job<Nothing>
}

data class Stats(val streak: Int, val total: Int, val avgMood7: Double?, val moodSeries: List<Pair<String, Float>>)

class AppViewModel(private val app: App) : ViewModel() {
    private val db = app.db
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    val client get() = app.client

    val ai: StateFlow<AiSettings> = app.store.flow("ai", AiSettings.serializer(), AiSettings())
    val settings: StateFlow<Settings> = app.store.flow("settings", Settings.serializer(), Settings())
    val weekly: StateFlow<WeeklyInsight> = app.store.flow("weekly", WeeklyInsight.serializer(), WeeklyInsight())
    fun saveAi(a: AiSettings) = app.store.set("ai", AiSettings.serializer(), a)
    fun saveSettings(s: Settings) = app.store.set("settings", Settings.serializer(), s.copy(onboarded = true))

    val entries: StateFlow<List<Entry>> = db.entries().all().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stats: StateFlow<Stats> = db.entries().all().map { list ->
        val days = list.map { LocalDate.parse(it.date) }.toSet()
        var streak = 0; var d = LocalDate.now()
        if (d !in days) d = d.minusDays(1)
        while (d in days) { streak++; d = d.minusDays(1) }
        val last7 = list.filter { LocalDate.parse(it.date) >= LocalDate.now().minusDays(6) }
        val series = (0 until 14).map { LocalDate.now().minusDays(13L - it) }.map { day ->
            val m = list.filter { it.date == day.toString() }.map { it.mood }
            day.toString() to (if (m.isEmpty()) 0f else m.average().toFloat())
        }
        Stats(streak, list.size, last7.takeIf { it.isNotEmpty() }?.map { it.mood }?.average(), series)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Stats(0, 0, null, emptyList()))

    private val _reflect = MutableStateFlow<Job<Reflection>>(Job.Idle)
    val reflect: StateFlow<Job<Reflection>> = _reflect
    private val _weeklyJob = MutableStateFlow<Job<WeeklyInsight>>(Job.Idle)
    val weeklyJob: StateFlow<Job<WeeklyInsight>> = _weeklyJob
    val prompt = MutableStateFlow(PROMPTS.random())
    private val _promptJob = MutableStateFlow<Job<String>>(Job.Idle)
    val promptJob: StateFlow<Job<String>> = _promptJob
    val selected = MutableStateFlow<Entry?>(null)

    fun decode(e: Entry): Reflection? = e.reflection.takeIf { it.isNotBlank() }?.let { runCatching { json.decodeFromString(Reflection.serializer(), it) }.getOrNull() }

    /** Save the entry, then (if a key is set) reflect on it and attach the result. */
    fun save(text: String, mood: Int, tags: List<String>, reflectNow: Boolean) = viewModelScope.launch {
        val e = Entry(date = LocalDate.now().toString(), text = text.trim(), mood = mood, tags = tags.joinToString(","))
        val id = db.entries().insert(e)
        val saved = e.copy(id = id)
        selected.value = saved
        if (reflectNow) reflectOn(saved)
    }

    fun reflectOn(e: Entry) {
        _reflect.value = Job.Loading
        viewModelScope.launch {
            _reflect.value = runCatching { app.journal.reflect(ai.value, settings.value, e) }.fold({ r ->
                val updated = e.copy(reflection = json.encodeToString(Reflection.serializer(), r))
                db.entries().update(updated); selected.value = updated
                Job.Done(r)
            }, { Job.Failed(it.message ?: "Reflection failed") })
        }
    }
    fun clearReflect() { _reflect.value = Job.Idle }
    fun delete(id: Long) = viewModelScope.launch { db.entries().delete(id) }

    fun generateWeekly() {
        _weeklyJob.value = Job.Loading
        viewModelScope.launch {
            val recent = db.entries().recent(14).filter { LocalDate.parse(it.date) >= LocalDate.now().minusDays(7) }
            _weeklyJob.value = if (recent.size < 2) Job.Failed("Write at least two entries this week first.")
            else runCatching { app.journal.weekly(ai.value, settings.value, recent) }.fold({ app.store.set("weekly", WeeklyInsight.serializer(), it); Job.Done(it) }, { Job.Failed(it.message ?: "Failed") })
        }
    }

    fun newPrompt(useAi: Boolean) {
        if (!useAi || !ai.value.configured) { prompt.value = PROMPTS.filter { it != prompt.value }.random(); return }
        _promptJob.value = Job.Loading
        viewModelScope.launch {
            runCatching { app.journal.prompt(ai.value, settings.value, db.entries().recent(5)) }
                .onSuccess { prompt.value = it; _promptJob.value = Job.Idle }
                .onFailure { prompt.value = PROMPTS.random(); _promptJob.value = Job.Idle }
        }
    }
}
