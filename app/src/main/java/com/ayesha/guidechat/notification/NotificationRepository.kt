
package com.ayesha.guidechat.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class NotificationRepository {

    private val auth =
        FirebaseAuth.getInstance()

    private val db =
        FirebaseFirestore.getInstance()

    // =========================================================
    // REGISTER CURRENT DEVICE FOR NOTIFICATIONS
    // =========================================================

    fun registerCurrentDevice(
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {

        val currentUser =
            auth.currentUser

        // User is not logged in
        if (currentUser == null) {
            onError("No logged-in user found")
            return
        }

        val userId =
            currentUser.uid

        // Get the FCM token for this device
        FirebaseMessaging
            .getInstance()
            .token
            .addOnCompleteListener { task ->

                if (!task.isSuccessful) {

                    onError(
                        task.exception?.message
                            ?: "Unable to get FCM token"
                    )

                    return@addOnCompleteListener
                }

                val token =
                    task.result

                if (token.isNullOrBlank()) {

                    onError(
                        "FCM token is empty"
                    )

                    return@addOnCompleteListener
                }

                // =================================================
                // SAVE TOKEN TO FIRESTORE
                //
                // users/{currentUserUid}
                //     fcmToken: "..."
                // =================================================

                db.collection("users")
                    .document(userId)
                    .update(
                        "fcmToken",
                        token
                    )
                    .addOnSuccessListener {

                        onSuccess()
                    }
                    .addOnFailureListener { exception ->

                        /*
                         * If the user document exists but does not
                         * contain fcmToken, update() normally works
                         * because Firestore allows adding a new field.
                         */

                        onError(
                            exception.message
                                ?: "Unable to save FCM token"
                        )
                    }
            }
    }


    // =========================================================
    // UPDATE TOKEN
    //
    // Call this when Firebase generates a new token.
    // =========================================================

    fun updateToken(
        token: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {

        val currentUser =
            auth.currentUser

        if (currentUser == null) {

            onError(
                "No logged-in user found"
            )

            return
        }

        if (token.isBlank()) {

            onError(
                "FCM token is empty"
            )

            return
        }

        db.collection("users")
            .document(currentUser.uid)
            .update(
                "fcmToken",
                token
            )
            .addOnSuccessListener {

                onSuccess()
            }
            .addOnFailureListener { exception ->

                onError(
                    exception.message
                        ?: "Unable to update FCM token"
                )
            }
    }


    // =========================================================
    // REMOVE DEVICE TOKEN
    //
    // Useful when logging out.
    // =========================================================

    fun removeCurrentDeviceToken(
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {

        val currentUser =
            auth.currentUser

        if (currentUser == null) {

            onSuccess()
            return
        }

        db.collection("users")
            .document(currentUser.uid)
            .update(
                "fcmToken",
                ""
            )
            .addOnSuccessListener {

                onSuccess()
            }
            .addOnFailureListener { exception ->

                onError(
                    exception.message
                        ?: "Unable to remove FCM token"
                )
            }
    }
}
