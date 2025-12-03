package com.example.rupaygo

import android.content.Context
import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import com.example.rupaygo.TransactionStorage

object ApiSync {

    private val client = OkHttpClient()
    private const val BASE_URL = "http://192.168.0.3:3000"

    private fun redeemToken(tokenJson: JSONObject): Boolean {
        return try {

            val serial = tokenJson.getString("serial")
            val amount = tokenJson.getInt("amount")
            val issuerSig = tokenJson.getString("issuerSig")
            val chain = tokenJson.getJSONArray("chain")

            // Use senderName instead of senderMobile
            val senderName = tokenJson.optString("senderName", "Unknown User")

            val finalHolderSpki = if (chain.length() > 0) {
                chain.getJSONObject(chain.length() - 1)
                    .getString("holderPubKeySpkiB64")
            } else {
                SecurityUtils.getPublicKeyBase64()
            }

            val payload = JSONObject().apply {
                put("serial", serial)
                put("amount", amount)
                put("issuerSig", issuerSig)
                put("chain", chain)
                put("redeemerPubKeySpkiB64", finalHolderSpki)
                put("senderName", senderName)    // IMPORTANT
            }

            val body = payload.toString()
                .toRequestBody("application/json".toMediaType())

            val req = Request.Builder()
                .url("$BASE_URL/redeem")
                .post(body)
                .build()

            val resp = client.newCall(req).execute()
            val text = resp.body?.string() ?: ""
            Log.d("SYNC", "Redeem response for $serial: $text")

            resp.isSuccessful
        } catch (e: Exception) {
            Log.e("SYNC", "Redeem failed", e)
            false
        }
    }

    fun syncPending(context: Context) {
        val pending = TokenStorage.loadPendingTransfers(context)

        if (pending.isEmpty()) {
            Log.d("SYNC", "No pending transfers to sync.")
            return
        }

        Log.d("SYNC", "Uploading ${pending.size} pending transfers...")

        val myPub = SecurityUtils.getPublicKeyBase64()

        pending.forEach { p ->
            val serial = p.getString("serial")
            val amount = p.getInt("amount")
            val chain = p.getJSONArray("chain")
            val senderName = p.optString("senderName", "Unknown User")

            // ✅ Ignore OLD SENDER-SIDE pending entries (have receiverWalletId)
            if (p.has("receiverWalletId")) {
                Log.d("SYNC", "Skipping sender-side pending for $serial (will delete locally)")
                TokenStorage.removePendingTransferBySerial(context, serial)
                return@forEach
            }

            val ok = redeemToken(p)

            if (ok) {
                val finalHolder = chain
                    .getJSONObject(chain.length() - 1)
                    .getString("holderPubKeySpkiB64")

                if (finalHolder == myPub) {
                    // Only RECEIVER sees Redeemed
                    TransactionStorage.saveEvent(
                        context,
                        "Redeemed",
                        amount,
                        "From $senderName"
                    )
                    Log.d("SYNC", "Logged Redeemed for RECEIVER ($serial)")
                } else {
                    Log.d("SYNC", "Redemption success but not final owner here, skipping log ($serial)")
                }

                TokenStorage.removePendingTransferBySerial(context, serial)
                TokenStorage.removeTokenBySerial(context, serial)
            } else {
                Log.e("SYNC", "❌ Failed redemption: $serial")
            }
        }
    }
}
