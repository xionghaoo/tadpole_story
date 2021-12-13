package xh.zero.tadpolestory.ui.serach

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import xh.zero.core.paging.PagingViewModel
import xh.zero.tadpolestory.repo.Repository
import xh.zero.tadpolestory.repo.data.Album
import xh.zero.tadpolestory.repo.data.AlbumResponse
import xh.zero.tadpolestory.repo.paging.AlbumRepository
import xh.zero.tadpolestory.repo.paging.SearchRepository
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repo: Repository,
    private val searchRepo: SearchRepository
) : PagingViewModel<AlbumResponse, Album>(searchRepo) {

//    fun searchAlbums(q: String) = repo.searchAlbums(q)
    fun getHotKeyword(categoryId: Int) = repo.getHotKeyword(20, categoryId)

    fun getSearchWords(q: String) = repo.getSearchWords(q)

    fun saveSearchRecord(txt: String) = repo.saveSearchHistory(txt)

    fun getHotAlbumsList(categoryId: Int) = repo.getAlbumsList(calcDimension = 1, pageSize = 10, categoryId = categoryId)

    fun loadSearchRecords() = repo.loadAllSearchHistory()
    fun clearSearchHistory() = repo.clearSearchHistory()
}