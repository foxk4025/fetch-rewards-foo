package com.fetchrewards.foo.presentation.state

import com.fetchrewards.foo.domain.model.Item

data class ItemsState(
    val items: Map<Int, List<Item>> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null
) 