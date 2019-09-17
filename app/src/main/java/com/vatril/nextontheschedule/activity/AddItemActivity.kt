package com.vatril.nextontheschedule.activity


import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.core.view.children
import com.vatril.nextontheschedule.R
import com.vatril.nextontheschedule.TaskWidget
import com.vatril.nextontheschedule.entity.Task
import com.vatril.nextontheschedule.entity.TaskDatabase
import kotlinx.android.synthetic.main.activity_add_item.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class AddItemActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item)

        val taskID = intent.getIntExtra("task", -1)
        var task: Task? = null
        val taskdao = TaskDatabase.getDatabase(this).taskDao()

        if(taskID >= 0){
            runBlocking {
                val temptask = taskdao.get(taskID)
                if(temptask != null){
                    editName.setText(temptask.name)
                    editDescription.setText(temptask.description)
                    addToBottom.isChecked = temptask.backToBottom
                    colorRadios.check(getRadioFromColor(temptask.color))
                    task = temptask
                }
            }
        }

        savebtn.setOnClickListener {
            if(TextUtils.isEmpty(editName.text?.trim())){
                editName.error = getString(R.string.error_must_have_name)
                return@setOnClickListener
            }

            GlobalScope.launch {
                taskdao.insert(
                    Task(
                        if (task != null) task!!.uid else 0,
                        if (task != null) task!!.rank else taskdao.getLastRank()?.plus(1)?:0,
                        editName.text?.trim().toString(),
                        editDescription.text?.trim().toString(),
                        (findViewById<View>(colorRadios.checkedRadioButtonId)
                            .background as ColorDrawable).color,
                        addToBottom.isChecked
                    )
                )
                TaskWidget.updateAll(editName.context)
                finish()
            }
        }
    }

    private fun getRadioFromColor(color: Int) :Int{
        colorRadios.children.forEach {
            if((it.background as ColorDrawable).color == color){
                return it.id
            }
        }
        return R.id.radioWhite
    }
}
