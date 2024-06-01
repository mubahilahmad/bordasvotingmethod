package de.fra_uas.fb2.mobiledevices.bordasvotingmethod

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class VoteActivity : AppCompatActivity() {

    private lateinit var buttonCancel: Button
    private lateinit var buttonConfirm: Button
    private lateinit var tvResults: TextView
    private lateinit var scrollView: ScrollView
    private val seekBars = mutableListOf<Pair<SeekBar, String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vote)

        buttonCancel = findViewById(R.id.buttonCancel)
        buttonConfirm = findViewById(R.id.buttonConfirmYourVote)
        tvResults = findViewById(R.id.tvResults)
        scrollView = findViewById(R.id.scrollView)

        val options = intent.getStringArrayListExtra("options")
        val numberOfOptions = intent.getIntExtra("numberOfOptions", 0)

        val dynamicContent = findViewById<LinearLayout>(R.id.dynamicContent)

        options?.forEachIndexed { index, option ->
            val textView = TextView(this).apply {
                id = View.generateViewId()
                text = option
                textSize = 14f
                setPadding(0, 2, 0, 2)  // Padding to create space between elements
            }

            val seekBar = SeekBar(this).apply {
                id = View.generateViewId()
                max = 10
                progress = 0
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 3, 0, 3)  // Margins to create space between elements
                }
            }

            seekBars.add(Pair(seekBar, option))

            dynamicContent.addView(textView)
            dynamicContent.addView(seekBar)

            // Add an OnSeekBarChangeListener to update tvResults in real-time
            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    updateResults(numberOfOptions)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    // No action needed
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    // No action needed
                }
            })
        }

        buttonCancel.setOnClickListener {
            val intent = Intent()
            intent.putExtra("cancelled", true)
            setResult(RESULT_CANCELED, intent)
            finish()
        }

        buttonConfirm.setOnClickListener {
            if (!validateUniqueScores()) {
                Toast.makeText(this, getString(R.string.toast_vote_not_unique), Toast.LENGTH_SHORT).show()
            } else {
                calculateScores(numberOfOptions)
                val results = tvResults.text.toString()
                val intent = Intent()
                intent.putExtra("votingResults", results)
                setResult(RESULT_OK, intent)
                finish()
            }
            saveScrollViewContent()
        }
    }

    private fun validateUniqueScores(): Boolean {
        val scores = seekBars.map { it.first.progress }
        return scores.size == scores.distinct().size
    }

    private fun calculateScores(numberOfOptions: Int) {
        val scores = mutableMapOf<String, Int>()
        val progressMap = mutableMapOf<String, Int>()

        seekBars.sortedByDescending { it.first.progress }
            .forEachIndexed { index, pair ->
                val points = numberOfOptions - 1 - index
                scores[pair.second] = points
                progressMap[pair.second] = pair.first.progress
            }

        val duplicateProgresses = progressMap.values.groupBy { it }.filter { it.value.size > 1 }.keys
        val maxScore = scores.values.maxOrNull()

        val resultsText = seekBars.joinToString(separator = "\n") { (seekBar, option) ->
            val score = scores[option] ?: 0
            if (seekBar.progress in duplicateProgresses) {
                "$option -> <not unique>"
            } else {
                if (score == maxScore) {
                    "*** $option -> $score points ***"
                } else {
                    "$option -> $score points"
                }
            }
        }
        tvResults.text = resultsText

        if (duplicateProgresses.isNotEmpty()) {
            Toast.makeText(this, getString(R.string.toast_vote_not_unique), Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateResults(numberOfOptions: Int) {
        val scores = mutableMapOf<String, Int>()
        val progressMap = mutableMapOf<String, Int>()

        seekBars.sortedByDescending { it.first.progress }
            .forEachIndexed { index, pair ->
                val points = numberOfOptions - 1 - index
                scores[pair.second] = points
                progressMap[pair.second] = pair.first.progress
            }

        val duplicateProgresses = progressMap.values.groupBy { it }.filter { it.value.size > 1 }.keys
        val maxScore = scores.values.maxOrNull()

        val resultsText = seekBars.joinToString(separator = "\n") { (seekBar, option) ->
            val score = scores[option] ?: 0
            if (seekBar.progress in duplicateProgresses) {
                "$option -> <not unique>"
            } else {
                if (score == maxScore) {
                    "*** $option -> $score points ***"
                } else {
                    "$option -> $score points"
                }
            }
        }
        tvResults.text = resultsText
    }

    private fun saveScrollViewContent() {
        val dynamicContent = findViewById<LinearLayout>(R.id.dynamicContent)
        val contentBuilder = StringBuilder()

        for (i in 0 until dynamicContent.childCount) {
            val view = dynamicContent.getChildAt(i)
            when (view) {
                is TextView -> contentBuilder.append(view.text).append("\n")
                is SeekBar -> contentBuilder.append("SeekBar progress: ").append(view.progress).append("\n")
            }
        }

        val scrollViewContent = contentBuilder.toString()
        // You can now use this string to save or debug
        println(scrollViewContent)
    }
}
