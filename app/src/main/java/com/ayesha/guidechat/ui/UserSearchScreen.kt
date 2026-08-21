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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ayesha.guidechat.model.UserProfile
import com.ayesha.guidechat.data.UserRepository

private val Green = androidx.compose.ui.graphics.Color(0xFF2E6F40)
private val LightGreen = androidx.compose.ui.graphics.Color(0xFFCFFDDC)
private val Background = androidx.compose.ui.graphics.Color(0xFFF7FAF8)

@androidx.compose.runtime.Composable
fun UserSearchScreen(
    currentUserId: String,
    onBack: () -> Unit,
    onUserSelected: (UserProfile) -> Unit
) {

    var searchText by remember {
        mutableStateOf("")
    }

    var users by remember {
        mutableStateOf<List<UserProfile>>(emptyList())
    }

    var isLoading by remember {
        mutableStateOf(false)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    val repository = remember {
        UserRepository()
    }

    fun performSearch() {

        isLoading = true
        errorMessage = null

        repository.searchUsers(
            searchText = searchText,
            currentUserId = currentUserId,

            onSuccess = { result ->

                users = result
                isLoading = false
            },

            onError = { error ->

                users = emptyList()
                errorMessage = error
                isLoading = false
            }
        )
    }

    LaunchedEffect(Unit) {
        performSearch()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(20.dp)
    ) {

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(
                onClick = onBack
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Green
                )
            }

            Text(
                text = "New Conversation",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Green,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        // Search field
        OutlinedTextField(
            value = searchText,

            onValueChange = { value ->

                searchText = value

                // Search whenever the user changes the text
                performSearch()
            },

            modifier = Modifier.fillMaxWidth(),

            singleLine = true,

            placeholder = {
                Text(
                    text = "Search by name or email..."
                )
            },

            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Green
                )
            },

            shape = RoundedCornerShape(16.dp)
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Text(
            text = "Interns & Mentors",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Green
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        when {

            isLoading -> {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {

                    CircularProgressIndicator(
                        color = Green
                    )
                }
            }

            errorMessage != null -> {

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 15.sp
                    )
                }
            }

            users.isEmpty() -> {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 60.dp),
                    contentAlignment = Alignment.Center
                ) {

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text(
                            text = "No users found",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Green
                        )

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        Text(
                            text = "Try another name or email.",
                            fontSize = 14.sp,
                            color = androidx.compose.ui.graphics.Color.Gray
                        )
                    }
                }
            }

            else -> {

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),

                    verticalArrangement = Arrangement.spacedBy(
                        10.dp
                    )
                ) {

                    items(
                        items = users,
                        key = { user ->
                            user.uid
                        }
                    ) { user ->

                        UserSearchItem(
                            user = user,

                            onClick = {
                                onUserSelected(user)
                            }
                        )
                    }
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun UserSearchItem(
    user: UserProfile,
    onClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(18.dp)
            )
            .background(
                androidx.compose.ui.graphics.Color.White
            )
            .clickable {
                onClick()
            }
            .padding(16.dp),

        verticalAlignment = Alignment.CenterVertically
    ) {

        // Avatar
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(LightGreen),

            contentAlignment = Alignment.Center
        ) {

            val initial =
                user.name
                    .trim()
                    .firstOrNull()
                    ?.uppercase()
                    ?: "?"

            Text(
                text = initial,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                color = Green
            )
        }

        Spacer(
            modifier = Modifier.size(14.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = user.name.ifBlank {
                    "Unnamed User"
                },

                fontSize = 17.sp,

                fontWeight = FontWeight.Bold,

                color = androidx.compose.ui.graphics.Color(
                    0xFF253D2C
                )
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = user.role.ifBlank {
                    "User"
                },

                fontSize = 14.sp,

                color = Green
            )

            if (user.email.isNotBlank()) {

                Spacer(
                    modifier = Modifier.height(2.dp)
                )

                Text(
                    text = user.email,

                    fontSize = 12.sp,

                    color = androidx.compose.ui.graphics.Color.Gray
                )
            }
        }
    }
}