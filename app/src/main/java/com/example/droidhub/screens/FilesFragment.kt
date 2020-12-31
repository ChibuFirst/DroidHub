package com.example.droidhub.screens

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.droidhub.R
import com.example.droidhub.adapter.FileRecyclerAdapter
import com.example.droidhub.databinding.FragmentFilesBinding
import com.example.droidhub.model.File
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FilesFragment : Fragment(R.layout.fragment_files) {

    private lateinit var binding: FragmentFilesBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var fileList: ArrayList<File>
    private lateinit var fileKeys: ArrayList<String>

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (auth.currentUser == null) {
            findNavController().navigate(R.id.action_filesFragment_to_loginFragment)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFilesBinding.bind(view)
        auth = FirebaseAuth.getInstance()
        databaseReference =
                FirebaseDatabase.getInstance().getReference("files").child(auth.currentUser!!.uid)

        binding.imageSignOut.setOnClickListener {
            auth.signOut()
            Toast.makeText(
                    requireContext(),
                    "You've been signed out successfully.",
                    Toast.LENGTH_LONG
            ).show()
            findNavController().navigate(R.id.action_filesFragment_to_loginFragment)
        }

        binding.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
        }

        fileList = ArrayList()
        fileKeys = ArrayList()
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                fileList.clear()
                if (snapshot.exists()) {
                    for (f in snapshot.children) {
                        val file = f.getValue(File::class.java)
                        fileList.add(file!!)
                        fileKeys.add(f.key!!)
                    }
                    if (fileList.size < 1) {
                        binding.recyclerView.visibility = View.GONE
                        binding.textInfo.visibility = View.VISIBLE
                    } else {
                        binding.recyclerView.visibility = View.VISIBLE
                        binding.textInfo.visibility = View.GONE
                        val adapter = FileRecyclerAdapter(requireActivity(), fileList, fileKeys)
                        binding.recyclerView.adapter = adapter
                    }
                    binding.progressBar.visibility = View.GONE
                } else {
                    binding.progressBar.visibility = View.GONE
                    binding.textInfo.visibility = View.VISIBLE
                    binding.recyclerView.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_filesFragment_to_uploadFragment)
        }
    }

}