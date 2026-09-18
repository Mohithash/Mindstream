@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)

package com.mohithash.mindstream.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohithash.mindstream.domain.MOODS
import com.mohithash.mindstream.domain.Reflection
import com.mohithash.mindstream.ui.AppViewModel
import com.mohithash.mindstream.ui.HeroCard
import com.mohithash.mindstream.ui.Job
import com.mohithash.mindstream.ui.Label
import com.mohithash.mindstream.ui.ShapeIcon
import com.mohithash.mindstream.ui.StatCard
import com.mohithash.mindstream.ui.prettyDate
import com.mohithash.mindstream.ui.theme.Brand

@Composable
fun EntryScreen(vm: AppViewModel, onBack: () -> Unit) {
    val e by vm.selected.collectAsState()
    val entry = e ?: run { onBack(); return }
    val job by vm.reflect.collectAsState()
    val ai by vm.ai.collectAsState()
    val cs = MaterialTheme.colorScheme
    val reflection: Reflection? = (job as? Job.Done)?.value ?: vm.decode(entry)

    Scaffold(topBar = {
        TopAppBar(title = { Text(entry.date.prettyDate()) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = cs.surface),
            navigationIcon = { IconButton({ vm.clearReflect(); onBack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
            actions = { IconButton({ vm.delete(entry.id); vm.clearReflect(); onBack() }) { Icon(Icons.Default.Delete, "Delete") } })
    }) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(MOODS[entry.mood - 1].first, style = MaterialTheme.typography.headlineMedium)
                    Column { Text(MOODS[entry.mood - 1].second, style = MaterialTheme.typography.titleMedium); if (entry.tags.isNotBlank()) Text(entry.tags.replace(",", " · "), style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant) }
                }
                Text(entry.text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 6.dp))
            }
            when (val j = job) {
                Job.Loading -> Row(Modifier.fillMaxWidth().padding(20.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) { LoadingIndicator(); Spacer(Modifier.size(12.dp)); Text("Reading between the lines…", color = cs.onSurfaceVariant) }
                is Job.Failed -> StatCard(container = cs.errorContainer) { Text(j.message, color = cs.onErrorContainer); Button({ vm.reflectOn(entry) }, shapes = ButtonDefaults.shapes()) { Text("Retry") } }
                else -> {}
            }
            reflection?.let { r ->
                HeroCard(colors = listOf(cs.primary, Brand.heroDeep), blobShape = MaterialShapes.Sunny) {
                    val on = cs.onPrimary
                    Label("Reflection · feels ${r.mood_word}", on.copy(alpha = 0.8f))
                    Text(r.summary, style = MaterialTheme.typography.titleLarge, color = on)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) { r.themes.forEach { SuggestionChip(onClick = {}, label = { Text(it) }, colors = androidx.compose.material3.SuggestionChipDefaults.suggestionChipColors(containerColor = on.copy(alpha = 0.14f), labelColor = on), border = null) } }
                }
                Insight(Icons.Default.QuestionMark, "A question for you", r.question, cs.secondaryContainer, cs.onSecondaryContainer)
                if (r.reframe.isNotBlank()) Insight(Icons.Default.Lightbulb, "Another way to see it", r.reframe, cs.tertiaryContainer, cs.onTertiaryContainer)
                Insight(Icons.Default.DirectionsWalk, "Tiny step for tomorrow", r.tiny_step, cs.surfaceContainerHigh, cs.onSurface)
            }
            if (reflection == null && job == Job.Idle) Button({ vm.reflectOn(entry) }, enabled = ai.configured, shapes = ButtonDefaults.shapes(), modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Icon(Icons.Default.AutoAwesome, null); Spacer(Modifier.size(8.dp)); Text("Reflect on this entry")
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun Insight(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, body: String, bg: androidx.compose.ui.graphics.Color, fg: androidx.compose.ui.graphics.Color) {
    StatCard(container = bg) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ShapeIcon(icon, fg.copy(alpha = 0.12f), fg, MaterialShapes.Cookie7Sided)
            Column { Label(title, fg.copy(alpha = 0.8f)); Text(body, style = MaterialTheme.typography.bodyLarge, color = fg) }
        }
    }
}
