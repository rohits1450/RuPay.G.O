package com.example.rupaygo.ui.screens

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.rupaygo.TokenStorage
import com.example.rupaygo.SecurityUtils
import com.example.rupaygo.TransactionStorage
import com.example.rupaygo.ui.components.QRCodeView
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun SendScreen() {

    val context = LocalContext.current
    val tokens = TokenStorage.loadTokensAsList(context)

    var selectedToken by remember { mutableStateOf<JSONObject?>(null) }
    var receiverWalletId by remember { mutableStateOf<String?>(null) }
    var qrData by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }

    // 🔍 SCAN RECEIVER WALLET QR
    val walletScanLauncher = rememberLauncherForActivityResult(
        contract = ScanContract(),
        onResult = { scanResult ->
            if (scanResult.contents == null) {
                Log.d("SEND", "Wallet QR scan cancelled")
                status = "Scan cancelled"
            } else {
                receiverWalletId = scanResult.contents
                Log.d("SEND", "Scanned Wallet ID = $receiverWalletId")
                status = "Receiver Wallet ID = $receiverWalletId"
            }
        }
    )

    Column(Modifier.padding(20.dp)) {

        Text("Pay Offline", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        // 🔍 SCAN RECEIVER WALLET BUTTON
        Button(onClick = {
            Log.d("SEND", "Launching wallet QR scanner...")
            val opts = ScanOptions()
            opts.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            opts.setPrompt("Scan Receiver Wallet QR")
            walletScanLauncher.launch(opts)
        }) {
            Text(if (receiverWalletId == null) "Scan Receiver Wallet" else "Rescan Wallet")
        }

        Spacer(Modifier.height(12.dp))
        receiverWalletId?.let { Text("Paying to: $it") }

        Spacer(Modifier.height(20.dp))

        // SELECT TOKEN
        Text("Select Token", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        var expanded by remember { mutableStateOf(false) }

        Box {
            OutlinedButton(onClick = { expanded = true }) {
                Text(
                    selectedToken?.let {
                        "₹${it.getInt("amount")} — ${it.getString("serial")}"
                    } ?: "Tap to select token"
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                tokens.forEach { token ->
                    DropdownMenuItem(
                        text = { Text("₹${token.getInt("amount")} — ${token.getString("serial")}") },
                        onClick = {
                            selectedToken = token
                            Log.d("SEND", "Selected token: $token")
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // GENERATE PAYMENT QR
        Button(
            enabled = selectedToken != null && receiverWalletId != null,
            onClick = {
                val token = selectedToken!!
                val walletId = receiverWalletId!!

                val serial = token.getString("serial")
                val amount = token.getInt("amount")
                val issuerSig = token.getString("issuerSig")

                Log.d("SEND", "Preparing token for send: serial=$serial amount=$amount")

                val oldChain = token.optJSONArray("chain") ?: JSONArray()
                val msgBytes = "$serial|$amount".encodeToByteArray()

                val holderPub = SecurityUtils.getPublicKeyBase64()
                val holderSig = SecurityUtils.signData(msgBytes)
                val deviceId = SecurityUtils.getDeviceId(context)

                Log.d("SEND", "Sender hop pubKey=$holderPub device=$deviceId")

                val newChain = JSONArray().apply {
                    for (i in 0 until oldChain.length()) put(oldChain.getJSONObject(i))
                    put(
                        JSONObject().apply {
                            put("holderPubKeySpkiB64", holderPub)
                            put("holderSignatureBase64", holderSig)
                            put("deviceId", deviceId)
                        }
                    )
                }

                val senderMobile = TokenStorage.getUserMobile(context)
                Log.d("SEND", "Using senderMobile = $senderMobile")

                val jsonToSend = JSONObject().apply {
                    put("receiverWalletId", walletId)
                    put("serial", serial)
                    put("amount", amount)
                    put("issuerSig", issuerSig)
                    put("chain", newChain)
                    put("senderMobile", senderMobile)
                    put("senderName", TokenStorage.getUserName(context))

                }

                // REMOVE TOKEN LOCALLY (burn note)
                Log.d("SEND", "Burning token locally...")
                TokenStorage.removeTokenBySerial(context, serial)

                // ONLY LOG LOCALLY (no pending transfer here!)
                TransactionStorage.saveEvent(
                    context,
                    "Sent",
                    amount,
                    "To $receiverWalletId"
                )

                qrData = jsonToSend.toString()
                Log.d("SEND", "Generated Payment QR: $qrData")

                status = "Show this QR to Receiver"
            }
        ) {
            Text("Generate Payment QR")
        }

        Spacer(Modifier.height(16.dp))
        Text(status)

        Spacer(Modifier.height(20.dp))

        if (qrData.isNotEmpty()) {
            QRCodeView(data = qrData)
        }
    }
}
