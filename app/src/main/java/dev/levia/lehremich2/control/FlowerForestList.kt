package dev.levia.lehremich2.control

import android.content.Context
import android.os.Bundle
import android.text.Layout
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.constraintlayout.helper.widget.Flow
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import com.google.android.flexbox.FlexboxLayout
import dev.levia.lehremich2.R
import java.util.stream.Collectors

public class FlowerForestList(
    val conx: Context,
): ConstraintLayout(conx) {
    lateinit var flower: FlexboxLayout;
    var ids_offset: Int = 0;
    lateinit var callback: (wort: String) -> Unit;
    lateinit var layout: ConstraintLayout;
    companion object {
        const val ID_OFFSET = 10_000;
    }
    var list_buttons: ArrayList<Button> = ArrayList();
    init {
        val view = inflate(context, R.layout.flower_forest_layout, this)
        layout = view as ConstraintLayout;
        flower = view.findViewById<FlexboxLayout>(R.id.flower_manger);
        // var counter = 0;
        // list.map {
        //     val button = Button(context);
        //     button.id = ID_OFFSET + ids_offset + counter++
        //     button.text = it
        //     button.setOnClickListener {
        //         callback.invoke(button.text.toString())
        //     }
        //     list_buttons.add(button)
        //     flower.addView(button)
        // }
        // ids_offset += counter;
    }
    fun addCall(callback: (wort: String) -> Unit) {this.callback = callback}
    fun add(text: String) {
        val button = Button(context)
        button.id = generateViewId() // ID_OFFSET+ids_offset++
        button.text = text
        button.setOnClickListener {
            callback.invoke(text)
            flower.removeView(button)
            list_buttons.remove(button)
        }
        list_buttons.add(button)
        // addView(button)
        flower.addView(button)
        // flower.referencedIds = list_buttons.map{it.id}.toIntArray()
        Log.d("EMPTY", String.format("%s %s", list_buttons.map{it.id}.toIntArray().joinToString(","), list_buttons.joinToString(" ") { it.text }))
    }
    fun clean() {
        list_buttons.map(flower::removeView)
        list_buttons.clear()
    }
    override fun toString(): String {
        return list_buttons.joinToString(";", transform = Button::getText)
    }
}