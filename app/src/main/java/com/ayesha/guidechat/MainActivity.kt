package com.ayesha.guidechat

import android.os.Bundle
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ayesha.guidechat.ui.theme.GuideChatTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            GuideChatTheme {
                MentorConnectApp()
            }
        }
    }
}

@Composable
fun MentorConnectApp() {

    var showRegisterScreen by remember {
        mutableStateOf(false)
    }

    if (showRegisterScreen) {
        RegisterScreen(
            onBackToLogin = {
                showRegisterScreen = false
            }
        )
    } else {
        LoginScreen(
            onCreateAccount = {
                showRegisterScreen = true
            }
        )
    }
}

@Composable
fun LoginScreen(
    onCreateAccount: () -> Unit
) {

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

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "MentorConnect",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF253D2C)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Connect • Collaborate • Grow",
                fontSize = 14.sp,
                color = Color(0xFF68BA7F)
            )

            Spacer(modifier = Modifier.height(30.dp))

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

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Sign in to continue your conversations",
                        fontSize = 14.sp,
                        color = Color(0xFF6B756E)
                    )

                    Spacer(modifier = Modifier.height(22.dp))

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

                    Spacer(modifier = Modifier.height(14.dp))

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
                        visualTransformation = if (passwordVisible) {
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
                                    passwordVisible = !passwordVisible
                                }
                            ) {
                                Text(
                                    text = if (passwordVisible) "Hide" else "Show",
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

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = {
                            // Firebase password reset will be added later.
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            text = "Forgot password?",
                            color = Color(0xFF2E6F40),
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            // Firebase login will be added later.
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2E6F40)
                        )
                    ) {
                        Text(
                            text = "SIGN IN",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "New to MentorConnect?",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp,
                        color = Color(0xFF6B756E)
                    )

                    TextButton(
                        onClick = onCreateAccount,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = "Create an account",
                            color = Color(0xFF2E6F40),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = "Secure communication for interns & mentors",
                fontSize = 12.sp,
                color = Color(0xFF6B756E),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun RegisterScreen(
    onBackToLogin: () -> Unit
) {

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
                    text = "M",
                    color = Color(0xFF2E6F40),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Create Account",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF253D2C)
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = "Join your internship community",
                fontSize = 14.sp,
                color = Color(0xFF68BA7F)
            )

            Spacer(modifier = Modifier.height(24.dp))

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

                    Spacer(modifier = Modifier.height(13.dp))

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

                    Spacer(modifier = Modifier.height(13.dp))

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
                        visualTransformation = if (passwordVisible) {
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
                                    passwordVisible = !passwordVisible
                                }
                            ) {
                                Text(
                                    text = if (passwordVisible) "Hide" else "Show",
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

                    Spacer(modifier = Modifier.height(13.dp))

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
                        visualTransformation = PasswordVisualTransformation(),
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

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "I am a...",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF253D2C)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            RadioButton(
                                selected = selectedRole == "Intern",
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
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            RadioButton(
                                selected = selectedRole == "Mentor",
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

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            // Firebase registration will be added next.
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2E6F40)
                        )
                    ) {
                        Text(
                            text = "CREATE ACCOUNT",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = onBackToLogin,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
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