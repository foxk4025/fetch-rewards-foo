package com.fetchrewards.foo.presentation.state

sealed interface ItemsIntent {
    data object LoadItems : ItemsIntent
    data object RefreshItems : ItemsIntent
} 