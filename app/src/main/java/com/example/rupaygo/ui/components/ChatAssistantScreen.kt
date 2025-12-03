package com.example.rupaygo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ChatAssistantScreen(onClose: () -> Unit) {

    val cyberGradient = Brush.linearGradient(
        listOf(Color(0xFF00FF9D), Color(0xFF3ABEFF))
    )

    val messages = remember { mutableStateListOf("Hey! I'm your RupayGO assistant 😊 How can I help you?") }
    var inputText by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.55f),
        colors = CardDefaults.cardColors(Color.Black),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(Modifier.padding(16.dp)) {

            // Title
            Text(
                text = "RupayGO Chat Assistant",
                style = MaterialTheme.typography.titleLarge.copy(
                    brush = cyberGradient
                )
            )


            Spacer(Modifier.height(12.dp))

            // MESSAGES
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
            ) {
                messages.forEachIndexed { index, msg ->
                    val isBot = index % 2 == 0

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        contentAlignment = if (isBot) Alignment.CenterStart else Alignment.CenterEnd
                    ) {
                        Text(
                            text = msg,
                            modifier = Modifier
                                .background(
                                    if (isBot) Color(0xFF1A1A1A) else Color(0xFF0050A0),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp),
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // INPUT BAR
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF111111),
                        unfocusedContainerColor = Color(0xFF111111),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.Cyan
                    )

                )

                Spacer(Modifier.width(8.dp))

                Button(onClick = {
                    if (inputText.isNotBlank()) {
                        messages.add(inputText)
                        messages.add(mockAiReply(inputText))
                        inputText = ""
                    }
                }) {
                    Text("Send")
                }
            }
        }
    }
}

// Dummy reply generator (replace with real AI later)
fun mockAiReply(input: String): String {
    return when {
        "logout" in input.lowercase() -> "To logout, open the assistant and tap Logout 🔐"
        "wallet" in input.lowercase() -> "You can clear wallet using the Clear Wallet option 🧹"
        else -> "Got it! I'll help you with that 😄"
    }
}
