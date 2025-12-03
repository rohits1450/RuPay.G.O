package com.example.rupaygo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.rupaygo.TokenStorage
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun TokensScreen() {

    val context = LocalContext.current

    // Reactively watch token list JSON
    val tokensJson by TokenStorage.tokensJsonFlow.collectAsState()
    val balance by TokenStorage.balanceFlow.collectAsState()

    val tokens: List<JSONObject> = remember(tokensJson) {
        try {
            val arr = JSONArray(tokensJson)
            List(arr.length()) { idx -> arr.getJSONObject(idx) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text("Your Tokens", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))

        Text("Balance: ₹$balance")
        Spacer(Modifier.height(20.dp))

        LazyColumn {
            items(tokens) { token ->

                val serial = token.getString("serial")
                val amount = token.getInt("amount")
                val issuerSig = token.getString("issuerSig")

                // ✔ NEW: chain depth
                val chainDepth = token.optJSONArray("chain")?.length() ?: 0

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {

                        Text("Serial: $serial")
                        Text("Amount: ₹$amount")

                        Text("IssuerSig: ${issuerSig.take(30)}...")
                        Spacer(Modifier.height(8.dp))

                        // ✔ Show how many offline hops this token has passed through
                        Text("Chain depth: $chainDepth hops")
                    }
                }
            }
        }
    }
}
