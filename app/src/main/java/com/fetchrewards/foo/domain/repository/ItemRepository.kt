package com.fetchrewards.foo.domain.repository

import com.fetchrewards.foo.domain.model.Item
import com.fetchrewards.foo.util.Result

interface ItemRepository {
    suspend fun getItems(forceRefresh: Boolean = false): Result<List<Item>>
} 