package com.example.movieguide.data.repository

import com.example.movieguide.data.local.AppDatabase
import com.example.movieguide.data.local.FavoriteMovie
import com.example.movieguide.data.model.MovieDetail
import kotlinx.coroutines.flow.Flow

class FavoritesRepository(private val database: AppDatabase) {
    private val dao = database.favoriteMovieDao()

    fun getAllFavorites(): Flow<List<FavoriteMovie>> = dao.getAllFavorites()

    fun isFavorite(movieId: Int): Flow<Boolean> = dao.isFavorite(movieId)

    suspend fun addToFavorites(movieDetail: MovieDetail) {
        val favoriteMovie = FavoriteMovie(
            id = movieDetail.id,
            title = movieDetail.title,
            posterPath = movieDetail.posterPath,
            voteAverage = movieDetail.voteAverage,
            releaseDate = movieDetail.releaseDate,
            overview = movieDetail.overview,
            backdropPath = movieDetail.backdropPath,
            runtime = movieDetail.runtime
        )
        dao.insertFavorite(favoriteMovie)
    }

    suspend fun removeFromFavorites(movieId: Int) {
        dao.deleteFavoriteById(movieId)
    }

    suspend fun toggleFavorite(movieDetail: MovieDetail) {
        val existing = dao.getFavoriteById(movieDetail.id)
        if (existing != null) {
            removeFromFavorites(movieDetail.id)
        } else {
            addToFavorites(movieDetail)
        }
    }
}

