package dev.levia.lehremich2

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dev.levia.lehremich2.control.ListViewContains
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.stream.Collectors

data class ArrayMap(val name: String, val words: ArrayList<String>) {}

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
