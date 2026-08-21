package com.ayesha.guidechat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DarkGreen = Color(0xFF2E6F40)
private val MediumGreen = Color(0xFF68BA7F)
private val LightMint = Color(0xFFCFFDDC)
private val DarkText = Color(0xFF253D2C)
private val Background = Color(0xFFF7FAF7)

data class ChatPreview(
    val name: String,
    val message: String,
    val time: String,
    val unreadCount: Int = 0,
    val isOnline: Boolean = false,
    val isGroup: Boolean = false
)

@Composable
fun HomeScreen(
    userName: String = "Ayesha",
    onChatClick: (ChatPreview) -> Unit = {},
    onNewChatClick: () -> Unit = {}
) {

    var searchText by remember {
        mutableStateOf("")
    }

    var selectedBottomItem by remember {
        mutableIntStateOf(0)
    }

    val conversations = remember {
        listOf(
            ChatPreview(
                name = "Sarah Khan",
                message = "See you tomorrow!",
                time = "10:42 AM",
                unreadCount = 2,
                isOnline = true
            ),
            ChatPreview(
                name = "Ali Ahmed",
                message = "Thanks for the help!",
                time = "09:31 AM",
                isOnline = true
            ),
            ChatPreview(
                name = "Development Team",
                message = "Meeting at 3 PM",
                time = "08:45 AM",
                unreadCount = 4,
                isGroup = true
            ),
            ChatPreview(
                name = "Hassan Raza",
                message = "I've sent the documents.",
                time = "Yesterday",
                isOnline = false
            ),
            ChatPreview(
                name = "UI/UX Mentors",
                message = "New resources available",
                time = "Yesterday",
                isGroup = true,
                unreadCount = 1
            )
        )
    }

    val filteredConversations = conversations.filter { chat ->

        chat.name.contains(
            searchText,
            ignoreCase = true
        )
    }

    Scaffold(
        containerColor = Background,

        bottomBar = {

            NavigationBar(
                containerColor = Color.White
            ) {

                NavigationBarItem(
                    selected = selectedBottomItem == 0,
                    onClick = {
                        selectedBottomItem = 0
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.ChatBubble,
                            contentDescription = "Chats"
                        )
                    },
                    label = {
                        Text("Chats")
                    }
                )

                NavigationBarItem(
                    selected = selectedBottomItem == 1,
                    onClick = {
                        selectedBottomItem = 1
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = "Groups"
                        )
                    },
                    label = {
                        Text("Groups")
                    }
                )

                NavigationBarItem(
                    selected = selectedBottomItem == 2,
                    onClick = {
                        selectedBottomItem = 2
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile"
                        )
                    },
                    label = {
                        Text("Me")
                    }
                )
            }
        },

        floatingActionButton = {

            FloatingActionButton(
                onClick = onNewChatClick,
                containerColor = DarkGreen,
                contentColor = Color.White
            ) {

                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New chat"
                )
            }
        }

    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            // Header

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Column {

                    Text(
                        text = "Good morning 👋",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )

                    Spacer(
                        modifier = Modifier.height(3.dp)
                    )

                    Text(
                        text = userName,
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(LightMint),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = userName
                            .firstOrNull()
                            ?.uppercase()
                            ?: "A",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkGreen
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(22.dp)
            )

            // Search

            TextField(
                value = searchText,
                onValueChange = {
                    searchText = it
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),

                placeholder = {
                    Text(
                        text = "Search conversations..."
                    )
                },

                leadingIcon = {

                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },

                singleLine = true,

                shape = RoundedCornerShape(16.dp),

                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,

                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            Spacer(
                modifier = Modifier.height(26.dp)
            )

            // Messages header

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = "Messages",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )

                IconButton(
                    onClick = {}
                ) {

                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = DarkText
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            // Chat list

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                items(filteredConversations) { chat ->

                    ChatListItem(
                        chat = chat,
                        onClick = {
                            onChatClick(chat)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatListItem(
    chat: ChatPreview,
    onClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(18.dp)
            )
            .background(Color.White)
            .clickable {
                onClick()
            }
            .padding(14.dp),

        verticalAlignment = Alignment.CenterVertically
    ) {

        // Avatar

        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(
                    if (chat.isGroup) {
                        LightMint
                    } else {
                        Color(0xFFE8F4EA)
                    }
                ),

            contentAlignment = Alignment.Center
        ) {

            if (chat.isGroup) {

                Icon(
                    imageVector = Icons.Default.Groups,
                    contentDescription = "Group",
                    tint = DarkGreen
                )

            } else {

                Text(
                    text = chat.name
                        .firstOrNull()
                        ?.uppercase()
                        ?: "?",

                    fontSize = 18.sp,

                    fontWeight = FontWeight.Bold,

                    color = DarkGreen
                )
            }

            if (chat.isOnline && !chat.isGroup) {

                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(MediumGreen)
                        .align(Alignment.BottomEnd)
                )
            }
        }

        Spacer(
            modifier = Modifier.size(13.dp)
        )

        // Chat information

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = chat.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = chat.message,
                fontSize = 13.sp,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(
            modifier = Modifier.size(8.dp)
        )

        // Time + unread count

        Column(
            horizontalAlignment = Alignment.End
        ) {

            Text(
                text = chat.time,
                fontSize = 11.sp,
                color = Color.Gray
            )

            if (chat.unreadCount > 0) {

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(DarkGreen),

                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = chat.unreadCount.toString(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}