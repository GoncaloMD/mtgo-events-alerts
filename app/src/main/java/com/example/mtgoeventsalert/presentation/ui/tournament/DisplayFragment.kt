package com.example.mtgoeventsalert.presentation.ui.tournament

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.mtgoeventsalert.DisplayFragmentArgs
import com.example.mtgoeventsalert.R
import com.example.mtgoeventsalert.presentation.ui.state.TournamentUiState
import com.example.mtgoeventsalert.presentation.ui.state.MonitoringUiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DisplayFragment : Fragment() {

    private val args: DisplayFragmentArgs by navArgs()
    private val viewModel: TournamentViewModel by viewModels()
    
    private lateinit var nameTextView: TextView
    private lateinit var resultTextView: TextView
    private lateinit var returnButton: Button
    private var refreshButton: Button? = null
    private var monitoringButton: Button? = null
    private var statusTextView: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_display, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViews(view)
        setupClickListeners()
        observeViewModel()
        viewModel.loadTournaments(args.name)
    }
    private fun initializeViews(view: View) {
        nameTextView = view.findViewById(R.id.nameTextView)
        resultTextView = view.findViewById(R.id.resultTextView)
        returnButton = view.findViewById(R.id.returnButton)
        
        // These are optional - only use if they exist in the layout
        refreshButton = view.findViewById(R.id.refreshButton)
        monitoringButton = view.findViewById(R.id.monitoringButton)
        statusTextView = view.findViewById(R.id.statusTextView)
        
        nameTextView.text = "Tournament status: ${args.name}"
    }

    private fun setupClickListeners() {
        returnButton.setOnClickListener {
            findNavController().popBackStack()
        }
        
        refreshButton?.setOnClickListener {
            viewModel.refreshTournaments()
        }
        
        monitoringButton?.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                when (viewModel.monitoringState.value) {
                    is MonitoringUiState.Active -> viewModel.stopMonitoring()
                    else -> viewModel.startMonitoring()
                }
            }
        }
    }
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe tournament state
                launch {
                    viewModel.tournamentState.collect { state ->
                        handleTournamentState(state)
                    }
                }
                
                // Observe monitoring state
                launch {
                    viewModel.monitoringState.collect { state ->
                        handleMonitoringState(state)
                    }
                }
            }
        }
    }

    private fun handleTournamentState(state: TournamentUiState) {
        when (state) {
            is TournamentUiState.Loading -> {
                resultTextView.text = "Loading tournaments..."
                refreshButton?.visibility = View.GONE
                refreshButton?.isEnabled = false
            }
            
            is TournamentUiState.Success -> {
                displayTournaments(state)
                refreshButton?.visibility = View.VISIBLE
                refreshButton?.isEnabled = true
            }
            
            is TournamentUiState.Error -> {
                resultTextView.text = "Error: ${state.message}"
                refreshButton?.visibility = if (state.canRetry) View.VISIBLE else View.GONE
                refreshButton?.isEnabled = state.canRetry
            }
            
            is TournamentUiState.Empty -> {
                resultTextView.text = "No active tournaments found for ${args.name}"
                refreshButton?.visibility = View.VISIBLE
                refreshButton?.isEnabled = true
            }
        }
    }
    private fun handleMonitoringState(state: MonitoringUiState) {
        when (state) {
            is MonitoringUiState.Stopped -> {
                monitoringButton?.text = "Start Monitoring"
                monitoringButton?.isEnabled = true
                monitoringButton?.visibility = View.VISIBLE
                statusTextView?.text = "Monitoring stopped"
                statusTextView?.visibility = View.VISIBLE
            }
            
            is MonitoringUiState.Starting -> {
                monitoringButton?.text = "Starting..."
                monitoringButton?.isEnabled = false
                statusTextView?.text = "Starting monitoring..."
                statusTextView?.visibility = View.VISIBLE
            }
            
            is MonitoringUiState.Active -> {
                monitoringButton?.text = "Stop Monitoring"
                monitoringButton?.isEnabled = true
                monitoringButton?.visibility = View.VISIBLE
                statusTextView?.text = "Monitoring ${state.tournamentCount} tournament(s)\nLast update: ${formatTimestamp(state.lastUpdate)}"
                statusTextView?.visibility = View.VISIBLE
            }
            
            is MonitoringUiState.Error -> {
                monitoringButton?.text = "Start Monitoring"
                monitoringButton?.isEnabled = true
                statusTextView?.text = "Monitoring error: ${state.message}"
                statusTextView?.visibility = View.VISIBLE
            }
        }
    }
    private fun displayTournaments(state: TournamentUiState.Success) {
        val stringBuilder = StringBuilder()
        
        if (state.tournaments.isEmpty()) {
            stringBuilder.append("No active tournaments found")
        } else {
            state.tournaments.forEachIndexed { index, tournament ->
                stringBuilder.append("Tournament ${index + 1}:\n")
                stringBuilder.append("Name: ${tournament.name}\n")
                stringBuilder.append("Format: ${tournament.format}\n")
                stringBuilder.append("Status: ${tournament.status.currentStatus}\n")
                stringBuilder.append("Record: ${tournament.status.record}\n")
                
                tournament.status.roundNumber?.let { round ->
                    stringBuilder.append("Round: $round\n")
                }
                
                if (tournament.status.isWaitingForRound) {
                    stringBuilder.append("⏰ Waiting for next round\n")
                }
                
                if (tournament.status.hasEnded) {
                    stringBuilder.append("🏁 Tournament has ended\n")
                }
                
                stringBuilder.append("Last Updated: ${formatTimestamp(tournament.status.lastUpdated)}\n")
                
                if (index < state.tournaments.size - 1) {
                    stringBuilder.append("\n---\n\n")
                }
            }
        }
        
        resultTextView.text = stringBuilder.toString()
    }

    private fun formatTimestamp(timestamp: Long): String {
        val currentTime = System.currentTimeMillis()
        val diff = currentTime - timestamp
        
        return when {
            diff < 60000 -> "Just now"
            diff < 3600000 -> "${diff / 60000} minutes ago"
            diff < 86400000 -> "${diff / 3600000} hours ago"
            else -> "${diff / 86400000} days ago"
        }
    }
}