package com.example.stitchcounterv3.feature.library

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.stitchcounterv3.domain.model.Project
import kotlin.math.roundToInt

@Composable
fun SwipeableProjectRow(
    project: Project,
    isSelected: Boolean,
    isMultiSelectMode: Boolean,
    onOpen: () -> Unit,
    onSelect: () -> Unit,
    onDelete: () -> Unit,
    onToggleMultiSelect: () -> Unit,
    onInfoClick: () -> Unit
) {
    val swipeThreshold = 80.dp
    val density = LocalDensity.current
    val swipeThresholdPx = with(density) { swipeThreshold.toPx() }
    
    var offsetX by remember { mutableFloatStateOf(0f) }
    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = tween(durationMillis = 300),
        label = "swipe_offset"
    )

    LaunchedEffect(isMultiSelectMode) {
        if (isMultiSelectMode) {
            offsetX = 0f
        }
    }

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        if (!isMultiSelectMode) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(swipeThreshold)
                    .align(Alignment.CenterEnd)
                    .background(
                        MaterialTheme.colorScheme.error,
                        RoundedCornerShape(16.dp)
                    )
                    .clickable {
                        if (offsetX < -swipeThresholdPx / 2) {
                            onDelete()
                            offsetX = 0f
                        }
                    }
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        ProjectRow(
            project = project,
            isSelected = isSelected,
            isMultiSelectMode = isMultiSelectMode,
            swipeOffset = animatedOffsetX,
            onOpen = {
                if (offsetX == 0f) {
                    onOpen()
                } else {
                    offsetX = 0f
                }
            },
            onSelect = {
                offsetX = 0f
                onSelect()
            },
            onDelete = onDelete,
            onToggleMultiSelect = onToggleMultiSelect,
            onInfoClick = onInfoClick,
            onResetSwipe = { offsetX = 0f },
            modifier = Modifier
                .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                .pointerInput(project.id, isMultiSelectMode) {
                    if (!isMultiSelectMode) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (offsetX >= -swipeThresholdPx / 2) {
                                    offsetX = 0f
                                }
                            }
                        ) { _, dragAmount ->
                            val newOffset = (offsetX + dragAmount).coerceIn(-swipeThresholdPx, 0f)
                            offsetX = newOffset
                        }
                    }
                }
        )
    }
}

@Composable
fun ProjectRow(
    modifier: Modifier = Modifier,
    project: Project,
    isSelected: Boolean,
    isMultiSelectMode: Boolean,
    swipeOffset: Float = 0f,
    onOpen: () -> Unit,
    onSelect: () -> Unit,
    onDelete: () -> Unit,
    onToggleMultiSelect: () -> Unit,
    onInfoClick: () -> Unit,
    onResetSwipe: () -> Unit = {},
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(project.id, isMultiSelectMode) {
                if (!isMultiSelectMode) {
                    detectTapGestures(
                        onTap = {
                            if (swipeOffset < 0f) {
                                onResetSwipe()
                            } else {
                                onOpen()
                            }
                        },
                        onLongPress = {
                            onToggleMultiSelect()
                            onSelect()
                        }
                    )
                } else {
                    detectTapGestures(
                        onTap = {
                            onSelect()
                        }
                    )
                }
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected && isMultiSelectMode) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProjectImageOrCheckbox(
                project = project,
                isSelected = isSelected,
                isMultiSelectMode = isMultiSelectMode,
                onSelect = onSelect
            )
            
            ProjectInfoSection(
                project = project,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            
            if (!isMultiSelectMode) {
                ProjectActionButtons(
                    onInfoClick = onInfoClick,
                    onDelete = onDelete
                )
            }
        }
    }
}

