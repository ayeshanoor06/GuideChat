package com.ayesha.guidechat.data

import android.content.Context
import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class EncryptionManager(
    private val context: Context
) {

    private val secureRandom = SecureRandom()

    private val transformation =
        "AES/GCM/NoPadding"

    private val keySizeBytes = 32 // AES-256

    private val ivSize = 12

    private val authenticationTagBits = 128


    // =========================================================
    // CREATE A CONSISTENT CONVERSATION KEY
    // =========================================================
    //
    // Both users calculate this using the same two UIDs.
    //
    // Example:
    //
    // User A + User B
    //
    // and
    //
    // User B + User A
    //
    // produce exactly the same key.
    //
    // =========================================================

    private fun getConversationKey(
        userId1: String,
        userId2: String
    ): SecretKeySpec {

        val sortedIds =
            listOf(
                userId1,
                userId2
            )
                .sorted()
                .joinToString("_")

        val keyMaterial =
            "GuideChat-E2EE-$sortedIds"

        val digest =
            MessageDigest.getInstance("SHA-256")

        val keyBytes =
            digest.digest(
                keyMaterial.toByteArray(
                    StandardCharsets.UTF_8
                )
            )

        return SecretKeySpec(
            keyBytes.copyOf(keySizeBytes),
            "AES"
        )
    }


    // =========================================================
    // ENCRYPT MESSAGE
    // =========================================================

    fun encrypt(
        plainText: String,
        userId1: String,
        userId2: String
    ): String {

        if (plainText.isEmpty()) {
            return ""
        }

        return try {

            val key =
                getConversationKey(
                    userId1,
                    userId2
                )

            // Every message gets a NEW random IV.
            val iv =
                ByteArray(ivSize)

            secureRandom.nextBytes(iv)

            val cipher =
                Cipher.getInstance(
                    transformation
                )

            val gcmSpec =
                GCMParameterSpec(
                    authenticationTagBits,
                    iv
                )

            cipher.init(
                Cipher.ENCRYPT_MODE,
                key,
                gcmSpec
            )

            val encryptedBytes =
                cipher.doFinal(
                    plainText.toByteArray(
                        StandardCharsets.UTF_8
                    )
                )

            /*
             * Store:
             *
             * IV + encrypted message
             *
             * The IV is not secret.
             */

            val combined =
                ByteArray(
                    iv.size +
                            encryptedBytes.size
                )

            System.arraycopy(
                iv,
                0,
                combined,
                0,
                iv.size
            )

            System.arraycopy(
                encryptedBytes,
                0,
                combined,
                iv.size,
                encryptedBytes.size
            )

            Base64.encodeToString(
                combined,
                Base64.NO_WRAP
            )

        } catch (exception: Exception) {

            throw IllegalStateException(
                "Unable to encrypt message",
                exception
            )
        }
    }


    // =========================================================
    // DECRYPT MESSAGE
    // =========================================================

    fun decrypt(
        encryptedText: String,
        userId1: String,
        userId2: String
    ): String {

        if (encryptedText.isEmpty()) {
            return ""
        }

        return try {

            val key =
                getConversationKey(
                    userId1,
                    userId2
                )

            val combined =
                Base64.decode(
                    encryptedText,
                    Base64.NO_WRAP
                )

            if (combined.size <= ivSize) {

                throw IllegalArgumentException(
                    "Invalid encrypted message"
                )
            }

            // Extract IV.
            val iv =
                combined.copyOfRange(
                    0,
                    ivSize
                )

            // Extract encrypted data.
            val encryptedBytes =
                combined.copyOfRange(
                    ivSize,
                    combined.size
                )

            val cipher =
                Cipher.getInstance(
                    transformation
                )

            val gcmSpec =
                GCMParameterSpec(
                    authenticationTagBits,
                    iv
                )

            cipher.init(
                Cipher.DECRYPT_MODE,
                key,
                gcmSpec
            )

            val decryptedBytes =
                cipher.doFinal(
                    encryptedBytes
                )

            String(
                decryptedBytes,
                StandardCharsets.UTF_8
            )

        } catch (exception: Exception) {

            "[Unable to decrypt message]"
        }
    }
}