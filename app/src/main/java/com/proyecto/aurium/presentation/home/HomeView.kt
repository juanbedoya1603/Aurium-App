package com.proyecto.aurium.presentation.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.proyecto.aurium.domain.model.Transaction
import com.proyecto.aurium.presentation.transactions.TransactionsView
import com.proyecto.aurium.ui.theme.AuriumOrange
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeView(
    navController: NavController,
    phoneNumber: String,
    viewModel: HomeViewModel = viewModel()
) {
    LaunchedEffect(phoneNumber) {
        viewModel.loadUserData(phoneNumber)
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    val uiState by viewModel.uiState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Estás seguro que deseas cerrar sesión?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                ) {
                    Text("Cerrar sesión", color = Color(0xFFE74C3C))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar", color = AuriumOrange)
                }
            },
            containerColor = Color(0xFF252B3D),
            titleContentColor = Color.White,
            textContentColor = Color.White.copy(alpha = 0.7f)
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            AuriumBottomNav(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> HomeContent(
                    uiState = uiState,
                    onLogoutClick = { showLogoutDialog = true }
                )
                1 -> TransactionsView(navController = navController)
            }
        }
    }
}

@Composable
fun AuriumBottomNav(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar(
        containerColor = Color(0xFF161B28),
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
            label = { Text("Inicio", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AuriumOrange,
                selectedTextColor = AuriumOrange,
                unselectedIconColor = Color.White.copy(alpha = 0.45f),
                unselectedTextColor = Color.White.copy(alpha = 0.45f),
                indicatorColor = AuriumOrange.copy(alpha = 0.15f)
            )
        )
        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            icon = { Icon(Icons.Default.SwapHoriz, contentDescription = "Transacciones") },
            label = { Text("Transacciones", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AuriumOrange,
                selectedTextColor = AuriumOrange,
                unselectedIconColor = Color.White.copy(alpha = 0.45f),
                unselectedTextColor = Color.White.copy(alpha = 0.45f),
                indicatorColor = AuriumOrange.copy(alpha = 0.15f)
            )
        )
    }
}

@Composable
fun HomeContent(uiState: HomeUiState, onLogoutClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HomeHeader(
            userName = uiState.userName,
            isLoading = uiState.isLoadingUser,
            onLogoutClick = onLogoutClick
        )
        BalanceCard(
            balanceBtc = uiState.balanceBtc,
            balanceUsd = uiState.balanceUsd,
            currentBtcPrice = uiState.currentBtcPrice,
            isLoading = uiState.isLoadingUser
        )
        BitcoinChartCard(
            priceHistory = uiState.priceHistory,
            currentPrice = uiState.currentBtcPrice,
            changePercent = uiState.priceChangePercent,
            isLoading = uiState.isLoadingChart
        )
        TransactionHistorySection(
            transactions = uiState.transactions,
            isLoading = uiState.isLoadingTransactions
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun HomeHeader(userName: String, isLoading: Boolean, onLogoutClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Bienvenido${if (!isLoading && userName.isNotEmpty()) ", $userName" else ""}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            Text(
                text = "Aurium",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(AuriumOrange),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF252B3D)),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = onLogoutClick) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Cerrar sesión",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun BalanceCard(
    balanceBtc: Double,
    balanceUsd: Double,
    currentBtcPrice: Float,
    isLoading: Boolean
) {
    val usdFormatter = NumberFormat.getCurrencyInstance(Locale.US)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFF39C12), Color(0xFFE67E22))
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(24.dp)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(28.dp))
                }
            } else {
                Column {
                    Text("Saldo disponible", fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "₿ ${"%.6f".format(balanceBtc)}",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        Column {
                            Text("Equivalente USD", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                            Text(
                                text = if (balanceUsd > 0) usdFormatter.format(balanceUsd) else "---",
                                fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White
                            )
                        }
                        Column {
                            Text("Precio BTC", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                            Text(
                                text = if (currentBtcPrice > 0) usdFormatter.format(currentBtcPrice) else "---",
                                fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionHistorySection(
    transactions: List<Transaction>,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF252B3D))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Movimientos",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Historial de tu cuenta",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.45f),
                modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
            )

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AuriumOrange, strokeWidth = 2.dp)
                    }
                }
                transactions.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Aún no hay movimientos",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.35f)
                        )
                    }
                }
                else -> {
                    // Mostrar máximo 20 movimientos recientes
                    transactions.take(20).forEachIndexed { index, tx ->
                        TransactionItem(transaction = tx)
                        if (index < transactions.size - 1 && index < 19) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = Color.White.copy(alpha = 0.07f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    val (icon, iconBg, label, amountPrefix) = when (transaction.type) {
        "DEPOSIT" -> Quad(
            Icons.Default.ArrowDownward,
            Color(0xFF2ECC71),
            "Depósito",
            "+"
        )
        "WITHDRAW" -> Quad(
            Icons.Default.ArrowUpward,
            Color(0xFFE74C3C),
            "Retiro",
            "-"
        )
        "TRANSFER_SENT" -> Quad(
            Icons.Default.SwapHoriz,
            Color(0xFFF39C12),
            "Enviado${if (transaction.counterpartName.isNotEmpty()) " a ${transaction.counterpartName}" else ""}",
            "-"
        )
        "TRANSFER_RECEIVED" -> Quad(
            Icons.Default.SwapHoriz,
            Color(0xFF3498DB),
            "Recibido${if (transaction.counterpartName.isNotEmpty()) " de ${transaction.counterpartName}" else ""}",
            "+"
        )
        else -> Quad(Icons.Default.SwapHoriz, Color.Gray, transaction.type, "")
    }

    val amountColor = if (amountPrefix == "+") Color(0xFF2ECC71) else Color(0xFFE74C3C)
    val dateFormat = SimpleDateFormat("dd MMM · HH:mm", Locale("es", "CO"))
    val dateStr = dateFormat.format(Date(transaction.timestamp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconBg.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconBg, modifier = Modifier.size(20.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(text = dateStr, fontSize = 11.sp, color = Color.White.copy(alpha = 0.45f))
        }

        Text(
            text = "$amountPrefix₿ ${"%.6f".format(transaction.amountBtc)}",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = amountColor
        )
    }
}

data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun BitcoinChartCard(
    priceHistory: List<PricePoint>,
    currentPrice: Float,
    changePercent: Float,
    isLoading: Boolean
) {
    val isPositive = changePercent >= 0
    val changeColor = if (isPositive) Color(0xFF2ECC71) else Color(0xFFE74C3C)
    val usdFormatter = NumberFormat.getCurrencyInstance(Locale.US)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF252B3D))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(CircleShape).background(AuriumOrange.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("₿", fontSize = 18.sp, color = AuriumOrange)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Bitcoin", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("BTC / USD", fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (currentPrice > 0) usdFormatter.format(currentPrice) else "---",
                        fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White
                    )
                    if (!isLoading) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isPositive) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                contentDescription = null, tint = changeColor, modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "${"%.2f".format(kotlin.math.abs(changePercent))}%",
                                fontSize = 13.sp, fontWeight = FontWeight.Bold, color = changeColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(Color(0xFF2ECC71)))
                Text("Tiempo real · 24h", fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AuriumOrange, strokeWidth = 2.dp)
                }
            } else if (priceHistory.isNotEmpty()) {
                BitcoinLineChart(
                    priceHistory = priceHistory,
                    isPositive = isPositive,
                    modifier = Modifier.fillMaxWidth().height(160.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("24h atrás", fontSize = 10.sp, color = Color.White.copy(alpha = 0.35f))
                Text("Ahora", fontSize = 10.sp, color = Color.White.copy(alpha = 0.35f))
            }
        }
    }
}

@Composable
fun BitcoinLineChart(
    priceHistory: List<PricePoint>,
    isPositive: Boolean,
    modifier: Modifier = Modifier
) {
    val lineColor = if (isPositive) Color(0xFF2ECC71) else Color(0xFFE74C3C)
    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(priceHistory) {
        animProgress.animateTo(targetValue = 1f, animationSpec = tween(durationMillis = 1200))
    }

    Canvas(modifier = modifier) {
        val prices = priceHistory.map { it.price }
        val minPrice = prices.min()
        val maxPrice = prices.max()
        val priceRange = maxPrice - minPrice
        if (priceRange == 0f) return@Canvas

        val width = size.width
        val height = size.height
        val visibleCount = (prices.size * animProgress.value).toInt().coerceAtLeast(2)
        val visiblePrices = prices.take(visibleCount)

        fun xOf(index: Int) =
            if (visiblePrices.size <= 1) 0f
            else (index.toFloat() / (visiblePrices.size - 1)) * width

        fun yOf(price: Float) =
            height - ((price - minPrice) / priceRange) * height * 0.85f - height * 0.075f

        val fillPath = Path().apply {
            moveTo(xOf(0), height)
            lineTo(xOf(0), yOf(visiblePrices[0]))
            visiblePrices.forEachIndexed { i, p -> if (i > 0) lineTo(xOf(i), yOf(p)) }
            lineTo(xOf(visiblePrices.size - 1), height)
            close()
        }
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = 0.35f), lineColor.copy(alpha = 0f)),
                startY = 0f, endY = height
            )
        )

        val linePath = Path().apply {
            moveTo(xOf(0), yOf(visiblePrices[0]))
            visiblePrices.forEachIndexed { i, p -> if (i > 0) lineTo(xOf(i), yOf(p)) }
        }
        drawPath(path = linePath, color = lineColor, style = Stroke(width = 2.5f, cap = StrokeCap.Round))

        val lastX = xOf(visiblePrices.size - 1)
        val lastY = yOf(visiblePrices.last())
        drawCircle(color = lineColor.copy(alpha = 0.3f), radius = 12f, center = Offset(lastX, lastY))
        drawCircle(color = lineColor, radius = 5f, center = Offset(lastX, lastY))
        drawCircle(color = Color.White, radius = 2.5f, center = Offset(lastX, lastY))
        drawLine(
            color = Color.White.copy(alpha = 0.06f),
            start = Offset(0f, yOf(minPrice)), end = Offset(width, yOf(minPrice)), strokeWidth = 1f
        )
    }
}