package com.example.mtgoeventsalert

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController

class DisplayFragment : Fragment() {

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_display, container, false)

        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        val nameTextView = view.findViewById<TextView>(R.id.nameTextView)
        val returnButton = view.findViewById<Button>(R.id.returnButton)

        // Set the name in the TextView
        val name = viewModel.name.value
        nameTextView.text = "Hello, $name"

        // Return to the previous fragment
        returnButton.setOnClickListener {
            findNavController().popBackStack()
        }

        return view
    }
}
