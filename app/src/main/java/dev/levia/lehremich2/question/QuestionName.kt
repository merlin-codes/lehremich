package dev.levia.lehremich2.question

import android.content.Context
import android.view.View
import android.widget.TextView

enum class ArticleTypes { DER, DIE, DAS }

data class QuestionName(var wort: String): Question() {
    private var article: ArticleTypes = ArticleTypes.DER;
    lateinit var your: String;

    init {
        val before = wort.split(";")
        if (before[0] == "der") this.article = ArticleTypes.DER
        else if (before[0] == "die") this.article = ArticleTypes.DIE
        else this.article = ArticleTypes.DAS
        this.original = before[1]
    }
    override fun check(wort: String): Boolean {
        your = wort
        return article.name == wort
    }
    override fun view(context: Context): View {
        val text = TextView(context);
        text.text = "${article.name} $original nicht $your";
        text.textAlignment = View.TEXT_ALIGNMENT_CENTER
        text.textSize = 20F
        return text;
    }

    override fun getString(): String {
        if (correct) return "$article $original;${timeout.toFloat()/100} s";
        else return "$article $original nicht $your"
    }
}
