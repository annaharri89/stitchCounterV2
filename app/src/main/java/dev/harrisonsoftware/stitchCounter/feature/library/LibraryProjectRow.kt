package dev.harrisonsoftware.stitchCounter.feature.library

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Surface
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.harrisonsoftware.stitchCounter.R
import dev.harrisonsoftware.stitchCounter.domain.model.Project
import dev.harrisonsoftware.stitchCounter.domain.model.ProjectType
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import dev.harrisonsoftware.stitchCounter.ui.theme.StitchCounterV3Theme
import kotlin.math.roundToInt

internal enum class ProjectRowSwipeState {
    Closed,
    Revealed
}

@OptIn(ExperimentalFoundationApi::class)
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
    val swipeVelocityThresholdPx = with(density) { 125.dp.toPx() }
    var swipeResetVersion by remember(project.id) { mutableIntStateOf(0) }
    val anchors = remember(swipeThresholdPx) {
        DraggableAnchors {
            ProjectRowSwipeState.Closed at 0f
            ProjectRowSwipeState.Revealed at -swipeThresholdPx
        }
    }
    val swipeState = remember(project.id, swipeResetVersion, anchors) {
        AnchoredDraggableState(
            initialValue = ProjectRowSwipeState.Closed,
            positionalThreshold = { totalDistance -> totalDistance / 2f },
            velocityThreshold = { swipeVelocityThresholdPx },
            animationSpec = tween(durationMillis = 220)
        ).apply {
            updateAnchors(anchors)
        }
    }

    LaunchedEffect(isMultiSelectMode) {
        if (isMultiSelectMode) {
            swipeResetVersion += 1
        }
    }

    val deleteActionLabel = stringResource(R.string.cd_delete_project)
    val multiSelectActionLabel = stringResource(R.string.cd_enter_multi_select)
    val detailsActionLabel = stringResource(R.string.cd_project_details)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                customActions = buildList {
                    if (!isMultiSelectMode) {
                        add(CustomAccessibilityAction(deleteActionLabel) { onDelete(); true })
                        add(CustomAccessibilityAction(multiSelectActionLabel) { onToggleMultiSelect(); onSelect(); true })
                        add(CustomAccessibilityAction(detailsActionLabel) { onInfoClick(); true })
                    }
                }
            }
    ) {
        if (!isMultiSelectMode) {
            Box(
                modifier = Modifier.matchParentSize(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(swipeThreshold)
                        .background(
                            MaterialTheme.colorScheme.error,
                            RoundedCornerShape(16.dp)
                        )
                        .combinedClickable(
                            onClick = {
                                if (swipeState.currentValue == ProjectRowSwipeState.Revealed) {
                                    onDelete()
                                    swipeResetVersion += 1
                                }
                            }
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.cd_delete),
                        tint = MaterialTheme.colorScheme.onError,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        ProjectRow(
            project = project,
            isSelected = isSelected,
            isMultiSelectMode = isMultiSelectMode,
            isSwipeRevealed = swipeState.currentValue == ProjectRowSwipeState.Revealed,
            onOpen = onOpen,
            onSelect = onSelect,
            onDelete = onDelete,
            onToggleMultiSelect = onToggleMultiSelect,
            onInfoClick = onInfoClick,
            onResetSwipe = {
                swipeResetVersion += 1
            },
            modifier = Modifier
                .offset { IntOffset(swipeState.requireOffset().roundToInt(), 0) }
                .anchoredDraggable(
                    state = swipeState,
                    orientation = Orientation.Horizontal,
                    enabled = !isMultiSelectMode
                )
        )
    }
}

internal enum class ProjectRowTapAction {
    OpenProject,
    ToggleSelection,
    ResetSwipeState
}

internal fun resolveProjectRowTapAction(
    isMultiSelectMode: Boolean,
    isSwipeRevealed: Boolean
): ProjectRowTapAction {
    return when {
        isMultiSelectMode -> ProjectRowTapAction.ToggleSelection
        isSwipeRevealed -> ProjectRowTapAction.ResetSwipeState
        else -> ProjectRowTapAction.OpenProject
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProjectRow(
    modifier: Modifier = Modifier,
    project: Project,
    isSelected: Boolean,
    isMultiSelectMode: Boolean,
    isSwipeRevealed: Boolean = false,
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
            .combinedClickable(
                onClick = {
                    when (resolveProjectRowTapAction(isMultiSelectMode, isSwipeRevealed)) {
                        ProjectRowTapAction.OpenProject -> onOpen()
                        ProjectRowTapAction.ToggleSelection -> onSelect()
                        ProjectRowTapAction.ResetSwipeState -> onResetSwipe()
                    }
                },
                onLongClick = {
                    if (!isMultiSelectMode) {
                        onToggleMultiSelect()
                        onSelect()
                    }
                }
            ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected && isMultiSelectMode) {
                MaterialTheme.colorScheme.secondaryContainer
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
        ProjectStatsContent(
            project = project,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProjectRowPreview() {
    StitchCounterV3Theme {
        Surface {
            ProjectRow(
                project = Project(
                    id = 1,
                    type = ProjectType.SINGLE,
                    title = "Cable Hat",
                    notes = "Needle size 5mm",
                    stitchCounterNumber = 84,
                    rowCounterNumber = 18,
                    totalRows = 72
                ),
                isSelected = false,
                isMultiSelectMode = false,
                isSwipeRevealed = false,
                onOpen = {},
                onSelect = {},
                onDelete = {},
                onToggleMultiSelect = {},
                onInfoClick = {}
            )
        }
    }
}
