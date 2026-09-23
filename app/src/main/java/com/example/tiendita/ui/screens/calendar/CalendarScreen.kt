package com.example.tiendita.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tiendita.R
import com.example.tiendita.ui.components.AdminCard
import com.example.tiendita.ui.components.NexoTopBar
import com.example.tiendita.ui.components.ScreenBackground
import com.example.tiendita.ui.theme.NexoStockTheme
import java.util.Calendar

@Composable
fun CalendarScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val initialCal = Calendar.getInstance()
    var viewYear by rememberSaveable { mutableIntStateOf(initialCal.get(Calendar.YEAR)) }
    var viewMonth by rememberSaveable { mutableIntStateOf(initialCal.get(Calendar.MONTH)) }

    var selectedDay by rememberSaveable { mutableIntStateOf(-1) }
    var selectedMonth by rememberSaveable { mutableIntStateOf(-1) }
    var selectedYear by rememberSaveable { mutableIntStateOf(-1) }

    val daysInGrid = getDaysInMonth(viewYear, viewMonth)
    val monthNames = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )

    ScreenBackground(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize()) {
            NexoTopBar(title = stringResource(R.string.title_calendar), onBackClick = onBack)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                AdminCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Header: Prev, Month/Year, Next
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = {
                                if (viewMonth == 0) {
                                    viewMonth = 11
                                    viewYear -= 1
                                } else {
                                    viewMonth -= 1
                                }
                            }) {
                                Text("◀", color = MaterialTheme.colorScheme.primary)
                            }
                            
                            Text(
                                text = "${monthNames[viewMonth]} $viewYear",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            IconButton(onClick = {
                                if (viewMonth == 11) {
                                    viewMonth = 0
                                    viewYear += 1
                                } else {
                                    viewMonth += 1
                                }
                            }) {
                                Text("▶", color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Days of week header
                        Row(modifier = Modifier.fillMaxWidth()) {
                            val daysOfWeek = listOf(
                                R.string.day_sun, R.string.day_mon, R.string.day_tue,
                                R.string.day_wed, R.string.day_thu, R.string.day_fri, R.string.day_sat
                            )
                            daysOfWeek.forEach { dayRes ->
                                Text(
                                    text = stringResource(dayRes),
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Grid
                        val rows = daysInGrid.chunked(7)
                        rows.forEach { row ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                for (day in row) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .size(40.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (day != null) {
                                            val isSelected = day == selectedDay && viewMonth == selectedMonth && viewYear == selectedYear
                                            val isToday = day == initialCal.get(Calendar.DAY_OF_MONTH) &&
                                                    viewMonth == initialCal.get(Calendar.MONTH) &&
                                                    viewYear == initialCal.get(Calendar.YEAR)

                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        when {
                                                            isSelected -> MaterialTheme.colorScheme.primary
                                                            isToday -> MaterialTheme.colorScheme.primaryContainer
                                                            else -> MaterialTheme.colorScheme.surface
                                                        }
                                                    )
                                                    .clickable {
                                                        selectedDay = day
                                                        selectedMonth = viewMonth
                                                        selectedYear = viewYear
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = day.toString(),
                                                    color = when {
                                                        isSelected -> MaterialTheme.colorScheme.onPrimary
                                                        isToday -> MaterialTheme.colorScheme.primary
                                                        else -> MaterialTheme.colorScheme.onSurface
                                                    },
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
                                                )
                                            }
                                        }
                                    }
                                }
                                // Fill remaining slots in the last row if needed
                                val remaining = 7 - row.size
                                for (i in 0 until remaining) {
                                    Spacer(modifier = Modifier.weight(1f).size(40.dp))
                                }
                            }
                        }
                    }
                }

                // Selected Date Info Card
                AdminCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (selectedDay != -1) {
                            Text(
                                text = "$selectedDay de ${monthNames[selectedMonth]} de $selectedYear",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.msg_no_activities),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.msg_select_date),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CalendarScreenPreview() {
    NexoStockTheme {
        CalendarScreen(onBack = {})
    }
}
