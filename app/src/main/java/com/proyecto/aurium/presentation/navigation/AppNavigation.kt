package com.proyecto.aurium.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.proyecto.aurium.presentation.home.HomeView
import com.proyecto.aurium.presentation.login.LoginView
import com.proyecto.aurium.presentation.register.RegisterView

@Composable
fun AppNavigation(){

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login"){
            LoginView(navController = navController)
        }

        composable("register"){
            RegisterView(navController = navController)
        }

        composable("home"){
            HomeView(navController = navController)
        }

    }
}
