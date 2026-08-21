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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ayesha.guidechat.data.ChatRepository
import com.ayesha.guidechat.model.UserProfile

private val DarkGreen = Color(0xFF2E6F40)
private val LightMint = Color(0xFFCFFDDC)
private val Background = Color(0xFFF7FAF7)
private val DarkText = Color(0xFF253D2C)

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
    val context = androidx.compose.ui.platform.LocalContext.current

    val chatRepository = remember {
        ChatRepository()
    }

    var messageText by remember {
        mutableStateOf("")
    }

    var messages by remember {
        mutableStateOf<List<ChatMessage>>(emptyList())
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    // Listen to Firestore messages in real time.
    DisposableEffect(
        currentUserId,
        otherUser.uid
    ) {
        val listener =
            chatRepository.listenForMessages(
                currentUserId = currentUserId,
                otherUserId = otherUser.uid,

                onMessagesChanged = { newMessages ->
                    messages = newMessages
                    errorMessage = null
                },

                onError = { error ->
                    errorMessage = error
                }
            )

        onDispose {
            listener.remove()
        }
    }

    // Mark messages received by this user as read.
    LaunchedEffect(
        currentUserId,
        otherUser.uid,
        messages.size
    ) {
        if (messages.isNotEmpty()) {
            chatRepository.markMessagesAsRead(
                currentUserId = currentUserId,
                otherUserId = otherUser.uid
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {


        // TOP BAR


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(
                    horizontal = 8.dp,
                    vertical = 10.dp
                ),

            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(
                onClick = onBack
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = DarkGreen
                )
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(LightMint),

                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = otherUser.name
                        .firstOrNull()
                        ?.uppercase()
                        ?: "?",

                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkGreen
                )
            }

            Spacer(
                modifier = Modifier.size(12.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = otherUser.name.ifBlank {
                        "User"
                    },

                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )

                Spacer(
                    modifier = Modifier.height(2.dp)
                )

                Text(
                    text = otherUser.role.ifBlank {
                        "User"
                    },

                    fontSize = 12.sp,
                    color = DarkGreen
                )
            }

            IconButton(
                onClick = {
                    // Chat options will be added later.
                }
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Chat options",
                    tint = DarkText
                )
            }
        }


        // ERROR

        if (errorMessage != null) {

            Text(
                text = errorMessage ?: "",
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color(0xFFFFE8E8)
                    )
                    .padding(10.dp),

                color = Color(0xFFB3261E),
                fontSize = 13.sp
            )
        }

        // MESSAGES

        if (messages.isEmpty()) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),

                contentAlignment = Alignment.Center
            ) {

                Column(
                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(LightMint),

                        contentAlignment =
                            Alignment.Center
                    ) {

                        Text(
                            text = otherUser.name
                                .firstOrNull()
                                ?.uppercase()
                                ?: "?",

                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkGreen
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(18.dp)
                    )

                    Text(
                        text = "Start a conversation",

                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )

                    Text(
                        text =
                            "Send a message to ${otherUser.name}",

                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

        } else {

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(
                        horizontal = 16.dp,
                        vertical = 12.dp
                    ),

                verticalArrangement =
                    Arrangement.spacedBy(8.dp),

                reverseLayout = false
            ) {

                items(
                    items = messages,
                    key = { message ->
                        message.id
                    }
                ) { message ->

                    MessageBubble(
                        message = message,
                        currentUserId = currentUserId
                    )
                }
            }
        }


        // MESSAGE INPUT


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(
                    horizontal = 12.dp,
                    vertical = 10.dp
                ),

            verticalAlignment = Alignment.CenterVertically
        ) {

            OutlinedTextField(
                value = messageText,

                onValueChange = {
                    messageText = it
                },

                modifier = Modifier.weight(1f),

                placeholder = {
                    Text("Type a message...")
                },

                singleLine = true,

                shape = RoundedCornerShape(24.dp)
            )

            Spacer(
                modifier = Modifier.size(8.dp)
            )

            IconButton(
                onClick = {

                    val textToSend =
                        messageText.trim()

                    if (textToSend.isEmpty()) {
                        return@IconButton
                    }

                    chatRepository.sendMessage(
                        senderId = currentUserId,
                        receiverId = otherUser.uid,
                        text = textToSend,

                        onSuccess = {
                            messageText = ""
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

                enabled = messageText.isNotBlank()
            ) {

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (messageText.isNotBlank()) {
                                DarkGreen
                            } else {
                                Color.LightGray
                            }
                        ),

                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        imageVector = Icons.Default.Send,

                        contentDescription = "Send",

                        tint = Color.White,

                        modifier = Modifier.size(21.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessage,
    currentUserId: String
) {

    val isMine =
        message.senderId == currentUserId

    Row(
        modifier = Modifier.fillMaxWidth(),

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
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart =
                                if (isMine) 18.dp else 4.dp,
                            bottomEnd =
                                if (isMine) 4.dp else 18.dp
                        )
                    )
                    .background(
                        if (isMine) {
                            LightMint
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
                    text = message.text,
                    fontSize = 15.sp,
                    color = DarkText
                )
            }

            if (isMine && message.isRead) {

                Row(
                    verticalAlignment =
                        Alignment.CenterVertically,

                    modifier = Modifier.padding(
                        top = 2.dp,
                        end = 4.dp
                    )
                ) {

                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = "Read",
                        modifier = Modifier.size(13.dp),
                        tint = DarkGreen
                    )

                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = "Read",
                        modifier = Modifier
                            .size(13.dp)
                            .padding(start = 0.dp),
                        tint = DarkGreen
                    )
                }
            }
        }
    }
}