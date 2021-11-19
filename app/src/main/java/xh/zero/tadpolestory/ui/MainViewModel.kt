package xh.zero.tadpolestory.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import xh.zero.tadpolestory.repo.Repository
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val repo: Repository
) : ViewModel() {
    fun getLoginUrl() = repo.getLoginUrl()
}