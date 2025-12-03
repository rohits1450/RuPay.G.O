package com.example.rupaygo

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import java.security.*
import java.security.spec.ECGenParameterSpec

object SecurityUtils {

    private const val KEY_ALIAS = "RUPAYGO_USER_KEY"

    fun generateWalletKey(context: Context) {
        val ks = KeyStore.getInstance("AndroidKeyStore")
        ks.load(null)

        // 🔥 If key exists, DO NOT regenerate
        if (ks.containsAlias(KEY_ALIAS)) {
            Log.d("KEYGEN", "Wallet key already exists, skipping")
            return
        }

        try {
            val kpg = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_EC,
                "AndroidKeyStore"
            )

            val spec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
            )
                .setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
                .setDigests(KeyProperties.DIGEST_SHA256)
                .setAttestationChallenge("RUPAYGO_ATTEST".toByteArray())
                .build()

            kpg.initialize(spec)
            kpg.generateKeyPair()
            Log.d("KEYGEN", "Wallet key generated successfully")

        } catch (e: Exception) {
            Log.e("KEYGEN", "error = ${e.message}")
        }
    }


    fun getPublicKeyBase64(): String {
        val ks = KeyStore.getInstance("AndroidKeyStore")
        ks.load(null)
        val entry = ks.getEntry(KEY_ALIAS, null) as KeyStore.PrivateKeyEntry
        return Base64.encodeToString(entry.certificate.publicKey.encoded, Base64.NO_WRAP)
    }

    fun signData(data: ByteArray): String {
        val ks = KeyStore.getInstance("AndroidKeyStore")
        ks.load(null)
        val entry = ks.getEntry(KEY_ALIAS, null) as KeyStore.PrivateKeyEntry

        val sig = Signature.getInstance("SHA256withECDSA")
        sig.initSign(entry.privateKey)
        sig.update(data)

        return Base64.encodeToString(sig.sign(), Base64.NO_WRAP)
    }

    fun verifySignature(data: ByteArray, signature: ByteArray, senderKeyBase64: String): Boolean {
        return try {
            val keyBytes = Base64.decode(senderKeyBase64, Base64.NO_WRAP)
            val keyFactory = KeyFactory.getInstance("EC")
            val pubKey = keyFactory.generatePublic(java.security.spec.X509EncodedKeySpec(keyBytes))

            val sig = Signature.getInstance("SHA256withECDSA")
            sig.initVerify(pubKey)
            sig.update(data)
            sig.verify(signature)
        } catch (e: Exception) {
            false
        }
    }

    /** Returns attestation as Base64 array for server */
    fun getAttestationChain(): List<String> {
        val ks = KeyStore.getInstance("AndroidKeyStore")
        ks.load(null)
        val entry = ks.getEntry(KEY_ALIAS, null) as KeyStore.PrivateKeyEntry
        val chain = entry.certificateChain

        return chain.map { cert ->
            Base64.encodeToString(cert.encoded, Base64.NO_WRAP)
        }
    }

    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    fun getWalletId(): String {
        val pubKeyB64 = getPublicKeyBase64()
        val bytes = Base64.decode(pubKeyB64, Base64.NO_WRAP)
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)

        // take first 10 bytes for a short readable ID
        val short = digest.copyOfRange(0, 10)
        val hex = short.joinToString("") { "%02x".format(it) }

        return "WALLET-$hex"
    }


}