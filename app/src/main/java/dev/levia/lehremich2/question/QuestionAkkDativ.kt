package dev.levia.lehremich2.question

import android.content.Context
import android.view.View
import android.widget.TextView

enum class AkkusativDativTypes {
    AKKUSATIV, DATIV
}
class QuestionAkkDativ(var wort: String): Question() {
    lateinit var dativType: AkkusativDativTypes;
    lateinit var your: AkkusativDativTypes;
    lateinit var word: String;
    init {
        val neu = wort.split(";");
        dativType = if (neu[0] == "akk") AkkusativDativTypes.AKKUSATIV else AkkusativDativTypes.DATIV
        type = QuestionTypes.AkkDativ
        word = neu[1];
    }

    override fun check(wort: String): Boolean {
        your = if (wort == AkkusativDativTypes.AKKUSATIV.name) AkkusativDativTypes.AKKUSATIV else AkkusativDativTypes.DATIV;
        return dativType.name == wort;
    }
     override fun getString(): String {
         return if (correct) "${dativType.name} $original;${timeout.toFloat()/100} s";
             else "${dativType.name} $original nicht $your"
     }

    override fun view(context: Context): View {
        val text = TextView(context)
        text.text = "${dativType.name} $word nicht $your";
        text.textAlignment = View.TEXT_ALIGNMENT_CENTER
        text.textSize = 20F;
        return text;
    }

    override fun value(): String {
        return word
    }
}
