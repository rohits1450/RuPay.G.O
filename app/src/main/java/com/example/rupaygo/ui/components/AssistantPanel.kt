package com.example.rupaygo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.rupaygo.TokenStorage
import com.example.rupaygo.ui.navigation.Routes
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import com.example.rupaygo.ApiClient
import com.example.rupaygo.SecurityUtils

@Composable
fun AssistantPanel(
    onClose: () -> Unit,
    navController: NavController
) {
    val cyberGradient = Brush.linearGradient(
        listOf(
            Color(0xFF00FF9D),
            Color(0xFF3ABEFF)
        )
    )

    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xAA000000)),
        contentAlignment = Alignment.BottomCenter
    ) {

        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black
            )
        ) {
            Column(Modifier.padding(20.dp)) {

                Text(
                    text = "RupayGO Assistant",
                    style = MaterialTheme.typography.titleLarge.copy(
                        brush = cyberGradient
                    )
                )

                Spacer(Modifier.height(12.dp))

                AssistantOption(
                    title = "Logout",
                    icon = "🔐",
                    textBrush = cyberGradient
                ) {
                    TokenStorage.clearAll(context)
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                    onClose()
                }

                // ✅ FIXED — SCOPE NOW VALID


                AssistantOption(
                    title = "Re-Link Account",
                    icon = "🔄",
                    textBrush = cyberGradient
                ) {
                    navController.navigate(Routes.ONBOARDING)
                    onClose()
                }

                AssistantOption(
                    title = "Clear Wallet",
                    icon = "🧹",
                    textBrush = cyberGradient
                ) {
                    TokenStorage.clearAll(context)
                    onClose()
                }

                AssistantOption(
                    title = "Debug Info",
                    icon = "🐞",
                    textBrush = cyberGradient
                ) {
                    navController.navigate(Routes.DEBUG)
                    onClose()
                }

                AssistantOption(
                    title = "User Guide",
                    icon = "📘",
                    textBrush = cyberGradient
                ) {
                    navController.navigate(Routes.GUIDE)
                    onClose()
                }

                Spacer(Modifier.height(10.dp))

                Button(
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0050A0)
                    )
                ) {
                    Text("Close", color = Color.White)
                }
            }
        }
    }
}
