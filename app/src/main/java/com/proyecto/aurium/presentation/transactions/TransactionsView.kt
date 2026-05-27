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
    var selectedMode by remember { mutableStateOf("Deposit") } // Modes: Deposit, Withdraw, Transfer
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
                    Text("Accept", color = AuriumOrange)
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
            text = "Transactions",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Text(
            text = "Deposit, withdraw, or transfer funds instantly",
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
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) Brush.horizontalGradient(
                                colors = listOf(Color(0xFFF39C12), Color(0xFFE67E22))
                            ) else Color.Transparent
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
                        text = mode,
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
                // Header inside the card depending on the selected mode
                Text(
                    text = when (selectedMode) {
                        "Deposit" -> "Deposit BTC"
                        "Withdraw" -> "Withdraw BTC"
                        else -> "Transfer BTC"
                    },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = when (selectedMode) {
                        "Deposit" -> "Add Bitcoin to your balance."
                        "Withdraw" -> "Withdraw Bitcoin from your wallet."
                        else -> "Send Bitcoin to another Aurium user."
                    },
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )

                // Render destination phone input if "Transfer" is selected
                if (selectedMode == "Transfer") {
                    OutlinedTextField(
                        value = destinationPhone,
                        onValueChange = {
                            if (it.all { char -> char.isDigit() } && it.length <= 10) {
                                destinationPhone = it
                            }
                        },
                        label = { Text("Recipient Phone Number") },
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = "Phone", tint = AuriumOrange)
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

                // Amount Input
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() || char == '.' }) {
                            amountText = it
                        }
                    },
                    label = { Text("Amount in BTC") },
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

                // Action Button
                Button(
                    onClick = {
                        when (selectedMode) {
                            "Deposit" -> {
                                viewModel.processTransaction(amountText, isDeposit = true) { success, message ->
                                    alertTitle = if (success) "Success!" else "Error"
                                    alertMessage = message
                                    showMessageAlert = true
                                    if (success) amountText = ""
                                }
                            }
                            "Withdraw" -> {
                                viewModel.processTransaction(amountText, isDeposit = false) { success, message ->
                                    alertTitle = if (success) "Success!" else "Error"
                                    alertMessage = message
                                    showMessageAlert = true
                                    if (success) amountText = ""
                                }
                            }
                            "Transfer" -> {
                                viewModel.transferBtc(amountText, destinationPhone) { success, message ->
                                    alertTitle = if (success) "Success!" else "Error"
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
                        "Deposit" -> "Deposit BTC"
                        "Withdraw" -> "Withdraw BTC"
                        else -> "Transfer BTC"
                    }
                    Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = label, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
