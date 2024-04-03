package com.bignerdranch.android.simpleboggle

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.Locale


class GameFragment : Fragment() {

    private lateinit var gameFragmentListener: GameFragmentListener
    private lateinit var buttons: List<Button>
    private val usedButtons = mutableListOf<Button>()
    private var lastClickedButton: Button? = null
    private lateinit var dictionary: Set<String>
    private val submittedWords = mutableSetOf<String>()


    private var score = 0 // Initialize score variable

    interface GameFragmentListener {
        fun onSubmitWord(word: String)
        fun onNewGame()
        fun onScoreUpdated(score: Int)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_game, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize buttons
        buttons = listOf(
            view.findViewById(R.id.button1),
            view.findViewById(R.id.button2),
            view.findViewById(R.id.button3),
            view.findViewById(R.id.button4),
            view.findViewById(R.id.button5),
            view.findViewById(R.id.button6),
            view.findViewById(R.id.button7),
            view.findViewById(R.id.button8),
            view.findViewById(R.id.button9),
            view.findViewById(R.id.button10),
            view.findViewById(R.id.button11),
            view.findViewById(R.id.button12),
            view.findViewById(R.id.button13),
            view.findViewById(R.id.button14),
            view.findViewById(R.id.button15),
            view.findViewById(R.id.button16)
        )

        // Set onClickListener for each button
        for (button in buttons) {
            button.setOnClickListener {
                buttonClick(button)
            }
        }

        // Set onClickListener for clear button
        val clearButton = view.findViewById<Button>(R.id.clear_button)
        clearButton.setOnClickListener {
            clearInput()
        }

        // Set onClickListener for submit button
        val submitButton = view.findViewById<Button>(R.id.submit_button)
        submitButton.setOnClickListener {
            submitInput()
        }

        initializeButtons()
        initializeDictionary()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun initializeDictionary() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                // Download dictionary from the URL
                val dictionaryText = downloadDictionary()

                // Parse and store dictionary words
                dictionary = parseDictionary(dictionaryText)
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Failed to download dictionary", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun downloadDictionary(): String {
        val okHttpClient = OkHttpClient()
        val httpRequest = Request.Builder()
            .url("https://raw.githubusercontent.com/dwyl/english-words/master/words.txt")
            .get() // Explicitly specifying the GET method for clarity
            .build()

        okHttpClient.newCall(httpRequest).execute().use { httpResponse ->
            if (!httpResponse.isSuccessful) throw IOException("Error fetching dictionary: ${httpResponse.code}")
            return httpResponse.body?.string() ?: throw IOException("Dictionary content is null")
        }
    }

    private fun parseDictionary(dictionaryText: String): Set<String> {
        return dictionaryText.trim().split("\n").toSet()
    }

    private fun isValidWord(word: String): Boolean {
        return word.lowercase() in dictionary
    }

    fun setListener(listener: GameFragmentListener) {
        this.gameFragmentListener = listener
    }

    fun initializeButtons() {
        val letters = getRandomChar()
        for ((index, button) in buttons.withIndex()) {
            button.text = letters[index].toString()
        }
    }

    private fun getRandomChar(): List<Char> {
        val vowels = setOf('A', 'E', 'I', 'O', 'U')
        val consonants = ('A'..'Z').filterNot { it in vowels }
        val randomLetters = mutableListOf<Char>()

        // Add 3 unique random vowels
        randomLetters += vowels.shuffled().take(3)

        // Add 13 random consonants, allowing repeats
        randomLetters += List(13) { consonants.random() }

        return randomLetters.shuffled()
    }

    private fun buttonClick(button: Button) {
        if (!moveCheck(button)) {
            Toast.makeText(requireContext(), "Invalid move. Try a button adjacent to the last button.", Toast.LENGTH_SHORT).show()
        } else {
            view?.findViewById<TextView>(R.id.userInputTextView)?.let { userInputTextView ->
                if (userInputTextView.text.toString() == "User Input") {
                    userInputTextView.text = ""
                }
                userInputTextView.append(button.text)
            }

            usedButtons.add(button)
            button.apply {
                isEnabled = false
            }
            lastClickedButton = button
        }
    }

    private fun moveCheck(button: Button): Boolean {
        lastClickedButton?.let {
            val lastIndex = buttons.indexOf(it)
            val currentIndex = buttons.indexOf(button)

            val lastPosition = lastIndex / 4 to lastIndex % 4
            val currentPosition = currentIndex / 4 to currentIndex % 4

            return (currentPosition.first in lastPosition.first - 1..lastPosition.first + 1) && // Row check
                    (currentPosition.second in lastPosition.second - 1..lastPosition.second + 1) && // Column check
                    !(currentPosition.first == lastPosition.first && currentPosition.second == lastPosition.second) // Not the same button
        }
        return true
    }

    fun clearInput() {
        view?.findViewById<TextView>(R.id.userInputTextView)?.text = "User Input"

        usedButtons.forEach { button ->
            button.isEnabled = true
            button.setBackgroundColor(Color.WHITE)
        }
        usedButtons.clear()

        lastClickedButton = null
    }

    private fun submitInput() {
        val word = view?.findViewById<TextView>(R.id.userInputTextView)?.text.toString()

        when {
            word.isEmpty() || word == "User Input" -> showToast("Please enter a word.")
            word.length < 4 -> showToast("Word must be at least 4 characters long")
            !isValidWord(word) -> {
                showToast("Invalid word. Please enter a valid English word.")
                deductPoints(10) // Deduct 10 points for incorrect word
            }
            word.count { it in "AEIOU" } < 2 -> showToast("Word must contain at least two vowels")
            word in submittedWords -> showToast("Word already submitted")
            else -> {
                submittedWords.add(word) // Add the word to the set of submitted words
                val wordScore = calculateScore(word)
                showToast(if (wordScore > 0) "That's correct! +$wordScore" else "That's incorrect. -10")
                gameFragmentListener.onSubmitWord(word)
                if (wordScore > 0) addPoints(wordScore) else deductPoints(10)
                clearInput()
            }
        }
    }


    private fun deductPoints(points: Int) {
        score = maxOf(score - points, 0) // Deduct points, but ensure the score doesn't go negative
        gameFragmentListener.onScoreUpdated(score)
    }

    private fun addPoints(points: Int) {
        score += points // Add points to the score
        gameFragmentListener.onScoreUpdated(score) // Update the score in the ScoreFragment
    }

    private fun calculateScore(word: String): Int {
        var wordScore = 0
        var consonantCount = 0
        val specialConsonants = "SZPXQ"

        word.uppercase(Locale.ROOT).forEach { char ->
            if (char in "AEIOU") {
                wordScore += 5
            } else {
                wordScore += 1
                consonantCount++
            }
        }

        if (consonantCount > 0 && word.uppercase(Locale.ROOT).any { it in specialConsonants }) {
            wordScore *= 2
        }

        return if (wordScore < 0) 0 else wordScore
    }

    private fun String.containsAny(chars: CharSequence): Boolean {
        for (char in chars) {
            if (this.contains(char)) {
                return true
            }
        }
        return false
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    fun resetSubmittedWords() {
        submittedWords.clear()
        score = 0 // Reset score when new game starts
    }
}