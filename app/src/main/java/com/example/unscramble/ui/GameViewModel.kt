package com.example.unscramble.ui

import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.unscramble.data.MAX_NO_OF_WORDS
import com.example.unscramble.data.SCORE_INCREASE
import com.example.unscramble.data.allWords
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


class GameViewModel : ViewModel() {

    var userGuess by mutableStateOf("")
        private set

    // Game UI state

    // Backing property to avoid state updates from other classes
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState : StateFlow<GameUiState> = _uiState.asStateFlow() // asStateFlow() makes this state a read only

    private lateinit var currentWord : String
    private var usedWords : MutableSet<String> = mutableSetOf()

    //  helper methods

    private fun shuffleCurrentWord(word : String): String{
        val tempWord = word.toCharArray()
        // Scramble the word
        tempWord.shuffle()
        while (String(tempWord).equals(word)){
            tempWord.shuffle()
        }
        return String(tempWord)
    }

    private fun pickRandomWordAndShuffle(): String{
        // Continue picking up a new random word until you get one that hasn't been used before
        currentWord = allWords.random()
        if (usedWords.contains(currentWord)){
            return pickRandomWordAndShuffle()
        }else {
            usedWords.add(currentWord)
            return shuffleCurrentWord(currentWord)
        }
    }

    //helper function to initialize the game
    fun resetGame(){
        usedWords.clear()
        _uiState.value = GameUiState(currentScrambledWord = pickRandomWordAndShuffle())
    }

    init {
        resetGame()
    }

    fun updateUserGuess(guessedWord : String){
        userGuess = guessedWord
    }

    fun checkUserGuess(){
        if (userGuess.equals(currentWord, ignoreCase = true)){
            // User's guess is correct, increase the score
            // and call updateGameState() to prepare the game for the next round
            val updatedScore = _uiState.value.score.plus(SCORE_INCREASE)
            updateGameState(updatedScore)

        }else{
            // User's guess is wrong, show an error
            _uiState.update { currentState ->
                currentState.copy(isGuessedWordWrong = true)
            }
        }
        // Reset user guess
        updateUserGuess("")
    }

    private fun updateGameState(updatedScore: Int) {
        if (usedWords.size == MAX_NO_OF_WORDS) {
            // Last round in the game, update isGameOver tp true, don't pick a new word
            _uiState.update { currentState ->
                currentState.copy(
                    isGuessedWordWrong = false,
                    score = updatedScore,
                    isGameOver = true
                )
            }
        } else {
            // Normal round in the game
            _uiState.update { currentState ->
                currentState.copy(
                    isGuessedWordWrong = false,
                    currentScrambledWord = pickRandomWordAndShuffle(),
                    score = updatedScore,
                    currentWordCount = currentState.currentWordCount.inc(),
                )
            }
        }
    }

    fun skipWord(){
        updateGameState(_uiState.value.score)
        // Reset user guess
        updateUserGuess("")
    }




}