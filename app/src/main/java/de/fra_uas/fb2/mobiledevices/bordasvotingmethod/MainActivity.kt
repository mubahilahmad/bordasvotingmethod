package de.fra_uas.fb2.mobiledevices.bordasvotingmethod

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var editTextNumberOfOptions: EditText
    private lateinit var editTextEnterAllOptions: EditText
    private lateinit var buttonAddAVote: Button
    private var validOptionCount: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editTextNumberOfOptions = findViewById(R.id.editTextNumberOfOptions)
        editTextEnterAllOptions = findViewById(R.id.editTextEnterAllOptions)
        buttonAddAVote = findViewById(R.id.buttonAddAVote)

        editTextNumberOfOptions.hint = "3"

        editTextNumberOfOptions.filters =
            arrayOf(InputFilter { source, start, end, dest, dstart, dend ->
                val updatedText = StringBuilder(dest).apply {
                    replace(dstart, dend, source.subSequence(start, end).toString())
                }.toString()

                val value = updatedText.toIntOrNull()

                if (value != null && value in 2..10) {
                    validOptionCount = value
                    null
                } else {
                    if (updatedText == "1") {
                        return@InputFilter null
                    }
                    ""
                }
            })

        // TextWatcher hinzufügen, um die Eingabe im zweiten EditText zu überwachen
        editTextEnterAllOptions.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Validate options without callback
                validateOptions(null)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        buttonAddAVote.setOnClickListener {
            // Optionen validieren und an VoteActivity senden
            validateOptions { options ->
                val intent = Intent(this, VoteActivity::class.java)
                intent.putStringArrayListExtra("options", ArrayList(options))
                intent.putExtra("numberOfOptions", validOptionCount)
                startActivity(intent)
            }
        }
    }

    private fun validateOptions(onValidOptions: ((List<String>) -> Unit)?) {
        val numberInput = editTextNumberOfOptions.text.toString()
        if (numberInput.isNotEmpty()) {
            val maxOptions = numberInput.toInt()
            val optionsText = editTextEnterAllOptions.text.toString().trim()

            // Check if there are no options entered
            if (optionsText.isEmpty()) {
                return
            }

            // Split the options by comma, trim them, and filter out any empty options
            val options = optionsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val optionCount = options.size

            // Adjust the options based on the number of options required
            val adjustedOptions = if (optionCount < maxOptions) {
                // If there are less options, fill up with "Option x"
                options + (optionCount + 1..maxOptions).map { "Option $it" }
            } else {
                // If there are more options, take only the required number
                options.take(maxOptions)
            }

            // Show a Toast message if there are more options than allowed
            if (optionCount > maxOptions) {
                Toast.makeText(
                    this,
                    "You can only enter up to $maxOptions options",
                    Toast.LENGTH_SHORT
                ).show()
            }

            // Pass the adjusted options to the callback if provided
            onValidOptions?.invoke(adjustedOptions)
        }
    }
}


