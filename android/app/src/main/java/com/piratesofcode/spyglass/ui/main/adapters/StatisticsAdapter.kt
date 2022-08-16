package com.piratesofcode.spyglass.ui.main.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.piratesofcode.spyglass.R
import com.piratesofcode.spyglass.databinding.UserStatsBinding
import com.piratesofcode.spyglass.model.stats.UserStats
import com.piratesofcode.spyglass.model.user.UserRole

class StatisticsAdapter(private var stats: List<UserStats> = listOf()) :
    RecyclerView.Adapter<StatisticsAdapter.StatsViewHolder>() {


    inner class StatsViewHolder(private val binding: UserStatsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(stats: UserStats) {
            binding.apply {
                userName.text = root.context.getString(
                    R.string.first_name_last_name,
                    stats.firstName,
                    stats.lastName
                )
                role.text = stats.userRole.value

                val validPercentage =
                    if ((stats.scannedImproperlyNo + stats.scannedProperlyNo) == 0) {
                        "0"
                    } else {
                        String.format(
                            "%.1f",
                            (stats.scannedProperlyNo.toFloat() / (stats.scannedProperlyNo + stats.scannedImproperlyNo) * 100)
                        )
                    }

                statsContainer.removeAllViews()

                statsContainer.addView(TextView(root.context).apply {
                    text = root.context.getString(
                        R.string.scanned_properly_number,
                        stats.scannedProperlyNo
                    )
                })

                statsContainer.addView(TextView(root.context).apply {
                    text = root.context.getString(
                        R.string.scanned_improperly_number,
                        stats.scannedImproperlyNo
                    )
                })

                statsContainer.addView(TextView(root.context).apply {
                    text = root.context.getString(
                        R.string.properly_scanned_percentage,
                        validPercentage
                    )
                })


                when (stats.userRole.value) {
                    UserRole.REVISER.value -> {
                        statsContainer.addView(TextView(root.context).apply {
                            text = root.context.getString(R.string.revised_number, stats.revisedNo)
                        })
                    }
                    UserRole.ACCOUNTANT.value -> {
                        statsContainer.addView(TextView(root.context).apply {
                            text =
                                root.context.getString(R.string.archived_number, stats.archivedNo)
                        })
                    }
                    UserRole.DIRECTOR.value -> {
                        statsContainer.addView(TextView(root.context).apply {
                            text = root.context.getString(R.string.signed_number, stats.signedNo)
                        })
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): StatisticsAdapter.StatsViewHolder {
        return StatsViewHolder(
            UserStatsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: StatisticsAdapter.StatsViewHolder, position: Int) {
        holder.bind(stats[position])
    }

    override fun getItemCount() = stats.size

    @SuppressLint("NotifyDataSetChanged")
    fun setStats(items: List<UserStats>) {
        this.stats = items
        notifyDataSetChanged()
    }
}
