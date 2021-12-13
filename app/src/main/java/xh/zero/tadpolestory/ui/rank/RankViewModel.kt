package xh.zero.tadpolestory.ui.rank

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import xh.zero.core.paging.PagingViewModel
import xh.zero.tadpolestory.repo.Repository
import xh.zero.tadpolestory.repo.data.Album
import xh.zero.tadpolestory.repo.data.AlbumResponse
import xh.zero.tadpolestory.repo.paging.RankAlbumRepository
import javax.inject.Inject

@HiltViewModel
class RankViewModel @Inject constructor(
    private val rankRepo: RankAlbumRepository,
) : PagingViewModel<AlbumResponse, Album>(rankRepo) {

}