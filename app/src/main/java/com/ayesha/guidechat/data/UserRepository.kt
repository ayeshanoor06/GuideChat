package com.ayesha.guidechat.data

import com.ayesha.guidechat.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestore

class UserRepository {

    private val db = FirebaseFirestore.getInstance()

    fun createUserProfile(
        userProfile: UserProfile,
        onComplete: (Boolean, String?) -> Unit
    ) {
        db.collection("users")
            .document(userProfile.uid)
            .set(userProfile)
            .addOnSuccessListener {
                onComplete(true, null)
            }
            .addOnFailureListener { exception ->
                onComplete(
                    false,
                    exception.message ?: "Unable to create user profile"
                )
            }
    }

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
                    val profile = document.toObject(UserProfile::class.java)

                    if (profile != null) {
                        onSuccess(profile.copy(uid = uid))
                    } else {
                        onError("User profile data is invalid")
                    }
                } else {
                    onError("User profile not found")
                }
            }
            .addOnFailureListener { exception ->
                onError(
                    exception.message ?: "Unable to load user profile"
                )
            }
    }
}