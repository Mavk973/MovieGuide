package com.example.movieguide.data.repository

import com.example.movieguide.BuildConfig
import com.example.movieguide.data.api.RetrofitClient
import com.example.movieguide.data.api.TmdbApi
import com.example.movieguide.data.model.Cast
import com.example.movieguide.data.model.Genre
import com.example.movieguide.data.model.Movie
import com.example.movieguide.data.model.MovieDetail
import com.example.movieguide.data.model.Video

class MovieRepository {
    private val api: TmdbApi = RetrofitClient.api

    suspend fun getPopularMovies(): Result<List<Movie>> {
        return try {
            val response = api.getPopularMovies(BuildConfig.TMDB_API_KEY)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.results)
            } else {
                Result.failure(Exception("Failed to fetch movies: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMovieDetails(movieId: Int): Result<MovieDetail> {
        return try {
            val response = api.getMovieDetails(movieId, BuildConfig.TMDB_API_KEY)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch movie details: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMovieCredits(movieId: Int): Result<List<Cast>> {
        return try {
            val response = api.getMovieCredits(movieId, BuildConfig.TMDB_API_KEY)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.cast)
            } else {
                Result.failure(Exception("Failed to fetch credits: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMovieVideos(movieId: Int): Result<List<Video>> {
        return try {
            val response = api.getMovieVideos(movieId, BuildConfig.TMDB_API_KEY)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.results)
            } else {
                Result.failure(Exception("Failed to fetch videos: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchMovies(query: String): Result<List<Movie>> {
        return try {
            val response = api.searchMovies(BuildConfig.TMDB_API_KEY, query)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.results)
            } else {
                Result.failure(Exception("Failed to search movies: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMoviesByGenre(genreId: Int): Result<List<Movie>> {
        return try {
            val response = api.discoverMoviesByGenre(BuildConfig.TMDB_API_KEY, genreId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.results)
            } else {
                Result.failure(Exception("Failed to fetch movies by genre: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getGenres(): Result<List<Genre>> {
        return try {
            val response = api.getGenres(BuildConfig.TMDB_API_KEY)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.genres)
            } else {
                Result.failure(Exception("Failed to fetch genres: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

