package dev.levia.lehremich2

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.get
import dev.levia.lehremich2.control.FlowerForestList
import dev.levia.lehremich2.question.AkkusativDativTypes
import dev.levia.lehremich2.question.ArticleTypes
import dev.levia.lehremich2.question.Question
import dev.levia.lehremich2.question.QuestionAkkDativ
import dev.levia.lehremich2.question.QuestionName
import dev.levia.lehremich2.question.QuestionSentence
import dev.levia.lehremich2.question.QuestionTypes
import dev.levia.lehremich2.question.QuestionVerb
import java.util.Random

class ActivitySign(var context: Activity) {
    data class DERDIEDAS(var der:Button, var die:Button, var das:Button) {}
    data class VERB(var text: EditText, var prufen:Button)
    data class SENTENCE(val prufen: Button, var guessing: FlowerForestList, var original: FlowerForestList)
    data class AKKDATIV(val akkusativ: Button, val dativ: Button)


    lateinit var question: ArrayList<Question>;
    var derdiedas: DERDIEDAS;
    var akkdativ: AKKDATIV
    var verb: VERB
    var image: ImageView;
    var sentence: SENTENCE;
    var position: Int = 0
    var progress: LinearLayout
    var text: TextView
    data class BODY(var name: LinearLayout, var verb: LinearLayout, var setz: LinearLayout, var akkDativ: LinearLayout)
    var bodies: BODY

    init {
        text = context.findViewById(R.id.quiz_text)
        image = context.findViewById(R.id.question_image)
        progress = context.findViewById<LinearLayout>(R.id.progressed)
        bodies = BODY(
            context.findViewById(R.id.quiz_name),
            context.findViewById(R.id.quiz_verb),
            context.findViewById(R.id.sentence_buttons),
            context.findViewById(R.id.akkdat_quiz)
        );

        // NOTICE DerDieDas
        derdiedas = DERDIEDAS(
            context.findViewById<Button>(R.id.btn_der),
            context.findViewById<Button>(R.id.btn_die),
            context.findViewById<Button>(R.id.btn_das),
        )

        verb = VERB(context.findViewById(R.id.antwort), context.findViewById(R.id.btn_confirm))

        // NOTICE Akkusativ und Dativ
        akkdativ = AKKDATIV(context.findViewById(R.id.akkusativ_btn), context.findViewById(R.id.dativ_btn))

        // NOTICE SENTENCES
        sentence = SENTENCE(
            Button(context),
            FlowerForestList(context),
            FlowerForestList(context)
        )
        sentence.prufen.text = "PRÜFEN"
        bodies.setz.addView(sentence.prufen)
        bodies.setz.addView(sentence.guessing)
        bodies.setz.addView(sentence.original)
    }
    public fun get(): Question { return this.question[this.position]; }
}

class Quiz: Activity() {
    private lateinit var o: ActivitySign
    private var words: ArrayList<String> = ArrayList();
    private var question: ArrayList<Question> = ArrayList()
    var name: String = ""
    private var position = 0
    private var timer: Long = 0L
    private var COUNT_NUMBER: Int = 10;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.question)
        o = ActivitySign(this)
        timer = System.currentTimeMillis();
        val rgn = Random();
        this.words = intent.getStringArrayListExtra("options")!!;

        COUNT_NUMBER = intent.getIntExtra("fragens", 10);

        //("options")
        Log.d("EMPTY", String.format("words len %d", this.words.size))

        val end_sentence = arrayOf('.', '?', '!')
        val derdiedas_names = arrayOf("der", "die", "das");
        val akk_dativ = arrayOf("akk", "dat");

        while (question.size < COUNT_NUMBER) {
            val word = this.words[rgn.nextInt(this.words.size)]

            // NOTE here add next Question solution
            if (end_sentence.contains(word.last())) {
                question.add(QuestionSentence(word));
            } else if (derdiedas_names.contains(word.substring(0, 3))) {
                question.add(QuestionName(word))
            } else if (akk_dativ.contains(word.substring(0, 3))) {
                question.add(QuestionAkkDativ(word))
            } else {
                question.add(QuestionVerb(this, word))
            }
        }

        val inflater = LayoutInflater.from(this);
        for (i in 0..<COUNT_NUMBER) {
            var view = inflater.inflate(R.layout.progress_item, o.progress)
            Log.d("EMPTY", "add one more view to progress")
        }

        o.sentence.guessing.addCall { o.sentence.original.add(it) }
        o.sentence.original.addCall { o.sentence.guessing.add(it) }
        o.text.text = question[position].value()

        o.derdiedas.der.setOnClickListener { watch(ArticleTypes.DER.name) }
        o.derdiedas.die.setOnClickListener { watch(ArticleTypes.DIE.name) }
        o.derdiedas.das.setOnClickListener { watch(ArticleTypes.DAS.name) }
        o.akkdativ.akkusativ.setOnClickListener { watch(AkkusativDativTypes.AKKUSATIV.name) }
        o.akkdativ.dativ.setOnClickListener { watch(AkkusativDativTypes.DATIV.name) }
        o.sentence.prufen.setOnClickListener { watch(o.sentence.guessing.toString()) }
        o.verb.prufen.setOnClickListener { watch(o.verb.text.toString()) }

        decideNext()
    }

    /**
     * decide next is because every next question will change little layout visibility
     * something like loading next image because value can be with unique language symbols
     */
    private fun decideNext() {
        if (question.size == position) return;
        Log.d("EMPTY", String.format("next is %s", question[position].type.name));
        val id_str = "drawable/"+question[position].getURL().lowercase()
            .replace("-","_")
            .replace("ß","s")
            .replace("u","u")
            .replace("ö","o")
            .replace("ä","a")
        ;
        val id = resources.getIdentifier(id_str, "drawable", packageName)
        if (id != 0) o.image.setImageDrawable(resources.getDrawable(id))
        else o.image.setImageDrawable(resources.getDrawable(R.drawable.load))

        o.text.text = question[position].value()
        when (question[position].type) {
            QuestionTypes.Name -> {
                o.bodies.name.visibility = View.VISIBLE
                o.bodies.verb.visibility = View.INVISIBLE
                o.bodies.setz.visibility = View.INVISIBLE
                o.bodies.akkDativ.visibility = View.INVISIBLE
            }
            QuestionTypes.Verb -> {
                o.bodies.name.visibility = View.INVISIBLE
                o.bodies.verb.visibility = View.VISIBLE
                o.bodies.setz.visibility = View.INVISIBLE
                o.bodies.akkDativ.visibility = View.INVISIBLE
                o.text.text = question[position].value()
                o.verb.text.text.clear()
            }
            QuestionTypes.Setz -> {
                o.bodies.name.visibility = View.INVISIBLE
                o.bodies.verb.visibility = View.INVISIBLE
                o.bodies.setz.visibility = View.VISIBLE
                o.bodies.akkDativ.visibility = View.INVISIBLE
                val quest = question[position] as QuestionSentence;
                o.sentence.original.clean()
                o.sentence.guessing.clean()
                quest.words.map(o.sentence.original::add)
                o.text.text = ""
            }
            QuestionTypes.AkkDativ -> {
                o.bodies.name.visibility = View.INVISIBLE
                o.bodies.verb.visibility = View.INVISIBLE
                o.bodies.setz.visibility = View.INVISIBLE
                o.bodies.akkDativ.visibility = View.VISIBLE
                o.text.text = question[position].value()
            }
        }
    }
    // this is glory magic of all this is made for disable buttons to not be multiple pressed
    // turns off/on buttons if they are not needed
    private fun setArticleBool(there: Boolean) {
        o.derdiedas.der.isEnabled = there;
        o.derdiedas.die.isEnabled = there;
        o.derdiedas.das.isEnabled = there;
        o.verb.prufen.isEnabled = there
    }
    private fun showInfo(view: View) {
        var dialog = AlertDialog.Builder(this);
        dialog.setTitle("CORRECT")
        // dialog.setMessage(content);
        dialog.setView(view)
        if (position == COUNT_NUMBER) dialog.setOnCancelListener { changeLayout() }
        dialog.show()
        //Log.d("EMPTY", "cancelling dialog $position")
    }
    private fun changeLayout() {
        timer = System.currentTimeMillis()
        val size = question.filter { it.correct }.size
        intent.putExtra("total", String.format("Total is %d/%d", size, COUNT_NUMBER))
        val intent = Intent(this, ShowResult::class.java)
        intent.putStringArrayListExtra("questions", ArrayList(question.map(Question::getString)))
        startActivity(intent)
        this.finish();
    }
    private fun watch(input: String) {
        if (position >= COUNT_NUMBER) {
            Log.d("EMPTY", "pressing button has no result");
            // Log.d("EMPTY", String.format("NEW ONE: %s %s", quest.article.name, quest.word, ));
            // intent.putParcelableArrayListExtra("questions", question);
            return;
        }

        val quest = question[position]
        quest.correct = quest.check(input)
        Log.d("EMPTY", String.format("CORRECT IS %s %s", input, quest.value()))
        quest.timeout = (System.currentTimeMillis() - timer)/10

        val layout = findViewById<ConstraintLayout>(R.id.question_view);
        layout.setBackgroundColor(getColor(if (quest.correct ) R.color.light_green else R.color.light_red))

        o.progress[position++].setBackgroundColor(if (quest.correct) Color.GREEN else Color.RED)
        setArticleBool(false)
        if (!quest.correct) showInfo(quest.view(this))

        if (position >= COUNT_NUMBER) { changeLayout(); return; }
        Handler().postDelayed({
            layout.setBackgroundColor(Color.TRANSPARENT)
            timer = System.currentTimeMillis()
            decideNext()
            setArticleBool(true)
        }, 1000);
    }
}
