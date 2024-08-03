package com.imcys.bilibilias.tool

sealed interface SearchResultUiState {
    data object Loading : SearchResultUiState
    data object EmptyQuery : SearchResultUiState

    data object LoadFailed : SearchResultUiState
    data class Success(
        val pic: String = "",
        val title: String = "",
        val desc: String = "",
        val view: String = "",
        val danmaku: String = "",
        val aid: Long = 0,
        val bvid: String = "",
        val cid: Long = 0,
        val epId: String = ""
    ) : SearchResultUiState
}
