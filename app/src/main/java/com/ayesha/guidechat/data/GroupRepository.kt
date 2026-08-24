package com.ayesha.guidechat.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

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

class GroupRepository {

    private val db =
        FirebaseFirestore.getInstance()



    // CREATE GROUP

    fun createGroup(
        name: String,
        createdBy: String,
        memberIds: List<String>,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {

        val cleanName =
            name.trim()

        if (cleanName.isEmpty()) {

            onError(
                "Group name cannot be empty"
            )

            return
        }

        val allMembers =
            (memberIds + createdBy)
                .distinct()

        if (allMembers.size < 2) {

            onError(
                "A group must have at least 2 members"
            )

            return
        }

        val groupRef =
            db.collection("groups")
                .document()

        val group =
            hashMapOf(

                "name" to
                        cleanName,

                "createdBy" to
                        createdBy,

                "createdAt" to
                        System.currentTimeMillis(),

                "memberIds" to
                        allMembers
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



    // LISTEN TO GROUPS


    fun listenToGroups(
        currentUserId: String,
        onGroupsChanged:
            (List<GroupModel>) -> Unit,
        onError:
            (String) -> Unit
    ): ListenerRegistration {

        return db
            .collection("groups")

            .whereArrayContains(
                "memberIds",
                currentUserId
            )

            .addSnapshotListener {
                    snapshot,
                    exception ->

                if (exception != null) {

                    onError(
                        exception.message
                            ?: "Unable to load groups"
                    )

                    return@addSnapshotListener
                }

                val groups =
                    snapshot
                        ?.documents
                        ?.map { document ->

                            GroupModel(

                                id =
                                    document.id,

                                name =
                                    document
                                        .getString("name")
                                        ?: "Unnamed Group",

                                createdBy =
                                    document
                                        .getString("createdBy")
                                        ?: "",

                                createdAt =
                                    document
                                        .getLong("createdAt")
                                        ?: 0L,

                                memberIds =
                                    (document
                                        .get("memberIds")
                                            as? List<*>)
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



    // SEND GROUP MESSAGE


    fun sendGroupMessage(
        groupId: String,
        senderId: String,
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

        val messageRef =
            db.collection("groups")
                .document(groupId)
                .collection("messages")
                .document()



        val message =
            hashMapOf(

                "senderId" to
                        senderId,

                "text" to
                        cleanText,

                "timestamp" to
                        System.currentTimeMillis(),

                "readBy" to
                        listOf(senderId)
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
    }



    // LISTEN TO GROUP MESSAGES


    fun listenToGroupMessages(
        groupId: String,
        onMessagesChanged:
            (List<GroupMessage>) -> Unit,
        onError:
            (String) -> Unit
    ): ListenerRegistration {

        return db
            .collection("groups")
            .document(groupId)
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
                            ?: "Unable to load group messages"
                    )

                    return@addSnapshotListener
                }

                val messages =
                    snapshot
                        ?.documents
                        ?.map { document ->

                            GroupMessage(

                                id =
                                    document.id,

                                senderId =
                                    document
                                        .getString("senderId")
                                        ?: "",

                                text =
                                    document
                                        .getString("text")
                                        ?: "",

                                timestamp =
                                    document
                                        .getLong("timestamp")
                                        ?: 0L,

                                readBy =
                                    (document
                                        .get("readBy")
                                            as? List<*>)
                                        ?.filterIsInstance<String>()
                                        ?: emptyList()
                            )
                        }
                        ?: emptyList()

                onMessagesChanged(
                    messages
                )
            }
    }



    // MARK GROUP MESSAGE AS READ


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
                (snapshot
                    .get("readBy")
                        as? List<*>)
                    ?.filterIsInstance<String>()
                    ?: emptyList()

            if (!currentReadBy.contains(userId)) {

                transaction.update(
                    messageRef,
                    "readBy",
                    currentReadBy + userId
                )
            }
        }
    }
}