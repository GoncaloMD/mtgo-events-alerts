package com.example.mtgoeventsalert.presentation.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.mtgoeventsalert.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainFragment : Fragment() {

    private val viewModel: MainViewModel by viewModels()
    
    private lateinit var nameEditText: EditText
    private lateinit var searchButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews(view)
        setupObservers()
        setupListeners()
        
        (activity as AppCompatActivity).supportActionBar?.title = "MTGO Events Alert"
    }

    private fun setupViews(view: View) {
        nameEditText = view.findViewById(R.id.nameEditText)
        searchButton = view.findViewById(R.id.saveButton)
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe username changes
                launch {
                    viewModel.username.collect { username ->
                        if (nameEditText.text.toString() != username) {
                            nameEditText.setText(username)
                        }
                    }
                }

                // Observe loading state
                launch {
                    viewModel.isLoading.collect { isLoading ->
                        searchButton.isEnabled = !isLoading
                        searchButton.text = if (isLoading) "Starting..." else "Search"
                    }
                }
                // Observe error messages
                launch {
                    viewModel.errorMessage.collect { error ->
                        error?.let {
                            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                            viewModel.clearError()
                        }
                    }
                }

                // Observe navigation events
                launch {
                    viewModel.navigationEvent.collect { event ->
                        when (event) {
                            is MainViewModel.NavigationEvent.NavigateToTournaments -> {
                                val action = MainFragmentDirections.actionMainFragmentToDisplayFragment(event.username)
                                findNavController().navigate(action)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        nameEditText.doOnTextChanged { text, _, _, _ ->
            viewModel.updateUsername(text.toString())
        }

        searchButton.setOnClickListener {
            viewModel.onSearchClick()
        }
    }
}