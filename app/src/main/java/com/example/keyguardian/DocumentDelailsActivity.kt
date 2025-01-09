package com.example.keyguardian

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DocumentDetailsActivity : AppCompatActivity() {

    private lateinit var linearLayoutContainer: LinearLayout
    private lateinit var buttonSave: Button
    private lateinit var buttonAddRecord: Button
    private lateinit var textDocumentName: TextView
    private var documentMap: MutableMap<String, String> = mutableMapOf()
    private lateinit var documentName: String
    private lateinit var user: String
    private lateinit var token: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_document_details)

        linearLayoutContainer = findViewById(R.id.linearLayoutContainer)
        textDocumentName = findViewById(R.id.textDocumentName)
        buttonAddRecord = findViewById(R.id.buttonAddRecord)
        buttonSave = findViewById(R.id.buttonSave)

        documentName = intent.getStringExtra("document_name") ?: run {
            Toast.makeText(this, "Missing document_name", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        user = intent.getStringExtra("user") ?: run {
            Toast.makeText(this, "Missing user", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        token = intent.getStringExtra("token") ?: run {
            Toast.makeText(this, "Missing token", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        val documentJson = intent.getStringExtra("document_json") ?: run {
            Toast.makeText(this, "Missing document_json", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        textDocumentName.text = documentName

        val gson = Gson()
        documentMap = gson.fromJson(documentJson, object : TypeToken<MutableMap<String, String>>() {}.type)

        documentMap.forEach { (key, value) ->
            addEditTextForKeyValue(key, value)
        }

        buttonAddRecord.setOnClickListener {
            addNewRecord()
        }

        buttonSave.setOnClickListener {
            saveChanges()
        }
    }

    private fun addEditTextForKeyValue(key: String, value: String) {
        val horizontalLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 8
            }
        }

        val keyEditText = EditText(this).apply {
            setText(key)
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                rightMargin = 8
            }
        }

        val valueEditText = EditText(this).apply {
            setText(value)
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        val buttonDelete = Button(this).apply {
            text = "Delete"
            setTextColor(getResources().getColor(android.R.color.white))
            backgroundTintList = getResources().getColorStateList(android.R.color.holo_red_light)
            setOnClickListener {
                showDeleteConfirmationDialog(horizontalLayout)
            }
        }

        horizontalLayout.addView(keyEditText)
        horizontalLayout.addView(valueEditText)
        horizontalLayout.addView(buttonDelete)
        linearLayoutContainer.addView(horizontalLayout)
    }

    private fun addNewRecord() {
        addEditTextForKeyValue("", "")
    }

    private fun showDeleteConfirmationDialog(horizontalLayout: LinearLayout) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirm Delete")
        builder.setMessage("Are you sure you want to delete this record?")
        builder.setPositiveButton("Yes") { dialog, _ ->
            linearLayoutContainer.removeView(horizontalLayout)
            dialog.dismiss()
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun saveChanges() {
        documentMap.clear()
        // Empieza desde el índice 0 para incluir todos los elementos
        for (i in 0 until linearLayoutContainer.childCount) {
            val view = linearLayoutContainer.getChildAt(i)
            if (view is LinearLayout && view.childCount == 3) {  // Asegúrate de que el LinearLayout contiene los tres elementos (keyEditText, valueEditText, buttonDelete)
                val keyEditText = view.getChildAt(0) as EditText
                val valueEditText = view.getChildAt(1) as EditText

                val key = keyEditText.text.toString()
                val value = valueEditText.text.toString()

                if (key.isNotEmpty()) {
                    documentMap[key] = value
                }
            }
        }

        val requestBody = UpdateDocumentRequest(user = user, document_name = documentName, data = documentMap)
        RetrofitClient.getApiServiceWithToken(token).updateDocument(requestBody).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        Toast.makeText(this@DocumentDetailsActivity, responseBody, Toast.LENGTH_SHORT).show()
                    } else {
                        val rawResponse = response.errorBody()?.string()
                        println("Raw response: $rawResponse")
                        Toast.makeText(this@DocumentDetailsActivity, "Unexpected response format", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorResponse = response.errorBody()?.string()
                    println("Error response: $errorResponse")
                    Toast.makeText(this@DocumentDetailsActivity, "Failed to update document: $errorResponse", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Toast.makeText(this@DocumentDetailsActivity, "API call failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
