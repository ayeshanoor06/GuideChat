package com.ayesha.guidechat.ui

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ayesha.guidechat.data.GroupMessage
import com.ayesha.guidechat.data.GroupRepository

private val GroupGreen = Color(0xFF2E6F40)
private val GroupLightGreen = Color(0xFFCFFDDC)
private val GroupBackground = Color(0xFFF8FBF9)
private val GroupText = Color(0xFF253D2C)
private val GroupSecondary = Color(0xFF6B756E)

@Composable
fun GroupChatScreen(
    group: com.ayesha.guidechat.data.GroupModel,
    currentUserId: String,
    onBack: () -> Unit
) {

    val repository = remember {
        GroupRepository()
    }

    var messages by remember {
        mutableStateOf<List<GroupMessage>>(emptyList())
    }

    var messageText by remember {
        mutableStateOf("")
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    val listState = rememberLazyListState()


    // LISTEN FOR GROUP MESSAGES


    DisposableEffect(group.id) {

        val listener =
            repository.listenToGroupMessages(
                groupId = group.id,

                onMessagesChanged = { result ->

                    messages = result

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


    // SCROLL TO LATEST MESSAGE


    LaunchedEffect(messages.size) {

        if (messages.isNotEmpty()) {

            listState.animateScrollToItem(
                messages.lastIndex
            )
        }
    }

    Scaffold(
        containerColor = GroupBackground,

        topBar = {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
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
                            Icons.Default.ArrowBack,

                        contentDescription =
                            "Back",

                        tint = GroupText
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(GroupLightGreen),

                    contentAlignment =
                        Alignment.Center
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.Group,

                        contentDescription =
                            "Group",

                        tint = GroupGreen
                    )
                }

                Spacer(
                    modifier =
                        Modifier.size(12.dp)
                )

                Column {

                    Text(
                        text =
                            group.name.ifBlank {
                                "Group"
                            },

                        fontSize = 17.sp,

                        fontWeight =
                            FontWeight.Bold,

                        color = GroupText
                    )

                    Text(
                        text =
                            "${group.memberIds.size} members",

                        fontSize = 12.sp,

                        color = GroupSecondary
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
                    .imePadding()
                    .padding(10.dp),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                OutlinedTextField(

                    value = messageText,

                    onValueChange = {
                        messageText = it
                    },

                    modifier =
                        Modifier.weight(1f),

                    placeholder = {
                        Text("Type a message...")
                    },

                    singleLine = true,

                    shape =
                        RoundedCornerShape(22.dp),

                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor =
                                GroupGreen,

                            unfocusedBorderColor =
                                Color(0xFFD5DED7),

                            cursorColor =
                                GroupGreen
                        )
                )

                Spacer(
                    modifier =
                        Modifier.size(8.dp)
                )

                IconButton(

                    onClick = {

                        val text =
                            messageText.trim()

                        if (text.isEmpty()) {
                            return@IconButton
                        }

                        repository.sendGroupMessage(

                            groupId =
                                group.id,

                            senderId =
                                currentUserId,

                            text =
                                text,

                            onSuccess = {

                                messageText = ""
                            },

                            onError = { error ->

                                errorMessage = error
                            }
                        )
                    },

                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(GroupGreen)
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.Send,

                        contentDescription =
                            "Send",

                        tint = Color.White
                    )
                }
            }
        }

    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            if (errorMessage != null) {

                Text(
                    text =
                        errorMessage ?: "",

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(12.dp),

                    color =
                        Color(0xFFB3261E),

                    fontSize = 13.sp
                )
            }

            if (messages.isEmpty()) {

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
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(
                                    GroupLightGreen
                                ),

                            contentAlignment =
                                Alignment.Center
                        ) {

                            Icon(
                                imageVector =
                                    Icons.Default.Group,

                                contentDescription =
                                    "Group",

                                tint =
                                    GroupGreen,

                                modifier =
                                    Modifier.size(38.dp)
                            )
                        }

                        Spacer(
                            modifier =
                                Modifier.height(16.dp)
                        )

                        Text(
                            text =
                                "Start the group conversation",

                            fontSize = 18.sp,

                            fontWeight =
                                FontWeight.Bold,

                            color =
                                GroupText
                        )

                        Spacer(
                            modifier =
                                Modifier.height(5.dp)
                        )

                        Text(
                            text =
                                "Send the first message to the group.",

                            fontSize = 13.sp,

                            color =
                                GroupSecondary
                        )
                    }
                }

            } else {

                LazyColumn(

                    state = listState,

                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(
                                horizontal = 14.dp
                            ),

                    verticalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {

                    items(
                        items = messages,

                        key = { message ->
                            message.id
                        }
                    ) { message ->

                        GroupMessageBubble(
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
}



// MESSAGE BUBBLE


@Composable
private fun GroupMessageBubble(
    message: GroupMessage,
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
            modifier =
                Modifier
                    .clip(
                        RoundedCornerShape(
                            18.dp
                        )
                    )
                    .background(
                        if (isMine) {
                            GroupGreen
                        } else {
                            Color.White
                        }
                    )
                    .padding(
                        horizontal = 14.dp,
                        vertical = 10.dp
                    )
        ) {

            if (!isMine) {

                Text(
                    text =
                        "Member",

                    fontSize = 11.sp,

                    fontWeight =
                        FontWeight.Bold,

                    color =
                        GroupGreen
                )

                Spacer(
                    modifier =
                        Modifier.height(3.dp)
                )
            }

            Text(
                text =
                    message.text,

                fontSize = 15.sp,

                color =
                    if (isMine) {
                        Color.White
                    } else {
                        GroupText
                    }
            )
        }
    }
}