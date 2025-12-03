package com.example.rupaygo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun GuideScreen() {
    Column(
        Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Text("How to Use RupayGO", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(20.dp))

        GuideStep(
            title = "1. Issue Tokens",
            text = "Load CBDC tokens online from the bank. They appear in your offline wallet."
        )

        GuideStep(
            title = "2. Send Offline Payments",
            text = "Scan receiver’s wallet QR → choose amount/token → show payment QR → they scan and receive."
        )

        GuideStep(
            title = "3. Receive Offline Payments",
            text = "Scan the payer’s payment QR. The token is securely added to your wallet."
        )

        GuideStep(
            title = "4. Sync Online",
            text = "Tap Sync Now to settle redeemed tokens into your linked bank account."
        )

        GuideStep(
            title = "5. Transaction History",
            text = "You can view all Sent / Received / Redeemed logs anytime."
        )

        GuideStep(
            title = "6. Settings",
            text = "You can Logout, Clear Wallet, Relink Account, and view Debug Info."
        )
    }
}

@Composable
fun GuideStep(title: String, text: String) {
    Column(Modifier.padding(vertical = 10.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(5.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
        Divider(Modifier.padding(top = 10.dp))
    }
}
