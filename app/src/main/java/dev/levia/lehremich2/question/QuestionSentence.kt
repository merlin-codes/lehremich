package dev.levia.lehremich2.question

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.TextView
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexboxLayout
import dev.levia.lehremich2.R


data class QuestionSentence(var wort: String): Question() {
    var words: List<String> = wort.split(";");
    var your: List<String> = listOf();
    init {
        original = wort;
        type = QuestionTypes.Setz
        words.shuffled()
    }

    override fun check(wort: String): Boolean {
        your = wort.split(";");
        Log.d("EMPTY", String.format("%s == %s", wort, original))
        return wort == original
    }

    override fun view(context: Context): View {
        val layout = FlexboxLayout(context);
        layout.alignItems = AlignItems.CENTER
        val origin = original.split(";")

        Log.d("EMPTY", String.format("%s == %s", your.joinToString(";"), origin.joinToString(";")))
        for (i in origin.indices) {
            val text = TextView(context)
            text.setBackgroundColor(context.getColor(
                if (your.size > i && your[i] == origin[i]) R.color.light_green else R.color.light_red
            ))
            text.text = " "+origin[i]+" ";
            text.textSize = 15F
            text.textAlignment = View.TEXT_ALIGNMENT_CENTER
            layout.addView(text)
        }
        return layout;
    }
    override fun getString(): String {
        val response = original.replace(";", " ")
        return if (correct) "$response;${timeout.toFloat()/100} s";
        else "$response nicht ${your.ifEmpty { "EMPTY" }}"
    }
}

