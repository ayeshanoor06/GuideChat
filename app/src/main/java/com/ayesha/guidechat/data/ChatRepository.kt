package com.ayesha.guidechat.data

import com.ayesha.guidechat.model.ChatMessage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatRepository {

    private val db = FirebaseFirestore.getInstance()

    private fun getConversationId(
        userId1: String,
        userId2: String
    ): String {

        return if (userId1 < userId2) {
            "${userId1}_${userId2}"
        } else {
            "${userId2}_${userId1}"
        }
    }

    fun sendMessage(
        senderId: String,
        receiverId: String,
        text: String,
        onComplete: (Boolean, String?) -> Unit
    ) {

        val conversationId =
            getConversationId(senderId, receiverId)

        val conversationRef =
            db.collection("conversations")
                .document(conversationId)

        val messageRef =
            conversationRef
                .collection("messages")
                .document()

        val message = ChatMessage(
            id = messageRef.id,
            senderId = senderId,
            receiverId = receiverId,
            text = text,
            timestamp = System.currentTimeMillis(),
            read = false
        )

        val conversationData = mapOf(
            "participants" to listOf(
                senderId,
                receiverId
            ),
            "lastMessage" to text,
            "lastMessageTime" to message.timestamp
        )

        conversationRef
            .set(
                conversationData,
                com.google.firebase.firestore.SetOptions.merge()
            )
            .addOnSuccessListener {

                messageRef
                    .set(message)
                    .addOnSuccessListener {

                        onComplete(
                            true,
                            null
                        )
                    }
                    .addOnFailureListener { exception ->

                        onComplete(
                            false,
                            exception.message
                        )
                    }
            }
            .addOnFailureListener { exception ->

                onComplete(
                    false,
                    exception.message
                )
            }
    }

    fun listenForMessages(
        userId1: String,
        userId2: String,
        onMessagesChanged: (List<ChatMessage>) -> Unit,
        onError: (String) -> Unit
    ): () -> Unit {

        val conversationId =
            getConversationId(userId1, userId2)

        val listenerRegistration =
            db.collection("conversations")
                .document(conversationId)
                .collection("messages")
                .orderBy(
                    "timestamp",
                    Query.Direction.ASCENDING
                )
                .addSnapshotListener { snapshot, error ->

                    if (error != null) {

                        onError(
                            error.message
                                ?: "Unable to load messages"
                        )

                        return@addSnapshotListener
                    }

                    if (snapshot != null) {

                        val messages =
                            snapshot.documents.mapNotNull { document ->

                                document.toObject(
                                    ChatMessage::class.java
                                )
                            }

                        onMessagesChanged(messages)
                    }
                }

        return {
            listenerRegistration.remove()
        }
    }
}