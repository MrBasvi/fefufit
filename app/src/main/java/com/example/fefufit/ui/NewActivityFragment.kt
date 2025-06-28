package com.example.fefufit.ui

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.Group
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fefufit.R
import com.example.fefufit.model.ActivityType
import com.example.fefufit.model.UserActivity
import com.example.fefufit.ui.viewmodel.ActivityViewModel
import com.google.android.material.button.MaterialButton
import kotlin.random.Random

class NewActivityFragment : Fragment(R.layout.fragment_new_activity) {
    private var selectedActivityType: ActivityType? = null
    private var timer: CountDownTimer? = null
    private var isTimerRunning = false
    private var isPaused = false
    private var elapsedSeconds = 0
    private var startTime: Long = 0
    private var currentActivityId: Long = -1

    private val activityViewModel: ActivityViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activityTypes = listOf(
            ActivityType.BICYCLE,
            ActivityType.RUNNING,
            ActivityType.SWIMMING,
            ActivityType.WORKOUT,
            ActivityType.WALKING
        )

        val selectionState = view.findViewById<Group>(R.id.selectionState)
        val activeState = view.findViewById<Group>(R.id.activeState)
        val recyclerView = view.findViewById<RecyclerView>(R.id.activityTypeRecyclerView)
        val startButton = view.findViewById<MaterialButton>(R.id.startButton)
        val timerText = view.findViewById<TextView>(R.id.timerText)
        val activityTypeText = view.findViewById<TextView>(R.id.activityTypeText)
        val pauseButton = view.findViewById<MaterialButton>(R.id.pauseButton)
        val finishButton = view.findViewById<MaterialButton>(R.id.finishButton)

        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = ActivityTypeAdapter(activityTypes) { selectedType ->
            selectedActivityType = selectedType
            startButton.isEnabled = true
        }

        startButton.setOnClickListener {
            if (selectedActivityType == null) {
                Toast.makeText(requireContext(), "Выберите тип активности", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            startTime = System.currentTimeMillis()

            val activity = UserActivity(
                activityType = selectedActivityType!!,
                startTime = startTime,
                endTime = null,
                distance = null,
                duration = null
            )

            activityViewModel.insertActivity(activity) { activityId ->
                currentActivityId = activityId
            }

            selectionState.visibility = View.GONE
            activeState.visibility = View.VISIBLE
            activityTypeText.text = selectedActivityType!!.getFullName()

            startTimer(timerText)
        }

        pauseButton.setOnClickListener {
            if (isPaused) {
                startTimer(timerText)
                pauseButton.text = "Пауза"
            } else {
                timer?.cancel()
                pauseButton.text = "Продолжить"
            }
            isPaused = !isPaused
        }

        finishButton.setOnClickListener {
            stopTimer()

            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime

            if (currentActivityId != -1L) {
                activityViewModel.updateActivityWithRealData(
                    currentActivityId.toInt(),
                    endTime,
                    duration,
                    generateRandomDistance()
                )
            }
            
            Toast.makeText(requireContext(), "Активность завершена", Toast.LENGTH_SHORT).show()
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun startTimer(timerText: TextView) {
        timer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                elapsedSeconds++
                val hours = elapsedSeconds / 3600
                val minutes = (elapsedSeconds % 3600) / 60
                val secs = elapsedSeconds % 60
                timerText.text = String.format("%02d:%02d:%02d", hours, minutes, secs)
            }

            override fun onFinish() {
            }
        }.start()
        isTimerRunning = true
    }

    private fun stopTimer() {
        isTimerRunning = false
        isPaused = false
        timer?.cancel()
        elapsedSeconds = 0
    }

    private fun generateRandomDistance(): Double {
        return Random.nextDouble(1000.0, 15000.0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopTimer()
    }
}

class ActivityTypeAdapter(
    private val activityTypes: List<ActivityType>,
    private val onActivityTypeSelected: (ActivityType) -> Unit
) : RecyclerView.Adapter<ActivityTypeAdapter.ViewHolder>() {

    private var selectedPosition = -1

    class ViewHolder(val button: MaterialButton) : RecyclerView.ViewHolder(button)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val button = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity_type, parent, false) as MaterialButton
        return ViewHolder(button)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val activityType = activityTypes[position]
        holder.button.text = activityType.getFullName()

        if (position == selectedPosition) {
            holder.button.setBackgroundColor(holder.button.context.getColor(R.color.purple_500))
            holder.button.setTextColor(holder.button.context.getColor(android.R.color.white))
        } else {
            holder.button.setBackgroundColor(holder.button.context.getColor(android.R.color.transparent))
            holder.button.setTextColor(holder.button.context.getColor(R.color.purple_500))
        }

        holder.button.setOnClickListener {
            val previousSelected = selectedPosition
            selectedPosition = holder.adapterPosition
            notifyItemChanged(previousSelected)
            notifyItemChanged(selectedPosition)
            onActivityTypeSelected(activityType)
        }
    }

    override fun getItemCount() = activityTypes.size
} 