package com.example.rupaygo

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object ApiClient {

    private val client = OkHttpClient()
    private const val BASE_URL = "http://192.168.0.3:3000"  // your IP

    private val JSON_MEDIA = "application/json".toMediaType()

    /** 1) Create bank account (dynamic) */
    suspend fun createAccount(
        holderName: String,
        mobile: String,
        aadhaar: String,
        initialDeposit: Int,
        password: String
    ): JSONObject? = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("holderName", holderName)
                put("mobile", mobile)
                put("aadhaar", aadhaar)
                put("initialDeposit", initialDeposit)
                put("password",password)
            }

            val body = json.toString().toRequestBody(JSON_MEDIA)

            val req = Request.Builder()
                .url("$BASE_URL/createAccount")
                .post(body)
                .build()

            val resp = client.newCall(req).execute()
            val text = resp.body?.string() ?: return@withContext null

            Log.d("CREATE_ACCOUNT", "Response: $text")

            JSONObject(text)
        } catch (e: Exception) {
            Log.e("CREATE_ACCOUNT", "Failed", e)
            null
        }
    }


    /** 2) Register new wallet/device */
    suspend fun registerWallet(
        deviceId: String,
        pubKeySpkiB64: String,
        attestationChain: List<String>,
        accountId: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("device_id", deviceId)
                put("pubkey_spki_b64", pubKeySpkiB64)
                put("attestation_chain", attestationChain)
                put("account_id", accountId)
            }

            val body = json.toString().toRequestBody(JSON_MEDIA)

            val req = Request.Builder()
                .url("$BASE_URL/registerDevice")
                .post(body)
                .build()

            val res = client.newCall(req).execute()
            val str = res.body?.string()
            Log.d("REGISTER_WALLET", "Response: $str")
            str
        } catch (e: Exception) {
            Log.e("REGISTER_WALLET", "Failed", e)
            null
        }
    }



    /** 3) Procure e₹ (digital withdrawal into multiple notes) */
    suspend fun purchaseECurrency(
        accountId: String,
        holderPubKeySpkiB64: String,
        amount: Int
    ): JSONObject? = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("account_id", accountId)
                put("issued_to_pubkey", holderPubKeySpkiB64)
                put("amount", amount)
            }

            val body = json.toString().toRequestBody(JSON_MEDIA)

            val req = Request.Builder()
                .url("$BASE_URL/purchaseEcurrency")
                .post(body)
                .build()

            val resp = client.newCall(req).execute()
            val str = resp.body?.string() ?: return@withContext null

            Log.d("PURCHASE_E", "Response: $str")

            JSONObject(str)
        } catch (e: Exception) {
            Log.e("PURCHASE_E", "Failed", e)
            null
        }
    }

    /** 4) Get account balance */
    suspend fun getAccountBalance(accountId: String): JSONObject? =
        withContext(Dispatchers.IO) {
            try {
                val req = Request.Builder()
                    .url("$BASE_URL/account/$accountId")
                    .get()
                    .build()

                val resp = client.newCall(req).execute()
                val str = resp.body?.string() ?: return@withContext null

                Log.d("BALANCE", "Response: $str")

                JSONObject(str)
            } catch (e: Exception) {
                Log.e("BALANCE", "Failed", e)
                null
            }
        }
    /** 0) Lookup account by mobile (login) */
    suspend fun lookupAccountByMobile(mobile: String): JSONObject? =
        withContext(Dispatchers.IO) {
            try {
                val json = JSONObject().apply {
                    put("mobile", mobile)
                }

                val body = json.toString().toRequestBody(JSON_MEDIA)

                val req = Request.Builder()
                    .url("$BASE_URL/lookupAccountByMobile")
                    .post(body)
                    .build()

                val resp = client.newCall(req).execute()
                val text = resp.body?.string() ?: return@withContext null

                JSONObject(text)
            } catch (e: Exception) {
                Log.e("LOOKUP_ACC", "Failed", e)
                null
            }
        }
    // ApiClient.kt (add this or fix existing)
    suspend fun upiCredit(
        accountId: String,
        amount: Int,
        upiTxnId: String,
        payerVpa: String?,
        rawResponse: String?
    ): JSONObject? {
        return withContext(Dispatchers.IO) {
            try {
                val json = JSONObject().apply {
                    put("account_id", accountId)
                    put("amount", amount)
                    put("upi_txn_id", upiTxnId)      // 🔥 must be EXACTLY this
                    put("payer_vpa", payerVpa ?: "")
                    put("raw_response", rawResponse ?: "")
                }

                val body = json.toString().toRequestBody(JSON_MEDIA)

                val req = Request.Builder()
                    .url("$BASE_URL/upiCredit")
                    .post(body)
                    .build()

                val resp = client.newCall(req).execute()
                val text = resp.body?.string() ?: return@withContext null
                Log.d("UPI_CREDIT", "Server resp = $text")
                JSONObject(text)
            } catch (e: Exception) {
                Log.e("UPI_CREDIT", "Failed", e)
                null
            }
        }
    }
    suspend fun withdrawToBank(
        accountId: String,
        amount: Int,
        userUpiId: String
    ): JSONObject? {
        return try {
            val json = JSONObject().apply {
                put("account_id", accountId)
                put("amount", amount)
                put("user_upi_id", userUpiId)
            }

            val body = json.toString().toRequestBody(JSON_MEDIA)

            val req = Request.Builder()
                .url("$BASE_URL/withdrawToBank")
                .post(body)
                .build()

            val resp = client.newCall(req).execute()
            val text = resp.body?.string() ?: return null
            JSONObject(text)

        } catch (e: Exception) {
            Log.e("WITHDRAW", "Failed", e)
            null
        }
    }
    suspend fun ocrReceipt(bytes: ByteArray): JSONObject? = withContext(Dispatchers.IO) {
        try {
            // Create temp file
            val temp = File.createTempFile("upi_receipt", ".jpg")
            temp.writeBytes(bytes)

            // Create RequestBody for the file
            val reqFile = temp.asRequestBody("image/jpeg".toMediaTypeOrNull())

            // Correct multipart form-data
            val multipartBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    "receipt.jpg",
                    reqFile
                )
                .build()

            val request = Request.Builder()
                .url("http://192.168.0.3:5001/ocr")
                .post(multipartBody)
                .build()

            val resp = client.newCall(request).execute()
            val text = resp.body?.string()

            Log.e("OCR_RAW", "Response: $text")

            if (text == null) return@withContext null

            JSONObject(text)

        } catch (e: Exception) {
            Log.e("OCR", "Upload failed", e)
            null
        }
    }
    suspend fun loginWithPassword(mobile: String, password: String): JSONObject? =
        withContext(Dispatchers.IO) {
            try {
                val json = JSONObject().apply {
                    put("mobile", mobile)
                    put("password", password)
                }

                val body = json.toString().toRequestBody(JSON_MEDIA)

                val req = Request.Builder()
                    .url("$BASE_URL/login")
                    .post(body)
                    .build()

                val resp = client.newCall(req).execute()
                val text = resp.body?.string() ?: return@withContext null
                JSONObject(text)
            } catch (e: Exception) {
                null
            }
        }







}