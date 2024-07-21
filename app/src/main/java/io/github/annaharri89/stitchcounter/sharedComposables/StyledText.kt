package io.github.annaharri89.stitchcounter.sharedComposables

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import io.github.annaharri89.stitchcounter.dataObjects.StyledTextData

@Composable
fun StyledText(data: List<StyledTextData>) {
    Text(
        buildAnnotatedString {
            withStyle(style = ParagraphStyle(lineHeight = 32.sp)){
                data.forEachIndexed { index, sd ->
                    withStyle(style = sd.style) {
                        if (index >= 0 && index <= data.size - 1 && sd.text.firstOrNull() != ',') {
                            append(" ")
                        }
                        append(sd.text)
                    }
                }
            }
        }
    )
}