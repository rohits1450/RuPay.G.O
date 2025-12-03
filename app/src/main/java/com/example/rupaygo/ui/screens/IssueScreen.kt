package com.example.rupaygo.ui.screens

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import com.example.rupaygo.ApiClient
import com.example.rupaygo.SecurityUtils
import com.example.rupaygo.TokenStorage
import com.example.rupaygo.TransactionStorage
import kotlinx.coroutines.launch

@Composable
fun IssueScreen() {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val accountId = remember { TokenStorage.getLinkedAccount(context) }

    var amountText by remember { mutableStateOf("100") }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Text(
            "Convert Bank Money → e₹ Notes",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(16.dp))

        if (accountId.isEmpty()) {
            Text(
                "No linked account. Please complete onboarding first.",
                color = MaterialTheme.colorScheme.error
            )
            return@Column
        }

        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it },
            label = { Text("Amount (₹)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))

        Button(
            enabled = !loading,
            onClick = {
                val amt = amountText.toIntOrNull()
                if (amt == null || amt <= 0) {
                    message = "Enter a valid ₹ amount"
                    return@Button
                }

                loading = true
                message = "Requesting e₹..."

                val pubKey = SecurityUtils.getPublicKeyBase64()

                scope.launch {
                    try {
                        val json = ApiClient.purchaseECurrency(
                            accountId = accountId,
                            holderPubKeySpkiB64 = pubKey,
                            amount = amt
                        )

                        if (json == null || !json.optBoolean("ok")) {
                            message = "Purchase failed: ${json?.optString("error")}"
                        } else {
                            val arr = json.getJSONArray("tokens")
                            var total = 0

                            for (i in 0 until arr.length()) {
                                val t = arr.getJSONObject(i)
                                TokenStorage.saveToken(
                                    context,
                                    t.getString("serial"),
                                    t.getInt("amount"),
                                    t.getString("issuerSig")
                                )
                                total += t.getInt("amount")
                            }
                            TransactionStorage.saveEvent(
                                context,
                                "Issued",
                                total,
                                "Issued e₹ into wallet"
                            )


                            message = "Successfully issued ₹$total in offline e₹ notes "
                        }

                    } catch (e: Exception) {
                        message = "Server error. Try again later."
                    } finally {
                        loading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (loading) "Processing..." else "Convert to e₹")
        }

        Spacer(Modifier.height(16.dp))
        Text(message)
    }
}
