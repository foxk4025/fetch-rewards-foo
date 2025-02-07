package com.fetchrewards.foo.data.remote.api

import com.fetchrewards.foo.data.remote.dto.ItemDto
import retrofit2.Response
import retrofit2.http.GET

interface ApiService {
    @GET("hiring.json")
    suspend fun getItems(): Response<List<ItemDto>>
} 