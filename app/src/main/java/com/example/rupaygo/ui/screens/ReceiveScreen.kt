package com.example.rupaygo.ui.screens

import android.util.Base64
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.rupaygo.SecurityUtils
import com.example.rupaygo.TokenStorage
import com.example.rupaygo.TransactionStorage
import com.example.rupaygo.ui.components.QRCodeView
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import org.json.JSONObject

@Composable
fun ReceiveScreen() {

    val context = LocalContext.current
    val walletId = remember { SecurityUtils.getWalletId() }

    var message by remember { mutableStateOf("") }

    val payScanLauncher = rememberLauncherForActivityResult(
        contract = ScanContract(),
        onResult = { scanResult ->

            if (scanResult.contents == null) {
                Log.d("RECEIVE", "Payment scan cancelled")
                message = "Scan cancelled"
                return@rememberLauncherForActivityResult
            }

            Log.d("RECEIVE", "Scanned payment QR: ${scanResult.contents}")

            try {
                val incoming = JSONObject(scanResult.contents)

                val target = incoming.getString("receiverWalletId")
                Log.d("RECEIVE", "Payment intended for wallet: $target")

                if (target != walletId) {
                    message = "❌ Payment not intended for this wallet"
                    return@rememberLauncherForActivityResult
                }

                val serial = incoming.getString("serial")
                val amount = incoming.getInt("amount")
                val issuerSig = incoming.getString("issuerSig")
                val chain = incoming.getJSONArray("chain")

                Log.d("RECEIVE", "Receiving token serial=$serial amount=$amount")

                val msgBytes = "$serial|$amount".encodeToByteArray()

                // VERIFY SIGNATURE CHAIN
                for (i in 0 until chain.length()) {
                    val hop = chain.getJSONObject(i)
                    val pub = hop.getString("holderPubKeySpkiB64")
                    val sig = Base64.decode(hop.getString("holderSignatureBase64"), Base64.NO_WRAP)

                    val ok = SecurityUtils.verifySignature(msgBytes, sig, pub)

                    Log.d("RECEIVE", "Validating hop $i pub=$pub result=$ok")

                    if (!ok) {
                        message = "❌ Invalid signature at hop $i"
                        return@rememberLauncherForActivityResult
                    }
                }

                // ADD RECEIVER HOP
                val myPub = SecurityUtils.getPublicKeyBase64()
                val mySig = SecurityUtils.signData(msgBytes)
                val deviceId = SecurityUtils.getDeviceId(context)

                Log.d("RECEIVE", "Adding receiver hop pub=$myPub device=$deviceId")

                chain.put(
                    JSONObject().apply {
                        put("holderPubKeySpkiB64", myPub)
                        put("holderSignatureBase64", mySig)
                        put("deviceId", deviceId)
                    }
                )

                // Extract REAL sender identity (with default)
                val senderMobile = incoming.optString("senderMobile", "Unknown Sender")
                val senderName = incoming.optString("senderName", "Unknown User")

                Log.d("RECEIVE", "senderMobile from QR = $senderMobile")

                // SAVE TOKEN OFFLINE
                Log.d("RECEIVE", "Saving token locally...")
                TokenStorage.saveToken(
                    context,
                    serial,
                    amount,
                    issuerSig,
                    chain,
                    senderMobile,
                    senderName
                )

                // STORE PENDING (for online reconciliation)
                val pending = JSONObject().apply {
                    put("serial", serial)
                    put("amount", amount)
                    put("issuerSig", issuerSig)
                    put("chain", chain)
                    put("redeemerPubKeySpkiB64", myPub)
                    put("senderMobile", senderMobile)
                    put("senderName", senderName)    // NEW

                }
                TokenStorage.addPendingTransfer(context, pending.toString())

                // Store transaction in history
                TransactionStorage.saveEvent(
                    context,
                    "Received",
                    amount,
                    "From $senderName"
                )

                message = "✅ Received ₹$amount (serial: $serial)"

            } catch (e: Exception) {
                Log.e("RECEIVE", "QR parse error", e)
                message = "Invalid or corrupted QR"
            }
        }
    )

    Column(Modifier.padding(20.dp)) {

        Text("My Wallet", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                QRCodeView(data = walletId)
                Spacer(Modifier.height(6.dp))
                Text(walletId)
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                Log.d("RECEIVE", "Launching payment QR scan")
                val opts = ScanOptions()
                opts.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                opts.setPrompt("Scan Payment QR")
                payScanLauncher.launch(opts)
            }
        ) {
            Text("Scan Payment QR")
        }

        Spacer(Modifier.height(16.dp))
        Text(message)
    }
}
