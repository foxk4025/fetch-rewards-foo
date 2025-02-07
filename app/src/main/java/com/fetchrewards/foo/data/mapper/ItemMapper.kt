package com.fetchrewards.foo.data.mapper

import com.fetchrewards.foo.data.remote.dto.ItemDto
import com.fetchrewards.foo.domain.model.Item

fun ItemDto.toDomain(): Item? =
    if (!name.isNullOrBlank()) {
        Item(
            id = id,
            listId = listId,
            name = name
        )
    } else null 