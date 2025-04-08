package dev.levia.lehremich2.question

import android.content.Context
import android.view.View
import android.widget.TextView

enum class QuestionTypes { Verb, Name, Setz, AkkDativ }

abstract class Question {
    var original: String = ""
    var timeout: Long = 0L
    var correct: Boolean = false
    var type: QuestionTypes = QuestionTypes.Name
    open fun value(): String { return original; }
    open fun check(wort: String): Boolean {
        return original.contains(wort.lowercase())
    }
    open fun view(context: Context): View {
        val text = TextView(context);
        text.text = original
        return text;
    }
    open fun getString(): String {return original}
    open fun getURL(): String {return original}
}

