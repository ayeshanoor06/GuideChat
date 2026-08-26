package com.ayesha.guidechat.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.ayesha.guidechat.model.UserProfile
import kotlinx.coroutines.delay


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


data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val text: String = "",
    val timestamp: Long = 0L,
    val isRead: Boolean = false
)


@Composable
fun ChatScreen(
    currentUserId: String,
    currentUserName: String,
    otherUser: UserProfile,
    onBack: () -> Unit
) {

    val context =
        LocalContext.current

    val chatRepository =
        remember(context) {
            ChatRepository(context)
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

    val listState =
        rememberLazyListState()


    // =========================================================
    // LISTEN FOR MESSAGES
    // =========================================================

    DisposableEffect(
        currentUserId,
        otherUser.uid
    ) {

        val listener =
            chatRepository.listenForMessages(
                currentUserId =
                    currentUserId,

                otherUserId =
                    otherUser.uid,

                onMessagesChanged = { result ->

                    messages =
                        result

                    errorMessage =
                        null
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
        otherUser.uid
    ) {

        val typingListener =
            chatRepository.listenForTyping(
                currentUserId =
                    currentUserId,

                otherUserId =
                    otherUser.uid,

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
                    otherUser.uid
            )
        }
    }


    // =========================================================
    // MARK RECEIVED MESSAGES AS READ
    // =========================================================

    LaunchedEffect(
        currentUserId,
        otherUser.uid,
        messages.size
    ) {

        if (messages.isNotEmpty()) {

            chatRepository.markMessagesAsRead(
                currentUserId =
                    currentUserId,

                otherUserId =
                    otherUser.uid
            )
        }
    }


    // =========================================================
    // TYPING STATUS
    // =========================================================

    LaunchedEffect(messageText) {

        if (messageText.isBlank()) {

            chatRepository.setTyping(
                currentUserId =
                    currentUserId,

                otherUserId =
                    otherUser.uid,

                isTyping =
                    false
            )

            return@LaunchedEffect
        }

        chatRepository.setTyping(
            currentUserId =
                currentUserId,

            otherUserId =
                otherUser.uid,

            isTyping =
                true
        )

        delay(1500)

        chatRepository.setTyping(
            currentUserId =
                currentUserId,

            otherUserId =
                otherUser.uid,

            isTyping =
                false
        )
    }


    // =========================================================
    // SCROLL TO LATEST MESSAGE
    // =========================================================

    LaunchedEffect(messages.size) {

        if (messages.isNotEmpty()) {

            listState.animateScrollToItem(
                messages.lastIndex
            )
        }
    }


    // =========================================================
    // MAIN UI
    // =========================================================

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    ChatBackground
                )
    ) {


        // =====================================================
        // TOP BAR
        // =====================================================

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

            IconButton(
                onClick = onBack
            ) {

                Icon(
                    imageVector =
                        Icons.AutoMirrored.Filled.ArrowBack,

                    contentDescription =
                        "Back",

                    tint =
                        ChatText
                )
            }


            // AVATAR

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
                        otherUser.name
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


            // NAME + STATUS

            Column(
                modifier =
                    Modifier.weight(1f)
            ) {

                Text(
                    text =
                        otherUser.name.ifBlank {
                            "User"
                        },

                    fontSize =
                        17.sp,

                    fontWeight =
                        FontWeight.Bold,

                    color =
                        ChatText
                )

                Spacer(
                    modifier =
                        Modifier.height(2.dp)
                )

                if (isOtherUserTyping) {

                    Text(
                        text =
                            "typing...",

                        fontSize =
                            12.sp,

                        color =
                            ChatGreen,

                        fontWeight =
                            FontWeight.Medium
                    )

                } else {

                    Text(
                        text =
                            otherUser.role.ifBlank {
                                "User"
                            },

                        fontSize =
                            12.sp,

                        color =
                            ChatSecondary
                    )
                }
            }


            IconButton(
                onClick = {}
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


        // =====================================================
        // ERROR
        // =====================================================

        if (errorMessage != null) {

            Text(
                text =
                    errorMessage ?: "",

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(
                            Color(0xFFFFE8E8)
                        )
                        .padding(
                            10.dp
                        ),

                color =
                    Color(0xFFB3261E),

                fontSize =
                    13.sp
            )
        }


        // =====================================================
        // MESSAGES
        // =====================================================

        if (messages.isEmpty()) {

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),

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
                                .size(90.dp)
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
                                otherUser.name
                                    .firstOrNull()
                                    ?.uppercase()
                                    ?: "U",

                            fontSize =
                                32.sp,

                            fontWeight =
                                FontWeight.Bold,

                            color =
                                ChatGreen
                        )
                    }

                    Spacer(
                        modifier =
                            Modifier.height(18.dp)
                    )

                    Text(
                        text =
                            "Start a conversation",

                        fontSize =
                            20.sp,

                        fontWeight =
                            FontWeight.Bold,

                        color =
                            ChatText
                    )

                    Spacer(
                        modifier =
                            Modifier.height(6.dp)
                    )

                    Text(
                        text =
                            "Send a message to ${otherUser.name}",

                        fontSize =
                            14.sp,

                        color =
                            ChatSecondary
                    )
                }
            }

        } else {

            LazyColumn(
                state =
                    listState,

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(
                            horizontal = 14.dp,
                            vertical = 12.dp
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
                                    currentUserId
                    )
                }
            }
        }


        // =====================================================
        // MESSAGE INPUT
        // =====================================================

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(
                        Color.White
                    )
                    .padding(
                        horizontal = 12.dp,
                        vertical = 10.dp
                    ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            OutlinedTextField(

                value =
                    messageText,

                onValueChange = { newText ->

                    messageText =
                        newText

                    if (newText.isBlank()) {

                        chatRepository.clearTypingStatus(
                            currentUserId =
                                currentUserId,

                            otherUserId =
                                otherUser.uid
                        )

                    } else {

                        chatRepository.setTyping(
                            currentUserId =
                                currentUserId,

                            otherUserId =
                                otherUser.uid,

                            isTyping =
                                true
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


            // SEND BUTTON

            IconButton(

                onClick = {

                    val text =
                        messageText.trim()

                    if (text.isEmpty()) {
                        return@IconButton
                    }


                    chatRepository.clearTypingStatus(
                        currentUserId =
                            currentUserId,

                        otherUserId =
                            otherUser.uid
                    )


                    chatRepository.sendMessage(

                        senderId =
                            currentUserId,

                        receiverId =
                            otherUser.uid,

                        text =
                            text,

                        onSuccess = {

                            messageText =
                                ""
                        },

                        onError = { error ->

                            Toast.makeText(
                                context,
                                error,
                                Toast.LENGTH_LONG
                            ).show()
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
                                    .isNotBlank()
                            ) {
                                ChatGreen
                            } else {
                                Color.LightGray
                            }
                        )
            ) {

                Icon(
                    imageVector =
                        Icons.AutoMirrored.Filled.Send,

                    contentDescription =
                        "Send",

                    tint =
                        Color.White
                )
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
    isMine: Boolean
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
            horizontalAlignment =
                if (isMine) {
                    Alignment.End
                } else {
                    Alignment.Start
                }
        ) {

            Box(
                modifier =
                    Modifier
                        .clip(
                            RoundedCornerShape(
                                topStart = 18.dp,
                                topEnd = 18.dp,
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
                            horizontal = 15.dp,
                            vertical = 10.dp
                        )
            ) {

                Text(
                    text =
                        message.text,

                    fontSize =
                        15.sp,

                    color =
                        ChatText
                )
            }


            // =================================================
            // READ RECEIPT
            // =================================================

            if (isMine) {

                Row(
                    modifier =
                        Modifier.padding(
                            top = 2.dp,
                            end = 4.dp
                        ),

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
                            if (message.isRead) {
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