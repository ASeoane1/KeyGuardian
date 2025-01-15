package com.example.keyguardian

import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import org.json.JSONObject
import java.util.Base64

class DocumentListAdapter(private val context: Context, private var data: MutableList<String>, private val token: String, private val user: String) :
    RecyclerView.Adapter<DocumentListAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.document_text)
        val buttonEdit: Button = itemView.findViewById(R.id.button_edit)
        val buttonDelete: Button = itemView.findViewById(R.id.button_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_document_list, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val documentName = data[position]
        holder.textView.text = documentName

        holder.buttonEdit.setOnClickListener {
            showEnterPasswordDialog(documentName) { password ->
                val requestBody = DocumentRequest(user = user, document_name = documentName)

                RetrofitClient.getApiServiceWithToken(token).getDocument(requestBody).enqueue(object : Callback<DocumentResponse> {
                    override fun onResponse(call: Call<DocumentResponse>, response: Response<DocumentResponse>) {
                        if (response.isSuccessful) {
                            val documentResponse = response.body()
                            val json = Gson().toJson(documentResponse?.response)
                            val decryptedJson = decryptJson(json, password)
                            val intent = Intent(context, DocumentDetailsActivity::class.java).apply {
                                putExtra("document_json", decryptedJson)
                                putExtra("document_name", documentName)
                                putExtra("user", user)
                                putExtra("token", token)
                            }
                            context.startActivity(intent)
                        } else {
                            Toast.makeText(context, "Error in API", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<DocumentResponse>, t: Throwable) {
                        Toast.makeText(context, "Failure in API response", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }

        holder.buttonDelete.setOnClickListener {
            val position = holder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val builder = AlertDialog.Builder(context)
                builder.setTitle("Confirm Delete")
                builder.setMessage("Are you sure you want to delete this document?")
                builder.setPositiveButton("Yes") { dialog, _ ->
                    val documentName = data[position]
                    val requestBody = DocumentRequest(user = user, document_name = documentName)

                    RetrofitClient.getApiServiceWithToken(token).deleteDocument(requestBody).enqueue(object : Callback<String> {
                        override fun onResponse(call: Call<String>, response: Response<String>) {
                            if (response.isSuccessful) {
                                Toast.makeText(context, "Document deleted successfully!", Toast.LENGTH_SHORT).show()
                                data.removeAt(position)
                                notifyItemRemoved(position)
                                notifyItemRangeChanged(position, data.size)
                            } else {
                                Toast.makeText(context, "Failed to delete document: ${response.errorBody()?.string()}", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<String>, t: Throwable) {
                            Toast.makeText(context, "API call failed: ${t.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
                    dialog.dismiss()
                }
                builder.setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                builder.show()
            }
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    private fun showEnterPasswordDialog(documentName: String, onPasswordEntered: (String) -> Unit) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Enter Password")

        val passwordInput = android.widget.EditText(context)
        passwordInput.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        builder.setView(passwordInput)

        builder.setPositiveButton("Submit") { _, _ ->
            val password = passwordInput.text.toString().trim()
            if (password.isNotEmpty()) {
                SensitiveDataStore.setPassword(password)
                onPasswordEntered(password)
            } else {
                Toast.makeText(context, "Password cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    private fun decryptJson(json: String, password: String): String {
        return try {
            val jsonObject = JSONObject(json)
            val decryptedJson = JSONObject()

            jsonObject.keys().forEach { key ->
                try {
                    val decryptedKey = AESUtils.decrypt(key, password)
                    val decryptedValue = AESUtils.decrypt(jsonObject.optString(key, ""), password)

                    decryptedJson.put(decryptedKey, decryptedValue)
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to decrypt key or value: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            decryptedJson.toString()
        } catch (e: Exception) {
            Toast.makeText(context, "Decryption failed: ${e.message}", Toast.LENGTH_LONG).show()
            "{}"
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun isValidBase64(value: String): Boolean {
        return try {
            Base64.getDecoder().decode(value)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }
}
