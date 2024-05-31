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
        }

        buttonCancel.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        buttonConfirm.setOnClickListener {
            if (!validateUniqueScores()) {
                Toast.makeText(this, "Vote is not unique!", Toast.LENGTH_SHORT).show()
            }
            calculateScores(numberOfOptions)
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
            Toast.makeText(this, "Vote is not unique!", Toast.LENGTH_SHORT).show()
        }
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
        // Sie können diesen String jetzt verwenden, um ihn zu speichern oder zu debuggen
        println(scrollViewContent)
    }
}
