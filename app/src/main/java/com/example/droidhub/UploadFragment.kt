package com.example.droidhub

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.droidhub.databinding.FragmentUploadBinding
import com.example.droidhub.model.File
import com.example.droidhub.util.InputsValidation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*

class UploadFragment : Fragment(R.layout.fragment_upload) {

    companion object {
        private const val FILE_REQUEST_CODE = 1001
        private const val TAG = "UploadFragment"
    }

    private lateinit var binding: FragmentUploadBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var storageReference: StorageReference
    private lateinit var databaseReference: DatabaseReference
    private var filePath: Uri? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (auth.currentUser == null) {
            findNavController().navigate(R.id.action_uploadFragment_to_loginFragment)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentUploadBinding.bind(view)
        auth = FirebaseAuth.getInstance()
        storageReference = FirebaseStorage.getInstance().reference
        databaseReference = FirebaseDatabase.getInstance().reference

        binding.editFileName.addTextChangedListener(InputsValidation(binding.fileLayout))
        binding.buttonChoose.setOnClickListener {
            binding.buttonUpload.isEnabled = false
            val intent = Intent().apply {
                type = "*/*"
                action = Intent.ACTION_GET_CONTENT
            }
            startActivityForResult(Intent.createChooser(intent, "Select File"), FILE_REQUEST_CODE)
        }
        binding.buttonUpload.setOnClickListener {
            uploadFile()
        }
    }

    private fun uploadFile() {
        if (binding.editFileName.text.toString().isEmpty()) {
            binding.fileLayout.error = getString(R.string.field_required)
            binding.editFileName.requestFocus()
            return
        }

        if (filePath != null) {
            binding.progressBar.visibility = View.VISIBLE
            binding.buttonUpload.isEnabled = false

            val ref = storageReference.child("files/" + UUID.randomUUID().toString())
            ref.putFile(filePath!!).continueWithTask { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "uploadFile: failed", task.exception)
                    Toast.makeText(requireContext(), "Unable to upload file.", Toast.LENGTH_LONG)
                        .show()
                }
                ref.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "uploadFile: success")
                    val downloadUri = task.result
                    val file = File(binding.editFileName.text.toString(), downloadUri.toString())
                    databaseReference.child("files").child(auth.currentUser!!.uid).push()
                        .setValue(file)
                        .addOnCompleteListener { t ->
                            if (t.isSuccessful) {
                                Log.d(TAG, "addToDatabase: success")
                                findNavController().navigate(R.id.action_uploadFragment_to_filesFragment)
                                Toast.makeText(
                                    requireContext(),
                                    "File upload successful.",
                                    Toast.LENGTH_LONG
                                )
                                    .show()
                            } else {
                                Log.w(TAG, "addToDatabase: failed", t.exception)
                                Toast.makeText(
                                    requireContext(),
                                    "Not saved to database",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                        }

                    binding.progressBar.visibility = View.INVISIBLE
                    binding.buttonChoose.isEnabled = true
                    binding.buttonUpload.isEnabled = true
                } else {
                    Log.w(TAG, "uploadFile: failed", task.exception)
                    Toast.makeText(requireContext(), "File upload failed.", Toast.LENGTH_LONG)
                        .show()
                    binding.progressBar.visibility = View.INVISIBLE
                    binding.buttonChoose.isEnabled = true
                    binding.buttonUpload.isEnabled = true
                }
            }
        } else {
            binding.buttonChoose.isEnabled = true
            Toast.makeText(requireContext(), "Select file to upload.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null && data.data != null) {
                binding.buttonChoose.isEnabled = false
                binding.buttonUpload.isEnabled = true
                filePath = data.data
                Toast.makeText(requireContext(), "File chosen.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "No file selected.", Toast.LENGTH_LONG).show()
            }
        }
    }

}