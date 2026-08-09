package com.example.wordcrush.constants

internal object AppStrings {
    object Common {
        const val BACK = "Back"
        const val CANCEL = "Cancel"
        const val CLOSE = "Close"
        const val DELETE = "Delete"
        const val SAVE = "Save"
        const val UPDATE = "Update"
        const val SEARCH = "Search"
        const val START = "Start"
        const val RESTART = "Restart"
        const val RANKING = "Ranking"
        const val TIMER = "Timer"
        const val PLAY_AGAIN = "Play again"
        const val SCORE = "Score"
        const val WORDS = "Words"
        const val RECORDS = "Records"
        const val LEARNING = "Learning"
        const val MASTERED = "Mastered"
        const val ALL = "All"
        const val UK = "UK"
        const val US = "US"
        const val RESET = "Reset"
        const val MARK = "Mark"
        const val GUEST = "Guest"
    }

    object Auth {
        const val APP_NAME = "Word Crush"
        const val LOGIN_SUBTITLE = "Sign in to continue your vocabulary training."
        const val REGISTER_SUBTITLE = "A single activity host now drives the full app flow."
        const val USERNAME = "Username"
        const val PASSWORD = "Password"
        const val CONFIRM_PASSWORD = "Confirm password"
        const val LOGIN = "Log in"
        const val CREATE_ACCOUNT = "Create account"
        const val REGISTER = "Register"
        const val REGISTRATION_COMPLETE = "Registration complete. Please log in."
    }

    object Loading {
        const val CHECKING_SESSION = "Checking session..."
        const val PREPARING_APP = "Preparing app..."
    }

    object Game {
        const val MATCH = "Match"
        const val CLASSIC = "Classic"
        const val TIMED = "Timed"
        const val MODE_SUBTITLE = "Choose a mode, then start."
        const val READY_TO_START = "Ready to start"
        const val CLASSIC_EMPTY_MESSAGE = "Tap start to load a fresh set of words for classic mode."
        const val TIMED_EMPTY_MESSAGE = "The timer begins only after you tap start."
        const val DAILY_STUDY_MESSAGE = "Tap start to load today's study words."
        const val ROUND_COMPLETE = "Round complete"
        const val TIMES_UP = "Time's up"
        const val END_CURRENT_GAME = "End current game?"
        const val STOPPING_GAME_MESSAGE = "Stopping now will save the current game record and end this round."
        const val STOP = "Stop"
        const val LATEST_PAIR = "Latest pair"
        const val NOT_REMEMBERED = "Not remembered"
        const val LEARNED_THIS_ROUND = "Learned this round"
        const val LIVES = "Lives"
        const val LIFE_REMAINING = "Life remaining"
        const val LIFE_LOST = "Life lost"
        const val ENGLISH_LABEL = "EN"
        const val CHINESE_LABEL = "CN"
        const val GAME_OVER = "Game over."
        const val TIME_IS_UP = "Time is up."

        fun timer(seconds: Int): String = "${seconds}s"
        fun todayProgress(mastered: Int, total: Int): String = "Today: $mastered/$total"
        fun score(score: Int): String = "Score: $score"
        fun moreSummaries(count: Int): String = "+$count more"
        fun saved(mode: String, synced: Boolean): String = if (synced) {
            "Current $mode game has been saved and synced to cloud."
        } else {
            "Current $mode game is saved locally. Cloud upload failed."
        }

        fun markedAsLearning(english: String): String = "$english marked as learning again."
        fun modeName(isClassic: Boolean): String = if (isClassic) "classic" else "timed"
        fun gameTypeName(isClassic: Boolean): String = Records.modeTitle(isClassic)
    }

    object WordBook {
        const val TITLE = "Word Book"
        const val SUBTITLE = "Search, review and mark vocabulary mastery."
        const val SEARCH_WORDS = "Search words"
        const val NO_SEARCH_RESULTS = "No search results"
        const val NO_REMEMBERED_WORDS_FOUND = "No remembered words found"
        const val NO_LEARNING_WORDS_FOUND = "No learning words found"
        const val NO_REMEMBERED_WORDS_YET = "No remembered words yet"
        const val NO_LEARNING_WORDS = "No learning words"
        const val NO_WORDS_FOUND = "No words found"
        const val TRY_ANOTHER_KEYWORD = "Try another keyword or reset the filter."
        const val REMEMBERED = "Remembered"

        fun noWordsMatch(query: String): String = "No words match \"$query\". Try another keyword."
        fun noRememberedMatch(query: String): String = "No remembered words match \"$query\"."
        fun noLearningMatch(query: String): String = "No learning words match \"$query\"."
        fun rememberedWordsHint(): String = "Words marked as remembered will appear here."
        fun learningWordsHint(): String = "Your current learning list is empty."
        fun emptyBookHint(): String = "Your word book is empty right now."
        fun matched(count: Int): String = "$count matched"
        fun numbered(index: Int, value: String): String = "${index + 1}. $value"
    }

    object Profile {
        const val TITLE = "Profile"
        const val SUBTITLE = "Progress, daily plan and account actions."
        const val UPLOADING = "Uploading..."
        const val UPLOAD = "Upload"
        const val LEARNING_PROFILE = "Learning profile"
        const val SCORE_SUMMARY = "Score summary"
        const val DAILY_PLAN = "Daily learning plan"
        const val DAILY_WORD_COUNT = "Daily word count"
        const val QUICK_ACTIONS = "Quick actions"
        const val SYNC_CLOUD_DATA = "Sync cloud data"
        const val REFRESH_SCORES = "Refresh scores"
        const val CHANGE_PASSWORD = "Change password"
        const val LOG_OUT = "Log out"
        const val CURRENT_PASSWORD = "Current password"
        const val NEW_PASSWORD = "New password"
        const val CONFIRM_NEW_PASSWORD = "Confirm new password"
        const val IMAGE_CONTENT_TYPE = "image/*"
        const val ALL_WORDS_LEARNED = "All words have already been learned."
        const val DAILY_WORDS_DONE = "Today's words are done. Increase the daily learning count if you want more words today."
        const val FIXED_SET_COMPLETE = "Today's fixed set is complete."
        const val LEARNING_PROGRESS_SYNCED = "Learning progress is synced."
        const val LEARNING_SYNC_PENDING = "learning updates are waiting for network sync."

        fun matchScore(score: Int): String = "Match: $score"
        fun timedScore(score: Int): String = "Timed: $score"
        fun fixedSetProgress(completed: Int, total: Int): String =
            "Today's fixed set: $completed/$total learned. Each word needs " +
                "${AppConstants.Learning.REQUIRED_CORRECT_MATCHES} correct matches."

        fun pendingLearning(count: Int): String = "$count $LEARNING_SYNC_PENDING"
        fun syncSummary(uploadedCount: Int, learningPending: Boolean): String = buildString {
            append("Cloud data synced.")
            if (learningPending) append(" Learning progress is still pending.")
            if (uploadedCount > 0) {
                append(" Uploaded ")
                append(uploadedCount)
                append(" local record")
                if (uploadedCount > 1) append("s")
                append(".")
            }
        }
    }

    object Learning {
        const val ALL_WORDS_LEARNED = "All words learned"
        const val DAILY_WORDS_DONE = "Today's words are done"
        const val TODAY_FIXED_SET = "Today's fixed set"
        const val ALL_WORDS_BOOK_COMPLETE = "You have already learned every word in the word book."
        const val DAILY_COMPLETE_INCREASE_HINT =
            "Today's words are complete. Increase the daily learning count if you want more words today."
        const val DAILY_COMPLETE = "Today's words are complete."
        const val NO_STUDY_WORDS = "No study words are available yet."
        const val ALL_WORDS_COMPLETE = "You have already learned all words."
        const val DAILY_COMPLETE_CONTINUE_HINT =
            "Today's words are complete. Increase the daily learning count if you want to continue today."

        fun fixedPlan(total: Int): String =
            "Today's plan is fixed at $total words. Each word needs " +
                "${AppConstants.Learning.REQUIRED_CORRECT_MATCHES} correct matches."
    }

    object Ranking {
        const val MATCH_TITLE = "Match ranking"
        const val TIMED_TITLE = "Timed ranking"
        const val NO_DATA = "No ranking data"
        const val EMPTY_MESSAGE = "Play a few rounds and sync the leaderboard again."
        const val TOP_PLAYERS_DESCRIPTION =
            "Top players are ranked by best score, with earlier finish times breaking ties."
        const val PLAYERS = "Players"
        const val TOP_SCORE = "Top score"

        fun title(isMatch: Boolean): String = if (isMatch) MATCH_TITLE else TIMED_TITLE
        fun rank(index: Int): String = "Rank #${index + 1}"
        fun compactRank(index: Int): String = "#${index + 1}"
    }

    object Records {
        const val TITLE = "Game records"
        const val NO_RECORDS = "No local records"
        const val EMPTY_MESSAGE = "Finish a game to create your first record."
        const val DELETE_TITLE = "Delete record"
        const val DELETE_MESSAGE =
            "This removes the local record and attempts to sync the deletion to the server."
        const val YOUR_RECENT_RUNS = "Your recent runs"
        const val RECENT_RUNS_DESCRIPTION =
            "Review saved sessions, compare modes and reopen the learned words from each run."
        const val BEST_SCORE = "Best score"
        const val LEARNED_WORDS = "Learned words"
        const val NO_LEARNED_WORDS = "No learned words saved for this record."
        const val HIDE_DETAILS = "Hide details"
        const val VIEW_DETAILS = "View details"

        fun modeTitle(isMatch: Boolean): String = if (isMatch) "Match Challenge" else "Timed Match"
        fun progressLabel(english: String, correctCount: Int, isLearned: Boolean): String =
            if (isLearned) {
                "$english Learned"
            } else {
                "$english ${correctCount.coerceIn(1, AppConstants.Learning.REQUIRED_CORRECT_MATCHES)}/" +
                    AppConstants.Learning.REQUIRED_CORRECT_MATCHES
            }
    }

    object Errors {
        const val LOGIN_FAILED = "Login failed."
        const val REGISTRATION_FAILED = "Registration failed."
        const val PASSWORD_UPDATE_FAILED = "Unable to update password."
        const val AVATAR_UPDATED = "Avatar updated."
        const val AVATAR_UPLOAD_FAILED = "Avatar upload failed."
        const val CLOUD_SYNC_FAILED = "Cloud sync failed."
        const val LEARNING_SYNC_FAILED = "Learning progress sync failed."
        const val DAILY_TARGET_UPDATED = "Daily learning count updated."
        const val DAILY_TARGET_INVALID = "Please enter a daily learning count greater than 0."
        const val LOAD_RANKING_FAILED = "Unable to load ranking data."
        const val LOAD_RECORDS_FAILED = "Unable to load game records."
        const val DELETE_RECORD_FAILED = "Unable to delete record."
        const val RECORD_DELETED = "Record deleted."
        const val LOAD_WORDS_TITLE = "Unable to load words"
        const val LOAD_WORDS_FAILED = "Unable to load words."
        const val NO_LOGGED_IN_USER = "No logged-in user found."
        const val RECORD_NOT_FOUND = "Record was not found."
        const val CLOUD_RECORD_SYNC_FAILED = "Cloud sync failed while uploading local records."
        const val RESPONSE_DATA_MISSING = "Response data is missing."
        const val REQUEST_FAILED = "Request failed."
        const val EMPTY_RESPONSE_BODY = "Empty response body"
        const val SUCCESS = "success"
        const val SESSION_EXPIRED = "Session expired. Please log in again."
        const val LOCAL_CACHE_FALLBACK = "Learning data will use the local cache until sync succeeds."
        const val AUDIO_LOAD_FAILED = "Unable to load pronunciation audio."
        const val AVATAR_READ_FAILED = "Unable to read avatar file."
        const val UNSUPPORTED_AVATAR = "Unsupported avatar image."
        const val AVATAR_DECODE_FAILED = "Unable to decode avatar file."
        const val SESSION_VALIDATION_FAILED = "Session validation failed."
        const val DAILY_TARGET_RANGE = "Please enter a daily learning count between 1 and 500."
        const val NETWORK_OFFLINE = "Network connection is unavailable."
        const val NETWORK_TIMEOUT = "Network request timed out."
        const val NETWORK_SERIALIZATION = "Unable to read server response."
    }

    object Validation {
        const val USERNAME_AND_PASSWORD_REQUIRED = "Username and password are required."
        const val USERNAME_REQUIRED = "Username is required."
        const val PASSWORD_REQUIRED = "Password is required."
        const val PASSWORDS_DO_NOT_MATCH = "Passwords do not match."
        const val PASSWORD_MIN_LENGTH = "Password must be at least 6 characters."
        const val PASSWORD_FIELDS_REQUIRED = "Please fill in all password fields."
        const val NEW_PASSWORDS_DO_NOT_MATCH = "New passwords do not match."
        const val NEW_PASSWORD_MIN_LENGTH = "New password must be at least 6 characters."
    }

    fun scoreSummary(message: String, score: Int): String = "$message\n${Game.score(score)}"
    fun chineseSummary(english: String, chinese: String): String =
        "$english - ${chinese.replace("\n", " ").trim()}"
}
