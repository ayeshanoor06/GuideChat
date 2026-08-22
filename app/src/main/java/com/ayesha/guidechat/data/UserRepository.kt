package com.ayesha.guidechat.data

import com.ayesha.guidechat.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestore

class UserRepository {

    private val db = FirebaseFirestore.getInstance()

    // =========================================================
    // CREATE USER PROFILE
    // =========================================================

    fun createUserProfile(
        userProfile: UserProfile,
        onComplete: (Boolean, String?) -> Unit
    ) {

        db.collection("users")
            .document(userProfile.uid)
            .set(userProfile)
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
                        ?: "Unable to create user profile"
                )
            }
    }

    // =========================================================
    // GET CURRENT USER PROFILE
    // =========================================================

    fun getUserProfile(
        uid: String,
        onSuccess: (UserProfile) -> Unit,
        onError: (String) -> Unit
    ) {

        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->

                if (document.exists()) {

                    val profile = UserProfile(

                        uid = document.id,

                        name = document.getString("name")
                            ?: "",

                        email = document.getString("email")
                            ?: "",

                        role = document.getString("role")
                            ?: "",

                        profileImage = document.getString(
                            "profileImage"
                        ) ?: "",

                        isOnline = document.getBoolean(
                            "isOnline"
                        ) ?: false,

                        createdAt = document.getLong(
                            "createdAt"
                        ) ?: 0L
                    )

                    onSuccess(profile)

                } else {

                    onError(
                        "User profile not found"
                    )
                }
            }
            .addOnFailureListener { exception ->

                onError(
                    exception.message
                        ?: "Unable to load user profile"
                )
            }
    }

    // =========================================================
    // GET ALL USERS
    // =========================================================
    //
    // Used by GroupCreateScreen.
    //
    // This loads the real users stored in:
    //
    // Firebase Firestore
    //      └── users
    //
    // The currently logged-in user is removed from the list.
    // =========================================================

    fun getAllUsers(
        onSuccess: (List<UserProfile>) -> Unit,
        onError: (String) -> Unit
    ) {

        db.collection("users")
            .get()
            .addOnSuccessListener { result ->

                val users =
                    result.documents
                        .mapNotNull { document ->

                            val uid =
                                document.id

                            val name =
                                document.getString("name")
                                    ?: ""

                            val email =
                                document.getString("email")
                                    ?: ""

                            val role =
                                document.getString("role")
                                    ?: ""

                            val profileImage =
                                document.getString("profileImage")
                                    ?: ""

                            val isOnline =
                                document.getBoolean("isOnline")
                                    ?: false

                            val createdAt =
                                document.getLong("createdAt")
                                    ?: 0L

                            UserProfile(

                                uid = uid,

                                name = name,

                                email = email,

                                role = role,

                                profileImage =
                                    profileImage,

                                isOnline =
                                    isOnline,

                                createdAt =
                                    createdAt
                            )
                        }
                        .sortedBy { user ->

                            user.name.lowercase()
                        }

                onSuccess(users)
            }
            .addOnFailureListener { exception ->

                onError(
                    exception.message
                        ?: "Unable to load users"
                )
            }
    }

    // =========================================================
    // SEARCH USERS
    // =========================================================

    fun searchUsers(
        searchText: String,
        currentUserId: String,
        onSuccess: (List<UserProfile>) -> Unit,
        onError: (String) -> Unit
    ) {

        db.collection("users")
            .get()
            .addOnSuccessListener { result ->

                val search =
                    searchText.trim().lowercase()

                val users =
                    result.documents
                        .mapNotNull { document ->

                            val uid =
                                document.id

                            // Don't show the logged-in user
                            if (uid == currentUserId) {
                                return@mapNotNull null
                            }

                            val name =
                                document.getString("name")
                                    ?: ""

                            val email =
                                document.getString("email")
                                    ?: ""

                            val role =
                                document.getString("role")
                                    ?: ""

                            val profileImage =
                                document.getString(
                                    "profileImage"
                                ) ?: ""

                            val isOnline =
                                document.getBoolean(
                                    "isOnline"
                                ) ?: false

                            val createdAt =
                                document.getLong(
                                    "createdAt"
                                ) ?: 0L

                            UserProfile(

                                uid = uid,

                                name = name,

                                email = email,

                                role = role,

                                profileImage =
                                    profileImage,

                                isOnline =
                                    isOnline,

                                createdAt =
                                    createdAt
                            )
                        }
                        .filter { user ->

                            if (search.isBlank()) {

                                true

                            } else {

                                user.name
                                    .lowercase()
                                    .contains(search) ||

                                        user.email
                                            .lowercase()
                                            .contains(
                                                search
                                            )
                            }
                        }
                        .sortedBy { user ->

                            user.name.lowercase()
                        }

                onSuccess(users)
            }
            .addOnFailureListener { exception ->

                onError(
                    exception.message
                        ?: "Unable to search users"
                )
            }
    }
}