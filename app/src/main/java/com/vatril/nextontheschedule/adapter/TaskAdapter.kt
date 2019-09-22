package com.vatril.nextontheschedule.adapter

import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.vatril.nextontheschedule.R
import com.vatril.nextontheschedule.TaskWidget
import com.vatril.nextontheschedule.activity.AddItemActivity
import com.vatril.nextontheschedule.entity.Task
import com.vatril.nextontheschedule.entity.TaskDatabase
import kotlinx.android.synthetic.main.schedule_item.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer


class TaskAdapter internal constructor(
    private val context: Context
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {


    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var tasks = emptyList<Task>()

    override fun getItemCount() = tasks.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val itemView = inflater.inflate(R.layout.schedule_item, parent, false)
        return TaskViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val extensions = listOf(
            StrikethroughExtension.create()
        )
        val parser = Parser.builder().extensions(extensions).build()
        val renderer = HtmlRenderer.builder().extensions(extensions).build()
        holder.itemView.title.text = HtmlCompat.fromHtml(
            renderer.render(parser.parse(tasks[position].name)), HtmlCompat.FROM_HTML_MODE_COMPACT)
        holder.itemView.description.text = HtmlCompat.fromHtml(
            renderer.render(parser.parse(tasks[position].description.orEmpty())), HtmlCompat.FROM_HTML_MODE_COMPACT)
        holder.itemView.colorDisplay.background = ColorDrawable(tasks[position].color)
        holder.itemView.editbtn.setOnClickListener {
            val intent = Intent(it.context, AddItemActivity::class.java)
            intent.putExtra("task", this.tasks[position].uid)
            context.startActivity(intent)
        }
    }


    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private fun setTasks(tasks: List<Task>){
        this.tasks = tasks
        notifyDataSetChanged()
    }

    fun swapTasks(old: Int, new: Int){
        val ntask = this.tasks[new]
        val otask = this.tasks[old]
        val newRank = ntask.rank
        ntask.rank = otask.rank
        otask.rank = newRank

        val taskdao = TaskDatabase.getDatabase(this.context).taskDao()

        GlobalScope.launch {
            taskdao.insert(ntask)
            taskdao.insert(otask)
            TaskWidget.updateAll(context)
        }
    }

    fun removeTask(taskId: Int){
        val taskdao = TaskDatabase.getDatabase(this.context).taskDao()
        val task = this.tasks[taskId]

        runBlocking {
            if(task.backToBottom){
                taskdao.insert(
                    Task(
                        0,
                        taskdao.getLastRank()?.plus(1)?:0,
                        task.name,
                        task.description,
                        task.color,
                        task.backToBottom
                    )
                )
            }
            taskdao.delete(task)
            TaskWidget.updateAll(context)
            refreshTasks()
            notifyItemRemoved(taskId)
        }

    }

    internal fun refreshTasks() {

        val taskdao = TaskDatabase.getDatabase(this.inflater.context).taskDao()

        runBlocking {
            setTasks(taskdao.getAll())
        }
    }
}

class DragCallback(private val adapter: TaskAdapter) : ItemTouchHelper.Callback(){
    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ) = makeMovementFlags(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN,
        ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT)

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ) : Boolean {
        adapter.swapTasks(viewHolder.adapterPosition, target.adapterPosition)
        adapter.notifyItemMoved(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        adapter.removeTask(viewHolder.adapterPosition)
    }

}