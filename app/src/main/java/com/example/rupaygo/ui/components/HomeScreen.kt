package com.example.rupaygo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.rupaygo.ApiClient
import com.example.rupaygo.ApiSync
import com.example.rupaygo.TokenStorage
import com.example.rupaygo.ui.navigation.Routes
import kotlinx.coroutines.launch
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun HomeScreen(navController: NavController) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val accountId = remember { TokenStorage.getLinkedAccount(context) }

    var name by remember { mutableStateOf("User") }
    var balance by remember { mutableStateOf(0) }

    // 🌟 AI Assistant visibility
    var showAssistant by remember { mutableStateOf(false) }

    // Fetch account info + debug logs
    LaunchedEffect(Unit) {
        val res = ApiClient.getAccountBalance(accountId)
        if (res != null && res.optBoolean("ok")) {
            name = res.getString("holder_name")
            balance = res.getInt("balance")
        }

        val p = context.getSharedPreferences("wallet_tokens", android.content.Context.MODE_PRIVATE)
        val pendingJson = p.getString("pending_transfers", "NO DATA")
        Log.d("PENDING_DEBUG", ">>> Pending Transfers = $pendingJson")
    }

    // ======================
    // MAIN SCREEN WRAPPED IN A BOX
    // to allow floating AI button + panel
    // ======================

    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // 🔥 TOP TITLE
            Text(
                text = "RuPay.G.O",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(Modifier.height(25.dp))

            // 🔥  CARD
            DebitCardPlatinum(
                name = name,
                accountId = accountId,
                balance = balance
            )

            Spacer(Modifier.height(30.dp))

            // 🔥 TRANSACTION HISTORY BUTTON
            Button(
                onClick = { navController.navigate(Routes.TRANSACTIONS) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Text("Transaction History")
            }

            Spacer(Modifier.height(20.dp))

            // 🔥 SYNC BUTTON
            Button(
                onClick = {
                    scope.launch {
                        ApiSync.syncPending(context)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1E88E5)
                )
            ) {
                Text("Sync Now")
            }
            Spacer(Modifier.height(20.dp))

            Button(
                onClick = { navController.navigate(Routes.ADD_MONEY) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2E7D32)
                )
            ) {
                Text("Add Money From Bank (UPI)")
            }
            Spacer(Modifier.height(20.dp))

            /*Button(
                onClick = { navController.navigate(Routes.WITHDRAW) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0)),
                shape = MaterialTheme.shapes.large
            ) {
                Text("Withdraw to Bank (UPI)")
            }*/



        }

        // ============================
        //   AI ASSISTANT POPUP PANEL
        // ============================

        if (showAssistant) {
            AssistantPanel(
                onClose = { showAssistant = false },
                navController = navController
            )
        }

        // ============================
        //   FLOATING AI BUTTON
        // ============================
        FloatingActionButton(
            onClick = { showAssistant = true },
            containerColor = Color(0xFF2196F3),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.SmartToy,
                contentDescription = "Assistant",
                modifier = Modifier
                    .size(30.dp)
                    .graphicsLayer(alpha = 0.99f)
                    .drawWithCache {
                        val brush = Brush.linearGradient(
                            listOf(Color(0xFF00FF9D), Color(0xFF3ABEFF))
                        )
                        onDrawWithContent {
                            drawContent()
                            drawRect(brush, blendMode = BlendMode.SrcAtop)
                        }
                    }
            )
        }
    }
}
