package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
fun RestTimerOverlay(
    secondsRemaining: Int,
    onStopTimer: () -> Unit,
    onAddSeconds: (Int) -> Unit
) {
    AnimatedVisibility(
        visible = secondsRemaining > 0,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        val minutes = secondsRemaining / 60
        val seconds = secondsRemaining % 60
        val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 8.dp,
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Rest Timer",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "REST TIMER",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = formattedTime,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    AssistChip(
                        onClick = { onAddSeconds(30) },
                        label = { Text("+30s", fontSize = 12.sp) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onStopTimer) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Timer",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}
