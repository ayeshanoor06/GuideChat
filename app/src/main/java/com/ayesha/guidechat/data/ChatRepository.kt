package com.ayesha.guidechat.data

import com.ayesha.guidechat.model.UserProfile
import com.ayesha.guidechat.ui.ChatMessage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class ChatRepository {

    private val db = FirebaseFirestore.getInstance()

    private fun conversationId(
        userId1: String,
        userId2: String
    ): String {
        return listOf(userId1, userId2).sorted().joinToString("_")
    }

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

        val conversationId = conversationId(senderId, receiverId)
        val conversationRef =
            db.collection("conversations").document(conversationId)

        val messageRef =
            conversationRef.collection("messages").document()

        val message = hashMapOf(
            "senderId" to senderId,
            "receiverId" to receiverId,
            "text" to cleanText,
            "timestamp" to System.currentTimeMillis(),
            "read" to false
        )

        val conversation = hashMapOf(
            "participants" to listOf(senderId, receiverId),
            "lastMessage" to cleanText,
            "lastMessageSenderId" to senderId,
            "lastMessageTimestamp" to System.currentTimeMillis()
        )

        db.runBatch { batch ->
            batch.set(conversationRef, conversation)
            batch.set(messageRef, message)
        }
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(
                    exception.message ?: "Unable to send message"
                )
            }
    }

    fun listenForMessages(
        currentUserId: String,
        otherUserId: String,
        onMessagesChanged: (List<ChatMessage>) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration {

        val id = conversationId(
            currentUserId,
            otherUserId
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

                val messages =
                    snapshot?.documents?.map { document ->

                        ChatMessage(
                            id = document.id,

                            senderId =
                                document.getString("senderId")
                                    ?: "",

                            receiverId =
                                document.getString("receiverId")
                                    ?: "",

                            text =
                                document.getString("text")
                                    ?: "",

                            timestamp =
                                document.getLong("timestamp")
                                    ?: 0L,

                            isRead =
                                document.getBoolean("read")
                                    ?: false
                        )

                    } ?: emptyList()

                onMessagesChanged(messages)
            }
    }

    fun markMessagesAsRead(
        currentUserId: String,
        otherUserId: String
    ) {
        val id = conversationId(
            currentUserId,
            otherUserId
        )

        db.collection("conversations")
            .document(id)
            .collection("messages")
            .whereEqualTo("receiverId", currentUserId)
            .whereEqualTo("read", false)
            .get()
            .addOnSuccessListener { snapshot ->

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
}