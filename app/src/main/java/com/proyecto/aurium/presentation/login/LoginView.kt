package com.proyecto.aurium.presentation.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.proyecto.aurium.R
import com.proyecto.aurium.presentation.components.ShowLoadingAlertDialog
import com.proyecto.aurium.presentation.components.ShowMessageAlertDialog

@Composable
fun LoginView(
    viewModel: LoginViewModel = viewModel(),
    navController: NavController
) {
    var phoneNumber by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }

    var showLoadingAlert by remember { mutableStateOf(false) }
    var showMessageAlert by remember { mutableStateOf(false) }

    var titleDialog by remember { mutableIntStateOf(0) }
    var messageDialog by remember { mutableIntStateOf(0) }

    if (showLoadingAlert) {
        ShowLoadingAlertDialog()
    }

    if (showMessageAlert) {
        ShowMessageAlertDialog(
            onConfirmation = { showMessageAlert = false },
            dialogTitle = titleDialog,
            dialogText = messageDialog
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.login_title),
            fontSize = 36.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(48.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = {
                if (it.all { char -> char.isDigit() }) phoneNumber = it
            },
            label = { Text(stringResource(id = R.string.label_phone_number)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = pin,
            onValueChange = {
                if (it.all { char -> char.isDigit() } && it.length <= 4) {
                    pin = it
                }
            },
            label = { Text(stringResource(id = R.string.label_pin)) },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                showLoadingAlert = true
                viewModel.login(phoneNumber, pin) { success, message ->
                    showLoadingAlert = false
                    if (success) {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    } else {
                        titleDialog = R.string.dialog_error_title
                        messageDialog = message
                        showMessageAlert = true
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(text = stringResource(id = R.string.btn_login), fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { /* TODO: Lógica de recuperar PIN */ }) {
            Text(
                text = stringResource(id = R.string.btn_forgot_password),
                color = MaterialTheme.colorScheme.secondary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.text_no_account),
                color = MaterialTheme.colorScheme.onBackground
            )
            TextButton(onClick = { navController.navigate("register") }) {
                Text(
                    text = stringResource(id = R.string.text_register),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginViewPreview() {
    val navController = rememberNavController()
    LoginView(navController = navController)
}