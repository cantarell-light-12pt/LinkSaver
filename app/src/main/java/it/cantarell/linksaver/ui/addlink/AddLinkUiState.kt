package it.cantarell.linksaver.ui.addlink

import it.cantarell.linksaver.data.UrlError

data class AddLinkUiState(
    val url: String = "",
    val name: String = "",
    val icon: String = "",
    val category: String = "",
    val tagInput: String = "",
    val tags: List<String> = emptyList(),
    val urlError: UrlError? = null,
    val iconError: Boolean = false,
    val isSaving: Boolean = false,
    val saveFailed: Boolean = false,
    val savedMessagePending: Boolean = false,
)

sealed interface AddLinkEvent {
    data class UrlChanged(val value: String) : AddLinkEvent
    data class NameChanged(val value: String) : AddLinkEvent
    data class IconChanged(val value: String) : AddLinkEvent
    data class CategoryChanged(val value: String) : AddLinkEvent
    data class TagInputChanged(val value: String) : AddLinkEvent
    data object AddTag : AddLinkEvent
    data class RemoveTag(val tag: String) : AddLinkEvent
    data object Save : AddLinkEvent
    data object SavedMessageShown : AddLinkEvent
    data object SaveErrorShown : AddLinkEvent
}
