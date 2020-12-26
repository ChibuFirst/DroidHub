package com.example.droidhub

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.droidhub.databinding.FragmentSignUpBinding
import com.example.droidhub.util.InputsValidation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.UserProfileChangeRequest

class SignUpFragment : Fragment(R.layout.fragment_sign_up) {

    companion object {
        private const val TAG = "SignUpFragment"
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: FragmentSignUpBinding

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Toast.makeText(requireContext(), "Welcome back, ${currentUser.displayName}", Toast.LENGTH_LONG).show()
            findNavController().navigate(R.id.action_signUpFragment_to_filesFragment)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSignUpBinding.bind(view)

        auth = FirebaseAuth.getInstance()

        binding.editName.addTextChangedListener(InputsValidation(binding.nameLayout))
        binding.editEmail.addTextChangedListener(InputsValidation(binding.emailLayout))
        binding.editPassword.addTextChangedListener(InputsValidation(binding.passwordLayout))

        binding.buttonSignUp.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            signUpUser()
        }

        binding.textLogin.setOnClickListener {
            findNavController().navigate(R.id.action_signUpFragment_to_loginFragment)
        }
    }

    private fun signUpUser() {
        if (binding.editName.text.toString().isEmpty()) {
            binding.nameLayout.error = getString(R.string.field_required)
            binding.editName.requestFocus()
            binding.progressBar.visibility = View.INVISIBLE
            return
        } else {
            binding.nameLayout.error = null
        }

        when {
            binding.editEmail.text.toString().isEmpty() -> {
                binding.emailLayout.error = getString(R.string.field_required)
                binding.editEmail.requestFocus()
                binding.progressBar.visibility = View.INVISIBLE
                return
            }
            !Patterns.EMAIL_ADDRESS.matcher(binding.editEmail.text.toString()).matches() -> {
                binding.emailLayout.error = getString(R.string.invalid_email)
                binding.editEmail.requestFocus()
                binding.progressBar.visibility = View.INVISIBLE
                return
            }
            else -> {
                binding.emailLayout.error = null
            }
        }

        when {
            binding.editPassword.text.toString().isEmpty() -> {
                binding.passwordLayout.error = getString(R.string.field_required)
                binding.editPassword.requestFocus()
                binding.progressBar.visibility = View.INVISIBLE
                return
            }
            binding.editPassword.text.toString().length < 6 -> {
                binding.passwordLayout.error = getString(R.string.password_length_error)
                binding.editPassword.requestFocus()
                binding.progressBar.visibility = View.INVISIBLE
                return
            }
            else -> {
                binding.passwordLayout.error = null
            }
        }

        auth.createUserWithEmailAndPassword(
                binding.editEmail.text.toString(),
                binding.editPassword.text.toString()
        )
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        binding.progressBar.visibility = View.INVISIBLE
                        Log.d(TAG, "createUserWithEmailAndPassword:success")
                        val user = auth.currentUser
                        val profileUpdates = UserProfileChangeRequest.Builder()
                                .setDisplayName(binding.editName.text.toString())
                                .build()
                        user!!.updateProfile(profileUpdates)

                        Toast.makeText(
                                requireContext(), "Registration Successful.",
                                Toast.LENGTH_LONG
                        ).show()
                        findNavController().navigate(R.id.action_signUpFragment_to_filesFragment)
                    } else {
                        // If sign in fails, display a message to the user.
                        binding.progressBar.visibility = View.INVISIBLE
                        if (task.exception is FirebaseAuthUserCollisionException) {
                            Toast.makeText(
                                    requireContext(),
                                    "The email address is already in use by another account.",
                                    Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Log.w(TAG, "createUserWithEmailAndPassword:failure", task.exception)
                            Toast.makeText(
                                    requireContext(), "Authentication failed.",
                                    Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
    }

}