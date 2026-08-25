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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import com.ayesha.guidechat.data.GroupModel
import com.ayesha.guidechat.data.GroupRepository

private val DarkGreen = Color(0xFF2E6F40)
private val LightMint = Color(0xFFCFFDDC)
private val DarkText = Color(0xFF253D2C)
private val Background = Color(0xFFF7FAF7)

@Composable
fun GroupsScreen(
    currentUserId: String,
    onBack: () -> Unit,
    onCreateGroup: () -> Unit,
    onGroupClick: (GroupModel) -> Unit
) {

    val repository = remember {
        GroupRepository()
    }

    var groups by remember {
        mutableStateOf<List<GroupModel>>(emptyList())
    }

    var searchText by remember {
        mutableStateOf("")
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    DisposableEffect(currentUserId) {

        val listener =
            repository.listenToGroups(
                currentUserId = currentUserId,

                onGroupsChanged = {
                    groups = it
                    errorMessage = null
                },

                onError = {
                    errorMessage = it
                }
            )

        onDispose {
            listener.remove()
        }
    }

    val filteredGroups =
        groups.filter {
            it.name.contains(
                searchText,
                ignoreCase = true
            )
        }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {

        Column(
            modifier = Modifier.fillMaxSize()
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

                        tint = DarkGreen
                    )
                }

                Text(
                    text = "Groups",

                    modifier =
                        Modifier.weight(1f),

                    fontSize = 21.sp,

                    fontWeight =
                        FontWeight.Bold,

                    color = DarkText
                )
            }


            // SEARCH


            OutlinedTextField(
                value = searchText,

                onValueChange = {
                    searchText = it
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 20.dp,
                        vertical = 16.dp
                    ),

                placeholder = {
                    Text("Search groups...")
                },

                leadingIcon = {

                    Icon(
                        imageVector =
                            Icons.Default.Search,

                        contentDescription =
                            "Search",

                        tint = DarkGreen
                    )
                },

                singleLine = true,

                shape =
                    RoundedCornerShape(16.dp)
            )


            // GROUP LIST


            when {

                errorMessage != null -> {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),

                        contentAlignment =
                            Alignment.Center
                    ) {

                        Text(
                            text =
                                errorMessage
                                    ?: "Unable to load groups",

                            color =
                                Color(0xFFB3261E)
                        )
                    }
                }

                filteredGroups.isEmpty() -> {

                    Box(
                        modifier = Modifier
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
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(CircleShape)
                                    .background(LightMint),

                                contentAlignment =
                                    Alignment.Center
                            ) {

                                Icon(
                                    imageVector =
                                        Icons.Default.Groups,

                                    contentDescription =
                                        "No groups",

                                    tint = DarkGreen,

                                    modifier =
                                        Modifier.size(42.dp)
                                )
                            }

                            Spacer(
                                modifier =
                                    Modifier.size(16.dp)
                            )

                            Text(
                                text =
                                    "No groups yet",

                                fontSize = 20.sp,

                                fontWeight =
                                    FontWeight.Bold,

                                color = DarkText
                            )

                            Spacer(
                                modifier =
                                    Modifier.size(6.dp)
                            )

                            Text(
                                text =
                                    "Create a group to start collaborating.",

                                fontSize = 13.sp,

                                color = Color.Gray
                            )
                        }
                    }
                }

                else -> {

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(
                                horizontal = 20.dp
                            ),

                        verticalArrangement =
                            Arrangement.spacedBy(10.dp)
                    ) {

                        items(
                            items = filteredGroups,

                            key = {
                                it.id
                            }
                        ) { group ->

                            GroupListItem(
                                group = group,

                                onClick = {
                                    onGroupClick(group)
                                }
                            )
                        }
                    }
                }
            }
        }


        // CREATE GROUP BUTTON


        FloatingActionButton(
            onClick = onCreateGroup,

            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),

            containerColor = DarkGreen,

            contentColor = Color.White
        ) {

            Icon(
                imageVector =
                    Icons.Default.Add,

                contentDescription =
                    "Create group"
            )
        }
    }
}

@Composable
private fun GroupListItem(
    group: GroupModel,
    onClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(18.dp)
            )
            .background(Color.White)
            .clickable(
                onClick = onClick
            )
            .padding(14.dp),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(LightMint),

            contentAlignment =
                Alignment.Center
        ) {

            Icon(
                imageVector =
                    Icons.Default.Groups,

                contentDescription =
                    "Group",

                tint = DarkGreen,

                modifier =
                    Modifier.size(27.dp)
            )
        }

        Spacer(
            modifier =
                Modifier.size(13.dp)
        )

        Column(
            modifier =
                Modifier.weight(1f)
        ) {

            Text(
                text =
                    group.name,

                fontSize = 16.sp,

                fontWeight =
                    FontWeight.SemiBold,

                color = DarkText
            )

            Spacer(
                modifier =
                    Modifier.size(4.dp)
            )

            Text(
                text =
                    "${group.memberIds.size} members",

                fontSize = 12.sp,

                color = Color.Gray
            )
        }
    }
}