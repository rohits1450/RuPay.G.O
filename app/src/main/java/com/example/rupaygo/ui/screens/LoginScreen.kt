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
fun LoginScreen(navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var mobile by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text("Welcome to RupayGO", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text(
            "Login with your mobile if you already have an account, or sign up as a new user.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = mobile,
            onValueChange = { mobile = it },
            label = { Text("Mobile Number") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))


        Button(
            enabled = !loading,
            onClick = {
                if (mobile.isBlank()) {
                    message = "Please enter mobile number"
                    return@Button
                }

                loading = true
                message = "Checking account..."

                scope.launch {
                    val res = ApiClient.loginWithPassword(mobile, password)

                    if (res == null) {
                        message = "Network error while checking account"
                        loading = false
                        return@launch
                    }

                    if (!res.optBoolean("ok")) {
                        val err = res.optString("error")
                        message = when (err) {
                            "mobile_not_found" -> "No account found for this mobile"
                            "wrong_password" -> "Incorrect password"
                            else -> "Login failed"
                        }
                        loading = false
                        return@launch
                    }


                    val accountId = res.getString("account_id")

                    // Register device + wallet
                    val deviceId = SecurityUtils.getDeviceId(context)
                    val pubKey = SecurityUtils.getPublicKeyBase64()
                    val attestation = SecurityUtils.getAttestationChain()
                    TokenStorage.saveLinkedAccount(context, accountId)

                    TokenStorage.saveUserMobile(context, mobile)
                    val regReply = ApiClient.registerWallet(
                        deviceId = deviceId,
                        pubKeySpkiB64 = pubKey,
                        attestationChain = attestation,
                        accountId = accountId
                    )



                    if (regReply == null) {
                        message = "Account found, but wallet linking failed"
                        loading = false
                        return@launch
                    }

                    message = "Login successful! "


                    // Navigate to home and clear back stack
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }

                    loading = false
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (loading) "Checking..." else "Login")
        }

        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            onClick = {
                // Go to onboarding for new account creation
                navController.navigate(Routes.ONBOARDING) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign Up (Create New Account)")
        }

        Spacer(Modifier.height(16.dp))

        if (message.isNotEmpty()) {
            Text(message, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
