package com.jfranco.witcher3.quests.android.common.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp

@Composable
fun IndexedLazyColumn(
    modifier: Modifier = Modifier,
    state: IndexedLazyListState = rememberIndexedLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical = if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    content: IndexedLazyListScope.() -> Unit
) {
    LazyColumn(
        modifier = modifier,
        state = state.listState,
        contentPadding = contentPadding,
        reverseLayout = reverseLayout,
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
        flingBehavior = flingBehavior,
        userScrollEnabled = userScrollEnabled,
    ) {
        IndexedLazyListScope(this, state).content()
    }
}

@Composable
fun rememberIndexedLazyListState(
    listState: LazyListState = rememberLazyListState(),
): IndexedLazyListState {
    return remember { IndexedLazyListState(listState) }
}

@Stable
class IndexedLazyListState internal constructor(
    val listState: LazyListState,
) {
    private val itemIndexMapping = mutableStateMapOf<Any, Int>()

    internal fun linkKeyToIndex(key: Any, index: Int) {
        itemIndexMapping[key] = index
    }

    suspend fun animateScrollToItem(key: Any) {
        val index = itemIndexMapping[key] ?: return
        listState.animateScrollToItem(index)

        animateScrollAndCentralizeItem(key)
    }

    private suspend fun animateScrollAndCentralizeItem(key: Any) {
        val index = itemIndexMapping[key] ?: return

        val itemInfo = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }
        if (itemInfo != null) {
            val center = listState.layoutInfo.viewportEndOffset / 2
            val childCenter = itemInfo.offset + itemInfo.size / 2
            listState.animateScrollBy((childCenter - center).toFloat())
        } else {
            listState.animateScrollToItem(index)
        }
    }
}

@Stable
class IndexedLazyListScope(
    private val lazyListScope: LazyListScope,
    private val state: IndexedLazyListState,
) : LazyListScope {

    private var currentItemLayoutIndex = 0

    override fun item(key: Any?, contentType: Any?, content: @Composable LazyItemScope.() -> Unit) {
        lazyListScope.item(key, contentType, content)
        if (key != null)
            state.linkKeyToIndex(key, currentItemLayoutIndex)
        currentItemLayoutIndex++
    }

    @Deprecated("Use the non deprecated overload", level = DeprecationLevel.HIDDEN)
    override fun item(key: Any?, content: @Composable LazyItemScope.() -> Unit) {
        item(key, null, content)
    }

    override fun items(
        count: Int,
        key: ((index: Int) -> Any)?,
        contentType: (index: Int) -> Any?,
        itemContent: @Composable LazyItemScope.(index: Int) -> Unit
    ) {
        lazyListScope.items(count, key, contentType, itemContent)

        repeat(count) { i ->
            val resolvedKey = key?.invoke(i)
            if (resolvedKey != null)
                state.linkKeyToIndex(resolvedKey, currentItemLayoutIndex)
            currentItemLayoutIndex++
        }
    }

    @Deprecated("Use the non deprecated overload", level = DeprecationLevel.HIDDEN)
    override fun items(
        count: Int,
        key: ((index: Int) -> Any)?,
        itemContent: @Composable LazyItemScope.(index: Int) -> Unit
    ) {
        items(count, key, { null }, itemContent)
    }

    @ExperimentalFoundationApi
    override fun stickyHeader(
        key: Any?,
        contentType: Any?,
        content: @Composable LazyItemScope.() -> Unit
    ) {
        lazyListScope.stickyHeader(key, contentType, content)
        if (key != null)
            state.linkKeyToIndex(key, currentItemLayoutIndex)
        currentItemLayoutIndex++
    }
}