
package com.ayesha.guidechat.data

import com.ayesha.guidechat.ui.ChatMessage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class ChatRepository {

    private val db =
        FirebaseFirestore.getInstance()


    // =========================================================
    // CREATE CONSISTENT ONE-TO-ONE CONVERSATION ID
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

        val cleanText =
            text.trim()

        if (cleanText.isEmpty()) {

            onError(
                "Message cannot be empty"
            )

            return
        }

        val id =
            conversationId(
                senderId,
                receiverId
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


        // =====================================================
        // MESSAGE DATA
        // =====================================================

        val message =
            hashMapOf(

                "senderId" to
                        senderId,

                "receiverId" to
                        receiverId,

                "text" to
                        cleanText,

                "timestamp" to
                        timestamp,

                "read" to
                        false
            )


        // =====================================================
        // CONVERSATION DATA
        // =====================================================

        val conversation =
            hashMapOf(

                "participants" to
                        listOf(
                            senderId,
                            receiverId
                        ),

                "lastMessage" to
                        cleanText,

                "lastMessageSenderId" to
                        senderId,

                "lastMessageTimestamp" to
                        timestamp
            )


        // =====================================================
        // CREATE CONVERSATION + MESSAGE
        // =====================================================

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

                // Stop typing after message is sent.
                clearTypingStatus(
                    senderId,
                    receiverId
                )

                onSuccess()
            }

            .addOnFailureListener { exception ->

                onError(
                    exception.message
                        ?: "Unable to send message"
                )
            }
    }


    // =========================================================
    // LISTEN TO ONE-TO-ONE MESSAGES
    // =========================================================

    fun listenForMessages(
        currentUserId: String,
        otherUserId: String,
        onMessagesChanged:
            (List<ChatMessage>) -> Unit,
        onError:
            (String) -> Unit
    ): ListenerRegistration {

        val id =
            conversationId(
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

            .addSnapshotListener {
                    snapshot,
                    exception ->

                if (exception != null) {

                    onError(
                        exception.message
                            ?: "Unable to load messages"
                    )

                    return@addSnapshotListener
                }

                val messages =
                    snapshot
                        ?.documents
                        ?.map { document ->

                            ChatMessage(

                                id =
                                    document.id,

                                senderId =
                                    document
                                        .getString(
                                            "senderId"
                                        )
                                        ?: "",

                                receiverId =
                                    document
                                        .getString(
                                            "receiverId"
                                        )
                                        ?: "",

                                text =
                                    document
                                        .getString(
                                            "text"
                                        )
                                        ?: "",

                                timestamp =
                                    document
                                        .getLong(
                                            "timestamp"
                                        )
                                        ?: 0L,

                                isRead =
                                    document
                                        .getBoolean(
                                            "read"
                                        )
                                        ?: false
                            )
                        }
                        ?: emptyList()

                onMessagesChanged(
                    messages
                )
            }
    }


    // =========================================================
    // MARK RECEIVED ONE-TO-ONE MESSAGES AS READ
    // =========================================================

    fun markMessagesAsRead(
        currentUserId: String,
        otherUserId: String
    ) {

        val id =
            conversationId(
                currentUserId,
                otherUserId
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

                val batch =
                    db.batch()

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
    // SET ONE-TO-ONE TYPING STATUS
    // =========================================================
    //
    // Firestore structure:
    //
    // conversations/{conversationId}/typing/{userId}
    //
    // =========================================================

    fun setTyping(
        currentUserId: String,
        otherUserId: String,
        isTyping: Boolean
    ) {

        val id =
            conversationId(
                currentUserId,
                otherUserId
            )

        val typingRef =
            db.collection("conversations")
                .document(id)
                .collection("typing")
                .document(currentUserId)

        val typingData =
            hashMapOf(

                "isTyping" to
                        isTyping,

                "timestamp" to
                        System.currentTimeMillis()
            )

        typingRef
            .set(typingData)
    }


    // =========================================================
    // CLEAR ONE-TO-ONE TYPING STATUS
    // =========================================================
    //
    // This is the function your ChatScreen.kt currently needs.
    //
    // =========================================================

    fun clearTypingStatus(
        currentUserId: String,
        otherUserId: String
    ) {

        val id =
            conversationId(
                currentUserId,
                otherUserId
            )

        db.collection("conversations")
            .document(id)
            .collection("typing")
            .document(currentUserId)
            .delete()
    }


    // =========================================================
    // LISTEN TO OTHER USER TYPING STATUS
    // =========================================================

    fun listenForTyping(
        currentUserId: String,
        otherUserId: String,
        onTypingChanged:
            (Boolean) -> Unit,
        onError:
            (String) -> Unit
    ): ListenerRegistration {

        val id =
            conversationId(
                currentUserId,
                otherUserId
            )

        return db.collection("conversations")
            .document(id)
            .collection("typing")
            .document(otherUserId)

            .addSnapshotListener {
                    snapshot,
                    exception ->

                if (exception != null) {

                    onError(
                        exception.message
                            ?: "Unable to load typing status"
                    )

                    return@addSnapshotListener
                }

                val isTyping =
                    snapshot
                        ?.getBoolean(
                            "isTyping"
                        )
                        ?: false

                onTypingChanged(
                    isTyping
                )
            }
    }


    // =========================================================
    // COMPATIBILITY ALIAS
    // =========================================================

    fun listenToTyping(
        currentUserId: String,
        otherUserId: String,
        onTypingChanged:
            (Boolean) -> Unit,
        onError:
            (String) -> Unit
    ): ListenerRegistration {

        return listenForTyping(
            currentUserId = currentUserId,
            otherUserId = otherUserId,
            onTypingChanged = onTypingChanged,
            onError = onError
        )
    }
}