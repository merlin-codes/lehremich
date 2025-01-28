package dev.levia.lehremich2

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.Image
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Parcel
import android.os.Parcelable
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
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.GravityInt
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.get
import androidx.transition.Visibility
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.json.JSONObject
import org.w3c.dom.Text
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.Serializable
import java.nio.charset.StandardCharsets
import java.util.Arrays
import java.util.HashSet
import java.util.Random
import java.util.stream.Collectors
import java.util.zip.Inflater

data class ArrayMap(
    val name: String,
    val words: ArrayList<String>
) {}

class Main : Activity() {
    private val map: ArrayList<ArrayMap> = ArrayList()
    private val list: ArrayList<Boolean> = ArrayList();
    private var can_start: Boolean = false;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        val view = findViewById<ListView>(R.id.test_list_view)
        val fab = findViewById<FloatingActionButton>(R.id.test_fab_view)

        val names = ArrayList<String>()
        val content = JSONObject(loadFile(R.raw.worts))
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
        val adapter = ArrayAdapter<String>(this, R.layout.item, R.id.item_id, names)
        view.adapter = adapter
        fab.setOnClickListener {if (can_start) {
            val intent = Intent(this, Quiz::class.java)
            val l = ArrayList<String>()
            for (i in 0..<list.size) if (list[i]) l.addAll(map[i].words)
            Log.d("EMPTY", String.format("words len %d %d", l.size, list.size))
            intent.putStringArrayListExtra("options", l)
            startActivity(intent)
        }}
        view.setOnItemClickListener { _, v, i, _ ->
            Log.d("EMPTY", String.format("pressed item with id %d and value %b", i, list[i]))
            list[i] = !list[i]
            if (list[i]) {
                v.setBackgroundColor(Color.GRAY)
                can_start = true
            } else {
                v.setBackgroundColor(Color.TRANSPARENT)
                can_start = list.any { it -> it }
            }
            fab.isEnabled = can_start
        }
        fab.isEnabled = can_start
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
enum class QuestionTypes { Verb, Name }
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
        var text = TextView(context);
        text.text = original
        return text;
    }
    open fun getString(): String {return original}
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
        return if (correct) "$guessing $actual";
        else "$guessing $actual nicht ${worts.ifEmpty { "EMPTY" }}"
    }
    override fun check(wort: String): Boolean {
        view = LinearLayout(context);
        worts = wort
        view.gravity = Gravity.CENTER_HORIZONTAL;
        for (i in actual.indices) {
            val text = TextView(context)

            if (wort.length > i) {
                text.text = wort[i].toString()
                text.setBackgroundColor(if (wort[i]==actual[i])
                    context.getColor(R.color.light_green) else context.getColor(R.color.light_red))
            } else {
                text.text = actual[i].toString();
                text.setBackgroundColor(context.getColor(R.color.light_blue))
            }
            text.textSize = 20F
            text.width = 40
            text.textAlignment = View.TEXT_ALIGNMENT_CENTER
            view.addView(text)
        }
        return wort == actual
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
        if (correct) return "$article $original";
        else return "$article $original nicht $your"
    }
}
class Quiz: Activity() {
    private var words: ArrayList<String> = ArrayList();
    private var question: ArrayList<Question> = ArrayList()
    var name: String = ""
    private var position = 0
    private var timer: Long = 0L
    private val COUNT_NUMBER: Int = 10;
    private lateinit var text: TextView;
    private lateinit var image: ImageView;
    private lateinit var progress: LinearLayout;
    private lateinit var der: Button;
    private lateinit var die: Button;
    private lateinit var das: Button;
    private lateinit var prufen: Button;
    private lateinit var antwort: EditText;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.question)
        timer = System.currentTimeMillis();
        val rgn = Random();
        this.words = intent.getStringArrayListExtra("options")!!;
        //("options")
        Log.d("EMPTY", String.format("words len %d", this.words.size))

        while (question.size < COUNT_NUMBER) {
            val word = this.words[rgn.nextInt(this.words.size)]
            if (word.contains(";") && !question.any {i -> i.original == word.split(";")[1]}) {
                if (word.first() == 'd') question.add(QuestionName(word))
                else {
                    var verb = QuestionVerb(word);
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

        text.text = question[position].value()

        der.setOnClickListener { watch(ArticleTypes.DER.name) }
        die.setOnClickListener { watch(ArticleTypes.DIE.name) }
        das.setOnClickListener { watch(ArticleTypes.DAS.name) }
        prufen.setOnClickListener { watch(antwort.text.toString()) }

        if (question[position].type == QuestionTypes.Name) setName(); else setVerb();
    }

    private fun setVerb() {
        der.visibility = View.INVISIBLE
        die.visibility = View.INVISIBLE
        das.visibility = View.INVISIBLE
        prufen.visibility = View.VISIBLE
        antwort.visibility = View.VISIBLE
    }
    private fun setName() {
        der.visibility = View.VISIBLE
        die.visibility = View.VISIBLE
        das.visibility = View.VISIBLE
        prufen.visibility = View.INVISIBLE
        antwort.visibility = View.INVISIBLE
    }

    private fun setArticleBool(there: Boolean) {
        der.isEnabled = there; die.isEnabled = there; das.isEnabled = there; prufen.isEnabled = there
    }
    private fun showInfo(view: View) {
        var dialog = AlertDialog.Builder(this);
        dialog.setTitle("CORRECT")
        // dialog.setMessage(content);
        dialog.setView(view)
        if (position == COUNT_NUMBER)
            dialog.setOnCancelListener {
                Log.d("EMPTY", "cancelling dialog $position")
                changeLayout()
            }
        dialog.show()
    }
    private fun changeLayout() {
        val size = question.filter { it.correct }.size
        intent.putExtra("total", String.format("Total is %d/%d", size, COUNT_NUMBER))
        var intent = Intent(this, ShowResult::class.java)
        intent.putStringArrayListExtra("questions", ArrayList(question.map(Question::getString)))
        startActivity(intent)
        this.finish();
    }
    @SuppressLint("DefaultLocale")
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
        quest.timeout = System.currentTimeMillis() - timer

        val layout = findViewById<ConstraintLayout>(R.id.question_view);
        if (quest.correct) layout.setBackgroundColor(getColor(R.color.light_green))
        else layout.setBackgroundColor(getColor(R.color.light_red))

        progress[position].setBackgroundColor(if (quest.correct) Color.GREEN else Color.RED)
        setArticleBool(false)
        if (!quest.correct) showInfo(quest.view(this))

        position++;
        if (position >= COUNT_NUMBER) {
            changeLayout()
            return;
        }
        Handler().postDelayed({
            layout.setBackgroundColor(Color.TRANSPARENT)
            timer = System.currentTimeMillis()
            val newone = question[position];
            if (newone.type == QuestionTypes.Name) {
                setArticleBool(true)
                setName()
            } else {
                setArticleBool(true)
                setVerb()
                antwort.text.clear()
            }
            text.text = newone.value()
        }, 1000);
    }
}
class Adapter(inflater: Context): BaseAdapter() {
    var list: ArrayList<Question> = ArrayList();
    var inf: LayoutInflater = LayoutInflater.from(inflater);
    override fun getCount(): Int { return list.size; }
    override fun getItem(p0: Int): Any { return list[p0]; }
    override fun getItemId(p0: Int): Long { return 0L; }
    @SuppressLint("DefaultLocale", "SetTextI18n", "ViewHolder", "InflateParams")
    override fun getView(i: Int, view: View?, p2: ViewGroup?): View {
        var v = inf.inflate(R.layout.result_item, null);
        var content = v.findViewById<TextView>(R.id.col_word);
        var time = v.findViewById<TextView>(R.id.col_time);
        var quest = list[i];
        if (!quest.correct) v.setBackgroundColor(Color.parseColor("#550000"))
        content.text = quest.value()
        time.text = String.format("%.2f", quest.timeout)
        return v;
    }
    fun set(list: ArrayList<Question>) { this.list = list }
}
class ShowResult: Activity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.result)
        val view = findViewById<ListView>(R.id.result_list)
        val insie = intent.getStringArrayListExtra("questions")!!
        val adapter = ArrayAdapter<String>(this, R.layout.item, R.id.item_id, insie)
        view.adapter = adapter

        val btn = findViewById<Button>(R.id.btn_continue)
        btn.setOnClickListener {
            finish();
        }
    }
}