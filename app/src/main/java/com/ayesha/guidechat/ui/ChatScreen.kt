
package com.ayesha.guidechat.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ayesha.guidechat.data.ChatRepository
import com.ayesha.guidechat.data.UserRepository

private val ChatGreen =
    Color(0xFF2E6F40)

private val ChatLightGreen =
    Color(0xFFCFFDDC)

private val ChatBackground =
    Color(0xFFF8FBF9)

private val ChatText =
    Color(0xFF253D2C)

private val ChatSecondary =
    Color(0xFF6B756E)

private val ReadBlue =
    Color(0xFF2196F3)

private const val NOTIFICATION_CHANNEL_ID =
    "guidechat_messages"

private const val NOTIFICATION_CHANNEL_NAME =
    "GuideChat Messages"


// =============================================================
// CHAT MESSAGE MODEL
// =============================================================

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val text: String = "",
    val timestamp: Long = 0L,
    val isRead: Boolean = false
)


// =============================================================
// CREATE NOTIFICATION CHANNEL
// =============================================================

private fun createMessageNotificationChannel(
    context: Context
) {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

        val manager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        val channel =
            NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {

                description =
                    "Notifications for new GuideChat messages"

                enableVibration(true)
            }

        manager.createNotificationChannel(channel)
    }
}


// =============================================================
// SHOW LOCAL MESSAGE NOTIFICATION
// =============================================================

private fun showMessageNotification(
    context: Context,
    senderName: String,
    message: String
) {

    try {

        createMessageNotificationChannel(
            context
        )

        val manager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        val notificationId =
            (System.currentTimeMillis() % Int.MAX_VALUE)
                .toInt()

        val builder =
            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.O
            ) {

                android.app.Notification.Builder(
                    context,
                    NOTIFICATION_CHANNEL_ID
                )

            } else {

                @Suppress("DEPRECATION")
                android.app.Notification.Builder(
                    context
                )
            }

        builder
            .setSmallIcon(
                android.R.drawable.ic_dialog_email
            )
            .setContentTitle(
                senderName.ifBlank {
                    "New message"
                }
            )
            .setContentText(
                message.ifBlank {
                    "You received a new message"
                }
            )
            .setAutoCancel(true)
            .setPriority(
                android.app.Notification.PRIORITY_HIGH
            )

        manager.notify(
            notificationId,
            builder.build()
        )

    } catch (
        exception: SecurityException
    ) {

        // Notification permission is not granted.
        // Do not crash the chat screen.

    } catch (
        exception: Exception
    ) {

        // Prevent notification problems from
        // affecting messaging.
    }
}


// =============================================================
// CHAT SCREEN
// =============================================================

@Composable
fun ChatScreen(
    currentUserId: String,
    otherUserId: String,
    otherUserName: String,
    onBack: () -> Unit
) {

    val context =
        LocalContext.current

    val chatRepository =
        remember(context) {
            ChatRepository(context)
        }

    val userRepository =
        remember {
            UserRepository()
        }

    var messages by remember {

        mutableStateOf<List<ChatMessage>>(
            emptyList()
        )
    }

    var messageText by remember {
        mutableStateOf("")
    }

    var isOtherUserTyping by remember {
        mutableStateOf(false)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    var currentUserName by remember {
        mutableStateOf("You")
    }

    val listState =
        rememberLazyListState()


    // =========================================================
    // LOAD CURRENT USER PROFILE
    // =========================================================

    LaunchedEffect(currentUserId) {

        userRepository.getUserProfile(

            uid = currentUserId,

            onSuccess = { profile ->

                currentUserName =
                    profile.name.ifBlank {
                        "You"
                    }
            },

            onError = {

                currentUserName =
                    "You"
            }
        )
    }


    // =========================================================
    // LISTEN FOR MESSAGES
    // =========================================================

    DisposableEffect(
        currentUserId,
        otherUserId
    ) {

        /*
         * Used so that opening an existing conversation
         * does NOT immediately generate notifications
         * for all old messages.
         */

        var firstSnapshot =
            true

        val notifiedMessageIds =
            mutableSetOf<String>()

        val listener =
            chatRepository.listenForMessages(

                currentUserId =
                    currentUserId,

                otherUserId =
                    otherUserId,

                onMessagesChanged = { result ->

                    messages =
                        result

                    errorMessage =
                        null


                    // =================================================
                    // LOCAL NOTIFICATION
                    // =================================================

                    if (firstSnapshot) {

                        /*
                         * First snapshot contains existing messages.
                         * Mark them as already seen by this screen.
                         */

                        result.forEach { message ->

                            notifiedMessageIds.add(
                                message.id
                            )
                        }

                        firstSnapshot =
                            false

                    } else {

                        /*
                         * Find newly received messages.
                         */

                        result.forEach { message ->

                            val isIncoming =
                                message.senderId !=
                                        currentUserId

                            val isNew =
                                !notifiedMessageIds
                                    .contains(
                                        message.id
                                    )

                            if (
                                isIncoming &&
                                isNew
                            ) {

                                notifiedMessageIds.add(
                                    message.id
                                )

                                showMessageNotification(

                                    context =
                                        context,

                                    senderName =
                                        otherUserName,

                                    message =
                                        message.text
                                )
                            }
                        }
                    }
                },

                onError = { error ->

                    errorMessage =
                        error
                }
            )

        onDispose {

            listener.remove()
        }
    }


    // =========================================================
    // LISTEN FOR OTHER USER TYPING
    // =========================================================

    DisposableEffect(
        currentUserId,
        otherUserId
    ) {

        val typingListener =
            chatRepository.listenForTyping(

                currentUserId =
                    currentUserId,

                otherUserId =
                    otherUserId,

                onTypingChanged = { typing ->

                    isOtherUserTyping =
                        typing
                },

                onError = {

                    isOtherUserTyping =
                        false
                }
            )

        onDispose {

            typingListener.remove()

            chatRepository.clearTypingStatus(

                currentUserId =
                    currentUserId,

                otherUserId =
                    otherUserId
            )
        }
    }


    // =========================================================
    // MARK MESSAGES AS READ
    // =========================================================

    LaunchedEffect(
        messages.size,
        currentUserId,
        otherUserId
    ) {

        if (messages.isNotEmpty()) {

            chatRepository.markMessagesAsRead(

                currentUserId =
                    currentUserId,

                otherUserId =
                    otherUserId
            )
        }
    }


    // =========================================================
    // SCROLL TO LATEST MESSAGE
    // =========================================================

    LaunchedEffect(
        messages.size
    ) {

        if (messages.isNotEmpty()) {

            listState.animateScrollToItem(
                messages.lastIndex
            )
        }
    }


    // =========================================================
    // MAIN SCAFFOLD
    // =========================================================

    Scaffold(

        containerColor =
            ChatBackground,


        // =====================================================
        // TOP BAR
        // =====================================================

        topBar = {

            Row(

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(
                            Color.White
                        )
                        .padding(
                            horizontal = 8.dp,
                            vertical = 10.dp
                        ),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                // -------------------------------------------------
                // BACK BUTTON
                // -------------------------------------------------

                IconButton(
                    onClick = onBack
                ) {

                    Icon(

                        imageVector =
                            Icons
                                .AutoMirrored
                                .Filled
                                .ArrowBack,

                        contentDescription =
                            "Back",

                        tint =
                            ChatText
                    )
                }


                // -------------------------------------------------
                // AVATAR
                // -------------------------------------------------

                Box(

                    modifier =
                        Modifier
                            .size(48.dp)
                            .clip(
                                CircleShape
                            )
                            .background(
                                ChatLightGreen
                            ),

                    contentAlignment =
                        Alignment.Center
                ) {

                    Text(

                        text =
                            otherUserName
                                .trim()
                                .firstOrNull()
                                ?.uppercase()
                                ?: "U",

                        fontSize =
                            20.sp,

                        fontWeight =
                            FontWeight.Bold,

                        color =
                            ChatGreen
                    )
                }


                Spacer(
                    modifier =
                        Modifier.size(12.dp)
                )


                // -------------------------------------------------
                // NAME + TYPING
                // -------------------------------------------------

                Column(

                    modifier =
                        Modifier.weight(1f)
                ) {

                    Text(

                        text =
                            otherUserName.ifBlank {
                                "User"
                            },

                        fontSize =
                            17.sp,

                        fontWeight =
                            FontWeight.Bold,

                        color =
                            ChatText
                    )


                    if (
                        isOtherUserTyping
                    ) {

                        Text(

                            text =
                                "typing...",

                            fontSize =
                                13.sp,

                            color =
                                ChatGreen,

                            fontWeight =
                                FontWeight.Medium
                        )

                    } else {

                        Text(

                            text =
                                "mentor",

                            fontSize =
                                12.sp,

                            color =
                                ChatSecondary
                        )
                    }
                }


                // -------------------------------------------------
                // MORE
                // -------------------------------------------------

                IconButton(
                    onClick = {
                        // Future menu actions
                    }
                ) {

                    Icon(

                        imageVector =
                            Icons.Default.MoreVert,

                        contentDescription =
                            "More",

                        tint =
                            ChatText
                    )
                }
            }
        },


        // =====================================================
        // MESSAGE INPUT
        // =====================================================

        bottomBar = {

            Row(

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(
                            Color.White
                        )
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(10.dp),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                // -------------------------------------------------
                // TEXT FIELD
                // -------------------------------------------------

                OutlinedTextField(

                    value =
                        messageText,

                    onValueChange = { newText ->

                        messageText =
                            newText


                        // =========================================
                        // TYPING STATUS
                        // =========================================

                        if (
                            newText
                                .trim()
                                .isNotEmpty()
                        ) {

                            chatRepository.setTyping(

                                currentUserId =
                                    currentUserId,

                                otherUserId =
                                    otherUserId,

                                isTyping =
                                    true
                            )

                        } else {

                            chatRepository.clearTypingStatus(

                                currentUserId =
                                    currentUserId,

                                otherUserId =
                                    otherUserId
                            )
                        }
                    },

                    modifier =
                        Modifier.weight(1f),

                    placeholder = {

                        Text(
                            "Type a message..."
                        )
                    },

                    singleLine =
                        true,

                    shape =
                        RoundedCornerShape(
                            22.dp
                        ),

                    colors =
                        OutlinedTextFieldDefaults.colors(

                            focusedBorderColor =
                                ChatGreen,

                            unfocusedBorderColor =
                                Color(
                                    0xFFD5DED7
                                ),

                            cursorColor =
                                ChatGreen
                        )
                )


                Spacer(
                    modifier =
                        Modifier.size(8.dp)
                )


                // -------------------------------------------------
                // SEND BUTTON
                // -------------------------------------------------

                IconButton(

                    onClick = {

                        val text =
                            messageText.trim()

                        if (
                            text.isEmpty()
                        ) {
                            return@IconButton
                        }


                        // Stop typing

                        chatRepository.clearTypingStatus(

                            currentUserId =
                                currentUserId,

                            otherUserId =
                                otherUserId
                        )


                        // Send encrypted message

                        chatRepository.sendMessage(

                            senderId =
                                currentUserId,

                            receiverId =
                                otherUserId,

                            text =
                                text,

                            onSuccess = {

                                messageText =
                                    ""
                            },

                            onError = { error ->

                                errorMessage =
                                    error
                            }
                        )
                    },

                    modifier =
                        Modifier
                            .size(48.dp)
                            .clip(
                                CircleShape
                            )
                            .background(

                                if (
                                    messageText
                                        .trim()
                                        .isNotEmpty()
                                ) {

                                    ChatGreen

                                } else {

                                    Color(
                                        0xFFD4D4D4
                                    )
                                }
                            )
                ) {

                    Icon(

                        imageVector =
                            Icons
                                .AutoMirrored
                                .Filled
                                .Send,

                        contentDescription =
                            "Send",

                        tint =
                            Color.White
                    )
                }
            }
        }

    ) { paddingValues ->


        // =========================================================
        // CONTENT
        // =========================================================

        Column(

            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        paddingValues
                    )
        ) {


            // =====================================================
            // ERROR MESSAGE
            // =====================================================

            if (
                errorMessage != null
            ) {

                Text(

                    text =
                        errorMessage ?: "",

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 12.dp,
                                vertical = 6.dp
                            ),

                    color =
                        Color(
                            0xFFB3261E
                        ),

                    fontSize =
                        13.sp
                )
            }


            // =====================================================
            // EMPTY CHAT
            // =====================================================

            if (
                messages.isEmpty()
            ) {

                Box(

                    modifier =
                        Modifier.fillMaxSize(),

                    contentAlignment =
                        Alignment.Center
                ) {

                    Column(

                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {

                        Box(

                            modifier =
                                Modifier
                                    .size(80.dp)
                                    .clip(
                                        CircleShape
                                    )
                                    .background(
                                        ChatLightGreen
                                    ),

                            contentAlignment =
                                Alignment.Center
                        ) {

                            Icon(

                                imageVector =
                                    Icons.Default.Person,

                                contentDescription =
                                    "Person",

                                tint =
                                    ChatGreen,

                                modifier =
                                    Modifier.size(
                                        38.dp
                                    )
                            )
                        }


                        Spacer(
                            modifier =
                                Modifier.size(
                                    16.dp
                                )
                        )


                        Text(

                            text =
                                "Start a conversation",

                            fontSize =
                                18.sp,

                            fontWeight =
                                FontWeight.Bold,

                            color =
                                ChatText
                        )


                        Spacer(
                            modifier =
                                Modifier.size(
                                    5.dp
                                )
                        )


                        Text(

                            text =
                                "Send a message to start chatting.",

                            fontSize =
                                13.sp,

                            color =
                                ChatSecondary
                        )
                    }
                }

            } else {


                // =================================================
                // MESSAGE LIST
                // =================================================

                LazyColumn(

                    state =
                        listState,

                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(
                                horizontal = 14.dp
                            ),

                    verticalArrangement =
                        Arrangement.spacedBy(
                            8.dp
                        )
                ) {

                    items(

                        items =
                            messages,

                        key = {
                                message ->
                            message.id
                        }

                    ) { message ->

                        ChatMessageBubble(

                            message =
                                message,

                            isMine =
                                message.senderId ==
                                        currentUserId,

                            currentUserName =
                                currentUserName
                        )
                    }
                }
            }
        }
    }
}


// =============================================================
// MESSAGE BUBBLE
// =============================================================

@Composable
private fun ChatMessageBubble(

    message: ChatMessage,

    isMine: Boolean,

    currentUserName: String

) {

    Row(

        modifier =
            Modifier.fillMaxWidth(),

        horizontalArrangement =
            if (isMine) {

                Arrangement.End

            } else {

                Arrangement.Start
            }
    ) {

        Column(

            modifier =
                Modifier
                    .clip(

                        RoundedCornerShape(

                            topStart =
                                18.dp,

                            topEnd =
                                18.dp,

                            bottomStart =
                                if (isMine) {
                                    18.dp
                                } else {
                                    4.dp
                                },

                            bottomEnd =
                                if (isMine) {
                                    4.dp
                                } else {
                                    18.dp
                                }
                        )
                    )
                    .background(

                        if (isMine) {

                            ChatLightGreen

                        } else {

                            Color.White
                        }
                    )
                    .padding(

                        horizontal =
                            16.dp,

                        vertical =
                            11.dp
                    )
        ) {


            // =====================================================
            // MESSAGE TEXT
            // =====================================================

            Text(

                text =
                    message.text,

                fontSize =
                    16.sp,

                color =
                    ChatText
            )


            // =====================================================
            // READ RECEIPTS
            // =====================================================

            if (isMine) {

                Spacer(
                    modifier =
                        Modifier.size(
                            3.dp
                        )
                )


                Row(

                    modifier =
                        Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.End,

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Text(

                        text =
                            "✓✓",

                        fontSize =
                            14.sp,

                        fontWeight =
                            FontWeight.Bold,

                        color =
                            if (
                                message.isRead
                            ) {

                                ReadBlue

                            } else {

                                ChatGreen
                            }
                    )
                }
            }
        }
    }
}