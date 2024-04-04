package com.bignerdranch.android.simpleboggle

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment

class ScoreFragment : Fragment() {

    private lateinit var scoreFragmentListener: ScoreFragmentListener
    private var score: Int = 0

    interface ScoreFragmentListener {
        fun onNewGame()
        fun onScoreUpdated(score: Int)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_score, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val newGameButton = view.findViewById<Button>(R.id.new_game_button)
        newGameButton.setOnClickListener {
            newGameCreate()
        }
    }

    fun newGameCreate(){
        score = 0 // Reset score to 0 when starting a new game
        updateDisplayedScore()
        scoreFragmentListener.onNewGame()
    }

    fun setListener(listener: ScoreFragmentListener) {
        this.scoreFragmentListener = listener
    }

    fun updateScore(newScore: Int) {
        score = newScore // Update score with the new value
        updateDisplayedScore()
    }

    fun updateDisplayedScore() {
        view?.findViewById<TextView>(R.id.scoreTextView)?.text = "Score: $score"

    }
}