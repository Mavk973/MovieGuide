package com.example.movieguide.data.model

import com.google.gson.annotations.SerializedName

data class Cast(
    val id: Int,
    val name: String,
    @SerializedName("profile_path")
    val profilePath: String?,
    val character: String
)

data class CreditsResponse(
    val cast: List<Cast>
)

