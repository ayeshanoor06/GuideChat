package com.ayesha.guidechat.data

import com.ayesha.guidechat.ui.ChatMessage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class ChatRepository {

    private val db =
        FirebaseFirestore.getInstance()



    // CREATE A CONSISTENT ONE-TO-ONE CONVERSATION ID


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



    // SEND MESSAGE


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



        // MESSAGE DATA


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

                // New messages are unread.
                "read" to
                        false
            )



        // CONVERSATION DATA


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



        // CREATE CONVERSATION + MESSAGE TOGETHER


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
    }



    // LISTEN TO MESSAGES IN REAL TIME


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



                // FIRESTORE ERROR


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

                                // Firestore:
                                // "read"
                                //
                                // Kotlin:
                                // "isRead"
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



    // MARK RECEIVED MESSAGES AS READ


    fun markMessagesAsRead(
        currentUserId: String,
        otherUserId: String,
        onError: ((String) -> Unit)? = null
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



                // NOTHING TO UPDATE


                if (snapshot.isEmpty) {
                    return@addOnSuccessListener
                }


                val batch =
                    db.batch()



                // MARK ALL RECEIVED MESSAGES AS READ


                snapshot.documents.forEach { document ->

                    batch.update(
                        document.reference,

                        "read",

                        true
                    )
                }



                // COMMIT READ RECEIPTS


                batch.commit()

                    .addOnSuccessListener {

                        // Read receipts successfully updated.
                    }

                    .addOnFailureListener { exception ->

                        onError?.invoke(
                            exception.message
                                ?: "Unable to update read receipts"
                        )
                    }
            }

            .addOnFailureListener { exception ->

                onError?.invoke(
                    exception.message
                        ?: "Unable to check unread messages"
                )
            }
    }
}