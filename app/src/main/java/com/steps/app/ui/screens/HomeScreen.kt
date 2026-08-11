package com.steps.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.steps.app.ui.components.*
import com.steps.app.ui.theme.*
import com.steps.app.ui.viewmodel.UiState
import com.steps.app.util.Formatters
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(state: UiState, onOpenDate: (String) -> Unit) {
    val stats = state.stats
    val todayLabel = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.US))
    Column(Modifier.fillMaxSize().background(Bg).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("STEPS", color = Accent, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
                Text(todayLabel, color = TextSecondary, fontSize = 14.sp)
            }
            SourceBadge(isDemo = stats.todayIsDemo && !state.sensorAvailable)
        }
        Spacer(Modifier.height(28.dp))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            RingProgress((stats.todaySteps.toFloat() / stats.goal.coerceAtLeast(1)).coerceIn(0f, 1f), Modifier.size(260.dp), 16.dp) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    SectionLabel("TODAY")
                    AnimatedContent(stats.todaySteps, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "s") { v ->
                        Text(Formatters.steps(v), color = TextPrimary, fontSize = 52.sp, fontWeight = FontWeight.Light)
                    }
                    Text("of ${Formatters.steps(stats.goal)} goal", color = TextSecondary, fontSize = 14.sp)
                    val pct = ((stats.todaySteps.toFloat() / stats.goal) * 100).toInt().coerceAtLeast(0)
                    Text("$pct%", color = Accent, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Spacer(Modifier.height(28.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile("Weekly avg", Formatters.compact(stats.weekAvg), Modifier.weight(1f))
            StatTile("Monthly avg", Formatters.compact(stats.monthAvg), Modifier.weight(1f), Strain)
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile("Yearly avg", Formatters.compact(stats.yearAvg), Modifier.weight(1f))
            StatTile("Year total", Formatters.compact(stats.yearTotal), Modifier.weight(1f), Strain)
        }
        Spacer(Modifier.height(24.dp))
        StepsCard(Modifier.fillMaxWidth()) {
            SectionLabel("Last 14 days")
            Spacer(Modifier.height(16.dp))
            StepsBarChart(stats.recent, stats.goal, Modifier.fillMaxWidth()) { onOpenDate(it.date) }
        }
        Spacer(Modifier.height(16.dp))
        StepsCard(Modifier.fillMaxWidth()) {
            SectionLabel("Status")
            Spacer(Modifier.height(10.dp))
            Text(
                if (state.sensorAvailable) "Live step sensor is active."
                else "Demo history Jan 1-Aug 11, 2026 loaded. Grant activity permission for live tracking.",
                color = TextSecondary, fontSize = 13.sp
            )
        }
        Spacer(Modifier.height(100.dp))
    }
}
