package de.fra_uas.fb2.mobiledevices.bordasvotingmethod

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var editTextNumberOfOptions: EditText
    private lateinit var editTextEnterAllOptions: EditText
    private lateinit var buttonAddAVote: Button
    private lateinit var buttonStartOver: Button
    private lateinit var tvShowVotingResult: TextView
    private lateinit var tvNumberOfVotesSoFarNumber: TextView
    private lateinit var switchShowVotingResult: Switch
    private lateinit var startForResult: ActivityResultLauncher<Intent> // Neuer ActivityResultLauncher
    private var validOptionCount: Int? = null
    private var votingResults: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialisiere den ActivityResultLauncher
        startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) { // Überprüfung auf RESULT_OK
                val data = result.data
                val results = data?.getStringExtra("votingResults")
                results?.let {
                    votingResults = it
                    if (switchShowVotingResult.isChecked) {
                        tvShowVotingResult.text = it
                    }

                    // Inkrementiere die Anzahl der Stimmen
                    val currentVotes = tvNumberOfVotesSoFarNumber.text.toString().toInt()
                    tvNumberOfVotesSoFarNumber.text = (currentVotes + 1).toString()
                }
            } else if (result.resultCode == RESULT_CANCELED) { // Überprüfung auf RESULT_CANCELED
                val data = result.data
                val isCancelled = data?.getBooleanExtra("cancelled", false) ?: false
                if (isCancelled) {
                    Toast.makeText(this, getString(R.string.toast_vote_cancelled), Toast.LENGTH_SHORT).show() // Zeige den Toast an
                }
            }
        }

        editTextNumberOfOptions = findViewById(R.id.editTextNumberOfOptions)
        editTextEnterAllOptions = findViewById(R.id.editTextEnterAllOptions)
        buttonAddAVote = findViewById(R.id.buttonAddAVote)
        buttonStartOver = findViewById(R.id.buttonStartOver)
        tvShowVotingResult = findViewById(R.id.tvShowVotingResult)
        tvNumberOfVotesSoFarNumber = findViewById(R.id.tvNumberOfVotesSoFarNumber)
        switchShowVotingResult = findViewById(R.id.switchShowVotingResult)

        tvNumberOfVotesSoFarNumber.text = "0"

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

        // Add TextWatcher to monitor input in the EditText
        editTextEnterAllOptions.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val currentVotes = tvNumberOfVotesSoFarNumber.text.toString().toInt()
                if (currentVotes > 0) { // Überprüfe, ob die Anzahl der Stimmen größer als 0 ist
                    tvNumberOfVotesSoFarNumber.text = "0"
                    tvShowVotingResult.text = ""
                    Toast.makeText(this@MainActivity, getString(R.string.toast_votes_resetted), Toast.LENGTH_SHORT).show() // Zeige den Toast an
                } else {
                    tvNumberOfVotesSoFarNumber.text = "0"
                    tvShowVotingResult.text = ""
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Add TextWatcher to reset tvNumberOfVotesSoFarNumber and clear tvShowVotingResult when editTextNumberOfOptions changes
        editTextNumberOfOptions.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val currentVotes = tvNumberOfVotesSoFarNumber.text.toString().toInt()
                if (currentVotes > 0) { // Überprüfe, ob die Anzahl der Stimmen größer als 0 ist
                    tvNumberOfVotesSoFarNumber.text = "0"
                    tvShowVotingResult.text = ""
                    Toast.makeText(this@MainActivity, getString(R.string.toast_votes_resetted), Toast.LENGTH_SHORT).show() // Zeige den Toast an
                } else {
                    tvNumberOfVotesSoFarNumber.text = "0"
                    tvShowVotingResult.text = ""
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        buttonAddAVote.setOnClickListener {
            // Validate options and send to VoteActivity
            validateOptions { options ->
                val intent = Intent(this, VoteActivity::class.java)
                intent.putStringArrayListExtra("options", ArrayList(options))
                intent.putExtra("numberOfOptions", validOptionCount)
                startForResult.launch(intent) // Verwende den ActivityResultLauncher
            }
        }

        buttonStartOver.setOnClickListener {
            val currentVotes = tvNumberOfVotesSoFarNumber.text.toString().toInt()
            if (currentVotes > 0) {
                tvNumberOfVotesSoFarNumber.text = "0"
                Toast.makeText(this, getString(R.string.toast_starting_anew), Toast.LENGTH_SHORT).show()
            }
        }

        // Check if the activity was cancelled
        val isCancelled = intent.getBooleanExtra("cancelled", false)
        if (isCancelled) {
            Toast.makeText(this, getString(R.string.toast_cancelled), Toast.LENGTH_SHORT).show()
        }

        // Set the Switch listener to toggle the visibility of the text in tvShowVotingResult
        switchShowVotingResult.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                tvShowVotingResult.text = votingResults // Show text when switch is on
            } else {
                tvShowVotingResult.text = "" // Hide text when switch is off
            }
        }
    }

    private fun validateOptions(onValidOptions: ((List<String>) -> Unit)?) {
        val numberInput = editTextNumberOfOptions.text.toString()
        if (numberInput.isNotEmpty()) {
            val maxOptions = numberInput.toInt()
            val optionsText = editTextEnterAllOptions.text.toString().trim()

            val options = if (optionsText.isEmpty()) {
                // If no options are entered, generate "Option 1" to "Option n"
                (1..maxOptions).map { "Option $it" }
            } else {
                // Split the entered options by comma, trim them, and filter out empty options
                optionsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            }

            val optionCount = options.size

            // Adjust the options based on the number of options required
            val adjustedOptions = if (optionCount < maxOptions) {
                // If there are less options, fill up with "Option x"
                options + (optionCount + 1..maxOptions).map { "Option $it" }
            } else {
                // If there are more options, take only the required number
                options.take(maxOptions)
            }

            // Pass the adjusted options to the callback if provided
            onValidOptions?.invoke(adjustedOptions)
        }
    }
}
