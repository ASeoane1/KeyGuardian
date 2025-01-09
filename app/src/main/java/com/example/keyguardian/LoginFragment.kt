package com.example.keyguardian

import HomeViewModel
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.navigation.NavigationView
import androidx.navigation.fragment.findNavController
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        val buttonLogin = view.findViewById<Button>(R.id.buttonLogin)
        val buttonRegister = view.findViewById<Button>(R.id.buttonRegister)
        val textEmail = view.findViewById<EditText>(R.id.textEmailAddress)
        val textPassword = view.findViewById<EditText>(R.id.textPassword)

        buttonLogin.setOnClickListener {
            val email = textEmail.text.toString()
            val password = textPassword.text.toString()

            val requestBody = AuthRequest(email = email, password = password)

            RetrofitClient.getApiService().authenticate(requestBody).enqueue(object : Callback<AuthResponse> {
                override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                    if (response.isSuccessful) {
                        val authResponse = response.body()
                        if (authResponse != null) {
                            val token = authResponse.token
                            val userId = authResponse.user_id

                            // Crear shared preference para datos de login
                            val userDataPreference = requireContext().getSharedPreferences("userData", Context.MODE_PRIVATE)
                            with(userDataPreference.edit()) {
                                putString("token", token)
                                putString("userId", userId)
                                apply()
                            }

                            val navView = requireActivity().findViewById<NavigationView>(R.id.nav_view)
                            val headerView = navView.getHeaderView(0)
                            val subtitle = headerView.findViewById<TextView>(R.id.textEmail)
                            subtitle.text = email

                            val menu = navView.menu
                            val itemLogin = menu.findItem(R.id.nav_login)
                            val itemRegister = menu.findItem(R.id.nav_register)
                            val itemLogout = menu.findItem(R.id.nav_logout)

                            itemLogin.setTitle("Switch Account")
                            itemLogout.isVisible = true
                            itemRegister.isVisible = false

                            val homeViewModel = ViewModelProvider(requireActivity()).get(HomeViewModel::class.java)
                            homeViewModel.updateToken(token)
                            homeViewModel.updateUserId(userId)

                            Toast.makeText(requireContext(), "Login successful", Toast.LENGTH_SHORT).show()

                            textEmail.setText("")
                            textPassword.setText("")

                            findNavController().navigate(R.id.nav_home)
                        } else {
                            Toast.makeText(requireContext(), "Error en la respuesta de la API", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Email o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                        textPassword.setText("")
                    }
                }

                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error en la llamada a la API", Toast.LENGTH_SHORT).show()
                }
            })
        }

        buttonRegister.setOnClickListener {
            findNavController().navigate(R.id.nav_register)
        }

        return view
    }
}
