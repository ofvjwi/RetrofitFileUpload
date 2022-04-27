package com.example.myretrofitexamples.networking.retrofit

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.File

interface APIService {
    @Multipart
    @POST("attachment/upload")
    fun uploadPhoto(
        @Part photo: MultipartBody.Part
    ): Call<ResponseBody>
}


