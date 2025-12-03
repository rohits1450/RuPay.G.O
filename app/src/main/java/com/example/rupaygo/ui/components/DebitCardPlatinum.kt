package com.example.rupaygo.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun DebitCardPlatinum(
    name: String,
    accountId: String,
    balance: Int
) {
    // ✨ Entry animation
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        animProgress.animateTo(
            1f,
            tween(800, easing = FastOutSlowInEasing)
        )
    }

    // ✨ Metallic shimmer brush
    val shimmerBrush = Brush.linearGradient(
        listOf(
            Color(0xFFEFEFEF),
            Color(0xFFFFFFFF),
            Color(0xFFE0E0E0)
        ),
        start = Offset(0f, 0f),
        end = Offset(600f, 600f)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .graphicsLayer {
                translationY = (1f - animProgress.value) * 40f
                alpha = animProgress.value
                scaleX = animProgress.value
                scaleY = animProgress.value
            },
        shape = RoundedCornerShape(26.dp),
        elevation = CardDefaults.cardElevation(12.dp)
    ) {

        // 🎨 Premium PLATINUM metal background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFFCCCCCC),
                            Color(0xFFE6E6E6),
                            Color(0xFFD9D9D9),
                            Color(0xFFFFFFFF),
                            Color(0xFFBEBEBE)
                        )
                    )
                )
        ) {

            // 🌫 Soft glass overlay
            Box(
                Modifier
                    .fillMaxSize()
                    .blur(12.dp)
                    .background(Color.White.copy(alpha = 0.15f))
            )

            // 🎯 TEXT CONTENT
            Column(
                Modifier
                    .padding(20.dp)
                    .align(Alignment.TopStart)
            ) {

                Text(
                    "RuPay.G.O Express",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(Modifier.height(20.dp))

                Text(
                    "Account Balance",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.Black.copy(0.75f)
                    )
                )

                // 💰 Balance with shimmer
                Box(
                    Modifier
                        .padding(top = 4.dp)
                        .background(shimmerBrush)
                ) {
                    Text(
                        text = "₹$balance",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            color = Color.Black,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }

            Column(
                Modifier
                    .padding(20.dp)
                    .align(Alignment.BottomStart)
            ) {
                Text(
                    name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.Black,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    "A/c: $accountId",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.Black.copy(alpha = 0.9f)
                    )
                )
            }
        }
    }
}
