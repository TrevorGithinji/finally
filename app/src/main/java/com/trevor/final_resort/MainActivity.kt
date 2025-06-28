package com.trevor.final_resort

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.google.firebase.FirebaseApp
import com.trevor.final_resort.ui.theme.Final_resortTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Firebase
        try {
            FirebaseApp.initializeApp(this)
            Log.d("MainActivity", "Firebase initialized successfully")
            
            // Run comprehensive Firebase checks
            val report = FirebaseErrorChecker.checkAllFirebaseServices(this)
            Log.i("MainActivity", "Firebase Configuration Report:\n$report")
            
            // Test Firebase services
            FirebaseTest.testFirebaseInitialization()
            FirebaseTest.testFirebaseConnection()
            
            // Show report in toast for debugging
            Toast.makeText(this, "Firebase check completed. Check logs for details.", Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            Log.e("MainActivity", "Firebase initialization failed: ${e.message}", e)
            Toast.makeText(this, "Firebase initialization failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
        
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
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    
    // Check if user is already logged in
    LaunchedEffect(Unit) {
        val user = UserManager.getCurrentUser()
        if (user != null) {
            currentUser = user
            currentScreen = Screen.ProductList
        }
    }
    
    when (currentScreen) {
        Screen.Login -> {
            LoginScreen(
                onLoginClick = { email, password ->
                    scope.launch {
                        isLoading = true
                        try {
                            val result = UserManager.loginUser(email, password)
                            if (result.isSuccess) {
                                currentUser = result.getOrNull()
                                currentScreen = Screen.ProductList
                                onShowToast("Login successful!")
                            } else {
                                val error = result.exceptionOrNull()
                                onShowToast(error?.message ?: "Login failed")
                            }
                        } catch (e: Exception) {
                            onShowToast("Login failed: ${e.message}")
                        } finally {
                            isLoading = false
                        }
                    }
                },
                onRegisterClick = {
                    currentScreen = Screen.Register
                }
            )
        }
        
        Screen.Register -> {
            RegistrationScreen(
                onRegisterClick = { email, password, firstName, secondName ->
                    scope.launch {
                        isLoading = true
                        try {
                            val result = UserManager.registerUser(email, password, firstName, secondName)
                            if (result.isSuccess) {
                                onShowToast("Registration successful! Please login.")
                                currentScreen = Screen.Login
                            } else {
                                val error = result.exceptionOrNull()
                                onShowToast(error?.message ?: "Registration failed")
                            }
                        } catch (e: Exception) {
                            onShowToast("Registration failed: ${e.message}")
                        } finally {
                            isLoading = false
                        }
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
                        UserManager.logout()
                        currentUser = null
                        currentScreen = Screen.Login
                        onShowToast("Logged out successfully")
                    },
                    onViewProductsClick = {
                        currentScreen = Screen.ProductList
                    }
                )
            }
        }
        
        Screen.ProductList -> {
            ProductListScreen(
                currentUser = currentUser,
                onProductClick = { product ->
                    selectedProduct = product
                    currentScreen = Screen.ProductDetail
                },
                onAddProductClick = {
                    currentScreen = Screen.AddProduct
                },
                onLogoutClick = {
                    UserManager.logout()
                    currentUser = null
                    currentScreen = Screen.Login
                    onShowToast("Logged out successfully")
                }
            )
        }
        
        Screen.AddProduct -> {
            currentUser?.let { user ->
                AddProductScreen(
                    currentUser = user,
                    onProductAdded = {
                        scope.launch {
                            try {
                                // The product addition is handled in AddProductScreen
                                currentScreen = Screen.ProductList
                                onShowToast("Product added successfully!")
                            } catch (e: Exception) {
                                onShowToast("Failed to add product: ${e.message}")
                            }
                        }
                    },
                    onBackClick = {
                        currentScreen = Screen.ProductList
                    }
                )
            }
        }
        
        Screen.ProductDetail -> {
            selectedProduct?.let { product ->
                ProductDetailScreen(
                    product = product,
                    currentUser = currentUser,
                    onBackClick = {
                        currentScreen = Screen.ProductList
                    },
                    onShowToast = onShowToast
                )
            }
        }
    }
}

sealed class Screen {
    object Login : Screen()
    object Register : Screen()
    object Home : Screen()
    object ProductList : Screen()
    object AddProduct : Screen()
    object ProductDetail : Screen()
}