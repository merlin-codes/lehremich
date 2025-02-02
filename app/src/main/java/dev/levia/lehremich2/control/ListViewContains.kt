package dev.levia.lehremich2.control

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dev.levia.lehremich2.R
import java.util.concurrent.atomic.AtomicInteger
import java.util.stream.Collectors
import kotlin.math.absoluteValue

class ListViewContains() {
    companion object {
        fun from(list: List<String>): ArrayList<Item> {
            val list2 = ArrayList<Item>();
            for (item in list) list2.add(Item(item));
            return list2;
        }
    }

    data class Item(
        var name: String,
        var can_be_selected: Boolean = false,
        var id: Int = 0,
    ) {
        fun equal(value: String): Boolean {
            for (i in name.indices) if (value[i] != name[i]) return false;
            return true;
        }
    }

    class ViewHolder(v: View): RecyclerView.ViewHolder(v) {
        val text: Button = v.findViewById(R.id.item_id)
    }

    class Adapter(private var list: ArrayList<Item>): RecyclerView.Adapter<ViewHolder>() {
        lateinit var callback: (Boolean) -> Unit
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflate = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false));
            return inflate;
        }
        @SuppressLint("NotifyDataSetChanged")
        fun setData(input: ArrayList<Item>) {
            this.list = input;
            notifyDataSetChanged();
        }

        override fun getItemCount(): Int { return this.list.size; }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = this.list[position];
            holder.text.text = item.name;
            holder.text.setBackgroundColor(if (item.can_be_selected) Color.GRAY else Color.TRANSPARENT)

            item.id = position;
            holder.text.setOnClickListener(
                OnClickListener {
                    val item = list[position];
                    var start = true;
                    if (!item.can_be_selected) {
                        it.setBackgroundColor(Color.GRAY)
                        item.can_be_selected = true;
                    } else {
                        Log.d("EMPTY", String.format("id: %d with %b", item.id, item.can_be_selected))
                        it.setBackgroundColor(Color.TRANSPARENT)
                        item.can_be_selected = false;
                        start = list.any {i -> i.can_be_selected};
                    }
                    callback.invoke(start)
                }
            );
        }
        fun setAllListener(callback: (Boolean) -> Unit) {
            this.callback = callback
        }
        fun getSelectedIds(): List<Int> {
            return this.list.filter { i -> i.can_be_selected }.map { i -> i.id };
        }
    }
}

