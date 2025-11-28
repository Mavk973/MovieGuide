package com.example.movieguide.navigation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.movieguide.ui.screen.ActorDetailScreen
import com.example.movieguide.ui.screen.FavoritesScreen
import com.example.movieguide.ui.screen.ForgotPasswordScreen
import com.example.movieguide.ui.screen.LoginScreen
import com.example.movieguide.ui.screen.MovieDetailScreen
import com.example.movieguide.ui.screen.MoviesScreen
import com.example.movieguide.ui.screen.ProfileScreen
import com.example.movieguide.ui.screen.RegisterScreen
import com.example.movieguide.ui.viewmodel.AuthViewModel
import com.example.movieguide.ui.viewmodel.AuthUiState
import com.example.movieguide.ui.viewmodel.AuthViewModelFactory

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object Movies : Screen("movies")
    object MovieDetail : Screen("movie_detail/{movieId}") {
        fun createRoute(movieId: Int) = "movie_detail/$movieId"
    }
    object ActorDetail : Screen("actor_detail/{actorId}") {
        fun createRoute(actorId: Int) = "actor_detail/$actorId"
    }
    object Profile : Screen("profile")
    object Favorites : Screen("favorites")
}

@Composable
fun NavGraph(
    onLanguageChanged: ((String) -> Unit)? = null,
    onThemeChanged: ((String) -> Unit)? = null,
    onGoogleSignInRequest: ((android.content.Intent, (String?) -> Unit) -> Unit)? = null
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(
            context.applicationContext as Application
        )
    )
    val authState by authViewModel.authState.collectAsState()

    // Определяем стартовый экран на основе состояния аутентификации
    val startDestination = when (authState) {
        is AuthUiState.Authenticated -> Screen.Movies.route
        is AuthUiState.Guest -> Screen.Movies.route
        is AuthUiState.Unauthenticated -> Screen.Login.route
        else -> Screen.Login.route
    }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthUiState.Authenticated -> {
                // Если пользователь авторизован и находится на экране входа/регистрации, переходим на главный экран
                val currentRoute = navController.currentDestination?.route
                if (currentRoute == Screen.Login.route || 
                    currentRoute == Screen.Register.route ||
                    currentRoute == Screen.ForgotPassword.route) {
                    navController.navigate(Screen.Movies.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            }
            is AuthUiState.Guest -> {
                // Если пользователь вошел как гость и находится на экране входа/регистрации, переходим на главный экран
                val currentRoute = navController.currentDestination?.route
                if (currentRoute == Screen.Login.route || 
                    currentRoute == Screen.Register.route ||
                    currentRoute == Screen.ForgotPassword.route) {
                    navController.navigate(Screen.Movies.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            }
            is AuthUiState.Unauthenticated -> {
                // Если пользователь не авторизован и находится на защищенных экранах, переходим на экран входа
                val currentRoute = navController.currentDestination?.route
                if (currentRoute != Screen.Login.route && 
                    currentRoute != Screen.Register.route &&
                    currentRoute != Screen.ForgotPassword.route) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            else -> {}
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Movies.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onForgotPassword = {
                    navController.navigate(Screen.ForgotPassword.route)
                },
                viewModel = authViewModel,
                onGoogleSignInRequest = onGoogleSignInRequest
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Movies.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                viewModel = authViewModel
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                viewModel = authViewModel
            )
        }

        composable(Screen.Movies.route) {
            MoviesScreen(
                onMovieClick = { movieId ->
                    navController.navigate(Screen.MovieDetail.createRoute(movieId))
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                }
            )
        }

        composable(
            route = Screen.MovieDetail.route,
            arguments = listOf(
                navArgument("movieId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getInt("movieId") ?: 0
            if (movieId > 0) {
                MovieDetailScreen(
                    movieId = movieId,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onActorClick = { actorId ->
                        navController.navigate(Screen.ActorDetail.createRoute(actorId))
                    }
                )
            } else {
                // Если movieId невалиден, возвращаемся назад
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            }
        }

        composable(
            route = Screen.ActorDetail.route,
            arguments = listOf(
                navArgument("actorId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val actorId = backStackEntry.arguments?.getInt("actorId") ?: 0
            if (actorId > 0) {
                ActorDetailScreen(
                    actorId = actorId,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onMovieClick = { movieId ->
                        navController.navigate(Screen.MovieDetail.createRoute(movieId))
                    }
                )
            } else {
                // Если actorId невалиден, возвращаемся назад
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            }
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onSignOut = {
                    // Немедленная навигация на экран входа после выхода
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onFavoritesClick = {
                    navController.navigate(Screen.Favorites.route)
                },
                onLanguageChanged = onLanguageChanged,
                onThemeChanged = onThemeChanged,
                viewModel = authViewModel
            )
        }

        composable(Screen.Favorites.route) {
            // Проверяем, что пользователь не гость
            val currentAuthState = authState
            if (currentAuthState is AuthUiState.Guest) {
                // Если гость пытается зайти на экран избранного, возвращаем его назад
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            } else {
                FavoritesScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onMovieClick = { movieId ->
                        navController.navigate(Screen.MovieDetail.createRoute(movieId))
                    }
                )
            }
        }
    }
}

