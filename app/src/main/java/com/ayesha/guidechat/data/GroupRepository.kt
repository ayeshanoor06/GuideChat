package com.ayesha.guidechat.data

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions

data class GroupModel(
    val id: String = "",
    val name: String = "",
    val createdBy: String = "",
    val createdAt: Long = 0L,
    val memberIds: List<String> = emptyList()
)

data class GroupMessage(
    val id: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = 0L,
    val readBy: List<String> = emptyList()
)

class GroupRepository(
    context: Context
) {

    constructor() : this(
        FirebaseApp
            .getInstance()
            .applicationContext
    )

    private val db =
        FirebaseFirestore.getInstance()

    private val encryptionManager =
        EncryptionManager(
            context.applicationContext
        )

    // =========================================================
    // CREATE GROUP
    // =========================================================

    fun createGroup(
        name: String,
        createdBy: String,
        memberIds: List<String>,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {

        val cleanName = name.trim()

        if (cleanName.isEmpty()) {

            onError(
                "Group name cannot be empty"
            )

            return
        }

        val allMembers =
            (memberIds + createdBy).distinct()

        if (allMembers.size < 2) {

            onError(
                "A group must have at least 2 members"
            )

            return
        }

        val groupRef =
            db.collection("groups")
                .document()

        val group = hashMapOf(

            "name" to cleanName,

            "createdBy" to createdBy,

            "createdAt" to
                    System.currentTimeMillis(),

            "memberIds" to allMembers,

            "typing" to
                    emptyMap<String, Boolean>()
        )

        groupRef
            .set(group)
            .addOnSuccessListener {

                onSuccess(
                    groupRef.id
                )
            }
            .addOnFailureListener { exception ->

                onError(
                    exception.message
                        ?: "Unable to create group"
                )
            }
    }

    // =========================================================
    // LIST GROUPS
    // =========================================================

    fun listenToGroups(
        currentUserId: String,
        onGroupsChanged:
            (List<GroupModel>) -> Unit,
        onError:
            (String) -> Unit
    ): ListenerRegistration {

        return db.collection("groups")
            .whereArrayContains(
                "memberIds",
                currentUserId
            )
            .addSnapshotListener { snapshot, exception ->

                if (exception != null) {

                    onError(
                        exception.message
                            ?: "Unable to load groups"
                    )

                    return@addSnapshotListener
                }

                val groups: List<GroupModel> =
                    snapshot
                        ?.documents
                        ?.map { document ->

                            GroupModel(

                                id = document.id,

                                name =
                                    document.getString(
                                        "name"
                                    )
                                        ?: "Unnamed Group",

                                createdBy =
                                    document.getString(
                                        "createdBy"
                                    )
                                        ?: "",

                                createdAt =
                                    document.getLong(
                                        "createdAt"
                                    )
                                        ?: 0L,

                                memberIds =
                                    (
                                            document.get(
                                                "memberIds"
                                            ) as? List<*>
                                            )
                                        ?.filterIsInstance<String>()
                                        ?: emptyList()
                            )
                        }
                        ?: emptyList()

                onGroupsChanged(
                    groups.sortedByDescending {
                        it.createdAt
                    }
                )
            }
    }

    // =========================================================
    // SEND ENCRYPTED GROUP MESSAGE
    // =========================================================

    fun sendGroupMessage(
        groupId: String,
        senderId: String,
        text: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {

        val cleanText = text.trim()

        if (cleanText.isEmpty()) {

            onError(
                "Message cannot be empty"
            )

            return
        }

        try {

            val encryptedText =
                encryptionManager.encrypt(
                    plainText = cleanText,
                    userId1 = groupId,
                    userId2 = groupId
                )

            val timestamp =
                System.currentTimeMillis()

            val messageRef =
                db.collection("groups")
                    .document(groupId)
                    .collection("messages")
                    .document()

            val message = hashMapOf(

                "senderId" to senderId,

                "text" to encryptedText,

                "timestamp" to timestamp,

                "readBy" to listOf(senderId)
            )

            messageRef
                .set(message)
                .addOnSuccessListener {

                    onSuccess()
                }
                .addOnFailureListener { exception ->

                    onError(
                        exception.message
                            ?: "Unable to send group message"
                    )
                }

        } catch (exception: Exception) {

            onError(
                exception.message
                    ?: "Unable to encrypt group message"
            )
        }
    }

    // =========================================================
    // LISTEN TO GROUP MESSAGES
    // =========================================================

    fun listenToGroupMessages(
        groupId: String,
        onMessagesChanged:
            (List<GroupMessage>) -> Unit,
        onError:
            (String) -> Unit
    ): ListenerRegistration {

        return db.collection("groups")
            .document(groupId)
            .collection("messages")
            .orderBy(
                "timestamp",
                Query.Direction.ASCENDING
            )
            .addSnapshotListener { snapshot, exception ->

                if (exception != null) {

                    onError(
                        exception.message
                            ?: "Unable to load group messages"
                    )

                    return@addSnapshotListener
                }

                val messages: List<GroupMessage> =
                    snapshot
                        ?.documents
                        ?.map { document ->

                            val encryptedText =
                                document.getString(
                                    "text"
                                )
                                    ?: ""

                            val decryptedText =
                                if (encryptedText.isEmpty()) {
                                    ""
                                } else {
                                    encryptionManager.decrypt(
                                        encryptedText = encryptedText,
                                        userId1 = groupId,
                                        userId2 = groupId
                                    )
                                }

                            GroupMessage(

                                id = document.id,

                                senderId =
                                    document.getString(
                                        "senderId"
                                    )
                                        ?: "",

                                text =
                                    decryptedText,

                                timestamp =
                                    document.getLong(
                                        "timestamp"
                                    )
                                        ?: 0L,

                                readBy =
                                    (
                                            document.get(
                                                "readBy"
                                            ) as? List<*>
                                            )
                                        ?.filterIsInstance<String>()
                                        ?: emptyList()
                            )
                        }
                        ?: emptyList()

                onMessagesChanged(messages)
            }
    }

    // =========================================================
    // MARK GROUP MESSAGE AS READ
    // =========================================================

    fun markGroupMessageAsRead(
        groupId: String,
        messageId: String,
        userId: String
    ) {

        val messageRef =
            db.collection("groups")
                .document(groupId)
                .collection("messages")
                .document(messageId)

        db.runTransaction { transaction ->

            val snapshot =
                transaction.get(messageRef)

            val currentReadBy =
                (
                        snapshot.get(
                            "readBy"
                        ) as? List<*>
                        )
                    ?.filterIsInstance<String>()
                    ?: emptyList()

            if (!currentReadBy.contains(userId)) {

                transaction.update(
                    messageRef,
                    "readBy",
                    currentReadBy + userId
                )
            }

            null
        }
    }

    // =========================================================
    // SET GROUP TYPING STATUS
    // =========================================================

    fun setTyping(
        groupId: String,
        userId: String,
        isTyping: Boolean
    ) {

        val groupRef =
            db.collection("groups")
                .document(groupId)

        db.runTransaction { transaction ->

            val snapshot =
                transaction.get(groupRef)

            val currentTyping =
                (
                        snapshot.get(
                            "typing"
                        ) as? Map<*, *>
                        )
                    ?.mapNotNull { entry ->

                        val key =
                            entry.key as? String

                        val value =
                            entry.value as? Boolean

                        if (
                            key != null &&
                            value != null
                        ) {
                            key to value
                        } else {
                            null
                        }
                    }
                    ?.toMap()
                    ?: emptyMap()

            val updatedTyping =
                currentTyping.toMutableMap()

            updatedTyping[userId] =
                isTyping

            transaction.set(
                groupRef,
                mapOf(
                    "typing" to updatedTyping
                ),
                SetOptions.merge()
            )

            null
        }
    }

    // =========================================================
    // LISTEN TO GROUP TYPING
    // =========================================================

    fun listenToGroupTyping(
        groupId: String,
        currentUserId: String,
        onTypingUsersChanged:
            (List<String>) -> Unit,
        onError:
            (String) -> Unit
    ): ListenerRegistration {

        return db.collection("groups")
            .document(groupId)
            .addSnapshotListener { snapshot, exception ->

                if (exception != null) {

                    onError(
                        exception.message
                            ?: "Unable to listen for group typing"
                    )

                    return@addSnapshotListener
                }

                val typingMap =
                    snapshot?.get("typing")
                            as? Map<*, *>

                val typingUsers =
                    typingMap
                        ?.mapNotNull { entry ->

                            val userId =
                                entry.key as? String

                            val isTyping =
                                entry.value as? Boolean
                                    ?: false

                            if (
                                userId != null &&
                                userId != currentUserId &&
                                isTyping
                            ) {
                                userId
                            } else {
                                null
                            }
                        }
                        ?: emptyList()

                onTypingUsersChanged(
                    typingUsers
                )
            }
    }

    // =========================================================
    // CLEAR GROUP TYPING STATUS
    // =========================================================

    fun clearTypingStatus(
        groupId: String,
        userId: String
    ) {

        setTyping(
            groupId = groupId,
            userId = userId,
            isTyping = false
        )
    }
}
