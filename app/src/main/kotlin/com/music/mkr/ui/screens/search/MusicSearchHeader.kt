/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.screens.search

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.music.mkr.R
import com.music.mkr.constants.InterfaceAnimationsKey
import com.music.mkr.constants.SearchSource
import com.music.mkr.ui.component.ExpressiveIconButton
import com.music.mkr.utils.rememberPreference

@Composable
fun MusicSearchHeader(
    query: TextFieldValue,
    onQueryChange: (TextFieldValue) -> Unit,
    onSearch: (String) -> Unit,
    active: Boolean,
    onActiveChange: (Boolean) -> Unit,
    searchSource: SearchSource,
    onSearchSourceChange: ((SearchSource) -> Unit)?,
    modifier: Modifier = Modifier,
    showBackButton: Boolean = false,
    onBackClick: (() -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    val requester = remember { FocusRequester() }
    var focused by remember { mutableStateOf(false) }
    val animate by rememberPreference(InterfaceAnimationsKey, true)
    val targetBorder = if (focused) colors.primary else colors.outlineVariant
    val border by animateColorAsState(targetBorder, label = "search_focus")
    val label = stringResource(R.string.search_music_hint)

    Box(modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
        Surface(
            modifier = Modifier.widthIn(max = 760.dp).fillMaxWidth(),
            shape = CircleShape,
            color = if (focused) colors.surfaceContainerHighest else colors.surfaceContainerHigh,
            border = BorderStroke(1.dp, if (animate) border else targetBorder),
        ) {
            Row(
                Modifier.heightIn(min = 56.dp).padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (showBackButton || active) {
                    IconButton(onClick = { onBackClick?.invoke() ?: onActiveChange(false) }) {
                        Icon(painterResource(R.drawable.arrow_back), stringResource(R.string.back))
                    }
                } else {
                    IconButton(onClick = { requester.requestFocus() }) {
                        Icon(painterResource(R.drawable.search), stringResource(R.string.search))
                    }
                }

                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    cursorBrush = SolidColor(colors.primary),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.onSurface),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearch(query.text) }),
                    modifier = Modifier.weight(1f).focusRequester(requester)
                        .onFocusChanged {
                            focused = it.isFocused
                            if (it.isFocused && !active) onActiveChange(true)
                        }
                        .semantics { contentDescription = label },
                    decorationBox = { inner ->
                        Box(Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.CenterStart) {
                            if (query.text.isEmpty()) {
                                Text(
                                    label,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = colors.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            inner()
                        }
                    },
                )

                if (query.text.isNotEmpty()) {
                    IconButton(onClick = {
                        onQueryChange(TextFieldValue())
                        onActiveChange(true)
                        requester.requestFocus()
                    }) {
                        Icon(painterResource(R.drawable.close), stringResource(R.string.clear), Modifier.size(20.dp))
                    }
                }

                if (onSearchSourceChange != null) {
                    val online = searchSource == SearchSource.ONLINE
                    val sourceLabel = stringResource(if (online) R.string.online else R.string.local)
                    ExpressiveIconButton(
                        onClick = { onSearchSourceChange(if (online) SearchSource.LOCAL else SearchSource.ONLINE) },
                        modifier = Modifier.semantics { stateDescription = sourceLabel },
                        size = 40.dp,
                        shape = MaterialTheme.shapes.medium,
                        animationsEnabled = animate,
                        containerColor = if (online) colors.primaryContainer else colors.secondaryContainer,
                        contentColor = if (online) colors.onPrimaryContainer else colors.onSecondaryContainer,
                    ) {
                        Icon(
                            painterResource(if (online) R.drawable.globe_search else R.drawable.library_music),
                            contentDescription = stringResource(R.string.search_switch_source),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
    }
}
