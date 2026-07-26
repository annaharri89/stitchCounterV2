package dev.harrisonsoftware.stitchCounter.feature.sharedComposables

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val LANDSCAPE_SHEET_HEADER_TOP_PADDING = 18.dp

internal enum class SheetHeaderInsetStrategy {
    PORTRAIT_STATUS_BARS,
    LANDSCAPE_HORIZONTAL_WITH_FIXED_TOP,
}

internal fun sheetHeaderInsetStrategy(isWideLayout: Boolean): SheetHeaderInsetStrategy =
    if (isWideLayout) {
        SheetHeaderInsetStrategy.LANDSCAPE_HORIZONTAL_WITH_FIXED_TOP
    } else {
        SheetHeaderInsetStrategy.PORTRAIT_STATUS_BARS
    }

@Composable
internal fun navigationRailWindowInsets(): WindowInsets =
    WindowInsets.statusBars
        .only(WindowInsetsSides.Vertical)
        .union(WindowInsets.displayCutout.only(WindowInsetsSides.Start))

@Composable
internal fun windowInsetsForSheetHeader(strategy: SheetHeaderInsetStrategy): WindowInsets =
    when (strategy) {
        SheetHeaderInsetStrategy.PORTRAIT_STATUS_BARS -> WindowInsets.statusBars
        SheetHeaderInsetStrategy.LANDSCAPE_HORIZONTAL_WITH_FIXED_TOP ->
            WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)
    }

@Composable
fun Modifier.sheetHeaderInsetPadding(isWideLayout: Boolean): Modifier {
    val strategy = sheetHeaderInsetStrategy(isWideLayout)
    return windowInsetsPadding(windowInsetsForSheetHeader(strategy))
        .then(
            if (strategy == SheetHeaderInsetStrategy.LANDSCAPE_HORIZONTAL_WITH_FIXED_TOP) {
                Modifier.padding(top = LANDSCAPE_SHEET_HEADER_TOP_PADDING)
            } else {
                Modifier
            }
        )
}
