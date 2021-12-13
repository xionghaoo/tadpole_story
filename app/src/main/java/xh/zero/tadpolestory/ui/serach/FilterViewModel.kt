package xh.zero.tadpolestory.ui.serach

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import xh.zero.core.paging.PagingViewModel
import xh.zero.tadpolestory.repo.Repository
import xh.zero.tadpolestory.repo.data.Album
import xh.zero.tadpolestory.repo.data.AlbumResponse
import xh.zero.tadpolestory.repo.paging.AlbumRepository
import javax.inject.Inject

@HiltViewModel
class FilterViewModel @Inject constructor(
    private val repo: Repository,
    private val albumRepo: AlbumRepository
) : PagingViewModel<AlbumResponse, Album>(albumRepo) {

    fun getMetadataList(id: Int) = repo.getMetadataList(id)
}