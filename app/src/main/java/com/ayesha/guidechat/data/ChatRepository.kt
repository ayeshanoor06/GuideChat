package com.ayesha.guidechat.data

import android.content.Context
import com.ayesha.guidechat.ui.ChatMessage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class ChatRepository(
    context: Context
) {

    private val db = FirebaseFirestore.getInstance()

    private val encryptionManager =
        EncryptionManager(context.applicationContext)

    // =========================================================
    // CONSISTENT ONE-TO-ONE CONVERSATION ID
    // =========================================================

    private fun conversationId(
        userId1: String,
        userId2: String
    ): String {

        return listOf(
            userId1,
            userId2
        )
            .sorted()
            .joinToString("_")
    }

    // =========================================================
    // SEND MESSAGE
    // =========================================================

    fun sendMessage(
        senderId: String,
        receiverId: String,
        text: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {

        val cleanText = text.trim()

        if (cleanText.isEmpty()) {
            onError("Message cannot be empty")
            return
        }

        try {

            val encryptedText =
                encryptionManager.encrypt(
                    plainText = cleanText,
                    userId1 = senderId,
                    userId2 = receiverId
                )

            val id = conversationId(
                userId1 = senderId,
                userId2 = receiverId
            )

            val conversationRef =
                db.collection("conversations")
                    .document(id)

            val messageRef =
                conversationRef
                    .collection("messages")
                    .document()

            val timestamp =
                System.currentTimeMillis()

            val message = hashMapOf(
                "senderId" to senderId,
                "receiverId" to receiverId,
                "text" to encryptedText,
                "timestamp" to timestamp,
                "read" to false
            )

            val conversation = hashMapOf(
                "participants" to listOf(
                    senderId,
                    receiverId
                ),
                "lastMessage" to encryptedText,
                "lastMessageSenderId" to senderId,
                "lastMessageTimestamp" to timestamp
            )

            db.runBatch { batch ->

                batch.set(
                    conversationRef,
                    conversation
                )

                batch.set(
                    messageRef,
                    message
                )
            }
                .addOnSuccessListener {
                    onSuccess()
                }
                .addOnFailureListener { exception ->

                    onError(
                        exception.message
                            ?: "Unable to send message"
                    )
                }

        } catch (exception: Exception) {

            onError(
                exception.message
                    ?: "Unable to encrypt message"
            )
        }
    }

    // =========================================================
    // LISTEN FOR MESSAGES
    // =========================================================

    fun listenForMessages(
        currentUserId: String,
        otherUserId: String,
        onMessagesChanged: (List<ChatMessage>) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration {

        val id = conversationId(
            userId1 = currentUserId,
            userId2 = otherUserId
        )

        return db.collection("conversations")
            .document(id)
            .collection("messages")
            .orderBy(
                "timestamp",
                Query.Direction.ASCENDING
            )
            .addSnapshotListener { snapshot, exception ->

                if (exception != null) {

                    onError(
                        exception.message
                            ?: "Unable to load messages"
                    )

                    return@addSnapshotListener
                }

                val messages: List<ChatMessage> =
                    snapshot
                        ?.documents
                        ?.map { document ->

                            val encryptedText =
                                document.getString("text")
                                    ?: ""

                            val decryptedText =
                                if (encryptedText.isEmpty()) {
                                    ""
                                } else {
                                    encryptionManager.decrypt(
                                        encryptedText = encryptedText,
                                        userId1 = currentUserId,
                                        userId2 = otherUserId
                                    )
                                }

                            ChatMessage(
                                id = document.id,

                                senderId =
                                    document.getString(
                                        "senderId"
                                    ) ?: "",

                                receiverId =
                                    document.getString(
                                        "receiverId"
                                    ) ?: "",

                                text = decryptedText,

                                timestamp =
                                    document.getLong(
                                        "timestamp"
                                    ) ?: 0L,

                                isRead =
                                    document.getBoolean(
                                        "read"
                                    ) ?: false
                            )
                        }
                        ?: emptyList()

                onMessagesChanged(messages)
            }
    }

    // =========================================================
    // MARK MESSAGES AS READ
    // =========================================================

    fun markMessagesAsRead(
        currentUserId: String,
        otherUserId: String
    ) {

        val id = conversationId(
            userId1 = currentUserId,
            userId2 = otherUserId
        )

        val messagesRef =
            db.collection("conversations")
                .document(id)
                .collection("messages")

        messagesRef
            .whereEqualTo(
                "receiverId",
                currentUserId
            )
            .whereEqualTo(
                "read",
                false
            )
            .get()
            .addOnSuccessListener { snapshot ->

                if (snapshot.isEmpty) {
                    return@addOnSuccessListener
                }

                val batch = db.batch()

                snapshot.documents.forEach { document ->

                    batch.update(
                        document.reference,
                        "read",
                        true
                    )
                }

                batch.commit()
            }
    }

    // =========================================================
    // TYPING REFERENCE
    // =========================================================

    private fun typingReference(
        currentUserId: String,
        otherUserId: String,
        userId: String
    ) =

        db.collection("conversations")
            .document(
                conversationId(
                    userId1 = currentUserId,
                    userId2 = otherUserId
                )
            )
            .collection("typing")
            .document(userId)

    // =========================================================
    // SET TYPING
    // =========================================================

    fun setTyping(
        currentUserId: String,
        otherUserId: String,
        isTyping: Boolean
    ) {

        val reference =
            typingReference(
                currentUserId = currentUserId,
                otherUserId = otherUserId,
                userId = currentUserId
            )

        if (isTyping) {

            val data = hashMapOf(
                "userId" to currentUserId,
                "isTyping" to true,
                "timestamp" to System.currentTimeMillis()
            )

            reference.set(data)

        } else {

            reference.delete()
        }
    }

    // =========================================================
    // CLEAR TYPING STATUS
    // =========================================================

    fun clearTypingStatus(
        currentUserId: String,
        otherUserId: String
    ) {

        val reference =
            typingReference(
                currentUserId = currentUserId,
                otherUserId = otherUserId,
                userId = currentUserId
            )

        reference.delete()
    }

    // =========================================================
    // LISTEN FOR OTHER USER TYPING
    // =========================================================

    fun listenForTyping(
        currentUserId: String,
        otherUserId: String,
        onTypingChanged: (Boolean) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration {

        val reference =
            typingReference(
                currentUserId = currentUserId,
                otherUserId = otherUserId,
                userId = otherUserId
            )

        return reference
            .addSnapshotListener { snapshot, exception ->

                if (exception != null) {

                    onError(
                        exception.message
                            ?: "Unable to listen for typing"
                    )

                    return@addSnapshotListener
                }

                val isTyping =
                    snapshot?.getBoolean(
                        "isTyping"
                    ) ?: false

                onTypingChanged(isTyping)
            }
    }
}
