package com.example.rupaygo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.rupaygo.TransactionEvent
import com.example.rupaygo.TransactionStorage
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight

@Composable
fun TransactionScreen() {
    val context = LocalContext.current
    val events = remember { TransactionStorage.loadAllEvents(context) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        item {
            Text(
                "Recent Transactions",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        items(events) { ev ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            ) {
                // Icon Circle - UPI Style
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            if (ev.type == "Sent") Color(0xFFFFE6E6)
                            else Color(0xFFE6FFE6),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (ev.type == "Sent") "↑" else "↓",
                        fontWeight = FontWeight.Bold,
                        color = if (ev.type == "Sent") Color.Red else Color(0xFF007E33)
                    )
                }

                Spacer(Modifier.width(16.dp))

                Column(
                    Modifier.weight(1f)
                ) {
                    Text(ev.type, fontWeight = FontWeight.Bold)
                    Text(ev.details, style = MaterialTheme.typography.bodySmall)
                    Text(ev.time, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }

                Text(
                    text = if (ev.type == "Sent") "-₹${ev.amount}" else "+₹${ev.amount}",
                    fontWeight = FontWeight.Bold,
                    color = if (ev.type == "Sent") Color.Red else Color(0xFF1B5E20)
                )
            }

            Divider()
        }
    }
}
