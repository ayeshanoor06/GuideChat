package com.ayesha.guidechat.data

import com.ayesha.guidechat.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestore

class UserRepository {

    private val firestore = FirebaseFirestore.getInstance()

    private val usersCollection =
        firestore.collection("users")

    fun createUserProfile(
        userProfile: UserProfile,
        onResult: (Boolean, String?) -> Unit
    ) {

        usersCollection
            .document(userProfile.uid)
            .set(userProfile)
            .addOnSuccessListener {

                onResult(true, null)

            }
            .addOnFailureListener { exception ->

                onResult(
                    false,
                    exception.message
                        ?: "Failed to create user profile"
                )
            }
    }

    fun getUserProfile(
        uid: String,
        onResult: (UserProfile?, String?) -> Unit
    ) {

        usersCollection
            .document(uid)
            .get()
            .addOnSuccessListener { document ->

                if (document.exists()) {

                    val profile =
                        document.toObject(UserProfile::class.java)

                    onResult(profile, null)

                } else {

                    onResult(
                        null,
                        "User profile not found"
                    )
                }
            }
            .addOnFailureListener { exception ->

                onResult(
                    null,
                    exception.message
                        ?: "Failed to load profile"
                )
            }
    }
}