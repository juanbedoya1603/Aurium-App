package com.proyecto.aurium.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.proyecto.aurium.presentation.home.HomeView
import com.proyecto.aurium.presentation.login.LoginView
import com.proyecto.aurium.presentation.register.RegisterView

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginView(navController = navController)
        }

        composable("register") {
            RegisterView(navController = navController)
        }

        composable(
            route = "home/{phoneNumber}",
            arguments = listOf(navArgument("phoneNumber") { type = NavType.StringType })
        ) { backStackEntry ->
            val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: ""
            HomeView(navController = navController, phoneNumber = phoneNumber)
        }
    }
}