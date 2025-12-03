package com.example.rupaygo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.rupaygo.ui.navigation.Routes


@Composable
fun TransferScreen(navController: NavController) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Text("Transfer e₹ Offline", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(20.dp))

        Button(
            onClick = { navController.navigate(Routes.SEND) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Send Token")
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = { navController.navigate(Routes.RECEIVE) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Receive Token")
        }
    }
}
