package it.cantarell.linksaver.ui.addlink

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import it.cantarell.linksaver.LinkSaverApplication
import it.cantarell.linksaver.data.Link
import it.cantarell.linksaver.data.LinkInputValidator
import it.cantarell.linksaver.data.LinkRepository
import it.cantarell.linksaver.data.UrlValidation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock

class AddLinkViewModel(
    private val repository: LinkRepository,
    private val clock: Clock,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddLinkUiState())
    val uiState: StateFlow<AddLinkUiState> = _uiState.asStateFlow()

    fun onEvent(event: AddLinkEvent) {
        when (event) {
            is AddLinkEvent.UrlChanged -> _uiState.update { it.copy(url = event.value, urlError = null) }
            is AddLinkEvent.NameChanged -> _uiState.update { it.copy(name = event.value) }
            is AddLinkEvent.IconChanged -> _uiState.update { it.copy(icon = event.value, iconError = false) }
            is AddLinkEvent.CategoryChanged -> _uiState.update { it.copy(category = event.value) }
            is AddLinkEvent.TagInputChanged -> _uiState.update { it.copy(tagInput = event.value) }
            AddLinkEvent.AddTag -> _uiState.update {
                it.copy(tags = LinkInputValidator.mergeTags(it.tags, it.tagInput), tagInput = "")
            }
            is AddLinkEvent.RemoveTag -> _uiState.update { it.copy(tags = it.tags - event.tag) }
            AddLinkEvent.Save -> save()
            AddLinkEvent.SavedMessageShown -> _uiState.update { it.copy(savedMessagePending = false) }
            AddLinkEvent.SaveErrorShown -> _uiState.update { it.copy(saveFailed = false) }
        }
    }

    private fun save() {
        val state = _uiState.value
        if (state.isSaving) return

        val urlValidation = LinkInputValidator.validateUrl(state.url)
        val icon = state.icon.trim().ifEmpty { null }
        val iconError = icon != null && !LinkInputValidator.isSingleEmoji(icon)
        if (urlValidation is UrlValidation.Invalid || iconError) {
            _uiState.update {
                it.copy(urlError = (urlValidation as? UrlValidation.Invalid)?.error, iconError = iconError)
            }
            return
        }
        val url = (urlValidation as UrlValidation.Valid).url
        val link = Link(
            name = state.name.trim().ifEmpty { url },
            url = url,
            icon = icon,
            category = state.category.trim().ifEmpty { null },
            // Tags typed but not yet confirmed with "Add" are saved too.
            tags = LinkInputValidator.mergeTags(state.tags, state.tagInput),
            createdAt = clock.instant(),
        )

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                repository.add(link)
                _uiState.value = AddLinkUiState(savedMessagePending = true)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _uiState.update { it.copy(isSaving = false, saveFailed = true) }
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as LinkSaverApplication
                AddLinkViewModel(app.container.linkRepository, Clock.systemUTC())
            }
        }
    }
}
