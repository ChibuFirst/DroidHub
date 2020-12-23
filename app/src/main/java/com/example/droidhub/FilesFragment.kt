package com.example.droidhub

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.droidhub.databinding.FragmentFilesBinding

class FilesFragment : Fragment(R.layout.fragment_files) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentFilesBinding.bind(view)

        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_filesFragment_to_uploadFragment)
        }
    }

}