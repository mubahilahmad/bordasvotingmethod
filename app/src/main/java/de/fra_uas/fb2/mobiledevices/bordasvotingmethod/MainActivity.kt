package de.fra_uas.fb2.mobiledevices.bordasvotingmethod

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    // alle elemente, die es auch im userinterface gibt -> activity_main.xml

    private lateinit var editTextNumberOfOptions: EditText     // zahl zwischen 2 und 10, default wert ist 3
    private lateinit var editTextEnterAllOptions: EditText     // eingegebene optionen, getrennt durch comma
    private lateinit var tvNumberOfVotesSoFarNumber: TextView  // zahl per defaul 0, wird bei erfolgreiches voting inkrementiert (voteactivity -> confirm your vote button)
    private lateinit var buttonStartOver: Button               // beim drücken wird tvNumberOfVotesSoFarNumber auf 0 gesetzt + toast (nur wenn zahl > 0)
    private lateinit var buttonAddAVote: Button
    private lateinit var switchShowVotingResult: Switch
    private lateinit var tvShowVotingResult: TextView

    private lateinit var startForResult: ActivityResultLauncher<Intent>
    private var validOptionCount: Int? = null
    private var votingResults: String = ""
    private val scores = mutableMapOf<String, Int>()
    private val optionsOrder = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // methode verbindet diese activity mit userinterface (activity_main.xml)

        // die variablen haben den selben namen, wie die id der xml elemente
        editTextNumberOfOptions = findViewById(R.id.editTextNumberOfOptions)
        editTextEnterAllOptions = findViewById(R.id.editTextEnterAllOptions)
        tvNumberOfVotesSoFarNumber = findViewById(R.id.tvNumberOfVotesSoFarNumber)
        buttonStartOver = findViewById(R.id.buttonStartOver)
        buttonAddAVote = findViewById(R.id.buttonAddAVote)
        switchShowVotingResult = findViewById(R.id.switchShowVotingResult)
        tvShowVotingResult = findViewById(R.id.tvShowVotingResult)

        editTextNumberOfOptions.hint = "3"     // hint ist in diesem fall besser als settext() -> wenn user reinklickt, muss er die 3 nicht manuell löschen

        tvNumberOfVotesSoFarNumber.text = "0"  // textview wird auf string 0 gesetzt

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

        // stop
        editTextNumberOfOptions.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                if (editTextNumberOfOptions.text.toString() == "3") {
                    editTextNumberOfOptions.setText("")
                }
            } else {
                if (editTextNumberOfOptions.text.isEmpty()) {
                    editTextNumberOfOptions.setText("3")
                }
            }
        }

        editTextNumberOfOptions.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val currentVotes = tvNumberOfVotesSoFarNumber.text.toString().toInt()
                if (currentVotes > 0) {
                    resetVoting()
                    Toast.makeText(this@MainActivity, getString(R.string.toast_votes_resetted), Toast.LENGTH_SHORT).show()
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        editTextEnterAllOptions.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val currentVotes = tvNumberOfVotesSoFarNumber.text.toString().toInt()
                if (currentVotes > 0) {
                    resetVoting()
                    Toast.makeText(this@MainActivity, getString(R.string.toast_votes_resetted), Toast.LENGTH_SHORT).show()
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })


        buttonAddAVote.setOnClickListener {
            validateOptions { options ->
                if (editTextNumberOfOptions.text.toString().isEmpty()) {
                    editTextNumberOfOptions.setText("3")
                    validOptionCount = 3
                }
                optionsOrder.clear()
                optionsOrder.addAll(options)
                val intent = Intent(this, VoteActivity::class.java)
                intent.putStringArrayListExtra("options", ArrayList(options))
                intent.putExtra("numberOfOptions", validOptionCount)
                startForResult.launch(intent)
            }
        }

        buttonStartOver.setOnClickListener {
            val currentVotes = tvNumberOfVotesSoFarNumber.text.toString().toInt()
            if (currentVotes > 0) {
                resetVoting()
                Toast.makeText(this, getString(R.string.toast_starting_anew), Toast.LENGTH_SHORT).show()
            }
        }

        switchShowVotingResult.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showAggregatedResults()
            } else {
                tvShowVotingResult.text = ""
            }
        }

        startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val results = data?.getStringExtra("votingResults")
                val receivedScores = data?.getSerializableExtra("scores") as? HashMap<String, Int>
                val receivedOptionsOrder = data?.getStringArrayListExtra("optionsOrder")
                results?.let {
                    votingResults = it

                    val currentVotes = tvNumberOfVotesSoFarNumber.text.toString().toInt()
                    tvNumberOfVotesSoFarNumber.text = (currentVotes + 1).toString()

                    receivedScores?.forEach { (option, points) ->
                        scores[option] = scores.getOrDefault(option, 0) + points
                    }

                    receivedOptionsOrder?.let {
                        optionsOrder.clear()
                        optionsOrder.addAll(it)
                    }

                    if (switchShowVotingResult.isChecked) {
                        showAggregatedResults()
                    }
                }
            } else if (result.resultCode == RESULT_CANCELED) {
                val data = result.data
                val isCancelled = data?.getBooleanExtra("cancelled", false) ?: false
                if (isCancelled) {
                    Toast.makeText(this, getString(R.string.toast_vote_cancelled), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun validateOptions(onValidOptions: ((List<String>) -> Unit)?) {
        val numberInput = editTextNumberOfOptions.text.toString()
        val maxOptions = if (numberInput.isNotEmpty()) numberInput.toInt() else 3
        val optionsText = editTextEnterAllOptions.text.toString().trim()

        val options = if (optionsText.isEmpty()) {
            (1..maxOptions).map { "Option $it" }
        } else {
            optionsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        }

        val optionCount = options.size

        val adjustedOptions = if (optionCount < maxOptions) {
            options + (optionCount + 1..maxOptions).map { "Option $it" }
        } else {
            options.take(maxOptions)
        }

        onValidOptions?.invoke(adjustedOptions)
    }

    private fun showAggregatedResults() {
        val maxScore = scores.values.maxOrNull()
        val resultsText = optionsOrder.joinToString(separator = "\n") { option ->
            val points = scores[option] ?: 0
            if (points == maxScore) {
                "*** $option -> $points points ***"
            } else {
                "$option -> $points points"
            }
        }
        tvShowVotingResult.text = resultsText
    }

    private fun resetVoting() {
        tvNumberOfVotesSoFarNumber.text = "0"
        scores.clear()
        optionsOrder.clear()
        tvShowVotingResult.text = ""
        votingResults = ""
    }
}
