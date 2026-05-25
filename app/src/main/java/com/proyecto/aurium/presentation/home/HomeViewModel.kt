package com.proyecto.aurium.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.proyecto.aurium.data.repository.FirebaseUserRepositoryImpl
import com.proyecto.aurium.data.session.UserSession
import com.proyecto.aurium.domain.usecase.GetUserDataUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URL

data class PricePoint(
    val time: Long,
    val price: Float
)

data class HomeUiState(
    val userName: String = "",
    val balanceBtc: Double = 0.0,
    val currentBtcPrice: Float = 0f,
    val balanceUsd: Double = 0.0,
    val priceHistory: List<PricePoint> = emptyList(),
    val priceChangePercent: Float = 0f,
    val isLoadingUser: Boolean = true,
    val isLoadingChart: Boolean = true
)

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    private val getUserDataUseCase = GetUserDataUseCase(FirebaseUserRepositoryImpl())
    private val usersRef = FirebaseDatabase.getInstance().getReference("users")
    private var balanceListener: ValueEventListener? = null


    fun loadUserData(phoneNumber: String) {
        viewModelScope.launch {
            val user = getUserDataUseCase(phoneNumber)
            if (user != null) {
                _uiState.value = _uiState.value.copy(
                    userName      = user.fullName.split(" ").firstOrNull() ?: user.fullName,
                    balanceBtc    = user.balanceBtc,
                    isLoadingUser = false
                )
                listenToBalance()
            } else {
                _uiState.value = _uiState.value.copy(isLoadingUser = false)
            }
        }
        fetchBitcoinPrice()
    }

    private fun listenToBalance() {
        val userId = UserSession.userId ?: return

        balanceListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newBalance = when (val value = snapshot.value) {
                    is Double -> value
                    is Long   -> value.toDouble()
                    is String -> value.toDoubleOrNull() ?: 0.0
                    else      -> 0.0
                }
                UserSession.balanceBtc = newBalance
                val price = _uiState.value.currentBtcPrice
                _uiState.value = _uiState.value.copy(
                    balanceBtc = newBalance,
                    balanceUsd = if (price > 0) newBalance * price else 0.0
                )
            }
            override fun onCancelled(error: DatabaseError) {}
        }

        usersRef.child(userId).child("balanceBtc")
            .addValueEventListener(balanceListener!!)
    }

    private fun fetchBitcoinPrice() {
        viewModelScope.launch {
            try {
                val priceUrl = "https://api.coingecko.com/api/v3/simple/price" +
                        "?ids=bitcoin&vs_currencies=usd&include_24hr_change=true"
                val priceJson = JSONObject(URL(priceUrl).readText())
                val btcData = priceJson.getJSONObject("bitcoin")
                val currentPrice = btcData.getDouble("usd").toFloat()
                val change24h = btcData.getDouble("usd_24h_change").toFloat()

                val historyUrl = "https://api.coingecko.com/api/v3/coins/bitcoin/market_chart" +
                        "?vs_currency=usd&days=1&interval=hourly"
                val historyJson = JSONObject(URL(historyUrl).readText())
                val pricesArray = historyJson.getJSONArray("prices")

                val points = mutableListOf<PricePoint>()
                for (i in 0 until pricesArray.length()) {
                    val point = pricesArray.getJSONArray(i)
                    points.add(PricePoint(time = point.getLong(0), price = point.getDouble(1).toFloat()))
                }

                val btcBalance = _uiState.value.balanceBtc
                _uiState.value = _uiState.value.copy(
                    currentBtcPrice    = currentPrice,
                    priceChangePercent = change24h,
                    priceHistory       = points,
                    balanceUsd         = btcBalance * currentPrice,
                    isLoadingChart     = false
                )
            } catch (e: Exception) {
                val mockPrice = 67500f
                val btcBalance = _uiState.value.balanceBtc
                _uiState.value = _uiState.value.copy(
                    isLoadingChart     = false,
                    currentBtcPrice    = mockPrice,
                    priceChangePercent = 2.34f,
                    priceHistory       = generateMockPriceHistory(),
                    balanceUsd         = btcBalance * mockPrice
                )
            }
        }
    }

    private fun generateMockPriceHistory(): List<PricePoint> {
        val basePrice = 67500f
        val now = System.currentTimeMillis()
        return (23 downTo 0).map { hoursAgo ->
            val variation = (Math.random() * 2000 - 1000).toFloat()
            PricePoint(time = now - (hoursAgo * 3_600_000L), price = basePrice + variation)
        }
    }

    override fun onCleared() {
        super.onCleared()
        val userId = UserSession.userId ?: return
        balanceListener?.let {
            usersRef.child(userId).child("balanceBtc").removeEventListener(it)
        }
    }
}