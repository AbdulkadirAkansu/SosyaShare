package com.akansu.sosyashare.data.di

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("save-token")
    fun sendToken(@Body token: TokenRequest): Call<ResponseBody>
}

data class TokenRequest(val token: String)
