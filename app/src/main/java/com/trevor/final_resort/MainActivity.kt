package com.trevor.final_resort

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.trevor.final_resort.ui.theme.Final_resortTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Final_resortTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppContent(
                        modifier = Modifier.padding(innerPadding),
                        onShowToast = { message ->
                            Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AppContent(
    modifier: Modifier = Modifier,
    onShowToast: (String) -> Unit
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) }
    var currentUser by remember { mutableStateOf<User?>(null) }
    
    when (currentScreen) {
        Screen.Login -> {
            LoginScreen(
                onLoginClick = { email, password ->
                    val user = UserManager.loginUser(email, password)
                    if (user != null) {
                        currentUser = user
                        currentScreen = Screen.Home
                        onShowToast("Login successful!")
                    } else {
                        onShowToast("Invalid email or password")
                    }
                },
                onRegisterClick = {
                    currentScreen = Screen.Register
                }
            )
        }
        
        Screen.Register -> {
            RegistrationScreen(
                onRegisterClick = { user ->
                    val success = UserManager.registerUser(user)
                    if (success) {
                        onShowToast("Registration successful! Please login.")
                        currentScreen = Screen.Login
                    } else {
                        onShowToast("User with this email already exists")
                    }
                },
                onLoginClick = {
                    currentScreen = Screen.Login
                }
            )
        }
        
        Screen.Home -> {
            currentUser?.let { user ->
                HomeScreen(
                    user = user,
                    onLogoutClick = {
                        currentUser = null
                        currentScreen = Screen.Login
                        onShowToast("Logged out successfully")
                    }
                )
            }
        }
    }
}

sealed class Screen {
    object Login : Screen()
    object Register : Screen()
    object Home : Screen()
}