package com.ayesha.guidechat

import com.ayesha.guidechat.ui.HomeScreen
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
import com.ayesha.guidechat.data.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.ayesha.guidechat.model.UserProfile
import com.ayesha.guidechat.ui.theme.GuideChatTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            GuideChatTheme {

                var currentScreen by remember {
                    mutableStateOf("login")
                }

                var currentUserProfile by remember {
                    mutableStateOf<UserProfile?>(null)
                }

                var profileLoading by remember {
                    mutableStateOf(false)
                }

                var profileError by remember {
                    mutableStateOf<String?>(null)
                }

                LaunchedEffect(currentScreen) {
                    if (currentScreen == "home") {
                        val uid = FirebaseAuth
                            .getInstance()
                            .currentUser
                            ?.uid

                        if (uid != null) {
                            profileLoading = true
                            profileError = null

                            UserRepository().getUserProfile(
                                uid = uid,
                                onSuccess = { profile ->
                                    currentUserProfile = profile
                                    profileLoading = false
                                },
                                onError = { error ->
                                    profileError = error
                                    profileLoading = false
                                }
                            )
                        } else {
                            profileError = "No signed-in user found"
                        }
                    }
                }

                when (currentScreen) {

                    "login" -> {
                        LoginScreen(
                            onLoginSuccess = {
                                currentScreen = "home"
                            },
                            onCreateAccount = {
                                currentScreen = "register"
                            }
                        )
                    }

                    "register" -> {
                        RegisterScreen(
                            onBackToLogin = {
                                currentScreen = "login"
                            }
                        )
                    }

                    "home" -> {
                        when {
                            profileLoading -> {
                                LoadingProfileScreen()
                            }

                            currentUserProfile != null -> {
                                HomeScreen(
                                    userName = currentUserProfile!!.name,
                                    onChatClick = {
                                        // Real Firestore chat search will be connected in Step 8B.
                                    },
                                    onNewChatClick = {
                                        // Real Firestore user search will be connected in Step 8B.
                                    }
                                )
                            }

                            else -> {
                                ProfileErrorScreen(
                                    message = profileError ?: "Unable to load your profile",
                                    onBackToLogin = {
                                        currentScreen = "login"
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingProfileScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Loading your profile...",
            color = Color(0xFF2E6F40),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ProfileErrorScreen(
    message: String,
    onBackToLogin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Could not load your profile",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF253D2C)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = onBackToLogin,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2E6F40)
            )
        ) {
            Text("Back to Sign In")
        }
    }
}

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

            /* ---------- LOGO ---------- */

            Box(
                modifier = Modifier
                    .size(82.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFCFFDDC)),

                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = "M",

                    color = Color(0xFF2E6F40),

                    fontSize = 38.sp,

                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            /* ---------- APP NAME ---------- */

            Text(
                text = "MentorConnect",

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

            /* ---------- LOGIN CARD ---------- */

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

                    /* ---------- EMAIL ---------- */

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

                    /* ---------- PASSWORD ---------- */

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

                    /* ---------- FORGOT PASSWORD ---------- */

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

                    /* ---------- LOGIN BUTTON ---------- */

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

                    /* ---------- CREATE ACCOUNT ---------- */

                    Text(
                        text = "New to MentorConnect?",

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

            /* ---------- LOGO ---------- */

            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFCFFDDC)),

                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = "M",

                    color = Color(0xFF2E6F40),

                    fontSize = 32.sp,

                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            /* ---------- TITLE ---------- */

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

            /* ---------- REGISTER CARD ---------- */

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

                    /* ---------- NAME ---------- */

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

                    /* ---------- EMAIL ---------- */

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

                    /* ---------- PASSWORD ---------- */

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

                    /* ---------- CONFIRM PASSWORD ---------- */

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

                    /* ---------- ROLE ---------- */

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

                    /* ---------- CREATE ACCOUNT ---------- */

                    Button(
                        onClick = {

                            // Prevent multiple clicks
                            if (isCreatingAccount) {
                                return@Button
                            }

                            // ---------- VALIDATION ----------

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

                                    // Start registration
                                    isCreatingAccount = true

                                    authRepository.register(
                                        email = email.trim(),
                                        password = password
                                    ) { success, error ->

                                        if (success) {

                                            // Firebase Authentication succeeded
                                            val firebaseUser =
                                                authRepository.getCurrentUser()

                                            if (firebaseUser != null) {

                                                val userProfile = UserProfile(
                                                    uid = firebaseUser.uid,
                                                    name = name.trim(),
                                                    email = email.trim(),
                                                    role = selectedRole.lowercase(),
                                                    profileImage = "",
                                                    isOnline = false,
                                                    createdAt = System.currentTimeMillis()
                                                )

                                                // Create Firestore profile
                                                userRepository.createUserProfile(
                                                    userProfile
                                                ) { profileCreated, profileError ->

                                                    if (profileCreated) {

                                                        // EVERYTHING succeeded
                                                        isCreatingAccount = false

                                                        Toast.makeText(
                                                            context,
                                                            "Account created successfully!",
                                                            Toast.LENGTH_SHORT
                                                        ).show()

                                                        onBackToLogin()

                                                    } else {

                                                        // Auth succeeded but Firestore failed
                                                        isCreatingAccount = false

                                                        Toast.makeText(
                                                            context,
                                                            "Account created, but profile setup failed. Please try again.",
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                    }
                                                }

                                            } else {

                                                isCreatingAccount = false

                                                Toast.makeText(
                                                    context,
                                                    "Account created, but user information was unavailable.",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }

                                        } else {

                                            // Firebase Authentication failed
                                            isCreatingAccount = false

                                            Toast.makeText(
                                                context,
                                                error ?: "Registration failed",
                                                Toast.LENGTH_LONG
                                            ).show()
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
                            disabledContainerColor = Color(0xFF68BA7F),
                            disabledContentColor = Color.White
                        )
                    ) {

                        Text(
                            text = if (isCreatingAccount) {
                                "CREATING ACCOUNT..."
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

                    /* ---------- BACK TO LOGIN ---------- */

                    TextButton(
                        onClick = onBackToLogin,

                        modifier = Modifier.align(
                            Alignment.CenterHorizontally
                        )
                    ) {

                        Text(
                            text = "Already have an account? Sign in",

                            color = Color(0xFF2E6F40),

                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
