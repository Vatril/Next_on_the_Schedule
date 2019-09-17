package com.vatril.nextontheschedule.entity


import android.content.Context
import androidx.room.*


@Entity
data class Task(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    @ColumnInfo(name = "rank") var rank: Long,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "description") val description: String?,
    @ColumnInfo(name = "color") val color: Int,
    @ColumnInfo(name = "backToBottom") val backToBottom: Boolean
)

@Dao
interface TaskDao {

    @Query("SELECT * FROM task ORDER BY rank ASC")
    suspend fun getAll(): List<Task>

    @Query("SELECT rank FROM task ORDER BY rank DESC LIMIT 1")
    suspend fun getLastRank(): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task)

    @Delete
    suspend fun delete(user: Task)

    @Query("SELECT * FROM task WHERE uid = :uid")
    suspend fun get(uid: Int): Task?
}

@Database(entities = [Task::class], version = 1)
abstract class TaskDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao

    companion object {

        @Volatile
        private var INSTANCE: TaskDatabase? = null

        fun getDatabase(context: Context): TaskDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaskDatabase::class.java,
                    "task_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}