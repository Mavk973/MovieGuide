package com.example.movieguide.data.api

import com.example.movieguide.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://api.themoviedb.org/3/"
    private const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500"
    private const val BACKDROP_BASE_URL = "https://image.tmdb.org/t/p/w1280"

    val api: TmdbApi by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TmdbApi::class.java)
    }

    fun getImageUrl(posterPath: String?): String {
        return if (posterPath != null) {
            "$IMAGE_BASE_URL$posterPath"
        } else {
            ""
        }
    }

    fun getBackdropUrl(backdropPath: String?): String {
        return if (backdropPath != null) {
            "$BACKDROP_BASE_URL$backdropPath"
        } else {
            ""
        }
    }

    fun getProfileUrl(profilePath: String?): String {
        return if (profilePath != null) {
            "$IMAGE_BASE_URL$profilePath"
        } else {
            ""
        }
    }
}

