package com.example.movieguide.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.movieguide.ui.screen.MovieDetailScreen
import com.example.movieguide.ui.screen.MoviesScreen

sealed class Screen(val route: String) {
    object Movies : Screen("movies")
    object MovieDetail : Screen("movie_detail/{movieId}") {
        fun createRoute(movieId: Int) = "movie_detail/$movieId"
    }
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Movies.route
    ) {
        composable(Screen.Movies.route) {
            MoviesScreen(
                onMovieClick = { movieId ->
                    navController.navigate(Screen.MovieDetail.createRoute(movieId))
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
            MovieDetailScreen(
                movieId = movieId,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}

