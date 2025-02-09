package dev.levia.lehremich2

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.get
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dev.levia.lehremich2.control.FlowerForestList
import dev.levia.lehremich2.control.ListViewContains
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.security.KeyStore.Entry.Attribute
import java.util.Random
import java.util.stream.Collectors

data class ArrayMap(
    val name: String,
    val words: ArrayList<String>
) {}

class Main : Activity() {
    private val map: ArrayList<ArrayMap> = ArrayList()
    private val list: ArrayList<Boolean> = ArrayList();
    private var can_start: Boolean = false;
    private lateinit var fragens: SeekBar;
    private lateinit var fragens_num: TextView;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        val view = findViewById<RecyclerView>(R.id.test_list_view)
        val fab = findViewById<FloatingActionButton>(R.id.test_fab_view)

        val names = ArrayList<String>()
        val content = JSONObject(loadFile(R.raw.worts3))
        val keys = content.keys()
        var counter = 0
        while (keys.hasNext()) {
            val key = keys.next();
            val array = content.getJSONArray(key)
            val list = ArrayList<String>()
            for (i in 0..<array.length()) {
                list.add(array.getString(i))
            }
            Log.d("EMPTY", String.format("MAP SIZE: %d %d", map.size, array.length()))
            names.add(key)
            this.list.add(false)
            map.add(ArrayMap(key, list))
        }
        val adapter = ListViewContains.Adapter(ListViewContains.from(names));
        // val adapter = ArrayAdapter<String>(this, R.layout.item, R.id.item_id, names)
        view.adapter = adapter
        fab.setOnClickListener {if (can_start) {
            val intent = Intent(this, Quiz::class.java)
            val l = ArrayList<String>()
            val questions = adapter.getSelectedIds();
            for (id in questions) l.addAll(map[id].words)
            Log.d("EMPTY", String.format("words len %d %d", l.size, list.size))
            intent.putStringArrayListExtra("options", l)
            intent.putExtra("fragens", fragens.progress)
            startActivity(intent)
        }}
        view.layoutManager = LinearLayoutManager(this)
        adapter.setAllListener {
            Log.d("EMPTy", String.format("it is called darkness: %b", it))
            can_start = it;
            fab.isEnabled = it
        }

        fab.isEnabled = can_start
        fragens = findViewById<SeekBar>(R.id.main_bar)
        fragens_num = findViewById<TextView>(R.id.frage_text)
        fragens.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                fragens_num.text = "fragen: ${fragens.progress}"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) { }
            override fun onStopTrackingTouch(seekBar: SeekBar) { }
        })
    }
    private fun loadFile(name: Int): String {
        return BufferedReader(InputStreamReader(resources.openRawResource(name), StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == 200) {
            // TODO display new result of activity as new intent that can than be exited
            // val intent = Intent(this, ShowResult::class.java)
            // if (data != null) { intent.putParcelableArrayListExtra("questions", data.getParcelableArrayListExtra("questions")) };
            // startActivity(intent);
        }
        if (data != null) {
            // Toast.makeText(this, data.getStringExtra("total"), Toast.LENGTH_LONG).show()
        }
        Toast.makeText(this, "some random", Toast.LENGTH_LONG).show();
    }
}
enum class ArticleTypes { DER, DIE, DAS }
enum class QuestionTypes { Verb, Name, Setz }
enum class WortTypes {
    ich, du, er, sie, es, ihr, wir, Sie
}

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

data class QuestionVerb(var wort: String): Question() {
    var ich: String = ""
    var du: String = ""
    var er_sie_es: String = ""
    var ihr: String = ""
    var wir_Sie: String = ""
    var guessing: WortTypes = WortTypes.ich
    var actual: String = ""
    lateinit var view: LinearLayout;
    lateinit var context: Context;
    lateinit var worts: String;

    init {
        val words = wort.split(";")
        super.original = words[0]
        this.ich = words[1]
        this.du = words[2]
        this.er_sie_es = words[3]
        this.ihr = words[4]
        this.wir_Sie = words[5]
        this.type = QuestionTypes.Verb
    }
    @SuppressLint("DefaultLocale") override fun value(): String {
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
    fun getActual() {
        actual = when (this.guessing) {
            WortTypes.ich -> ich
            WortTypes.du -> du
            WortTypes.er, WortTypes.es, WortTypes.sie -> er_sie_es
            WortTypes.ihr -> ihr
            WortTypes.wir, WortTypes.Sie -> wir_Sie
        }
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
    @SuppressLint("SetTextI18n") override fun view(context: Context): View {
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
data class QuestionSentence(var wort: String): Question() {
    var words: List<String> = wort.split(";");
    var your: List<String> = listOf();
    init {
        original = wort;
        type = QuestionTypes.Setz
    }

    override fun check(wort: String): Boolean {
        your = wort.split(";");
        Log.d("EMPTY", String.format("%s == %s", wort, original))
        return wort == original
    }

    @SuppressLint("SetTextI18n") override fun view(context: Context): View {
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

class Quiz: Activity() {
    private var words: ArrayList<String> = ArrayList();
    private var question: ArrayList<Question> = ArrayList()
    var name: String = ""
    private var position = 0
    private var timer: Long = 0L
    private var COUNT_NUMBER: Int = 10;
    private lateinit var text: TextView;
    private lateinit var image: ImageView;
    private lateinit var progress: LinearLayout;

    private lateinit var quiz_name: LinearLayout;
    private lateinit var der: Button;
    private lateinit var die: Button;
    private lateinit var das: Button;

    private lateinit var quiz_verb: LinearLayout;
    private lateinit var antwort: EditText;
    private lateinit var prufen: Button;

    private lateinit var layout: View;

    private lateinit var quiz_setz: LinearLayout;
    private lateinit var sentence_guessing: FlowerForestList;
    private lateinit var sentence_original: FlowerForestList;

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.question)
        timer = System.currentTimeMillis();
        val rgn = Random();
        this.words = intent.getStringArrayListExtra("options")!!;

        COUNT_NUMBER = intent.getIntExtra("fragens", 10);

        //("options")
        Log.d("EMPTY", String.format("words len %d", this.words.size))

        while (question.size < COUNT_NUMBER) {
            val word = this.words[rgn.nextInt(this.words.size)]

            if (word.contains(";") && !question.any {i -> i.original == word.split(";")[1]}) {
                if(word.endsWith(".") || word.endsWith("!") || word.endsWith("?")) {
                    val quest = QuestionSentence(word);
                    quest.words = quest.words.shuffled()
                    question.add(quest);
                } else if (word.first() == 'd') {
                    question.add(QuestionName(word))
                } else {
                    val verb = QuestionVerb(word);
                    question.add(verb)
                    verb.guessing = WortTypes.entries.toTypedArray().random();
                    verb.getActual()
                    verb.context = this;
                }
            }
        }
        progress = findViewById<LinearLayout>(R.id.progressed)
        val inflater = LayoutInflater.from(this);
        for (i in 0..<COUNT_NUMBER) {
            var view = inflater.inflate(R.layout.progress_item, progress)
            Log.d("EMPTY", "add one more view to progress")
        }

        der = findViewById<Button>(R.id.btn_der)
        die = findViewById<Button>(R.id.btn_die)
        das = findViewById<Button>(R.id.btn_das)
        text = findViewById<TextView>(R.id.quiz_text)
        image = findViewById<ImageView>(R.id.question_image)
        prufen = findViewById<Button>(R.id.btn_confirm)
        antwort = findViewById<EditText>(R.id.antwort)
        quiz_name = findViewById<LinearLayout>(R.id.quiz_name)
        quiz_verb = findViewById<LinearLayout>(R.id.quiz_verb)

        quiz_setz = findViewById<LinearLayout>(R.id.sentence_buttons);
        val button = Button(this);
        button.text = "PRÜFEN";

        quiz_setz.addView(button)
        sentence_guessing = FlowerForestList(this);
        quiz_setz.addView(sentence_guessing)
        sentence_original = FlowerForestList(this);
        quiz_setz.addView(sentence_original)

        sentence_guessing.addCall { sentence_original.add(it) }
        sentence_original.addCall { sentence_guessing.add(it) }


        text.text = question[position].value()
        layout = findViewById<ConstraintLayout>(R.id.question_view);

        button.setOnClickListener { watch(sentence_guessing.toString()) }
        der.setOnClickListener { watch(ArticleTypes.DER.name) }
        die.setOnClickListener { watch(ArticleTypes.DIE.name) }
        das.setOnClickListener { watch(ArticleTypes.DAS.name) }
        prufen.setOnClickListener { watch(antwort.text.toString()) }

        decideNext()
    }
    @SuppressLint("UseCompatLoadingForDrawables")
    private fun loadImage() {
        val id_str = "drawable/"+question[position].getURL().lowercase()
            .replace("-","_")
            .replace("ß","s")
            .replace("u","u")
            .replace("ö","o")
            .replace("ä","a")
        ;
        val id = resources.getIdentifier(id_str, "drawable", packageName)
        if (id != 0) image.setImageDrawable(resources.getDrawable(id))
        else image.setImageDrawable(resources.getDrawable(R.drawable.load))
        Log.d("EMPTY", String.format("%s %s", id_str, id))
    }
    private fun decideNext() {
        Log.d("EMPTY", String.format("next is %s", question[position].type.name));
        when (question[position].type) {
            QuestionTypes.Name -> setName();
            QuestionTypes.Verb -> setVerb();
            QuestionTypes.Setz -> setSetz();
        }
    }

    private fun setVerb() {
        quiz_name.visibility = View.INVISIBLE
        quiz_verb.visibility = View.VISIBLE
        quiz_setz.visibility = View.INVISIBLE
        text.text = question[position].value()
        antwort.text.clear()
        loadImage()
    }
    private fun setName() {
        quiz_name.visibility = View.VISIBLE
        quiz_verb.visibility = View.INVISIBLE
        quiz_setz.visibility = View.INVISIBLE
        text.text = question[position].value()
        loadImage()
    }
    private fun setSetz() {
        quiz_name.visibility = View.INVISIBLE
        quiz_verb.visibility = View.INVISIBLE
        quiz_setz.visibility = View.VISIBLE
        val quest = question[position] as QuestionSentence;
        sentence_original.clean()
        sentence_guessing.clean()
        quest.words.map(sentence_original::add)
        text.text = ""
        loadImage()
    }

    // this is glory magic of all this is made for disable buttons to not be multiple pressed
    // turns off/on buttons if they are not needed
    private fun setArticleBool(there: Boolean) {
        der.isEnabled = there; die.isEnabled = there; das.isEnabled = there; prufen.isEnabled = there
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
    @SuppressLint("DefaultLocale") private fun changeLayout() {
        timer = System.currentTimeMillis()
        val size = question.filter { it.correct }.size
        intent.putExtra("total", String.format("Total is %d/%d", size, COUNT_NUMBER))
        val intent = Intent(this, ShowResult::class.java)
        intent.putStringArrayListExtra("questions", ArrayList(question.map(Question::getString)))
        startActivity(intent)
        this.finish();
    }
    @SuppressLint("DefaultLocale") private fun watch(input: String) {
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

        progress[position++].setBackgroundColor(if (quest.correct) Color.GREEN else Color.RED)
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
data class QuestionResultShow(
    var correct: String,
    var second: String,
    var incorrect: Boolean,
){}
class Adapter(inflater: Context): BaseAdapter() {
    private var list: ArrayList<QuestionResultShow> = ArrayList();
    private var inf: LayoutInflater = LayoutInflater.from(inflater);
    override fun getCount(): Int { return list.size; }
    override fun getItem(p0: Int): Any { return list[p0]; }
    override fun getItemId(p0: Int): Long { return 0L; }

    @SuppressLint("DefaultLocale", "SetTextI18n", "ViewHolder", "InflateParams")
    override fun getView(i: Int, view: View?, p2: ViewGroup?): View {
        val v = inf.inflate(R.layout.result_item, null);
        val your = v.findViewById<TextView>(R.id.col_your_word);
        val corr = v.findViewById<TextView>(R.id.col_correct_word);
        val quest = list[i];
        if (quest.incorrect) v.setBackgroundColor(Color.parseColor("#550000"))
        your.text = quest.correct;
        corr.text = quest.second // if (quest.incorrect != null) quest.incorrect else ""
        // time.text = String.format("%.2f", quest.timeout)
        return v;
    }
    fun set(list: ArrayList<String>) {
        for (res in list) {
            if (res.contains("nicht")) {
                val split = res.split("nicht")
                this.list.add(QuestionResultShow(split[0], split[1], true))
            } else {
                val split = res.split(";")
                this.list.add(QuestionResultShow(split[0], split[1], false))
            }
        };
    }
}
class ShowResult: Activity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.result)
        val view = findViewById<ListView>(R.id.result_list)
        val insie = intent.getStringArrayListExtra("questions")!!
        val adapter = Adapter(this) // ArrayAdapter<String>(this, R.layout.item, R.id.item_id, insie)
        adapter.set(insie)
        view.adapter = adapter

        val btn = findViewById<Button>(R.id.btn_continue)
        btn.setOnClickListener {
            finish();
        }
    }
}