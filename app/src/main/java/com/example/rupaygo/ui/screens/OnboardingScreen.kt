package com.example.rupaygo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.rupaygo.ApiClient
import com.example.rupaygo.SecurityUtils
import com.example.rupaygo.TokenStorage
import com.example.rupaygo.ui.navigation.Routes
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(navController: NavHostController) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var aadhaar by remember { mutableStateOf("") }
    var initialDeposit by remember { mutableStateOf("0") }
    var password by remember { mutableStateOf("") }


    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Text("Create Your CBDC Bank Account", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = mobile,
            onValueChange = { mobile = it },
            label = { Text("Mobile Number") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = aadhaar,
            onValueChange = { aadhaar = it },
            label = { Text("Aadhaar") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = initialDeposit,
            onValueChange = { initialDeposit = it },
            label = { Text("Initial Deposit (₹)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Set Password") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )


        Button(
            enabled = !loading,
            onClick = {
                if (name.isBlank() || mobile.isBlank() || aadhaar.isBlank()) {
                    message = "All fields are required"
                    return@Button
                }

                loading = true
                message = "Creating account..."

                scope.launch {
                    val initAmt = initialDeposit.toIntOrNull() ?: 0

                    // Create account
                    val accRes = ApiClient.createAccount(
                        holderName = name,
                        mobile = mobile,
                        aadhaar = aadhaar,
                        initialDeposit = initAmt,
                        password = password
                    )

                    if (accRes == null || !accRes.optBoolean("ok")) {
                        val err = accRes?.optString("error") ?: "Unknown error"
                        message = "Failed to create account: $err"
                        loading = false
                        return@launch
                    }

                    val accountId = accRes.getString("account_id")
                    TokenStorage.saveLinkedAccount(context, accountId)
                    TokenStorage.saveUserMobile(context, mobile)
                    TokenStorage.saveUserName(context, name)

                    // Register wallet
                    val deviceId = SecurityUtils.getDeviceId(context)
                    val pubKey = SecurityUtils.getPublicKeyBase64()
                    val attestation = SecurityUtils.getAttestationChain()

                    val regRes = ApiClient.registerWallet(
                        deviceId = deviceId,
                        pubKeySpkiB64 = pubKey,
                        attestationChain = attestation,
                        accountId = accountId
                    )



                    if (regRes == null) {
                        message = "Account created, wallet linking failed"
                    } else {
                        message = "Account created & wallet linked! "

                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    }

                    loading = false
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (loading) "Creating..." else "Create Account")
        }

        Spacer(Modifier.height(16.dp))

        if (message.isNotEmpty()) {
            Text(message)
        }
    }
}