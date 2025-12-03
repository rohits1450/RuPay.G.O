//package com.example.rupaygo.ui.screens

/*import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.rupaygo.ApiClient
import com.example.rupaygo.TokenStorage
import com.example.rupaygo.TransactionStorage
import com.example.rupaygo.ui.navigation.Routes
import kotlinx.coroutines.launch

@Composable
fun WithdrawScreen(navController: NavHostController) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val accountId = TokenStorage.getLinkedAccount(context)

    var amountText by remember { mutableStateOf("") }
    var upiId by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Text("Withdraw to Bank (UPI)", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it },
            label = { Text("Amount to Withdraw (₹)") },
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = upiId,
            onValueChange = { upiId = it },
            label = { Text("Your UPI ID") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))

        Button(
            enabled = !loading,
            onClick = {
                val amt = amountText.toIntOrNull()
                if (amt == null || amt <= 0) {
                    status = "Enter a valid amount"
                    return@Button
                }

                if (upiId.isBlank() || !upiId.contains("@")) {
                    status = "Enter a valid UPI ID (e.g., rohitselvam2006@oksbi)"
                    return@Button
                }

                loading = true
                status = "Processing…"

                scope.launch {
                    val res = ApiClient.withdrawToBank(
                        accountId = accountId,
                        amount = amt,
                        userUpiId = upiId
                    )

                    if (res != null && res.optBoolean("ok")) {
                        TransactionStorage.saveEvent(
                            context,
                            "Withdraw",
                            amt,
                            "To $upiId"
                        )
                        status = "₹$amt withdrawn to $upiId (Prototype Mode)"
                        navController.popBackStack(Routes.HOME, false)
                    } else {
                        status = "Withdrawal failed"
                    }

                    loading = false
                }

            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (loading) "Processing…" else "Withdraw")
        }

        Spacer(Modifier.height(12.dp))
        Text(status)
    }
}
*/