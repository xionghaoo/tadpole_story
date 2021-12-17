package xh.zero.tadpolestory.ui.home

import dagger.hilt.android.lifecycle.HiltViewModel
import xh.zero.core.paging.PagingViewModel
import xh.zero.tadpolestory.repo.Repository
import xh.zero.tadpolestory.repo.data.Album
import xh.zero.tadpolestory.repo.data.AlbumResponse
import xh.zero.tadpolestory.repo.paging.RecommendAlbumRepository
import javax.inject.Inject

@HiltViewModel
class RecommendViewModel @Inject constructor(
    private val repo: Repository,
    private val recommendRepo: RecommendAlbumRepository
) : PagingViewModel<AlbumResponse, Album>(recommendRepo) {
    fun getGuessLikeAlbums() = repo.getGuessLikeAlbums()
}