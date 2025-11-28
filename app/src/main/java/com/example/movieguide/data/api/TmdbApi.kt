package com.example.movieguide.data.api

import com.example.movieguide.data.model.ActorDetail
import com.example.movieguide.data.model.ActorMovieCredits
import com.example.movieguide.data.model.CreditsResponse
import com.example.movieguide.data.model.Genre
import com.example.movieguide.data.model.GenresResponse
import com.example.movieguide.data.model.MovieDetail
import com.example.movieguide.data.model.MovieResponse
import com.example.movieguide.data.model.VideosResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApi {
    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "ru-RU",
        @Query("page") page: Int = 1
    ): Response<MovieResponse>

    @GET("movie/{id}")
    suspend fun getMovieDetails(
        @Path("id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "ru-RU"
    ): Response<MovieDetail>

    @GET("movie/{id}/credits")
    suspend fun getMovieCredits(
        @Path("id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "ru-RU"
    ): Response<CreditsResponse>

    @GET("movie/{id}/videos")
    suspend fun getMovieVideos(
        @Path("id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "ru-RU"
    ): Response<VideosResponse>

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("language") language: String = "ru-RU",
        @Query("page") page: Int = 1
    ): Response<MovieResponse>

    @GET("discover/movie")
    suspend fun discoverMoviesByGenre(
        @Query("api_key") apiKey: String,
        @Query("with_genres") genreId: Int,
        @Query("language") language: String = "ru-RU",
        @Query("page") page: Int = 1
    ): Response<MovieResponse>

    @GET("genre/movie/list")
    suspend fun getGenres(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "ru-RU"
    ): Response<GenresResponse>

    @GET("person/{id}")
    suspend fun getActorDetails(
        @Path("id") actorId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "ru-RU"
    ): Response<ActorDetail>

    @GET("person/{id}/movie_credits")
    suspend fun getActorMovieCredits(
        @Path("id") actorId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "ru-RU"
    ): Response<ActorMovieCredits>
}

