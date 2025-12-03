package com.example.rupaygo

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import org.json.JSONObject

object TokenStorage {

    private fun prefs(context: Context) =
        EncryptedSharedPreferences.create(
            context,
            "wallet_tokens",
            MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

    // ---------- Reactive balance / tokens JSON ----------
    private val _balanceFlow = MutableStateFlow(0)
    val balanceFlow: StateFlow<Int> get() = _balanceFlow

    private val _tokensJsonFlow = MutableStateFlow("[]")
    val tokensJsonFlow: StateFlow<String> get() = _tokensJsonFlow

    fun init(context: Context) {
        val json = prefs(context).getString("tokens", "[]") ?: "[]"
        _tokensJsonFlow.value = json
        _balanceFlow.value = computeBalanceFromJson(json)
    }

    fun saveToken(
        context: Context,
        serial: String,
        amount: Int,
        issuerSig: String,
        chain: JSONArray = JSONArray(),
        senderMobile: String? = null,
        senderName: String? = null  // NEW

    ) {
        val p = prefs(context)
        val json = p.getString("tokens", "[]") ?: "[]"
        val arr = JSONArray(json)
        val newArr = JSONArray()

        // Replace existing token if exists
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            if (obj.optString("serial") != serial) {
                newArr.put(obj)
            }
        }

        val safeChain = try {
            chain ?: JSONArray()
        } catch (e: Exception) {
            JSONArray()
        }

        val obj = JSONObject().apply {
            put("serial", serial)
            put("amount", amount)
            put("issuerSig", issuerSig)
            put("chain", safeChain)
            if (senderMobile != null) {
                put("senderMobile", senderMobile)
            }
            if (senderName != null) put("senderName", senderName)

        }

        newArr.put(obj)
        val newJson = newArr.toString()

        p.edit().putString("tokens", newJson).apply()

        _tokensJsonFlow.value = newJson
        _balanceFlow.value = computeBalanceFromJson(newJson)

        Log.d("TOKEN_SAVE", "Saved token: $serial (₹$amount)")
    }

    fun loadTokens(context: Context): JSONArray {
        val json = prefs(context).getString("tokens", "[]") ?: "[]"
        return JSONArray(json)
    }

    fun loadTokensAsList(context: Context): List<JSONObject> {
        val arr = loadTokens(context)
        val list = mutableListOf<JSONObject>()
        for (i in 0 until arr.length()) {
            list.add(arr.getJSONObject(i))
        }
        return list
    }

    fun getBalance(context: Context): Int {
        val json = prefs(context).getString("tokens", "[]") ?: "[]"
        return computeBalanceFromJson(json)
    }

    private fun computeBalanceFromJson(json: String): Int {
        return try {
            val arr = JSONArray(json)
            var sum = 0
            for (i in 0 until arr.length()) {
                sum += arr.getJSONObject(i).getInt("amount")
            }
            sum
        } catch (e: Exception) {
            0
        }
    }

    fun clearAll(context: Context) {
        val p = prefs(context)
        p.edit().putString("tokens", "[]").apply()
        _tokensJsonFlow.value = "[]"
        _balanceFlow.value = 0
    }

    // ---------- Pending transfers (for server sync later) ----------

    fun addPendingTransfer(context: Context, transferJson: String) {
        val p = prefs(context)
        val arr = JSONArray(p.getString("pending_transfers", "[]") ?: "[]")

        val obj = JSONObject(transferJson)
        arr.put(obj)

        p.edit().putString("pending_transfers", arr.toString()).apply()
    }

    fun loadPendingTransfers(context: Context): List<JSONObject> {
        val p = prefs(context)
        val arr = JSONArray(p.getString("pending_transfers", "[]"))
        return List(arr.length()) { idx -> arr.getJSONObject(idx) }
    }

    fun removePendingTransferBySerial(context: Context, serial: String) {
        val p = prefs(context)
        val arr = JSONArray(p.getString("pending_transfers", "[]"))
        val newArr = JSONArray()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            if (o.optString("serial") != serial) newArr.put(o)
        }
        p.edit().putString("pending_transfers", newArr.toString()).apply()
    }

    fun removeTokenBySerial(context: Context, serial: String) {
        val p = prefs(context)
        val arr = JSONArray(p.getString("tokens", "[]"))
        val newArr = JSONArray()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            if (obj.optString("serial") != serial) {
                newArr.put(obj)
            }
        }
        p.edit().putString("tokens", newArr.toString()).apply()

        val newJson = newArr.toString()
        _tokensJsonFlow.value = newJson
        _balanceFlow.value = computeBalanceFromJson(newJson)
    }

    // ---------- Linked bank account (for online settlement) ----------

    fun saveLinkedAccount(context: Context, accountId: String) {
        val p = prefs(context)
        p.edit().putString("linked_account_id", accountId).apply()
    }

    fun getLinkedAccount(context: Context): String {
        val p = prefs(context)
        return p.getString("linked_account_id", "") ?: ""
    }

    fun saveUserMobile(context: Context, mobile: String) {
        prefs(context).edit().putString("user_mobile", mobile).apply()
    }

    fun getUserMobile(context: Context): String {
        return prefs(context).getString("user_mobile", "Unknown") ?: "Unknown"
    }



    fun saveUserName(context: Context, name: String) {
        prefs(context).edit().putString("user_name", name).apply()
    }

    fun getUserName(context: Context): String {
        return prefs(context).getString("user_name", "User") ?: "User"
    }

    }




