package com.proyecto.aurium.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.proyecto.aurium.R

@Composable
fun ShowLoadingAlertDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(stringResource(id = R.string.text_loading)) },
        text = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        },
        confirmButton = {
            Button(onClick = { onDismiss() }) {
                Text(stringResource(id = R.string.btn_cancel))
            }
        }
    )
}

@Composable
fun ShowMessageAlertDialog(
    onConfirmation: () -> Unit,
    dialogTitle: Int,
    dialogText: Int
) {
    AlertDialog(
        title = { Text(text = stringResource(id = dialogTitle)) },
        text = { Text(text = stringResource(id = dialogText)) },
        onDismissRequest = { },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text(stringResource(id = R.string.btn_accept))
            }
        }
    )
}