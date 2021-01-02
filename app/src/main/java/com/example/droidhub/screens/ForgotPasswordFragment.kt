package com.example.droidhub.screens

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.droidhub.R
import com.example.droidhub.databinding.FragmentForgotPasswordBinding
import com.example.droidhub.util.InputsValidation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class ForgotPasswordFragment : Fragment(R.layout.fragment_forgot_password) {

    companion object {
        private const val TAG = "ForgotPasswordFragment"
    }

    private lateinit var binding: FragmentForgotPasswordBinding
    private lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentForgotPasswordBinding.bind(view)
        auth = FirebaseAuth.getInstance()

        binding.editEmail.addTextChangedListener(InputsValidation(binding.emailLayout))
        binding.buttonReset.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            binding.buttonReset.isEnabled = false
            sendPasswordResetLink()
        }
    }

    private fun sendPasswordResetLink() {
        when {
            binding.editEmail.text.toString().isEmpty() -> {
                binding.emailLayout.error = getString(R.string.field_required)
                binding.editEmail.requestFocus()
                binding.progressBar.visibility = View.INVISIBLE
                binding.buttonReset.isEnabled = true
                return
            }
            !Patterns.EMAIL_ADDRESS.matcher(binding.editEmail.text.toString()).matches() -> {
                binding.emailLayout.error = getString(R.string.invalid_email)
                binding.editEmail.requestFocus()
                binding.progressBar.visibility = View.INVISIBLE
                binding.buttonReset.isEnabled = true
                return
            }
            else -> {
                binding.emailLayout.error = null
            }
        }

        auth.sendPasswordResetEmail(binding.editEmail.text.toString())
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.d(TAG, "sendPasswordResetLink: success")
                        Toast.makeText(requireContext(), "Password reset link sent.", Toast.LENGTH_LONG).show()
                        findNavController().navigate(R.id.action_forgotPasswordFragment_to_loginFragment)
                    } else {
                        if (it.exception is FirebaseAuthInvalidUserException) {
                            Toast.makeText(requireContext(), "No user record found in this email.", Toast.LENGTH_LONG).show()
                        } else {
                            Log.w(TAG, "sendPasswordResetLink: failed", it.exception)
                            Toast.makeText(requireContext(), "Unable to send link.", Toast.LENGTH_LONG).show()
                        }
                    }

                    binding.progressBar.visibility = View.INVISIBLE
                    binding.buttonReset.isEnabled = true
                }
    }
}