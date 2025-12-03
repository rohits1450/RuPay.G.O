package com.example.rupaygo.ui.screens

import android.util.Base64
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.rupaygo.SecurityUtils
import com.example.rupaygo.TokenStorage

@Composable
fun DebugScreen() {

    val context = LocalContext.current
    val prefs = context.getSharedPreferences("wallet_tokens", android.content.Context.MODE_PRIVATE)

    val pending = prefs.getString("pending_transfers", "[]")
    val tokens = prefs.getString("tokens", "[]")
    val linkedAccount = TokenStorage.getLinkedAccount(context)
    val myPub = SecurityUtils.getPublicKeyBase64()

    Column(
        Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Text("Debug Information", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(20.dp))

        DebugItem("Linked Account", linkedAccount)
        DebugItem("Public Key (Base64)", myPub.take(40) + "...")
        DebugItem("Stored Tokens", tokens ?: "[]")
        DebugItem("Pending Transfers", pending ?: "[]")
    }
}

@Composable
fun DebugItem(title: String, value: String) {
    Column(Modifier.padding(vertical = 12.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.fillMaxWidth()
        )
        Divider(Modifier.padding(top = 10.dp))
    }
}
