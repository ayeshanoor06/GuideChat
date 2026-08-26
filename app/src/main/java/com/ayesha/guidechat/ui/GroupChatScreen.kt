package com.ayesha.guidechat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Group
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
import com.ayesha.guidechat.data.GroupModel
import com.ayesha.guidechat.data.GroupRepository
import com.ayesha.guidechat.data.UserRepository
import com.ayesha.guidechat.model.UserProfile
import kotlinx.coroutines.delay

private val GroupGreen =
    Color(0xFF2E6F40)

private val GroupLightGreen =
    Color(0xFFCFFDDC)

private val GroupBackground =
    Color(0xFFF8FBF9)

private val GroupText =
    Color(0xFF253D2C)

private val GroupSecondary =
    Color(0xFF6B756E)

private val ReadBlue =
    Color(0xFF2196F3)


@Composable
fun GroupChatScreen(
    group: GroupModel,
    currentUserId: String,
    onBack: () -> Unit
) {

    val context =
        androidx.compose.ui.platform.LocalContext.current

    val groupRepository =
        remember {
            GroupRepository(context)
        }

    val userRepository =
        remember {
            UserRepository()
        }


    // =========================================================
    // STATE
    // =========================================================

    var messages by remember {
        mutableStateOf<List<GroupMessage>>(
            emptyList()
        )
    }

    var messageText by remember {
        mutableStateOf("")
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    var usersById by remember {
        mutableStateOf<Map<String, UserProfile>>(
            emptyMap()
        )
    }

    var typingUsers by remember {
        mutableStateOf<List<String>>(
            emptyList()
        )
    }

    val listState =
        rememberLazyListState()


    // =========================================================
    // LOAD GROUP MEMBERS
    // =========================================================

    LaunchedEffect(
        group.id,
        group.memberIds
    ) {

        val loadedUsers =
            mutableMapOf<String, UserProfile>()

        group.memberIds.forEach { memberId ->

            userRepository.getUserProfile(

                uid =
                    memberId,

                onSuccess = { profile ->

                    loadedUsers[
                        memberId
                    ] = profile

                    usersById =
                        loadedUsers.toMap()
                },

                onError = {
                    // UID fallback will be used.
                }
            )
        }
    }


    // =========================================================
    // LISTEN TO GROUP MESSAGES
    // =========================================================

    DisposableEffect(
        group.id
    ) {

        val listener =
            groupRepository.listenToGroupMessages(

                groupId =
                    group.id,

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
    // LISTEN TO GROUP TYPING
    // =========================================================

    DisposableEffect(
        group.id,
        currentUserId
    ) {

        val typingListener =
            groupRepository.listenToGroupTyping(

                groupId =
                    group.id,

                currentUserId =
                    currentUserId,

                onTypingUsersChanged = {
                        users ->

                    typingUsers =
                        users
                },

                onError = {
                    // Typing errors should not
                    // interrupt the group chat.
                }
            )

        onDispose {

            typingListener.remove()

            groupRepository.clearTypingStatus(

                groupId =
                    group.id,

                userId =
                    currentUserId
            )
        }
    }


    // =========================================================
    // MARK INCOMING MESSAGES AS READ
    // =========================================================

    LaunchedEffect(
        messages,
        currentUserId
    ) {

        messages.forEach { message ->

            if (
                message.senderId !=
                currentUserId &&
                !message.readBy.contains(
                    currentUserId
                )
            ) {

                groupRepository.markGroupMessageAsRead(

                    groupId =
                        group.id,

                    messageId =
                        message.id,

                    userId =
                        currentUserId
                )
            }
        }
    }


    // =========================================================
    // AUTOMATICALLY STOP TYPING
    // =========================================================

    LaunchedEffect(
        messageText
    ) {

        if (messageText.isBlank()) {

            groupRepository.setTyping(

                groupId =
                    group.id,

                userId =
                    currentUserId,

                isTyping =
                    false
            )

            return@LaunchedEffect
        }


        // User is typing.

        groupRepository.setTyping(

            groupId =
                group.id,

            userId =
                currentUserId,

            isTyping =
                true
        )


        delay(1500)


        // User stopped typing.

        groupRepository.setTyping(

            groupId =
                group.id,

            userId =
                currentUserId,

            isTyping =
                false
        )
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
    // TYPING TEXT
    // =========================================================

    val typingText: String? =

        when {

            typingUsers.isEmpty() ->
                null

            typingUsers.size == 1 -> {

                val user =
                    usersById[
                        typingUsers.first()
                    ]

                "${user?.name ?: "Someone"} is typing..."
            }

            typingUsers.size == 2 -> {

                val first =
                    usersById[
                        typingUsers[0]
                    ]?.name
                        ?: "Someone"

                val second =
                    usersById[
                        typingUsers[1]
                    ]?.name
                        ?: "Someone"

                "$first and $second are typing..."
            }

            else ->
                "${typingUsers.size} people are typing..."
        }


    // =========================================================
    // SCREEN
    // =========================================================

    Scaffold(

        containerColor =
            GroupBackground,


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

                IconButton(
                    onClick = onBack
                ) {

                    Icon(

                        imageVector =
                            Icons.AutoMirrored.Filled.ArrowBack,

                        contentDescription =
                            "Back",

                        tint =
                            GroupText
                    )
                }


                Box(

                    modifier =
                        Modifier
                            .size(44.dp)
                            .clip(
                                CircleShape
                            )
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
                            GroupGreen
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

                        fontSize =
                            17.sp,

                        fontWeight =
                            FontWeight.Bold,

                        color =
                            GroupText
                    )


                    Text(

                        text =
                            typingText
                                ?: "${group.memberIds.size} members",

                        fontSize =
                            12.sp,

                        fontWeight =
                            if (
                                typingText != null
                            ) {
                                FontWeight.Medium
                            } else {
                                FontWeight.Normal
                            },

                        color =
                            if (
                                typingText != null
                            ) {
                                GroupGreen
                            } else {
                                GroupSecondary
                            }
                    )
                }
            }
        },


        // =====================================================
        // MESSAGE INPUT
        // =====================================================

        bottomBar = {

            Column {

                // =================================================
                // TYPING INDICATOR
                // =================================================

                if (typingText != null) {

                    Row(

                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .background(
                                    Color.White
                                )
                                .padding(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = 5.dp,
                                    bottom = 3.dp
                                )
                    ) {

                        Text(

                            text =
                                typingText,

                            fontSize =
                                12.sp,

                            fontWeight =
                                FontWeight.Medium,

                            color =
                                GroupGreen
                        )
                    }
                }


                Row(

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(
                                Color.White
                            )
                            .navigationBarsPadding()
                            .imePadding()
                            .padding(
                                10.dp
                            ),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    OutlinedTextField(

                        value =
                            messageText,

                        onValueChange = {

                            messageText =
                                it

                            groupRepository.setTyping(

                                groupId =
                                    group.id,

                                userId =
                                    currentUserId,

                                isTyping =
                                    it.isNotBlank()
                            )
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


                            // Stop typing.

                            groupRepository.clearTypingStatus(

                                groupId =
                                    group.id,

                                userId =
                                    currentUserId
                            )


                            groupRepository.sendGroupMessage(

                                groupId =
                                    group.id,

                                senderId =
                                    currentUserId,

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

                        enabled =
                            messageText.isNotBlank(),

                        modifier =
                            Modifier
                                .size(48.dp)
                                .clip(
                                    CircleShape
                                )
                                .background(

                                    if (
                                        messageText.isNotBlank()
                                    ) {
                                        GroupGreen
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

    ) { paddingValues ->


        Column(

            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        paddingValues
                    )
        ) {


            // =================================================
            // ERROR
            // =================================================

            if (errorMessage != null) {

                Text(

                    text =
                        errorMessage ?: "",

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                12.dp
                            ),

                    color =
                        Color(0xFFB3261E),

                    fontSize =
                        13.sp
                )
            }


            // =================================================
            // EMPTY CHAT
            // =================================================

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

                            modifier =
                                Modifier
                                    .size(80.dp)
                                    .clip(
                                        CircleShape
                                    )
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

                            fontSize =
                                18.sp,

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

                            fontSize =
                                13.sp,

                            color =
                                GroupSecondary
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


                        val sender =
                            usersById[
                                message.senderId
                            ]


                        val senderName =

                            if (
                                message.senderId ==
                                currentUserId
                            ) {

                                "You"

                            } else {

                                sender
                                    ?.name
                                    ?.ifBlank {
                                        "Member"
                                    }
                                    ?: "Member"
                            }


                        val isMine =
                            message.senderId ==
                                    currentUserId


                        // Everyone except sender.

                        val otherMembers =
                            group.memberIds
                                .filter {
                                    it !=
                                            message.senderId
                                }


                        // True only when EVERY
                        // other member has read it.

                        val allMembersRead =
                            otherMembers.isNotEmpty() &&
                                    otherMembers.all {
                                        message.readBy.contains(
                                            it
                                        )
                                    }


                        GroupMessageBubble(

                            message =
                                message,

                            senderName =
                                senderName,

                            isMine =
                                isMine,

                            allMembersRead =
                                allMembersRead
                        )
                    }
                }
            }
        }
    }
}


// =============================================================
// GROUP MESSAGE BUBBLE
// =============================================================

@Composable
private fun GroupMessageBubble(

    message: GroupMessage,

    senderName: String,

    isMine: Boolean,

    allMembersRead: Boolean
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


            // =================================================
            // SENDER NAME
            // =================================================

            Text(

                text =
                    senderName,

                fontSize =
                    11.sp,

                fontWeight =
                    FontWeight.Bold,

                color =
                    if (isMine) {
                        GroupLightGreen
                    } else {
                        GroupGreen
                    }
            )


            Spacer(
                modifier =
                    Modifier.height(3.dp)
            )


            // =================================================
            // MESSAGE
            // =================================================

            Text(

                text =
                    message.text,

                fontSize =
                    15.sp,

                color =
                    if (isMine) {
                        Color.White
                    } else {
                        GroupText
                    }
            )


            // =================================================
            // GROUP READ RECEIPT
            // =================================================

            if (isMine) {

                Spacer(
                    modifier =
                        Modifier.height(3.dp)
                )


                Row(

                    modifier =
                        Modifier.align(
                            Alignment.End
                        ),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {


                    // FIRST TICK

                    Icon(

                        imageVector =
                            Icons.Default.Done,

                        contentDescription =
                            "Message sent",

                        modifier =
                            Modifier.size(
                                13.dp
                            ),

                        tint =
                            if (allMembersRead) {
                                ReadBlue
                            } else {
                                GroupLightGreen
                            }
                    )


                    // SECOND TICK

                    Icon(

                        imageVector =
                            Icons.Default.Done,

                        contentDescription =
                            if (
                                allMembersRead
                            ) {
                                "Read by everyone"
                            } else {
                                "Delivered"
                            },

                        modifier =
                            Modifier
                                .size(
                                    13.dp
                                )
                                .padding(
                                    start = 0.dp
                                ),

                        tint =
                            if (allMembersRead) {
                                ReadBlue
                            } else {
                                GroupLightGreen
                            }
                    )
                }
            }
        }
    }
}