package com.fetchrewards.foo.domain.usecase

import com.fetchrewards.foo.domain.model.Item
import com.fetchrewards.foo.domain.repository.ItemRepository
import javax.inject.Inject
import com.fetchrewards.foo.util.Result

class GetItemsUseCase @Inject constructor(
    private val repository: ItemRepository
) {
    suspend operator fun invoke(forceRefresh: Boolean = false): Result<List<Item>> =
        repository.getItems(forceRefresh)
} 