package dev.levia.lehremich2.question

import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import dev.levia.lehremich2.R

enum class WortTypes {
    ich, du, er, sie, es, ihr, wir, Sie
}

data class QuestionVerb(var context: Context, var wort: String): Question() {
    var ich: String = ""
    var du: String = ""
    var er_sie_es: String = ""
    var ihr: String = ""
    var wir_Sie: String = ""
    var guessing: WortTypes = WortTypes.ich
    var actual: String = ""
    lateinit var view: LinearLayout;
    lateinit var worts: String;

    init {
        Log.d("text", String.format(">>> %s", wort))
        val words = wort.split(";")
        super.original = words[0]
        this.ich = words[1]
        this.du = words[2]
        this.er_sie_es = words[3]
        this.ihr = words[4]
        this.wir_Sie = words[5]
        this.type = QuestionTypes.Verb
        this.guessing = WortTypes.entries.random();
        actual = when (this.guessing) {
            WortTypes.ich -> ich
            WortTypes.du -> du
            WortTypes.er, WortTypes.es, WortTypes.sie -> er_sie_es
            WortTypes.ihr -> ihr
            WortTypes.wir, WortTypes.Sie -> wir_Sie
        }
    }
    override fun value(): String {
        return this.guessing.name+" "+this.original;
    }

    private fun sameAs(a: String, b: String): Boolean {
        for (i in a.indices) {
            if (a[i] != b[i]) {
                Log.d("EMPTY", String.format("%s==%s pos: %d ", a, b, i))
                return false
            }
        }
        return true
    }
    override fun getString(): String {
        return if (correct) "$guessing $actual;${timeout.toFloat()/100} s";
        else "$guessing $actual nicht ${worts.ifEmpty { "EMPTY" }}"
    }

    override fun check(wort: String): Boolean {
        view = LinearLayout(context);
        worts = wort
        view.gravity = Gravity.CENTER_HORIZONTAL;
        var fixed_wort = wort.trim().lowercase();
        if (fixed_wort.contains(" ")) fixed_wort = fixed_wort.split(" ")[1].trim()
        for (i in actual.indices) {
            val text = TextView(context)

            if (fixed_wort.length > i) {
                text.text = actual[i].toString()
                text.setBackgroundColor(if (fixed_wort[i]==actual[i])
                    context.getColor(R.color.light_green) else context.getColor(R.color.light_red))
            } else {
                text.text = actual[i].toString();
                text.setBackgroundColor(context.getColor(R.color.light_blue))
            }
            text.textSize = 20F
            text.width = 60
            text.textAlignment = View.TEXT_ALIGNMENT_CENTER
            view.addView(text)
        }
        return fixed_wort == actual
    }
    override fun view(context: Context): View { return view; }
}
