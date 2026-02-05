package com.example.stitchcounterv3.feature.sharedComposables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.stitchcounterv3.ui.theme.onQuaternary
import com.example.stitchcounterv3.ui.theme.quaternary

@Composable
fun BottomActionButtons(labelText: String = "Reset",
                        onResetAll: () -> Unit) {
    Button(
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.quaternary,
            contentColor = MaterialTheme.onQuaternary
        ),
        onClick = { onResetAll.invoke() },
        modifier = Modifier.fillMaxWidth()
            .imePadding()
    ) {
        Text(labelText)
    }
}