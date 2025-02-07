package com.fetchrewards.foo.presentation.viewmodel

import MainDispatcherRule
import app.cash.turbine.test
import com.fetchrewards.foo.domain.model.Item
import com.fetchrewards.foo.domain.usecase.GetItemsUseCase
import com.fetchrewards.foo.presentation.state.ItemsIntent
import com.fetchrewards.foo.presentation.state.ItemsState
import com.fetchrewards.foo.util.Result
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ItemViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var getItemsUseCase: GetItemsUseCase
    private lateinit var viewModel: ItemViewModel

    @Before
    fun setup() {
        getItemsUseCase = mockk()
        viewModel = ItemViewModel(getItemsUseCase)
    }

    @Test
    fun `initial state is correct`() = runTest {
        // Then
        assertThat(viewModel.state.value).isEqualTo(ItemsState())
    }

    @Test
    fun `LoadItems intent with success result updates state correctly`() = runTest {
        // Given
        val mockItems = listOf(
            Item(1, 1, "Item 1"),
            Item(2, 1, "Item 2"),
            Item(3, 2, "Item 3")
        )
        coEvery { getItemsUseCase(false) } coAnswers {
            delay(1000)
            Result.Success(mockItems)
        }
        // When
        viewModel.handleIntent(ItemsIntent.LoadItems)

        // Then
        viewModel.state.test {

            val loadingState = awaitItem()
            assertThat(loadingState.isLoading).isTrue()
            assertThat(loadingState.error).isNull()

            val finalState = awaitItem()
            assertThat(finalState.isLoading).isFalse()
            assertThat(finalState.error).isNull()
            assertThat(finalState.items).isEqualTo(mockItems.groupBy { it.listId })
        }
    }

    @Test
    fun `RefreshItems intent with success result updates state correctly`() = runTest {
        // Given
        val mockItems = listOf(
            Item(1, 1, "Item 1"),
            Item(2, 2, "Item 2")
        )
        coEvery { getItemsUseCase(true) } coAnswers {
            delay(1000)
            Result.Success(mockItems)
        }

        // When
        viewModel.handleIntent(ItemsIntent.RefreshItems)

        // Then
        viewModel.state.test {
            val loadingState = awaitItem()
            assertThat(loadingState.isLoading).isTrue()
            assertThat(loadingState.error).isNull()

            val finalState = awaitItem()
            assertThat(finalState.isLoading).isFalse()
            assertThat(finalState.error).isNull()
            assertThat(finalState.items).isEqualTo(mockItems.groupBy { it.listId })
        }
    }

    @Test
    fun `LoadItems intent with error result updates state correctly`() = runTest {
        // Given
        val errorMessage = "Network error"
        coEvery { getItemsUseCase(false) } coAnswers {
            delay(1000)
            Result.Error(Exception(errorMessage))
        }

        // When
        viewModel.handleIntent(ItemsIntent.LoadItems)

        // Then
        viewModel.state.test {
            val loadingState = awaitItem()
            assertThat(loadingState.isLoading).isTrue()
            assertThat(loadingState.error).isNull()

            val finalState = awaitItem()
            assertThat(finalState.isLoading).isFalse()
            assertThat(finalState.error).isEqualTo(errorMessage)
            assertThat(finalState.items).isEmpty()
        }
    }

    @Test
    fun `LoadItems intent with loading result updates state correctly`() = runTest {
        // Given
        coEvery { getItemsUseCase(false) } returns Result.Loading

        // When
        viewModel.handleIntent(ItemsIntent.LoadItems)

        // Then
        viewModel.state.test {
            val loadingState = awaitItem()
            assertThat(loadingState.isLoading).isTrue()
            assertThat(loadingState.error).isNull()
        }
    }

    @Test
    fun `consecutive LoadItems calls handle state correctly`() = runTest {
        // Given
        val mockItems = listOf(Item(1, 1, "Item 1"))
        coEvery { getItemsUseCase(false) } coAnswers {
            delay(1000)
            Result.Success(mockItems)
        }

        // When - First call
        viewModel.handleIntent(ItemsIntent.LoadItems)

        // Then make another call
        viewModel.handleIntent(ItemsIntent.LoadItems)

        // Then
        viewModel.state.test {
            val state1 = awaitItem()
            assertThat(state1.isLoading).isTrue()

            val state2 = awaitItem()
            assertThat(state2.isLoading).isFalse()
            assertThat(state2.items).isEqualTo(mockItems.groupBy { it.listId })
        }
    }

    @Test
    fun `error message is null when loading new items`() = runTest {
        // Given - Start with error state
        coEvery { getItemsUseCase(false) } returns Result.Error(Exception("Initial error"))
        viewModel.handleIntent(ItemsIntent.LoadItems)

        // When - Load again
        coEvery { getItemsUseCase(false) } returns Result.Success(emptyList())
        viewModel.handleIntent(ItemsIntent.LoadItems)

        // Then
        viewModel.state.test {
            val finalState = awaitItem()
            assertThat(finalState.error).isNull()
        }
    }
}