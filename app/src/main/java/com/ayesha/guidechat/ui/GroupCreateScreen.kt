package com.ayesha.guidechat.ui

import android.widget.Toast
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.ayesha.guidechat.data.GroupRepository
import com.ayesha.guidechat.data.UserRepository
import com.ayesha.guidechat.model.UserProfile

private val GuideGreen = Color(0xFF2E6F40)
private val GuideLightGreen = Color(0xFFCFFDDC)
private val GuideBackground = Color(0xFFF8FBF9)
private val GuideText = Color(0xFF253D2C)
private val GuideSecondary = Color(0xFF6B756E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupCreateScreen(
    currentUserId: String,
    onBack: () -> Unit,
    onGroupCreated: (String, String, List<String>) -> Unit
) {

    val context = LocalContext.current


    // REPOSITORIES


    val userRepository = remember {
        UserRepository()
    }

    val groupRepository = remember {
        GroupRepository()
    }


    // STATES


    var groupName by remember {
        mutableStateOf("")
    }

    var users by remember {
        mutableStateOf<List<UserProfile>>(emptyList())
    }

    val selectedUsers = remember {
        mutableStateListOf<String>()
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var isCreatingGroup by remember {
        mutableStateOf(false)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }


    // LOAD USERS


    LaunchedEffect(Unit) {

        isLoading = true
        errorMessage = null

        userRepository.searchUsers(
            searchText = "",
            currentUserId = currentUserId,

            onSuccess = { result ->

                users = result

                isLoading = false
            },

            onError = { error ->

                isLoading = false
                errorMessage = error
            }
        )
    }


    // SCREEN


    Scaffold(

        containerColor = GuideBackground,

        topBar = {

            TopAppBar(

                title = {

                    Text(
                        text = "Create Group",
                        color = GuideText,
                        fontWeight = FontWeight.Bold
                    )
                },

                navigationIcon = {

                    IconButton(
                        onClick = onBack,
                        enabled = !isCreatingGroup
                    ) {

                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = GuideText
                        )
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GuideBackground
                )
            )
        }

    ) { paddingValues ->

        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {

            Spacer(
                modifier = Modifier.height(10.dp)
            )


            // GROUP ICON


            Box(

                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(GuideLightGreen)
                    .align(Alignment.CenterHorizontally),

                contentAlignment = Alignment.Center

            ) {

                Icon(

                    imageVector = Icons.Default.Group,

                    contentDescription = "Group",

                    tint = GuideGreen,

                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )


            // GROUP INFORMATION


            Text(
                text = "Group Information",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = GuideText
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )


            // GROUP NAME


            OutlinedTextField(

                value = groupName,

                onValueChange = {
                    groupName = it
                },

                modifier = Modifier.fillMaxWidth(),

                singleLine = true,

                enabled = !isCreatingGroup,

                label = {
                    Text("Group Name")
                },

                placeholder = {
                    Text("e.g. Android Interns")
                },

                shape = RoundedCornerShape(14.dp),

                colors = OutlinedTextFieldDefaults.colors(

                    focusedBorderColor = GuideGreen,

                    unfocusedBorderColor =
                        Color(0xFFD5DED7),

                    focusedLabelColor = GuideGreen,

                    cursorColor = GuideGreen
                )
            )

            Spacer(
                modifier = Modifier.height(22.dp)
            )


            // MEMBER TITLE


            Row(

                modifier = Modifier.fillMaxWidth(),

                verticalAlignment =
                    Alignment.CenterVertically

            ) {

                Text(

                    text = "Select Members",

                    fontSize = 20.sp,

                    fontWeight =
                        FontWeight.Bold,

                    color = GuideText,

                    modifier =
                        Modifier.weight(1f)
                )

                Text(

                    text =
                        "${selectedUsers.size} selected",

                    fontSize = 13.sp,

                    color = GuideGreen,

                    fontWeight =
                        FontWeight.SemiBold
                )
            }

            Spacer(
                modifier = Modifier.height(10.dp)
            )


            // LOADING


            if (isLoading) {

                Box(

                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),

                    contentAlignment =
                        Alignment.Center

                ) {

                    CircularProgressIndicator(
                        color = GuideGreen
                    )
                }

            } else if (errorMessage != null) {


                // ERROR


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

                        Text(

                            text =
                                errorMessage
                                    ?: "Unable to load users",

                            color =
                                Color(0xFFB3261E),

                            fontSize = 14.sp
                        )

                        Spacer(
                            modifier =
                                Modifier.height(10.dp)
                        )

                        Text(

                            text =
                                "Please check your internet connection.",

                            color =
                                GuideSecondary,

                            fontSize = 13.sp
                        )
                    }
                }

            } else if (users.isEmpty()) {


                // NO USERS


                Box(

                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),

                    contentAlignment =
                        Alignment.Center

                ) {

                    Text(

                        text =
                            "No other users found.",

                        color =
                            GuideSecondary,

                        fontSize = 14.sp
                    )
                }

            } else {

                // USER LIST


                LazyColumn(

                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),

                    verticalArrangement =
                        Arrangement.spacedBy(10.dp)

                ) {

                    items(

                        items = users,

                        key = { user ->
                            user.uid
                        }

                    ) { user ->

                        val isSelected =
                            selectedUsers.contains(
                                user.uid
                            )

                        UserSelectionCard(

                            user = user,

                            isSelected =
                                isSelected,

                            onClick = {

                                if (isCreatingGroup) {
                                    return@UserSelectionCard
                                }

                                if (isSelected) {

                                    selectedUsers.remove(
                                        user.uid
                                    )

                                } else {

                                    selectedUsers.add(
                                        user.uid
                                    )
                                }
                            }
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )


            // CREATE GROUP BUTTON


            Button(

                onClick = {

                    if (isCreatingGroup) {
                        return@Button
                    }


                    // VALIDATE GROUP NAME


                    if (groupName.trim().isEmpty()) {

                        Toast.makeText(
                            context,
                            "Please enter a group name",
                            Toast.LENGTH_SHORT
                        ).show()

                        return@Button
                    }


                    // VALIDATE MEMBERS


                    if (selectedUsers.isEmpty()) {

                        Toast.makeText(
                            context,
                            "Please select at least one member",
                            Toast.LENGTH_SHORT
                        ).show()

                        return@Button
                    }


                    // START CREATION


                    isCreatingGroup = true

                    val memberIds =
                        selectedUsers.toList()


                    // CREATE REAL FIRESTORE GROUP

                    groupRepository.createGroup(

                        name = groupName.trim(),

                        createdBy = currentUserId,

                        memberIds = memberIds,

                        onSuccess = { groupId ->

                            isCreatingGroup = false

                            Toast.makeText(
                                context,
                                "Group created successfully!",
                                Toast.LENGTH_SHORT
                            ).show()

                            // ---------------------------------
                            // SEND REAL GROUP ID BACK
                            // ---------------------------------

                            val allMemberIds =
                                (memberIds + currentUserId)
                                    .distinct()

                            onGroupCreated(

                                groupId,

                                groupName.trim(),

                                allMemberIds
                            )
                        },

                        onError = { error ->

                            isCreatingGroup = false

                            Toast.makeText(
                                context,
                                error,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    )
                },

                enabled =
                    !isCreatingGroup,

                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),

                shape =
                    RoundedCornerShape(14.dp),

                colors =
                    ButtonDefaults.buttonColors(

                        containerColor =
                            GuideGreen,

                        contentColor =
                            Color.White,

                        disabledContainerColor =
                            Color(0xFF9BBBA4)
                    )

            ) {

                if (isCreatingGroup) {

                    CircularProgressIndicator(

                        modifier =
                            Modifier.size(22.dp),

                        color =
                            Color.White,

                        strokeWidth =
                            2.dp
                    )

                    Spacer(
                        modifier =
                            Modifier.size(10.dp)
                    )

                    Text(
                        text = "CREATING...",
                        fontSize = 14.sp,
                        fontWeight =
                            FontWeight.Bold
                    )

                } else {

                    Text(
                        text = "CREATE GROUP",
                        fontSize = 14.sp,
                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )
        }
    }
}



// USER SELECTION CARD


@Composable
private fun UserSelectionCard(

    user: UserProfile,

    isSelected: Boolean,

    onClick: () -> Unit

) {

    Card(

        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },

        shape =
            RoundedCornerShape(16.dp),

        colors =
            CardDefaults.cardColors(

                containerColor =
                    if (isSelected) {
                        Color(0xFFE7F8EC)
                    } else {
                        Color.White
                    }
            ),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )

    ) {

        Row(

            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),

            verticalAlignment =
                Alignment.CenterVertically

        ) {


            // AVATAR


            Box(

                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(

                        if (isSelected) {
                            GuideGreen
                        } else {
                            GuideLightGreen
                        }
                    ),

                contentAlignment =
                    Alignment.Center

            ) {

                Text(

                    text =
                        user.name
                            .trim()
                            .firstOrNull()
                            ?.uppercase()
                            ?: "U",

                    color =
                        if (isSelected) {
                            Color.White
                        } else {
                            GuideGreen
                        },

                    fontSize = 20.sp,

                    fontWeight =
                        FontWeight.Bold
                )
            }

            Spacer(
                modifier =
                    Modifier.size(14.dp)
            )


            // USER DETAILS


            Column(

                modifier =
                    Modifier.weight(1f)

            ) {

                Text(

                    text =
                        user.name
                            .ifBlank {
                                "Unnamed User"
                            },

                    color =
                        GuideText,

                    fontSize = 16.sp,

                    fontWeight =
                        FontWeight.SemiBold
                )

                Spacer(
                    modifier =
                        Modifier.height(3.dp)
                )

                Text(

                    text =
                        user.role
                            .ifBlank {
                                "User"
                            }
                            .replaceFirstChar {
                                it.uppercase()
                            },

                    color =
                        GuideSecondary,

                    fontSize = 13.sp
                )

                if (user.email.isNotBlank()) {

                    Spacer(
                        modifier =
                            Modifier.height(2.dp)
                    )

                    Text(

                        text =
                            user.email,

                        color =
                            GuideSecondary,

                        fontSize = 12.sp
                    )
                }
            }


            // SELECTION INDICATOR


            Box(

                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(

                        if (isSelected) {
                            GuideGreen
                        } else {
                            Color(0xFFE7ECE8)
                        }
                    ),

                contentAlignment =
                    Alignment.Center

            ) {

                if (isSelected) {

                    Icon(

                        imageVector =
                            Icons.Default.Check,

                        contentDescription =
                            "Selected",

                        tint =
                            Color.White,

                        modifier =
                            Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}