

package com.example.rupaygo.ui.screens

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
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
fun AddMoneyScreen(navController: NavHostController) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val accountId = TokenStorage.getLinkedAccount(context)
    val registeredMobile = TokenStorage.getUserMobile(context)

    var amountText by remember { mutableStateOf("") }
    var upiTxnId by remember { mutableStateOf("") }
    var receiverUpiId by remember { mutableStateOf("") }
    var mobileInput by remember { mutableStateOf("") }

    var status by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    var screenshotBytes by remember { mutableStateOf<ByteArray?>(null) }
    var screenshotBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    // -------------------------
    // OCR extracted values
    // -------------------------
    var ocrAmt by remember { mutableStateOf("") }
    var ocrTxn by remember { mutableStateOf("") }
    var ocrUpi by remember { mutableStateOf("") }

    // Image Picker
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
            if (bytes != null) {
                screenshotBytes = bytes
                screenshotBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                status = "Screenshot selected"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {

        Text("Add Money (UPI Receipt Verification)", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it },
            label = { Text("Amount Sent") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = upiTxnId,
            onValueChange = { input -> upiTxnId = input.filter { it.isDigit() }.take(12) },
            label = { Text("UPI Transaction ID (12 digits)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = receiverUpiId,
            onValueChange = { receiverUpiId = it },
            label = { Text("Wallet UPI ID") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = mobileInput,
            onValueChange = { mobileInput = it.take(10) },
            label = { Text("Registered Mobile Number") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = { imagePicker.launch("image/*") },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Upload UPI Screenshot") }

        screenshotBitmap?.let {
            Spacer(Modifier.height(12.dp))
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        // -------------------------
        // OCR BUTTON
        // -------------------------
        Button(
            enabled = screenshotBytes != null,
            onClick = {
                if (screenshotBytes == null) {
                    status = "Upload screenshot first"
                    return@Button
                }

                loading = true
                status = "Extracting details using OCR…"

                scope.launch {
                    val res = ApiClient.ocrReceipt(screenshotBytes!!)
                    if (res == null) {
                        status = "OCR failed"
                        loading = false
                        return@launch
                    }

                    val parsed = res.optJSONObject("parsed")

                    ocrAmt = parsed?.optString("amount") ?: ""
                    ocrTxn = parsed?.optString("txn_id") ?: ""
                    ocrUpi = parsed?.optString("upi_id") ?: ""

                    if (amountText.isBlank()) amountText = ocrAmt
                    if (upiTxnId.isBlank()) upiTxnId = ocrTxn
                    if (receiverUpiId.isBlank()) receiverUpiId = ocrUpi

                    status = "OCR → Amount: $ocrAmt | TxnID: $ocrTxn | UPI: $ocrUpi"
                    loading = false
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Extract Details (OCR)") }

        Spacer(Modifier.height(20.dp))

        // -------------------------
        // FINAL CONFIRM BUTTON
        // -------------------------
        Button(
            enabled = !loading,
            onClick = {
                val amt = amountText.toIntOrNull()

                if (amt == null || amt <= 0 || amt.toString() != ocrAmt) {
                    status = "Invalid amount"
                    return@Button
                }

                if (upiTxnId.length != 12 || upiTxnId != ocrTxn) {
                    status = "Transaction ID mismatch"
                    return@Button
                }

                if (!receiverUpiId.contains("@") || receiverUpiId != ocrUpi) {
                    status = "Invalid UPI ID"
                    return@Button
                }

                if (mobileInput != registeredMobile) {
                    status = "Mobile number mismatch"
                    return@Button
                }

                loading = true
                status = "Verifying with OCR…"

                scope.launch {
                    val res = ApiClient.upiCredit(
                        accountId = accountId,
                        amount = amt,
                        upiTxnId = upiTxnId,
                        payerVpa = receiverUpiId,
                        rawResponse = "verified-ocr"
                    )

                    if (res != null && res.optBoolean("ok")) {
                        TransactionStorage.saveEvent(
                            context,
                            "Topup",
                            amt,
                            "Verified UPI Receipt"
                        )
                        navController.popBackStack(Routes.HOME, false)
                    } else {
                        status = "Duplicate amount crediting failed"
                    }

                    loading = false
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (loading) "Processing…" else "Confirm Credit")
        }

        Spacer(Modifier.height(16.dp))
        Text(status)
    }
}
