package com.example.keyguardian

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        val buttonRegister = view.findViewById<Button>(R.id.buttonRegister)
        val textEmail = view.findViewById<EditText>(R.id.textEmailAddress)
        val textPassword = view.findViewById<EditText>(R.id.textPassword)
        val textPasswordRepeat = view.findViewById<EditText>(R.id.textPasswordRepeat)

        if ((textPassword.text.toString() != textPasswordRepeat.text.toString()) && textPassword.text.toString() != "") {
            buttonRegister.isEnabled = false
        }

        buttonRegister.setOnClickListener {
            val email = textEmail.text.toString()
            val password = textPassword.text.toString()
            val passwordRepeat = textPasswordRepeat.text.toString()

            if (password != passwordRepeat && password != "") {
                Toast.makeText(requireContext(), "Provide a valid email and password", Toast.LENGTH_SHORT).show()
            } else {
                val requestBody = AuthRequest(email, password)

                RetrofitClient.getApiService().register(requestBody).enqueue(object : Callback<RegisterResponse> {
                    override fun onResponse(
                        call: Call<RegisterResponse>,
                        response: Response<RegisterResponse>
                    ) {
                        if (response.isSuccessful) {
                            val authResponse = response.body()
                            Toast.makeText(requireContext(), authResponse?.message, Toast.LENGTH_SHORT).show()

                            textEmail.setText("")
                            textPassword.setText("")
                            textPasswordRepeat.setText("")

                            findNavController().navigate(R.id.nav_login)
                        } else {
                            Toast.makeText(requireContext(), "Error during Sign Up", Toast.LENGTH_SHORT).show()
                            textPassword.setText("")
                        }
                    }

                    override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                        Toast.makeText(requireContext(), "Error during Sign Up", Toast.LENGTH_SHORT).show()
                        textPassword.setText("")
                    }
                })
            }
        }

        return view
    }
}
