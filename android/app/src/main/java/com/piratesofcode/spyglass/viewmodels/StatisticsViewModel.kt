package com.piratesofcode.spyglass.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piratesofcode.spyglass.api.StatisticsRepository
import com.piratesofcode.spyglass.model.stats.UserStats
import kotlinx.coroutines.launch

class StatisticsViewModel : ViewModel() {
    private var statsLiveData: MutableLiveData<List<UserStats>> =
        MutableLiveData<List<UserStats>>()

    fun getStatsLiveData(): LiveData<List<UserStats>> {
        return statsLiveData
    }

    fun getUserStats() {
        viewModelScope.launch {
            statsLiveData.value = StatisticsRepository.getUserStats()
        }
    }

}