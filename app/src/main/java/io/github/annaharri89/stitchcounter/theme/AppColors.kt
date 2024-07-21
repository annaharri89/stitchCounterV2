package io.github.annaharri89.stitchcounter.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

class AppColors(
    primary: Color,
    secondary: Color,
    accentDark: Color,
    accentLight: Color,
    iconTint: Color,
    cBlack: Color,
    cWhite: Color,
    cGrey: Color,
    background: Color,
    listItem: Color,
    textPrimary: Color,
    textSecondary: Color,
) {
    var primary by mutableStateOf(primary)
        private set

    var secondary by mutableStateOf(secondary)
        private set

    var accentDark by mutableStateOf(accentDark)
        private set

    var accentLight by mutableStateOf(accentLight)
        private set

    var iconTint by mutableStateOf(iconTint)
        private set

    var cBlack by mutableStateOf(cBlack)
        private set

    var cWhite by mutableStateOf(cWhite)
        private set

    var cGrey by mutableStateOf(cGrey)
        private set

    var background by mutableStateOf(background)
        private set

    var listItem by mutableStateOf(listItem)
        private set

    var textPrimary by mutableStateOf(textPrimary)
        private set

    var textSecondary by mutableStateOf(textSecondary)
        private set

    fun copy(
        primary: Color = this.primary,
        secondary: Color = this.secondary,
        accentDark: Color = this.accentDark,
        accentLight: Color = this.accentLight,
        iconTint: Color = this.iconTint,
        cBlack: Color = this.cBlack,
        cWhite: Color = this.cWhite,
        background: Color = this.background,
        textPrimary: Color = this.textPrimary,
        textSecondary: Color = this.textSecondary,
    ) = AppColors(
        primary = primary,
        secondary = secondary,
        accentDark = accentDark,
        accentLight = accentLight,
        iconTint = iconTint,
        cBlack = cBlack,
        cWhite = cWhite,
        background = background,
        textPrimary = textPrimary,
        textSecondary = textSecondary,
        cGrey = cGrey,
        listItem = listItem
    )

    fun updateColorsFrom(other: AppColors) {
        primary = other.primary
        secondary = other.secondary
        accentDark = other.accentDark
        accentLight = other.accentLight
        iconTint = other.iconTint
        cBlack = other.cBlack
        cWhite = other.cWhite
        background = other.background
        textPrimary = other.textPrimary
        textSecondary = other.textSecondary
        cGrey = other.cGrey
        listItem = listItem
    }
}

val LocalColors = staticCompositionLocalOf { seaCottageLightColors() }
