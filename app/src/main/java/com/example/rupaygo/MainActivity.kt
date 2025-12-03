package com.example.rupaygo

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.rupaygo.ui.components.BottomNavBar
import com.example.rupaygo.ui.navigation.AppNavGraph
import com.example.rupaygo.ui.navigation.Routes
import com.example.rupaygo.ui.theme.RupayGOTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Init wallet
        TokenStorage.init(this)
        SecurityUtils.generateWalletKey(this)

        val linkedAccount = TokenStorage.getLinkedAccount(this)
        val startDest = if (linkedAccount.isEmpty()) {
            Routes.LOGIN
        } else {
            Routes.HOME
        }

        enableEdgeToEdge()

        setContent {
            val navController = rememberNavController()

            RupayGOTheme {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                Scaffold(
                    bottomBar = {
                        // Hide bottom nav on login & onboarding
                        if (currentRoute != Routes.LOGIN && currentRoute != Routes.ONBOARDING) {
                            BottomNavBar(navController)
                        }
                    }
                ) { padding ->
                    AppNavGraph(
                        navController = navController,
                        startDestination = startDest,
                        modifier = Modifier.padding(padding)
                    )
                }
            }
        }

        // If already onboarded earlier, ensure device is registered and pending transfers synced
        if (linkedAccount.isNotEmpty()) {
            lifecycleScope.launch {
                val deviceId = SecurityUtils.getDeviceId(this@MainActivity)
                val pubKey = SecurityUtils.getPublicKeyBase64()
                val attestation = SecurityUtils.getAttestationChain()

                val reply = ApiClient.registerWallet(
                    deviceId = deviceId,
                    pubKeySpkiB64 = pubKey,
                    attestationChain = attestation,
                    accountId = linkedAccount
                )



                Log.d("REGISTER", "Wallet register reply: $reply")

                withContext(Dispatchers.IO) {
                    ApiSync.syncPending(this@MainActivity)
                }
            }
        }

        // Auto-sync listener
        NetworkSync.register(this)
    }
}
