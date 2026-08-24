package com.ayesha.guidechat.data

import com.ayesha.guidechat.ui.ChatPreview
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ConversationRepository {

    private val db =
        FirebaseFirestore.getInstance()

    /*
     * Listen to all one-to-one conversations
     * belonging to the currently logged-in user.
     */
    fun listenForConversations(
        currentUserId: String,
        onConversationsChanged:
            (List<ChatPreview>) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration {

        return db.collection("conversations")
            .whereArrayContains(
                "participants",
                currentUserId
            )
            .addSnapshotListener { snapshot, exception ->

                if (exception != null) {

                    onError(
                        exception.message
                            ?: "Unable to load conversations"
                    )

                    return@addSnapshotListener
                }

                val documents =
                    snapshot?.documents
                        ?: emptyList()

                if (documents.isEmpty()) {

                    onConversationsChanged(
                        emptyList()
                    )

                    return@addSnapshotListener
                }

                val previews =
                    mutableMapOf<String, ChatPreview>()

                var completed = 0

                documents.forEach { document ->

                    val participants =
                        document.get("participants")
                                as? List<*>
                            ?: emptyList<Any>()

                    val otherUserId =
                        participants
                            .filterIsInstance<String>()
                            .firstOrNull {
                                it != currentUserId
                            }

                    if (otherUserId == null) {

                        completed++

                        if (
                            completed ==
                            documents.size
                        ) {

                            publish(
                                previews,
                                onConversationsChanged
                            )
                        }

                        return@forEach
                    }

                    /*
                     * Get the other user's profile.
                     */
                    db.collection("users")
                        .document(otherUserId)
                        .get()
                        .addOnSuccessListener { userDocument ->

                            val name =
                                userDocument
                                    .getString("name")
                                    ?: "User"

                            val isOnline =
                                userDocument
                                    .getBoolean("isOnline")
                                    ?: false

                            val lastMessage =
                                document
                                    .getString("lastMessage")
                                    ?: ""

                            val timestamp =
                                document
                                    .getLong(
                                        "lastMessageTimestamp"
                                    )
                                    ?: 0L

                            /*
                             * Count unread messages.
                             *
                             * We only count messages:
                             * - sent by the other user
                             * - received by current user
                             * - not read yet
                             */
                            db.collection("conversations")
                                .document(document.id)
                                .collection("messages")
                                .whereEqualTo(
                                    "receiverId",
                                    currentUserId
                                )
                                .whereEqualTo(
                                    "read",
                                    false
                                )
                                .get()
                                .addOnSuccessListener { unreadSnapshot ->

                                    val unreadCount =
                                        unreadSnapshot.size()

                                    previews[otherUserId] =
                                        ChatPreview(

                                            userId =
                                                otherUserId,

                                            name =
                                                name,

                                            message =
                                                lastMessage,

                                            time =
                                                formatTime(
                                                    timestamp
                                                ),

                                            unreadCount =
                                                unreadCount,

                                            isOnline =
                                                isOnline,

                                            timestamp =
                                                timestamp,

                                            isGroup =
                                                false
                                        )

                                    completed++

                                    if (
                                        completed ==
                                        documents.size
                                    ) {

                                        publish(
                                            previews,
                                            onConversationsChanged
                                        )
                                    }
                                }
                                .addOnFailureListener { error ->

                                    /*
                                     * If unread count fails,
                                     * still show the conversation.
                                     */
                                    previews[otherUserId] =
                                        ChatPreview(

                                            userId =
                                                otherUserId,

                                            name =
                                                name,

                                            message =
                                                lastMessage,

                                            time =
                                                formatTime(
                                                    timestamp
                                                ),

                                            unreadCount =
                                                0,

                                            isOnline =
                                                isOnline,

                                            timestamp =
                                                timestamp,

                                            isGroup =
                                                false
                                        )

                                    completed++

                                    if (
                                        completed ==
                                        documents.size
                                    ) {

                                        publish(
                                            previews,
                                            onConversationsChanged
                                        )
                                    }
                                }
                        }
                        .addOnFailureListener { error ->

                            completed++

                            if (
                                completed ==
                                documents.size
                            ) {

                                publish(
                                    previews,
                                    onConversationsChanged
                                )
                            }

                            onError(
                                error.message
                                    ?: "Unable to load user"
                            )
                        }
                }
            }
    }

    /*
     * Publish conversations sorted
     * by newest message.
     */
    private fun publish(
        previews:
        Map<String, ChatPreview>,

        onConversationsChanged:
            (List<ChatPreview>) -> Unit
    ) {

        val result =
            previews.values
                .sortedByDescending {
                    it.timestamp
                }

        onConversationsChanged(
            result
        )
    }

    /*
     * Format message timestamp.
     */
    private fun formatTime(
        timestamp: Long
    ): String {

        if (timestamp <= 0L) {
            return ""
        }

        val messageDate =
            Date(timestamp)

        val today =
            SimpleDateFormat(
                "yyyyMMdd",
                Locale.getDefault()
            ).format(Date())

        val messageDay =
            SimpleDateFormat(
                "yyyyMMdd",
                Locale.getDefault()
            ).format(messageDate)

        return if (
            today == messageDay
        ) {

            SimpleDateFormat(
                "hh:mm a",
                Locale.getDefault()
            ).format(messageDate)

        } else {

            SimpleDateFormat(
                "dd MMM",
                Locale.getDefault()
            ).format(messageDate)
        }
    }
}