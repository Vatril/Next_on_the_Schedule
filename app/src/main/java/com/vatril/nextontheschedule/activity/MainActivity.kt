package com.vatril.nextontheschedule.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.vatril.nextontheschedule.R
import com.vatril.nextontheschedule.adapter.DragCallback
import com.vatril.nextontheschedule.adapter.TaskAdapter
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val adapter = TaskAdapter(this)
        ItemTouchHelper(DragCallback(adapter)).attachToRecyclerView(mainList)
        mainList.adapter = adapter
        mainList.layoutManager = LinearLayoutManager(this)

        addButton.setOnClickListener {
            startActivity(Intent(it.context, AddItemActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        (mainList.adapter as TaskAdapter).refreshTasks()

    }

}
