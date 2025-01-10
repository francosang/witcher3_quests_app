package com.jfranco.witcher3.quests.android.common.ui

import androidx.compose.foundation.clickable
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.semantics.Role
import kotlinx.coroutines.launch


@Composable
fun SuspendIconButton(
    onClick: suspend () -> Unit,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    val enabled = remember { mutableStateOf(true) }

    IconButton(
        enabled = enabled.value,
        onClick = {
            scope.launch {
                enabled.value = false
                onClick()
                enabled.value = true
            }
        }
    ) {
        content()
    }
}

fun Modifier.suspendClickable(
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    onClick: suspend () -> Unit
) = composed(
    inspectorInfo = debugInspectorInfo {
        name = "suspendClickable"
        properties["enabled"] = enabled
        properties["onClickLabel"] = onClickLabel
        properties["role"] = role
        properties["onClick"] = onClick
    }
) {
    val scope = rememberCoroutineScope()
    val isIdle = remember { mutableStateOf(true) }

    Modifier.clickable(
        enabled = enabled && isIdle.value,
        onClickLabel = onClickLabel,
        onClick = {
            scope.launch {
                isIdle.value = false
                onClick()
                isIdle.value = true
            }
        },
        role = role,
    )
}