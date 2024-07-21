package io.github.annaharri89.stitchcounter.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import io.github.annaharri89.stitchcounter.R


// Colors
val white = Color(0XFFFFFFFF)
val black = Color(0XFF000000)

val navy_blue = Color(0XFF030637)
// Greys
val grey_A9A9A9 = Color(0XFFA9A9A9)
val grey_272525 = Color(0XFF272525)
val grey_444444 = Color(0XFF444444)
val grey_b0b0b0 = Color(0XFFb0b0b0)
val grey_6d6e71 = Color(0XFF6d6e71)
val grey_dadbdb = Color(0XFFdadbdb)
val grey_f0f0f0 = Color(0XFFf0f0f0)
val grey_454545 = Color(0XFF454545)
val grey_F4F6F8 = Color(0XFFF4F6F8)
val grey_262527 = Color(0XFF262527)
val grey_A89EAF = Color(0XFFA89EAF)
val grey_D6D3E1 = Color(0XFFD6D3E1)
val grey_938C99 = Color(0XFF938C99)
val grey_817888 = Color(0XFF817888)
val grey_F1F1F1 = Color(0XFFF1F1F1)
val grey_DEDEDE = Color(0XFFDEDEDE)

// Sea Cottage Theme
val seaCottageMint = Color(0XFF77E4C8)//0x tells the compiler that this is a hexadecimal number. FF says 100% alpha and then the rest of it is the color
val seaCottageSurf = Color(0XFF36C2CE)
val seaCottageWhaleLight = Color(0XFF478CCF)
val seaCottageWhaleDark = Color(0XFF4535C1)
val grey_191919 = Color(0XFF191919)


// Retro Summer Theme
val retroSummerCactus = Color(0XFF36BA98)
val retroSummerSun = Color(0XFFE9C46A)
val retroSummerOrangeLight = Color(0XFFF4A261)
val retroSummerOrangeDark = Color(0XFFE76F51)

// Light TextColors
val textPrimaryLight = black
val textSecondaryLight = grey_444444

// Dark TextColors
val textPrimaryDark = white
val textSecondaryDark = grey_b0b0b0

// Fonts
val dancingScriptRegular = FontFamily(Font(R.font.dancing_script_regular))
val dancingScriptMedium = FontFamily(Font(R.font.dancing_script_medium))
val dancingScriptSemiBold = FontFamily(Font(R.font.dancing_script_semi_bold))
val dancingScriptBold = FontFamily(Font(R.font.dancing_script_bold))

val josefinSansRegular = FontFamily(Font(R.font.josefin_sans_regular))
val josefinSansMedium = FontFamily(Font(R.font.josefin_sans_medium))
val josefinSansSemiBold = FontFamily(Font(R.font.josefin_sans_semi_bold))
val josefinSansBold = FontFamily(Font(R.font.josefin_sans_bold))

val kalniaGlazeRegular = FontFamily(Font(R.font.kalnia_glaze_regular))
val kalniaGlazeMedium = FontFamily(Font(R.font.kalnia_glaze_medium))
val kalniaGlazeSemiBold = FontFamily(Font(R.font.kalnia_glaze_semi_bold))
val kalniaGlazeBold = FontFamily(Font(R.font.kalnia_glaze_bold))

val loraRegular = FontFamily(Font(R.font.lora_regular))
val loraMedium = FontFamily(Font(R.font.lora_medium))
val loraSemiBold = FontFamily(Font(R.font.lora_semi_bold))
val loraBold = FontFamily(Font(R.font.lora_bold))

// Resources
val Spaces = AppSpaces()
val Shapes = AppShapes()
val Typography = AppTypography()

// Resources - Theme Colors
fun seaCottageLightColors() = AppColors(
    primary = seaCottageMint,
    secondary = seaCottageSurf,
    accentDark = seaCottageWhaleDark,
    accentLight = seaCottageWhaleLight,
    iconTint = grey_272525,//todo stitchCounterV2
    background = white,
    listItem = white,
    textPrimary = textPrimaryLight,//todo stitchCounterV2 this should be black for light and white for dark
    textSecondary = textSecondaryLight,
    cBlack = black,
    cWhite = white,
    cGrey = grey_444444,
)

fun seaCottageDarkColors() = AppColors(
    primary = seaCottageMint,
    secondary = seaCottageSurf,
    accentDark = seaCottageWhaleDark,
    accentLight = seaCottageWhaleLight,
    iconTint = white,
    background = grey_191919,//todo stitchCounterV2
    listItem = grey_262527,//todo stitchCounterV2
    textPrimary = textPrimaryDark,
    textSecondary = textSecondaryDark,
    cBlack = black,
    cWhite = white,
    cGrey = grey_444444,
)

fun retroSummerLightColors() = AppColors(
    primary = retroSummerOrangeLight,
    secondary = retroSummerOrangeDark,
    accentDark = retroSummerCactus,
    accentLight = retroSummerSun,
    iconTint = grey_272525,//todo stitchCounterV2
    background = white,//todo stitchCounterV2
    listItem = white,//todo stitchCounterV2
    textPrimary = grey_272525,//todo stitchCounterV2
    textSecondary = grey_444444,//todo stitchCounterV2
    cBlack = black,
    cWhite = white,
    cGrey = grey_444444,//todo stitchCounterV2
)

fun retroSummerDarkColors() = AppColors(
    primary = retroSummerOrangeLight,
    secondary = retroSummerOrangeDark,
    accentDark = retroSummerCactus,
    accentLight = retroSummerSun,
    iconTint = Color.White,//todo stitchCounterV2
    background = grey_191919,//todo stitchCounterV2
    listItem = grey_262527,//todo stitchCounterV2
    textPrimary = white,//todo stitchCounterV2
    textSecondary = grey_b0b0b0,//todo stitchCounterV2
    cBlack = black,
    cWhite = white,
    cGrey = grey_444444,//todo stitchCounterV2
)