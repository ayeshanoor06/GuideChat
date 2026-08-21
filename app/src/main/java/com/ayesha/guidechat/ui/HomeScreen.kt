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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DarkGreen = Color(0xFF2E6F40)
private val MediumGreen = Color(0xFF68BA7F)
private val LightMint = Color(0xFFCFFDDC)
private val DarkText = Color(0xFF253D2C)
private val Background = Color(0xFFF7FAF7)

data class ChatPreview(
    val userId: String = "",
    val name: String,
    val message: String,
    val time: String,
    val unreadCount: Int = 0,
    val isOnline: Boolean = false,
    val isGroup: Boolean = false
)

@Composable
fun HomeScreen(
    userName: String,
    onChatClick: (ChatPreview) -> Unit,
    onNewChatClick: () -> Unit,
    onSearchClick: () -> Unit
) {

    var selectedBottomItem by remember {
        mutableIntStateOf(0)
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
                    contentDescription = "New conversation"
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


            // HEADER


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
                            ?: "?",

                        fontSize = 18.sp,

                        fontWeight = FontWeight.Bold,

                        color = DarkGreen
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(22.dp)
            )


            // SEARCH


            TextField(
                value = "",

                onValueChange = {
                    // We open the real Firebase search screen.
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clickable {
                        onSearchClick()
                    },

                enabled = false,

                placeholder = {
                    Text(
                        text = "Search interns & mentors..."
                    )
                },

                leadingIcon = {

                    Icon(
                        imageVector = Icons.Default.Search,

                        contentDescription = "Search",

                        tint = DarkGreen
                    )
                },

                singleLine = true,

                shape = RoundedCornerShape(16.dp),

                colors = TextFieldDefaults.colors(

                    disabledContainerColor = Color.White,

                    disabledTextColor = DarkText,

                    disabledPlaceholderColor = Color.Gray,

                    disabledLeadingIconColor = DarkGreen,

                    disabledIndicatorColor = Color.Transparent
                )
            )

            Spacer(
                modifier = Modifier.height(26.dp)
            )


            // MESSAGES HEADER


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
                    onClick = {
                        // Later: message options
                    }
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


            // EMPTY CONVERSATION STATE


            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),

                contentAlignment = Alignment.Center
            ) {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(LightMint),

                        contentAlignment = Alignment.Center
                    ) {

                        Icon(
                            imageVector = Icons.Default.ChatBubble,

                            contentDescription = null,

                            tint = DarkGreen,

                            modifier = Modifier.size(42.dp)
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(22.dp)
                    )

                    Text(
                        text = "No conversations yet",

                        fontSize = 21.sp,

                        fontWeight = FontWeight.Bold,

                        color = DarkText
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = "Start a conversation with an intern or mentor.",

                        fontSize = 14.sp,

                        color = Color.Gray
                    )

                    Spacer(
                        modifier = Modifier.height(20.dp)
                    )

                    Text(
                        text = "Tap + to find someone",

                        fontSize = 14.sp,

                        fontWeight = FontWeight.Medium,

                        color = DarkGreen
                    )
                }
            }
        }
    }
}