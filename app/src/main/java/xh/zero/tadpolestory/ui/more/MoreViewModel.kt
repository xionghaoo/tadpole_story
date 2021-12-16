package xh.zero.tadpolestory.ui.more

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import xh.zero.tadpolestory.repo.Repository
import javax.inject.Inject

@HiltViewModel
class MoreViewModel @Inject constructor(
    val repo: Repository
) : ViewModel() {
    fun getSubscribeAlbumsIds() = repo.getSubscribeAlbumsIds()
    fun getRecentAlbumsIds() = repo.getRecentAlbumsIds()
    fun getAlbumsForIds(ids: String) = repo.getAlbumsForIds(ids)
}