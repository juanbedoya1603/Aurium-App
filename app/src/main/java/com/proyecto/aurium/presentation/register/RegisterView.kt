package com.proyecto.aurium.presentation.register

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
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
fun RegisterView(
    viewModel: RegisterViewModel = viewModel(),
    navController: NavController
) {
    var fullName by remember { mutableStateOf("") }
    var documentNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }

    var showLoadingAlert by remember { mutableStateOf(false) }
    var showMessageAlert by remember { mutableStateOf(false) }

    var titleDialog by remember { mutableIntStateOf(0) }
    var messageDialog by remember { mutableIntStateOf(0) }

    val scrollState = rememberScrollState()

    if (showLoadingAlert) {
        ShowLoadingAlertDialog(onDismiss = { showLoadingAlert = false })
    }

    if (showMessageAlert) {
        ShowMessageAlertDialog(
            onConfirmation = {
                showMessageAlert = false
                if (titleDialog == R.string.dialog_success_title) {
                    navController.popBackStack()
                }
            },
            dialogTitle = titleDialog,
            dialogText = messageDialog
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(id = R.string.register_title),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text(stringResource(id = R.string.label_full_name)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = documentNumber,
            onValueChange = {
                if (it.all { char -> char.isDigit() }) documentNumber = it
            },
            label = { Text(stringResource(id = R.string.label_document_number)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(id = R.string.label_email)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = {
                if (it.all { char -> char.isDigit() } && it.length <= 10) phoneNumber = it
            },
            label = { Text(stringResource(id = R.string.label_phone_number)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = pin,
            onValueChange = {
                if (it.all { char -> char.isDigit() } && it.length <= 4) pin = it
            },
            label = { Text(stringResource(id = R.string.label_pin)) },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPin,
            onValueChange = {
                if (it.all { char -> char.isDigit() } && it.length <= 4) confirmPin = it
            },
            label = { Text(stringResource(id = R.string.label_confirm_pin)) },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                showLoadingAlert = true
                viewModel.register(
                    fullName = fullName,
                    documentNumber = documentNumber,
                    email = email,
                    phoneNumber = phoneNumber,
                    pin = pin,
                    confirmPin = confirmPin
                ) { success, message ->
                    if (showLoadingAlert) {
                        titleDialog = if (success) {
                            R.string.dialog_success_title
                        } else {
                            R.string.dialog_error_title
                        }
                        messageDialog = message
                        showLoadingAlert = false
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
            Text(text = stringResource(id = R.string.btn_register), fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.text_already_have_account),
                color = MaterialTheme.colorScheme.onBackground
            )
            TextButton(onClick = { navController.popBackStack() }) {
                Text(
                    text = stringResource(id = R.string.text_login_here),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterViewPreview() {
    val navController = rememberNavController()
    RegisterView(navController = navController)
}