package com.vatril.nextontheschedule

import android.app.IntentService
import android.content.Intent
import android.content.Context
import android.util.Log
import com.vatril.nextontheschedule.entity.Task
import com.vatril.nextontheschedule.entity.TaskDatabase
import kotlinx.coroutines.runBlocking


class RemoveTask : IntentService("RemoveTask") {

    override fun onHandleIntent(intent: Intent?) {
       if(intent != null){
           val taskdao = TaskDatabase.getDatabase(this).taskDao()
           val context = this

           runBlocking {
               try {
                   val task = taskdao.get(intent.getIntExtra("task", -1))
                   if(task != null) {
                       if (task.backToBottom) {
                           taskdao.insert(
                               Task(
                                   0,
                                   taskdao.getLastRank()?.plus(1) ?: 0,
                                   task.name,
                                   task.description,
                                   task.color,
                                   task.backToBottom
                               )
                           )
                       }
                       taskdao.delete(task)
                   }
               }finally {
                   TaskWidget.updateAll(context)
               }
           }
       }
    }
}
