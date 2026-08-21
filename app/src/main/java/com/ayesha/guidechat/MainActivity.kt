package com.ayesha.guidechat

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ayesha.guidechat.data.AuthRepository
import com.ayesha.guidechat.data.ConversationRepository
import com.ayesha.guidechat.data.UserRepository
import com.ayesha.guidechat.model.UserProfile
import com.ayesha.guidechat.ui.ChatPreview
import com.ayesha.guidechat.ui.ChatScreen
import com.ayesha.guidechat.ui.HomeScreen
import com.ayesha.guidechat.ui.UserSearchScreen
import com.ayesha.guidechat.ui.theme.GuideChatTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            GuideChatTheme {
                GuideChatApp()
            }
        }
    }
}

/*
 * GuideChat navigation:
 *
 * login
 *   -> register
 *   -> home
 *   -> userSearch
 *   -> chat
 *
 * The selected user is the real UserProfile loaded from Firestore.
 */
@Composable
fun GuideChatApp() {

    var currentScreen by remember {
        mutableStateOf("login")
    }

    var currentUserProfile by remember {
        mutableStateOf<UserProfile?>(null)
    }

    var selectedUser by remember {
        mutableStateOf<UserProfile?>(null)
    }

    val userRepository = remember {
        UserRepository()
    }

    val conversationRepository = remember {
        ConversationRepository()
    }

    var conversations by remember {
        mutableStateOf<List<ChatPreview>>(emptyList())
    }

    var isLoadingConversations by remember {
        mutableStateOf(false)
    }

    var conversationError by remember {
        mutableStateOf<String?>(null)
    }

    DisposableEffect(currentScreen) {

        val firebaseUser =
            FirebaseAuth.getInstance().currentUser

        if (currentScreen == "home" && firebaseUser != null) {

            isLoadingConversations = true
            conversationError = null

            val listener =
                conversationRepository.listenForConversations(
                    currentUserId = firebaseUser.uid,

                    onConversationsChanged = { result ->
                        conversations = result
                        isLoadingConversations = false
                        conversationError = null
                    },

                    onError = { error ->
                        isLoadingConversations = false
                        conversationError = error
                    }
                )

            onDispose {
                listener.remove()
            }

        } else {

            onDispose { }
        }
    }

    LaunchedEffect(currentScreen) {

        if (currentScreen == "home") {

            val firebaseUser =
                FirebaseAuth.getInstance().currentUser

            if (firebaseUser != null) {

                userRepository.getUserProfile(
                    uid = firebaseUser.uid,

                    onSuccess = { profile ->
                        currentUserProfile = profile
                    },

                    onError = {
                        // Keep the generic "User" fallback.
                        // The user can still continue using the app.
                    }
                )
            }
        }
    }

    when (currentScreen) {

        // =====================================================
        // LOGIN
        // =====================================================

        "login" -> {

            LoginScreen(
                onLoginSuccess = {

                    currentUserProfile = null
                    selectedUser = null
                    currentScreen = "home"
                },

                onCreateAccount = {

                    currentScreen = "register"
                }
            )
        }

        // =====================================================
        // REGISTER
        // =====================================================

        "register" -> {

            RegisterScreen(
                onBackToLogin = {

                    currentScreen = "login"
                }
            )
        }

        // =====================================================
        // HOME
        // =====================================================

        "home" -> {

            val displayName =
                currentUserProfile
                    ?.name
                    ?.trim()
                    ?.takeIf { it.isNotEmpty() }
                    ?: "User"

            HomeScreen(
                userName = displayName,

                conversations = conversations,

                isLoadingConversations =
                    isLoadingConversations,

                conversationError =
                    conversationError,

                onChatClick = { chat ->

                    if (chat.userId.isNotBlank()) {

                        userRepository.getUserProfile(
                            uid = chat.userId,

                            onSuccess = { profile ->
                                selectedUser = profile
                                currentScreen = "chat"
                            },

                            onError = { error ->
                                conversationError =
                                    error ?: "Unable to open chat"
                            }
                        )
                    }
                },

                onNewChatClick = {

                    currentScreen = "userSearch"
                },

                onSearchClick = {

                    currentScreen = "userSearch"
                }
            )
        }

        // =====================================================
        // REAL FIRESTORE USER SEARCH
        // =====================================================

        "userSearch" -> {

            val firebaseUser =
                FirebaseAuth.getInstance().currentUser

            if (firebaseUser != null) {

                UserSearchScreen(

                    currentUserId = firebaseUser.uid,

                    onBack = {

                        currentScreen = "home"
                    },

                    onUserSelected = { user ->

                        selectedUser = user
                        currentScreen = "chat"
                    }
                )

            } else {

                currentScreen = "login"
            }
        }

        // =====================================================
        // ONE-TO-ONE CHAT
        // =====================================================

        "chat" -> {

            val firebaseUser =
                FirebaseAuth.getInstance().currentUser

            val otherUser = selectedUser

            if (
                firebaseUser != null &&
                otherUser != null
            ) {

                ChatScreen(

                    currentUserId = firebaseUser.uid,

                    currentUserName =
                        currentUserProfile
                            ?.name
                            ?.ifBlank { "User" }
                            ?: "User",

                    otherUser = otherUser,

                    onBack = {

                        currentScreen = "userSearch"
                    }
                )

            } else {

                currentScreen = "home"
            }
        }

        // =====================================================
        // FALLBACK
        // =====================================================

        else -> {

            currentScreen = "login"
        }
    }
}

/* =========================================================
   LOGIN SCREEN
   ========================================================= */

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onCreateAccount: () -> Unit
) {

    val context = LocalContext.current

    val authRepository = remember {
        AuthRepository()
    }

    var email by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var passwordVisible by remember {
        mutableStateOf(false)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FBF9))
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),

            horizontalAlignment = Alignment.CenterHorizontally,

            verticalArrangement = Arrangement.Center
        ) {

            // Logo
            Box(
                modifier = Modifier
                    .size(82.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFCFFDDC)),

                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = "G",
                    color = Color(0xFF2E6F40),
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            Text(
                text = "GuideChat",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF253D2C)
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text = "Connect • Collaborate • Grow",
                fontSize = 14.sp,
                color = Color(0xFF68BA7F)
            )

            Spacer(
                modifier = Modifier.height(30.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),

                shape = RoundedCornerShape(24.dp),

                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),

                elevation = CardDefaults.cardElevation(
                    defaultElevation = 5.dp
                )
            ) {

                Column(
                    modifier = Modifier.padding(22.dp)
                ) {

                    Text(
                        text = "Welcome Back 👋",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF253D2C)
                    )

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )

                    Text(
                        text = "Sign in to continue your conversations",
                        fontSize = 14.sp,
                        color = Color(0xFF6B756E)
                    )

                    Spacer(
                        modifier = Modifier.height(22.dp)
                    )

                    OutlinedTextField(
                        value = email,

                        onValueChange = {
                            email = it
                        },

                        modifier = Modifier.fillMaxWidth(),

                        singleLine = true,

                        label = {
                            Text("Email")
                        },

                        placeholder = {
                            Text("Enter your email")
                        },

                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email
                        ),

                        shape = RoundedCornerShape(14.dp),

                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2E6F40),
                            unfocusedBorderColor = Color(0xFFD5DED7),
                            focusedLabelColor = Color(0xFF2E6F40),
                            cursorColor = Color(0xFF2E6F40)
                        )
                    )

                    Spacer(
                        modifier = Modifier.height(14.dp)
                    )

                    OutlinedTextField(
                        value = password,

                        onValueChange = {
                            password = it
                        },

                        modifier = Modifier.fillMaxWidth(),

                        singleLine = true,

                        label = {
                            Text("Password")
                        },

                        placeholder = {
                            Text("Enter your password")
                        },

                        visualTransformation =
                            if (passwordVisible) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },

                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password
                        ),

                        trailingIcon = {

                            TextButton(
                                onClick = {
                                    passwordVisible =
                                        !passwordVisible
                                }
                            ) {

                                Text(
                                    text =
                                        if (passwordVisible) {
                                            "Hide"
                                        } else {
                                            "Show"
                                        },

                                    color = Color(0xFF2E6F40),

                                    fontWeight = FontWeight.Medium
                                )
                            }
                        },

                        shape = RoundedCornerShape(14.dp),

                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2E6F40),
                            unfocusedBorderColor = Color(0xFFD5DED7),
                            focusedLabelColor = Color(0xFF2E6F40),
                            cursorColor = Color(0xFF2E6F40)
                        )
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    TextButton(
                        onClick = {

                            Toast.makeText(
                                context,
                                "Password reset will be added soon",
                                Toast.LENGTH_SHORT
                            ).show()
                        },

                        modifier = Modifier.align(
                            Alignment.End
                        )
                    ) {

                        Text(
                            text = "Forgot password?",
                            color = Color(0xFF2E6F40),
                            fontSize = 13.sp
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Button(
                        onClick = {

                            if (email.isBlank()) {

                                Toast.makeText(
                                    context,
                                    "Please enter your email",
                                    Toast.LENGTH_SHORT
                                ).show()

                                return@Button
                            }

                            if (password.isBlank()) {

                                Toast.makeText(
                                    context,
                                    "Please enter your password",
                                    Toast.LENGTH_SHORT
                                ).show()

                                return@Button
                            }

                            authRepository.login(
                                email = email.trim(),
                                password = password
                            ) { success, error ->

                                if (success) {

                                    Toast.makeText(
                                        context,
                                        "Login successful!",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    onLoginSuccess()

                                } else {

                                    Toast.makeText(
                                        context,
                                        error ?: "Login failed",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        },

                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),

                        shape = RoundedCornerShape(14.dp),

                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2E6F40),
                            contentColor = Color.White
                        )
                    ) {

                        Text(
                            text = "SIGN IN",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(18.dp)
                    )

                    Text(
                        text = "New to GuideChat?",

                        modifier = Modifier.fillMaxWidth(),

                        textAlign = TextAlign.Center,

                        fontSize = 13.sp,

                        color = Color(0xFF6B756E)
                    )

                    TextButton(
                        onClick = onCreateAccount,

                        modifier = Modifier.align(
                            Alignment.CenterHorizontally
                        )
                    ) {

                        Text(
                            text = "Create an account",

                            color = Color(0xFF2E6F40),

                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(22.dp)
            )

            Text(
                text = "Secure communication for interns & mentors",

                fontSize = 12.sp,

                color = Color(0xFF6B756E),

                textAlign = TextAlign.Center
            )
        }
    }
}

/* =========================================================
   REGISTRATION SCREEN
   ========================================================= */

@Composable
fun RegisterScreen(
    onBackToLogin: () -> Unit
) {

    val context = LocalContext.current

    val authRepository = remember {
        AuthRepository()
    }

    val userRepository = remember {
        UserRepository()
    }

    var name by remember {
        mutableStateOf("")
    }

    var email by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var confirmPassword by remember {
        mutableStateOf("")
    }

    var passwordVisible by remember {
        mutableStateOf(false)
    }

    var selectedRole by remember {
        mutableStateOf("Intern")
    }

    var isCreatingAccount by remember {
        mutableStateOf(false)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FBF9))
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),

            horizontalAlignment = Alignment.CenterHorizontally,

            verticalArrangement = Arrangement.Center
        ) {

            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFCFFDDC)),

                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = "G",
                    color = Color(0xFF2E6F40),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            Text(
                text = "Create Account",

                fontSize = 28.sp,

                fontWeight = FontWeight.Bold,

                color = Color(0xFF253D2C)
            )

            Spacer(
                modifier = Modifier.height(5.dp)
            )

            Text(
                text = "Join your internship community",

                fontSize = 14.sp,

                color = Color(0xFF68BA7F)
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),

                shape = RoundedCornerShape(24.dp),

                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),

                elevation = CardDefaults.cardElevation(
                    defaultElevation = 5.dp
                )
            ) {

                Column(
                    modifier = Modifier.padding(22.dp)
                ) {

                    OutlinedTextField(
                        value = name,

                        onValueChange = {
                            name = it
                        },

                        modifier = Modifier.fillMaxWidth(),

                        singleLine = true,

                        label = {
                            Text("Full Name")
                        },

                        placeholder = {
                            Text("Enter your name")
                        },

                        shape = RoundedCornerShape(14.dp),

                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2E6F40),
                            unfocusedBorderColor = Color(0xFFD5DED7),
                            focusedLabelColor = Color(0xFF2E6F40),
                            cursorColor = Color(0xFF2E6F40)
                        )
                    )

                    Spacer(
                        modifier = Modifier.height(13.dp)
                    )

                    OutlinedTextField(
                        value = email,

                        onValueChange = {
                            email = it
                        },

                        modifier = Modifier.fillMaxWidth(),

                        singleLine = true,

                        label = {
                            Text("Email")
                        },

                        placeholder = {
                            Text("Enter your email")
                        },

                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email
                        ),

                        shape = RoundedCornerShape(14.dp),

                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2E6F40),
                            unfocusedBorderColor = Color(0xFFD5DED7),
                            focusedLabelColor = Color(0xFF2E6F40),
                            cursorColor = Color(0xFF2E6F40)
                        )
                    )

                    Spacer(
                        modifier = Modifier.height(13.dp)
                    )

                    OutlinedTextField(
                        value = password,

                        onValueChange = {
                            password = it
                        },

                        modifier = Modifier.fillMaxWidth(),

                        singleLine = true,

                        label = {
                            Text("Password")
                        },

                        placeholder = {
                            Text("Create a password")
                        },

                        visualTransformation =
                            if (passwordVisible) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },

                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password
                        ),

                        trailingIcon = {

                            TextButton(
                                onClick = {
                                    passwordVisible =
                                        !passwordVisible
                                }
                            ) {

                                Text(
                                    text =
                                        if (passwordVisible) {
                                            "Hide"
                                        } else {
                                            "Show"
                                        },

                                    color = Color(0xFF2E6F40)
                                )
                            }
                        },

                        shape = RoundedCornerShape(14.dp),

                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2E6F40),
                            unfocusedBorderColor = Color(0xFFD5DED7),
                            focusedLabelColor = Color(0xFF2E6F40),
                            cursorColor = Color(0xFF2E6F40)
                        )
                    )

                    Spacer(
                        modifier = Modifier.height(13.dp)
                    )

                    OutlinedTextField(
                        value = confirmPassword,

                        onValueChange = {
                            confirmPassword = it
                        },

                        modifier = Modifier.fillMaxWidth(),

                        singleLine = true,

                        label = {
                            Text("Confirm Password")
                        },

                        placeholder = {
                            Text("Repeat your password")
                        },

                        visualTransformation =
                            PasswordVisualTransformation(),

                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password
                        ),

                        shape = RoundedCornerShape(14.dp),

                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2E6F40),
                            unfocusedBorderColor = Color(0xFFD5DED7),
                            focusedLabelColor = Color(0xFF2E6F40),
                            cursorColor = Color(0xFF2E6F40)
                        )
                    )

                    Spacer(
                        modifier = Modifier.height(18.dp)
                    )

                    Text(
                        text = "I am a...",

                        fontSize = 14.sp,

                        fontWeight = FontWeight.SemiBold,

                        color = Color(0xFF253D2C)
                    )

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),

                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Row(
                            verticalAlignment =
                                Alignment.CenterVertically,

                            modifier = Modifier.weight(1f)
                        ) {

                            RadioButton(
                                selected =
                                    selectedRole == "Intern",

                                onClick = {
                                    selectedRole = "Intern"
                                }
                            )

                            Text(
                                text = "Intern",
                                color = Color(0xFF253D2C)
                            )
                        }

                        Row(
                            verticalAlignment =
                                Alignment.CenterVertically,

                            modifier = Modifier.weight(1f)
                        ) {

                            RadioButton(
                                selected =
                                    selectedRole == "Mentor",

                                onClick = {
                                    selectedRole = "Mentor"
                                }
                            )

                            Text(
                                text = "Mentor",
                                color = Color(0xFF253D2C)
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(14.dp)
                    )

                    Button(
                        onClick = {

                            if (isCreatingAccount) {
                                return@Button
                            }

                            when {

                                name.isBlank() -> {

                                    Toast.makeText(
                                        context,
                                        "Please enter your name",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                                email.isBlank() -> {

                                    Toast.makeText(
                                        context,
                                        "Please enter your email",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                                password.length < 6 -> {

                                    Toast.makeText(
                                        context,
                                        "Password must be at least 6 characters",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                                password != confirmPassword -> {

                                    Toast.makeText(
                                        context,
                                        "Passwords do not match",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                                else -> {

                                    isCreatingAccount = true

                                    authRepository.register(
                                        email = email.trim(),
                                        password = password
                                    ) { success, error ->

                                        if (!success) {

                                            isCreatingAccount = false

                                            Toast.makeText(
                                                context,
                                                error
                                                    ?: "Registration failed",
                                                Toast.LENGTH_LONG
                                            ).show()

                                            return@register
                                        }

                                        val firebaseUser =
                                            authRepository
                                                .getCurrentUser()

                                        if (firebaseUser == null) {

                                            isCreatingAccount = false

                                            Toast.makeText(
                                                context,
                                                "Account created but user information was unavailable",
                                                Toast.LENGTH_LONG
                                            ).show()

                                            return@register
                                        }

                                        val userProfile =
                                            UserProfile(

                                                uid =
                                                    firebaseUser.uid,

                                                name =
                                                    name.trim(),

                                                email =
                                                    email.trim(),

                                                role =
                                                    selectedRole.lowercase(),

                                                profileImage = "",

                                                isOnline = false,

                                                createdAt =
                                                    System.currentTimeMillis()
                                            )

                                        userRepository
                                            .createUserProfile(
                                                userProfile
                                            ) { profileCreated, profileError ->

                                                isCreatingAccount = false

                                                if (profileCreated) {

                                                    Toast.makeText(
                                                        context,
                                                        "Account created successfully!",
                                                        Toast.LENGTH_SHORT
                                                    ).show()

                                                    onBackToLogin()

                                                } else {

                                                    Toast.makeText(
                                                        context,
                                                        profileError
                                                            ?: "Account created but profile failed",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                            }
                                    }
                                }
                            }
                        },

                        enabled = !isCreatingAccount,

                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),

                        shape = RoundedCornerShape(14.dp),

                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2E6F40),
                            contentColor = Color.White,
                            disabledContainerColor =
                                Color(0xFF9BBBA4)
                        )
                    ) {

                        Text(
                            text =
                                if (isCreatingAccount) {
                                    "CREATING..."
                                } else {
                                    "CREATE ACCOUNT"
                                },

                            fontSize = 14.sp,

                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    TextButton(
                        onClick = onBackToLogin,

                        modifier = Modifier.align(
                            Alignment.CenterHorizontally
                        )
                    ) {

                        Text(
                            text =
                                "Already have an account? Sign in",

                            color = Color(0xFF2E6F40),

                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}