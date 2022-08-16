package com.piratesofcode.spyglass.ui.statistics

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.piratesofcode.spyglass.databinding.ActivityStatisticsBinding
import com.piratesofcode.spyglass.ui.main.adapters.StatisticsAdapter
import com.piratesofcode.spyglass.viewmodels.StatisticsViewModel

class StatisticsActivity : AppCompatActivity() {

    private val binding by lazy { ActivityStatisticsBinding.inflate(layoutInflater) }
    private val adapter by lazy { StatisticsAdapter() }
    private val viewModel by viewModels<StatisticsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "User Statistics"

        binding.statsView.adapter = adapter
        binding.statsView.layoutManager = LinearLayoutManager(
            this, LinearLayoutManager.VERTICAL, false
        )

        viewModel.getStatsLiveData().observe(this) { stats ->
            binding.statsProgress.visibility = View.GONE
            if (stats.isNotEmpty()) {
                adapter.setStats(stats)
            } else {
                binding.emptyState.visibility = View.VISIBLE
            }
        }

        binding.statsProgress.visibility = View.VISIBLE
        viewModel.getUserStats()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    companion object {

        @JvmStatic
        fun start(context: Context) {
            Intent(context, StatisticsActivity::class.java)
                .also {
                    context.startActivity(it)
                }
        }
    }
}