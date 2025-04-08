package dev.levia.lehremich2.question

import android.content.Context
import android.view.View
import android.widget.TextView

enum class PrefixesTypes {
    HAT, IST, BOTH
}

class QuestionPerfekt(val wort: String): Question() {
    lateinit var your: PrefixesTypes
    lateinit var prefix: PrefixesTypes
    lateinit var word: String
    lateinit var old: String

    init {
        val neu = wort.split(";");
        prefix = if (neu[0] == "hat") PrefixesTypes.HAT
            else if (neu[0] == "ist") PrefixesTypes.IST
            else PrefixesTypes.BOTH
        word = neu[1]
        old = neu[2]
    }

    override fun check(wort: String): Boolean {
        return prefix.name == wort;
    }
    override fun getString(): String {
        return if (correct) "${prefix.name} $word;${timeout.toFloat()/100} s";
        else "${prefix.name} $word nicht $your"
    }

    override fun view(context: Context): View {
        val text = TextView(context)
        text.text = "${prefix.name} $word nicht $your";
        text.textAlignment = View.TEXT_ALIGNMENT_CENTER
        text.textSize = 20F;
        return text;
    }
}
