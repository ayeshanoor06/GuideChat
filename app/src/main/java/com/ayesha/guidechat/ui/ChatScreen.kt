

package com.ayesha.guidechat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import com.ayesha.guidechat.model.ChatMessage

private val ChatDarkGreen = Color(0xFF2E6F40)
private val ChatMint = Color(0xFFCFFDDC)
private val ChatDarkText = Color(0xFF253D2C)
private val ChatBackground = Color(0xFFF7FAF7)

@Composable
fun ChatScreen(
    currentUserId: String,
    otherUserId: String,
    otherUserName: String,
    onBack: () -> Unit
) {

    val chatRepository = remember {
        ChatRepository()
    }

    var messages by remember {
        mutableStateOf<List<ChatMessage>>(emptyList())
    }

    var messageText by remember {
        mutableStateOf("")
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    val listState = rememberLazyListState()

    DisposableEffect(
        currentUserId,
        otherUserId
    ) {

        val removeListener =
            chatRepository.listenForMessages(
                userId1 = currentUserId,
                userId2 = otherUserId,

                onMessagesChanged = {
                    messages = it
                },

                onError = {
                    errorMessage = it
                }
            )

        onDispose {
            removeListener()
        }
    }

    LaunchedEffect(messages.size) {

        if (messages.isNotEmpty()) {

            listState.animateScrollToItem(
                messages.lastIndex
            )
        }
    }

    Scaffold(
        containerColor = ChatBackground,

        topBar = {

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
                        tint = ChatDarkText
                    )
                }

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(ChatMint),

                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = otherUserName
                            .firstOrNull()
                            ?.uppercase()
                            ?: "?",

                        color = ChatDarkGreen,

                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(
                    modifier = Modifier.size(12.dp)
                )

                Column {

                    Text(
                        text = otherUserName,

                        color = ChatDarkText,

                        fontSize = 17.sp,

                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Online",

                        color = ChatDarkGreen,

                        fontSize = 12.sp
                    )
                }
            }
        },

        bottomBar = {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .navigationBarsPadding()
                    .padding(
                        horizontal = 12.dp,
                        vertical = 8.dp
                    ),

                verticalAlignment = Alignment.CenterVertically
            ) {

                TextField(
                    value = messageText,

                    onValueChange = {
                        messageText = it
                    },

                    modifier = Modifier.weight(1f),

                    placeholder = {
                        Text("Type a message...")
                    },

                    maxLines = 4,

                    shape = RoundedCornerShape(24.dp),

                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = ChatMint,
                        unfocusedContainerColor = ChatMint,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                Spacer(
                    modifier = Modifier.size(8.dp)
                )

                IconButton(
                    onClick = {

                        val trimmedText =
                            messageText.trim()

                        if (trimmedText.isEmpty()) {
                            return@IconButton
                        }

                        chatRepository.sendMessage(
                            senderId = currentUserId,
                            receiverId = otherUserId,
                            text = trimmedText
                        ) { success, error ->

                            if (success) {

                                messageText = ""

                                errorMessage = null

                            } else {

                                errorMessage =
                                    error
                                        ?: "Message could not be sent"
                            }
                        }
                    },

                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(ChatDarkGreen)
                ) {

                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = Color.White
                    )
                }
            }
        }

    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            if (errorMessage != null) {

                Text(
                    text = errorMessage!!,

                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color(0xFFFFE5E5)
                        )
                        .padding(12.dp),

                    color = Color(0xFFB3261E),

                    fontSize = 13.sp
                )
            }

            LazyColumn(
                state = listState,

                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = 14.dp
                    ),

                verticalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                items(
                    items = messages,
                    key = {
                        it.id
                    }
                ) { message ->

                    MessageBubble(
                        message = message,
                        isMine =
                            message.senderId ==
                                    currentUserId
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessage,
    isMine: Boolean
) {

    Row(
        modifier = Modifier.fillMaxWidth(),

        horizontalArrangement =
            if (isMine) {
                Arrangement.End
            } else {
                Arrangement.Start
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
                        ChatDarkGreen
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

                color = if (isMine) {
                    Color.White
                } else {
                    ChatDarkText
                },

                fontSize = 15.sp
            )
        }
    }
}