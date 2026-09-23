package it.cantarell.linksaver.ui.addlink

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import it.cantarell.linksaver.R
import it.cantarell.linksaver.data.UrlError
import it.cantarell.linksaver.ui.theme.LinkSaverTheme

@Composable
fun AddLinkRoute(viewModel: AddLinkViewModel = viewModel(factory = AddLinkViewModel.Factory)) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AddLinkScreen(state = state, onEvent = viewModel::onEvent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLinkScreen(state: AddLinkUiState, onEvent: (AddLinkEvent) -> Unit, modifier: Modifier = Modifier) {
    val snackbarHostState = remember { SnackbarHostState() }
    val savedMessage = stringResource(R.string.add_link_saved)
    val saveFailedMessage = stringResource(R.string.add_link_save_failed)

    LaunchedEffect(state.savedMessagePending) {
        if (state.savedMessagePending) {
            onEvent(AddLinkEvent.SavedMessageShown)
            snackbarHostState.showSnackbar(savedMessage)
        }
    }
    LaunchedEffect(state.saveFailed) {
        if (state.saveFailed) {
            onEvent(AddLinkEvent.SaveErrorShown)
            snackbarHostState.showSnackbar(saveFailedMessage)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text(stringResource(R.string.add_link_title)) }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = state.url,
                onValueChange = { onEvent(AddLinkEvent.UrlChanged(it)) },
                label = { Text(stringResource(R.string.add_link_url_label)) },
                isError = state.urlError != null,
                supportingText = state.urlError?.let { error -> { Text(stringResource(error.messageRes())) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.name,
                onValueChange = { onEvent(AddLinkEvent.NameChanged(it)) },
                label = { Text(stringResource(R.string.add_link_name_label)) },
                placeholder = { Text(stringResource(R.string.add_link_name_hint)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.icon,
                onValueChange = { onEvent(AddLinkEvent.IconChanged(it)) },
                label = { Text(stringResource(R.string.add_link_icon_label)) },
                placeholder = { Text(stringResource(R.string.add_link_icon_hint)) },
                isError = state.iconError,
                supportingText = if (state.iconError) {
                    { Text(stringResource(R.string.add_link_icon_error)) }
                } else {
                    null
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.category,
                onValueChange = { onEvent(AddLinkEvent.CategoryChanged(it)) },
                label = { Text(stringResource(R.string.add_link_category_label)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.tagInput,
                onValueChange = { onEvent(AddLinkEvent.TagInputChanged(it)) },
                label = { Text(stringResource(R.string.add_link_tags_label)) },
                supportingText = { Text(stringResource(R.string.add_link_tags_hint)) },
                trailingIcon = {
                    IconButton(onClick = { onEvent(AddLinkEvent.AddTag) }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_add),
                            contentDescription = stringResource(R.string.add_link_add_tag),
                        )
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onEvent(AddLinkEvent.AddTag) }),
                modifier = Modifier.fillMaxWidth(),
            )
            if (state.tags.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.tags.forEach { tag ->
                        InputChip(
                            selected = false,
                            onClick = { onEvent(AddLinkEvent.RemoveTag(tag)) },
                            label = { Text(tag) },
                            trailingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.ic_close),
                                    contentDescription = stringResource(R.string.add_link_remove_tag, tag),
                                )
                            },
                        )
                    }
                }
            }
            Button(
                onClick = { onEvent(AddLinkEvent.Save) },
                enabled = !state.isSaving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.add_link_save))
            }
        }
    }
}

private fun UrlError.messageRes(): Int = when (this) {
    UrlError.EMPTY -> R.string.add_link_url_error_empty
    UrlError.INVALID -> R.string.add_link_url_error_invalid
}

@Preview(showBackground = true)
@Composable
private fun AddLinkScreenPreview() {
    LinkSaverTheme {
        AddLinkScreen(
            state = AddLinkUiState(url = "example.com", tags = listOf("news", "tech")),
            onEvent = {},
        )
    }
}
