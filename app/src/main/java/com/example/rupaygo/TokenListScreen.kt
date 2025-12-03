package com.example.rupaygo

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TokenListScreen() {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var tokens by remember { mutableStateOf<List<JSONObject>>(emptyList()) }
    var totalValue by remember { mutableStateOf(0) }

    // For bottom sheet
    val sheetState = rememberModalBottomSheetState()
    var selectedToken by remember { mutableStateOf<JSONObject?>(null) }

    LaunchedEffect(Unit) {
        val list = TokenStorage.loadTokensAsList(context)
        tokens = list

        // Total value
        totalValue = list.sumOf { it.getInt("amount") }
    }

    // Bottom sheet UI
    if (selectedToken != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedToken = null },
            sheetState = sheetState
        ) {
            TokenDetailSheet(selectedToken!!)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp)
    ) {

        // --------------------------
        //   TOTAL OFFLINE BALANCE
        // --------------------------
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                Modifier
                    .padding(18.dp)
            ) {
                Text("Offline Balance", style = MaterialTheme.typography.titleMedium)
                Text(
                    "₹$totalValue",
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        // --------------------------
        //     TOKEN STACK LIST
        // --------------------------
        if (tokens.isEmpty()) {
            Text("No e₹ notes found in wallet.")
            return@Column
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tokens) { token ->
                TokenCard(token) {
                    selectedToken = token
                    scope.launch { sheetState.show() }
                }
            }
        }
    }
}

@Composable
fun TokenCard(token: JSONObject, onClick: () -> Unit) {

    val amount = token.getInt("amount")
    val serial = token.getString("serial")

    val noteColor = when {
        amount >= 500 -> Color(0xFFB71C1C)   // Red-ish for high denom
        amount >= 200 -> Color(0xFF4E342E)
        amount >= 100 -> Color(0xFFE65100)
        amount >= 50 -> Color(0xFF2E7D32)
        amount >= 20 -> Color(0xFF1976D2)
        else -> Color(0xFF616161)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .background(Color.Transparent)
            .defaultMinSize(minHeight = 90.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = noteColor.copy(alpha = 0.15f)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column {
                Text(
                    "₹$amount",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = noteColor
                )
                Text(
                    serial.take(16) + "...",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(
                "Tap",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun TokenDetailSheet(token: JSONObject) {

    val serial = token.getString("serial")
    val amount = token.getInt("amount")
    val issuerSig = token.getString("issuerSig")
    val chain = token.optJSONArray("chain") ?: null

    Column(
        Modifier
            .fillMaxWidth()
            .padding(18.dp)
    ) {
        Text(
            "e₹ Note Details",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(Modifier.height(12.dp))

        Text("Denomination: ₹$amount", style = MaterialTheme.typography.titleMedium)
        Text("Serial: $serial", style = MaterialTheme.typography.bodyMedium)

        Spacer(Modifier.height(12.dp))

        Text("Issuer Signature:", style = MaterialTheme.typography.titleSmall)
        Text(
            issuerSig.take(70) + "...\n",
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(Modifier.height(10.dp))

        Text("Transfer Chain (Audit):", style = MaterialTheme.typography.titleSmall)

        if (chain == null || chain.length() == 0) {
            Text("No previous holders", style = MaterialTheme.typography.bodySmall)
        } else {
            for (i in 0 until chain.length()) {
                val hop = chain.getJSONObject(i)
                Text(
                    "- Hop $i → ${hop.getString("holderPubKeySpkiB64").take(40)}...",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(Modifier.height(20.dp))
    }
}
