package dev.harrisonsoftware.stitchCounter.feature.notes

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import dev.harrisonsoftware.stitchCounter.R
import dev.harrisonsoftware.stitchCounter.domain.model.Note
import kotlin.math.roundToInt

internal const val NOTE_BODY_PREVIEW_MAX_LINES = 2

internal enum class NoteRowSwipeState {
    Closed,
    Revealed,
}

internal enum class NoteRowTapAction {
    OpenNote,
    ResetSwipeState,
}

internal fun resolveNoteRowTapAction(isSwipeRevealed: Boolean): NoteRowTapAction {
    return if (isSwipeRevealed) {
        NoteRowTapAction.ResetSwipeState
    } else {
        NoteRowTapAction.OpenNote
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwipeableNoteRow(
    note: Note,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val swipeThreshold = 80.dp
    val density = LocalDensity.current
    val swipeThresholdPx = with(density) { swipeThreshold.toPx() }
    val swipeVelocityThresholdPx = with(density) { 125.dp.toPx() }
    var swipeResetVersion by remember(note.id) { mutableIntStateOf(0) }
    val anchors = remember(swipeThresholdPx) {
        DraggableAnchors {
            NoteRowSwipeState.Closed at 0f
            NoteRowSwipeState.Revealed at -swipeThresholdPx
        }
    }
    val swipeState = remember(note.id, swipeResetVersion, anchors) {
        AnchoredDraggableState(
            initialValue = NoteRowSwipeState.Closed,
            positionalThreshold = { totalDistance -> totalDistance / 2f },
            velocityThreshold = { swipeVelocityThresholdPx },
            animationSpec = tween(durationMillis = 220)
        ).apply {
            updateAnchors(anchors)
        }
    }

    val deleteActionLabel = stringResource(R.string.cd_delete_note)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                customActions = listOf(
                    CustomAccessibilityAction(deleteActionLabel) {
                        onDelete()
                        true
                    }
                )
            }
    ) {
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
                            if (swipeState.currentValue == NoteRowSwipeState.Revealed) {
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

        NoteRow(
            note = note,
            isSwipeRevealed = swipeState.currentValue == NoteRowSwipeState.Revealed,
            onClick = onClick,
            onResetSwipe = { swipeResetVersion += 1 },
            modifier = modifier
                .fillMaxWidth()
                .offset { IntOffset(swipeState.requireOffset().roundToInt(), 0) }
                .anchoredDraggable(
                    state = swipeState,
                    orientation = Orientation.Horizontal,
                )
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteRow(
    note: Note,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSwipeRevealed: Boolean = false,
    onResetSwipe: () -> Unit = {},
) {
    val rowDescription = stringResource(R.string.cd_edit_note, note.title)
    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    when (resolveNoteRowTapAction(isSwipeRevealed)) {
                        NoteRowTapAction.OpenNote -> onClick()
                        NoteRowTapAction.ResetSwipeState -> onResetSwipe()
                    }
                }
            )
            .semantics {
                role = Role.Button
                contentDescription = rowDescription
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = note.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (note.body.isNotBlank()) {
                Text(
                    text = note.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = noteBodyPreviewMaxLines(),
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

internal fun noteBodyPreviewMaxLines(): Int = NOTE_BODY_PREVIEW_MAX_LINES
