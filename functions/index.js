const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore } = require("firebase-admin/firestore");
const { getMessaging } = require("firebase-admin/messaging");

initializeApp();

const db = getFirestore();

// ============================================================
// ONE-TO-ONE MESSAGE NOTIFICATION
//
// Trigger:
// conversations/{conversationId}/messages/{messageId}
//
// The message text is encrypted, so this function does NOT
// attempt to decrypt it.
// ============================================================

exports.sendMessageNotification = onDocumentCreated(
  "conversations/{conversationId}/messages/{messageId}",
  async (event) => {
    try {
      const snapshot = event.data;

      if (!snapshot) {
        console.log("No message data found.");
        return;
      }

      const message = snapshot.data();

      const senderId = message.senderId;
      const receiverId = message.receiverId;

      if (!senderId || !receiverId) {
        console.log("Missing senderId or receiverId.");
        return;
      }

      // --------------------------------------------------------
      // Get receiver's user document
      // --------------------------------------------------------

      const receiverDocument = await db
        .collection("users")
        .doc(receiverId)
        .get();

      if (!receiverDocument.exists) {
        console.log("Receiver user document does not exist.");
        return;
      }

      const receiverData = receiverDocument.data();

      const fcmToken = receiverData.fcmToken;

      if (!fcmToken) {
        console.log("Receiver has no FCM token.");
        return;
      }

      // --------------------------------------------------------
      // Get sender name
      // --------------------------------------------------------

      const senderDocument = await db.collection("users").doc(senderId).get();

      let senderName = "New message";

      if (senderDocument.exists) {
        const senderData = senderDocument.data();

        senderName =
          senderData.name ||
          senderData.fullName ||
          senderData.username ||
          "New message";
      }

      // --------------------------------------------------------
      // Send notification
      //
      // IMPORTANT:
      // Do not send encrypted message text as notification body.
      // --------------------------------------------------------

      const notification = {
        token: fcmToken,

        notification: {
          title: senderName,
          body: "You have received a new message",
        },

        data: {
          type: "chat",
          senderId: senderId,
          receiverId: receiverId,
          conversationId: event.params.conversationId,
        },

        android: {
          priority: "high",

          notification: {
            channelId: "guidechat_messages",
            sound: "default",
          },
        },
      };

      const response = await getMessaging().send(notification);

      console.log("Notification sent successfully:", response);
    } catch (error) {
      console.error("Error sending message notification:", error);
    }
  },
);
