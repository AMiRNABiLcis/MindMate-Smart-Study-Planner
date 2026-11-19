package com.example.mindmate

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mindmate.data.AppDatabase
import com.example.mindmate.data.Subject
import com.example.mindmate.data.Task
import com.example.mindmate.databinding.ActivityTasksBinding
import kotlinx.coroutines.launch

class TasksActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTasksBinding
    private val db by lazy { AppDatabase.get(this) }
    private lateinit var adapter: TaskAdapter
    private var subjectId = -1L

    private val priorities = listOf("High", "Medium", "Low")
    private val statuses = listOf("Pending", "In Progress", "Done")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTasksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = TaskAdapter(
            onToggle = { task, done ->
                lifecycleScope.launch {
                    db.taskDao().update(
                        task.copy(isDone = done, status = if (done) "Done" else "Pending")
                    )
                }
            },
            onEdit = ::showEditDialog,
            onDelete = { task -> lifecycleScope.launch { db.taskDao().delete(task) } }
        )
        binding.rvTasks.layoutManager = LinearLayoutManager(this)
        binding.rvTasks.adapter = adapter

        binding.spPriority.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, priorities)
        binding.spStatus.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, statuses)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnAddTask.setOnClickListener { addTask() }

        lifecycleScope.launch {
            subjectId = resolveSubjectId()

            db.subjectDao().getById(subjectId)?.let {
                binding.tvSubjectHeader.text = "${it.name}   ${it.studyGoal}%"
            }

            repeatOnLifecycle(Lifecycle.State.STARTED) {
                db.taskDao().observeBySubject(subjectId).collect { adapter.submitList(it) }
            }
        }
    }

    /** لو الشاشة اتفتحت من غير مادة (تجربة)، نستخدم أول مادة أو نعمل واحدة تجريبية */
    private suspend fun resolveSubjectId(): Long {
        val passed = intent.getLongExtra("subject_id", -1L)
        if (passed != -1L) return passed
        return db.subjectDao().getFirst()?.id
            ?: db.subjectDao().insert(Subject(name = "Programming", studyGoal = 35))
    }

    private fun addTask() {
        val title = binding.etTaskTitle.text.toString().trim()
        if (title.isEmpty()) {
            binding.etTaskTitle.error = "Enter task title"
            return
        }
        val status = binding.spStatus.selectedItem as String
        lifecycleScope.launch {
            db.taskDao().insert(
                Task(
                    subjectId = subjectId,
                    title = title,
                    priority = binding.spPriority.selectedItem as String,
                    status = status,
                    isDone = status == "Done"
                )
            )
            binding.etTaskTitle.text.clear()
        }
    }

    private fun showEditDialog(task: Task) {
        val input = EditText(this).apply { setText(task.title) }
        AlertDialog.Builder(this)
            .setTitle("Edit task")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newTitle = input.text.toString().trim()
                if (newTitle.isNotEmpty()) {
                    lifecycleScope.launch { db.taskDao().update(task.copy(title = newTitle)) }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}