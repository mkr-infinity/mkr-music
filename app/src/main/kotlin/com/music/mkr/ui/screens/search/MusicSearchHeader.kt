/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.screens.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.music.mkr.R
import com.music.mkr.constants.SearchSource
import com.music.mkr.ui.component.mkrPressFeedback

/**
 * Premium, bespoke Music Search Dock that replaces the stock Material 3 SearchBar.
 * Features an integrated provider switcher (Cloud/YouTube vs Local Library),
 * dynamic animated placeholders, clear action, and smooth focus transitions.
 */
@Composable
fun MusicSearchHeader(
    query: TextFieldValue,
    onQueryChange: (TextFieldValue) -> Unit,
    onSearch: (String) -> Unit,
    active: Boolean,
    onActiveChange: (Boolean) -> Unit,
    searchSource: SearchSource,
    onSearchSourceChange: (SearchSource) -> Unit,
    pureBlack: Boolean,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester? = null,
    showBackButton: Boolean = false,
    onBackClick: (() -> Unit)? = null
) {
    val containerBg = if (pureBlack) Color(0xFF141518) else MaterialTheme.colorScheme.surfaceContainerHigh
    val borderColor = if (pureBlack) Color(0xFF26282E) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(22.dp),
        color = containerBg,
        border = BorderStroke(1.dp, borderColor),
        tonalElevation = 2.dp,
        shadowElevation = if (pureBlack) 0.dp else 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Leading Icon: Back button when active or forced, Search icon otherwise
            if (showBackButton || active) {
                IconButton(
                    onClick = {
                        if (onBackClick != null) {
                            onBackClick()
                        } else {
                            onActiveChange(false)
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .mkrPressFeedback()
                ) {
                    Icon(
                        painter = painterResource(R.drawable.arrow_back),
                        contentDescription = stringResource(R.string.dismiss),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onActiveChange(true)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.search),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Text Input Field with Dynamic Placeholder
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onActiveChange(true)
                    },
                contentAlignment = Alignment.CenterStart
            ) {
                BasicTextField(
                    value = query,
                    onValueChange = {
                        onQueryChange(it)
                        if (!active) onActiveChange(true)
                    },
                    singleLine = true,
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearch(query.text) }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused && !active) {
                                onActiveChange(true)
                            }
                        }
                        .let { base ->
                            if (focusRequester != null) base.focusRequester(focusRequester) else base
                        },
                    decorationBox = { innerTextField ->
                        if (query.text.isEmpty()) {
                            DynamicSearchPlaceholder(
                                searchSource = searchSource,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    fontSize = 15.sp
                                )
                            )
                        }
                        innerTextField()
                    }
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Trailing Actions: Clear button when non-empty, and Provider Switcher Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                AnimatedVisibility(
                    visible = query.text.isNotEmpty(),
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    IconButton(
                        onClick = { onQueryChange(TextFieldValue("")) },
                        modifier = Modifier
                            .size(34.dp)
                            .mkrPressFeedback()
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.close),
                            contentDescription = stringResource(R.string.clear),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Provider switcher pill (Cloud vs Local Library)
                Surface(
                    onClick = {
                        val nextSource = if (searchSource == SearchSource.ONLINE) {
                            SearchSource.LOCAL
                        } else {
                            SearchSource.ONLINE
                        }
                        onSearchSourceChange(nextSource)
                    },
                    shape = CircleShape,
                    color = if (searchSource == SearchSource.ONLINE) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
                    } else {
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f)
                    },
                    modifier = Modifier
                        .size(34.dp)
                        .mkrPressFeedback()
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(
                                if (searchSource == SearchSource.ONLINE) {
                                    R.drawable.globe_search
                                } else {
                                    R.drawable.library_music
                                }
                            ),
                            contentDescription = stringResource(
                                if (searchSource == SearchSource.ONLINE) R.string.online else R.string.local
                            ),
                            tint = if (searchSource == SearchSource.ONLINE) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            },
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }
        }
    }
}
