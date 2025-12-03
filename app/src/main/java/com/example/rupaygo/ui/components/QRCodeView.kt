package com.example.rupaygo.ui.components

import androidx.compose.runtime.Composable
import android.graphics.Bitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import com.google.zxing.qrcode.QRCodeWriter

@Composable
fun QRCodeView(data: String) {
    val writer = QRCodeWriter()
    val bitMatrix = writer.encode(data, com.google.zxing.BarcodeFormat.QR_CODE, 512, 512)

    val bmp = Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565)
    for (x in 0 until 512) {
        for (y in 0 until 512) {
            bmp.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        }
    }

    Image(
        bitmap = bmp.asImageBitmap(),
        contentDescription = "QR Code",
        modifier = Modifier.size(300.dp)
    )
}
