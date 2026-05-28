package com.proyecto.aurium.presentation.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.proyecto.aurium.presentation.components.ShowLoadingAlertDialog
import com.proyecto.aurium.ui.theme.AuriumOrange

@Composable
fun TransactionsView(
    navController: NavController,
    viewModel: TransactionsViewModel = viewModel()
) {
    var selectedMode by remember { mutableStateOf("Deposit") }
    var amountText by remember { mutableStateOf("") }
    var destinationPhone by remember { mutableStateOf("") }

    val isLoading by viewModel.isLoading.collectAsState()

    var showMessageAlert by remember { mutableStateOf(false) }
    var alertTitle by remember { mutableStateOf("") }
    var alertMessage by remember { mutableStateOf("") }

    if (isLoading) {
        ShowLoadingAlertDialog(onDismiss = { })
    }

    if (showMessageAlert) {
        AlertDialog(
            onDismissRequest = { showMessageAlert = false },
            confirmButton = {
                TextButton(onClick = { showMessageAlert = false }) {
                    Text("Aceptar", color = AuriumOrange)
                }
            },
            title = { Text(alertTitle) },
            text = { Text(alertMessage) }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Transacciones",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Deposita, retira o transfiere fondos al instante",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(36.dp))

        // Pill-shaped operation selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF161B28), shape = RoundedCornerShape(16.dp))
                .padding(6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val modes = listOf("Deposit", "Withdraw", "Transfer")
            modes.forEach { mode ->
                val isSelected = selectedMode == mode
                val modeLabel = when (mode) {
                    "Deposit" -> "Depositar"
                    "Withdraw" -> "Retirar"
                    else -> "Transferir"
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .then(
                            if (isSelected) Modifier.background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFF39C12), Color(0xFFE67E22))
                                )
                            ) else Modifier.background(Color.Transparent)
                        )
                        .clickable {
                            selectedMode = mode
                            amountText = ""
                            destinationPhone = ""
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = modeLabel,
                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Container card for the transaction details
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF252B3D))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = when (selectedMode) {
                        "Deposit" -> "Depositar BTC"
                        "Withdraw" -> "Retirar BTC"
                        else -> "Transferir BTC"
                    },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = when (selectedMode) {
                        "Deposit" -> "Agrega Bitcoin a tu saldo."
                        "Withdraw" -> "Retira Bitcoin de tu billetera."
                        else -> "Envía Bitcoin a otro usuario de Aurium."
                    },
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )

                if (selectedMode == "Transfer") {
                    OutlinedTextField(
                        value = destinationPhone,
                        onValueChange = {
                            if (it.all { char -> char.isDigit() } && it.length <= 10) {
                                destinationPhone = it
                            }
                        },
                        label = { Text("Número de celular del destinatario") },
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = "Teléfono", tint = AuriumOrange)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AuriumOrange,
                            focusedLabelColor = AuriumOrange,
                            cursorColor = AuriumOrange,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() || char == '.' }) {
                            amountText = it
                        }
                    },
                    label = { Text("Monto en BTC") },
                    leadingIcon = {
                        Icon(Icons.Default.CurrencyBitcoin, contentDescription = "BTC", tint = AuriumOrange)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AuriumOrange,
                        focusedLabelColor = AuriumOrange,
                        cursorColor = AuriumOrange,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = {
                        when (selectedMode) {
                            "Deposit" -> {
                                viewModel.processTransaction(amountText, isDeposit = true) { success, message ->
                                    alertTitle = if (success) "Éxito" else "Error"
                                    alertMessage = message
                                    showMessageAlert = true
                                    if (success) amountText = ""
                                }
                            }
                            "Withdraw" -> {
                                viewModel.processTransaction(amountText, isDeposit = false) { success, message ->
                                    alertTitle = if (success) "Éxito" else "Error"
                                    alertMessage = message
                                    showMessageAlert = true
                                    if (success) amountText = ""
                                }
                            }
                            "Transfer" -> {
                                viewModel.transferBtc(amountText, destinationPhone) { success, message ->
                                    alertTitle = if (success) "Éxito" else "Error"
                                    alertMessage = message
                                    showMessageAlert = true
                                    if (success) {
                                        amountText = ""
                                        destinationPhone = ""
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when (selectedMode) {
                            "Deposit" -> Color(0xFF2ECC71)
                            "Withdraw" -> Color(0xFFE74C3C)
                            else -> AuriumOrange
                        },
                        contentColor = Color.White
                    )
                ) {
                    val icon = when (selectedMode) {
                        "Deposit" -> Icons.Default.ArrowDownward
                        "Withdraw" -> Icons.Default.ArrowUpward
                        else -> Icons.Default.Send
                    }
                    val label = when (selectedMode) {
                        "Deposit" -> "Depositar BTC"
                        "Withdraw" -> "Retirar BTC"
                        else -> "Transferir BTC"
                    }
                    Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = label, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
