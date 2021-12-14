package xh.zero.tadpolestory.ui.home

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import xh.zero.core.paging.PagingRepository
import xh.zero.core.paging.PagingViewModel
import xh.zero.tadpolestory.repo.Repository
import xh.zero.tadpolestory.repo.data.Album
import xh.zero.tadpolestory.repo.data.AlbumResponse
import xh.zero.tadpolestory.repo.paging.DayRecommendAlbumRepository
import javax.inject.Inject

@HiltViewModel
class DayRecommendViewModel @Inject constructor(
    private val repo: Repository,
    private val recommendRepo: DayRecommendAlbumRepository
) : PagingViewModel<AlbumResponse, Album>(recommendRepo) {

//    fun uploadPlayRecords() = repo.uploadPlayRecords()

}