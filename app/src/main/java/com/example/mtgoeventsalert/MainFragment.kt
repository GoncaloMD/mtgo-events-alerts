package com.example.mtgoeventsalert

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController

class MainFragment : Fragment() {

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        (activity as AppCompatActivity).supportActionBar?.title = "MTGO Events Alert"

        val sharedPref = activity?.getSharedPreferences("MTGOEventsAlert", Context.MODE_PRIVATE)

        val view = inflater.inflate(R.layout.fragment_main, container, false)

        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        val nameEditText = view.findViewById<EditText>(R.id.nameEditText)
        val saveButton = view.findViewById<Button>(R.id.saveButton)

        // Save the name and navigate to DisplayFragment
        saveButton.setOnClickListener {
            val name = nameEditText.text.toString()
            if (name.isNotEmpty()) {
                viewModel.saveName(name)
                val sharedPref = activity?.getSharedPreferences("MTGOEventsAlert", Context.MODE_PRIVATE)
                with(sharedPref?.edit()) {
                    this?.putString("mtgo-username", name)
                    this?.apply()
                }
                val action = MainFragmentDirections.actionMainFragmentToDisplayFragment(name)
                findNavController().navigate(action)
            }
        }

        return view
    }
}
