package com.bignerdranch.android.simpleboggle

import ShakeDetector
import android.content.Context
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity(), GameFragment.GameFragmentListener, ScoreFragment.ScoreFragmentListener {



    private lateinit var gameFragment: GameFragment
    private lateinit var scoreFragment: ScoreFragment

    private lateinit var sensorManager: SensorManager
    private lateinit var shakeDetector: ShakeDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setUp()
        shakeDetector()
    }


    private fun shakeDetector() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        shakeDetector = ShakeDetector().apply {
            setOnShakeListener {

                val scoreFragment = supportFragmentManager.findFragmentByTag("ScoreFragment") as? ScoreFragment
                scoreFragment?.newGameCreate()
                Toast.makeText(applicationContext, "New game started", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun setUp() {
        gameFragment = supportFragmentManager.findFragmentById(R.id.game_fragment) as GameFragment
        scoreFragment = supportFragmentManager.findFragmentById(R.id.score_fragment) as ScoreFragment

        gameFragment.setListener(this)
        scoreFragment.setListener(this)
    }



    override fun onNewGame() {
        Toast.makeText(this, "New game started", Toast.LENGTH_SHORT).show()
        gameFragment.resetSubmittedWords()
        gameFragment.initializeButtons()
    }
    override fun onSubmitWord(word: String) {
        Toast.makeText(this, "Word submitted: $word", Toast.LENGTH_SHORT).show()
    }



    override fun onScoreUpdated(score: Int) {
        scoreFragment.updateScore(score)
    }
}