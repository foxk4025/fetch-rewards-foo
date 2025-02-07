package com.fetchrewards.foo.data.repository

import com.fetchrewards.foo.data.mapper.toDomain
import com.fetchrewards.foo.data.remote.api.ApiService
import com.fetchrewards.foo.domain.model.Item
import com.fetchrewards.foo.domain.repository.ItemRepository
import javax.inject.Inject
import javax.inject.Singleton
import com.fetchrewards.foo.util.Result

@Singleton
class ItemRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : ItemRepository {
    private var cachedItems: List<Item>? = null

    override suspend fun getItems(forceRefresh: Boolean): Result<List<Item>> {
        return try {
            if (forceRefresh || cachedItems == null) {
                val response = apiService.getItems()
                if (response.isSuccessful) {
                    response.body()?.let { dtos ->
                        cachedItems = dtos
                            .mapNotNull { it.toDomain() }
                            .sortedWith(compareBy({ it.listId }, { it.name }))
                        Result.Success(cachedItems!!)
                    } ?: Result.Error(Exception("Empty response body"))
                } else {
                    Result.Error(Exception("Failed to fetch items: ${response.code()}"))
                }
            } else {
                Result.Success(cachedItems!!)
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}