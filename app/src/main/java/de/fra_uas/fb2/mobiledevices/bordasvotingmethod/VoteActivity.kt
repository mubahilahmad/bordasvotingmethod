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
    private val scores = mutableMapOf<String, Int>()

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

        options?.forEachIndexed { _, option ->
            val textView = TextView(this).apply {
                id = View.generateViewId()
                text = option
                textSize = 14f
                setPadding(10, 2, 0, 2)
            }

            val seekBar = SeekBar(this).apply {
                id = View.generateViewId()
                max = 10
                progress = 0
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(2, 2, 0, 2)
                }
            }

            seekBars.add(Pair(seekBar, option))

            dynamicContent.addView(textView)
            dynamicContent.addView(seekBar)

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    updateResults(numberOfOptions)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
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
                Toast.makeText(this, getString(R.string.toast_vote_not_unique), Toast.LENGTH_SHORT)
                    .show()
            } else {
                calculateScores(numberOfOptions)
                val results = tvResults.text.toString()
                val intent = Intent()
                intent.putExtra("votingResults", results)
                intent.putExtra("scores", HashMap(scores))
                intent.putStringArrayListExtra("optionsOrder",
                    options?.let { it1 -> ArrayList(it1) })
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
        scores.clear()
        val progressMap = mutableMapOf<String, Int>()

        seekBars.sortedByDescending { it.first.progress }
            .forEachIndexed { index, pair ->
                val points = numberOfOptions - 1 - index
                scores[pair.second] = points
                progressMap[pair.second] = pair.first.progress
            }

        val duplicateProgresses =
            progressMap.values.groupBy { it }.filter { it.value.size > 1 }.keys
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
            Toast.makeText(this, getString(R.string.toast_vote_not_unique), Toast.LENGTH_SHORT)
                .show()
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

        val duplicateProgresses =
            progressMap.values.groupBy { it }.filter { it.value.size > 1 }.keys
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
            when (val view = dynamicContent.getChildAt(i)) {
                is TextView -> contentBuilder.append(view.text).append("\n")
                is SeekBar -> contentBuilder.append("SeekBar progress: ").append(view.progress)
                    .append("\n")
            }
        }


    }
}
