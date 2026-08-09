package com.example.wordcrush.ui.compose

internal object AuthRoute {
    const val Login = "login"
    const val Register = "register"
}

internal object MainRoute {
    const val Breakthrough = "breakthrough"
    const val WordBook = "word_book"
    const val Home = "home"
    const val RankingPattern = "ranking/{gameType}"
    const val Records = "records"

    fun ranking(gameType: Int): String = "ranking/$gameType"
}

internal const val GAME_TYPE_ARGUMENT = "gameType"
