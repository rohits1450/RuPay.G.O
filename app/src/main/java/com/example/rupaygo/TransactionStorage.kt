package com.example.rupaygo

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import androidx.core.content.edit

data class TransactionEvent(
    val type: String,
    val amount: Int,
    val details: String,
    val time: String
)

object TransactionStorage {

    private const val PREF = "transactions_db"

    fun saveEvent(context: Context, type: String, amount: Int, details: String) {
        val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val arr = JSONArray(prefs.getString("events", "[]"))

        val now = System.currentTimeMillis()

        val obj = JSONObject().apply {
            put("type", type)
            put("amount", amount)
            put("details", details)
            put("time", now)
        }

        arr.put(obj)

        prefs.edit { putString("events", arr.toString()) }
    }

    fun loadAllEvents(context: Context): List<TransactionEvent> {
        val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val arr = JSONArray(prefs.getString("events", "[]"))

        val out = mutableListOf<TransactionEvent>()

        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out.add(
                TransactionEvent(
                    type = o.getString("type"),
                    amount = o.getInt("amount"),
                    details = o.getString("details"),
                    time = formatTime(o.getLong("time"))
                )
            )
        }

        return out.reversed() // newest first
    }

    private fun formatTime(ts: Long): String {
        return java.text.SimpleDateFormat("dd MMM yyyy • hh:mm a")
            .format(java.util.Date(ts))
    }
}
