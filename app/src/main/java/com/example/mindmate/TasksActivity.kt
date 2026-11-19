package com.example.mindmate

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mindmate.data.AppDatabase
import com.example.mindmate.data.Task
import com.example.mindmate.databinding.ActivityTasksBinding
import kotlinx.coroutines.flow.combine
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
        binding.root.applySystemBarsPadding()

        subjectId = intent.getLongExtra("subject_id", -1L)
        if (subjectId == -1L) {          // الشاشة لازم تتفتح من مادة
            finish()
            return
        }

        adapter = TaskAdapter(
            onToggle = { task, done ->
                lifecycleScope.launch {
                    db.taskDao().update(
                        task.copy(isDone = done, status = if (done) "Done" else "Pending")
                    )
                }
            },
            onEdit = ::showEditDialog,
            onDelete = ::confirmDelete
        )
        binding.rvTasks.layoutManager = LinearLayoutManager(this)
        binding.rvTasks.adapter = adapter

        binding.spPriority.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, priorities)
        binding.spPriority.setSelection(1) // Medium
        binding.spStatus.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, statuses)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnAddTask.setOnClickListener { addTask() }
        binding.tabOverview.setOnClickListener { finish() }   // Overview = شاشة تفاصيل المادة
        binding.tabNotes.setOnClickListener {
            Toast.makeText(this, "Notes: coming soon", Toast.LENGTH_SHORT).show()
        }

        // الهيدر والليست بيتحدثوا مع أي تغيير (الهيدر = نسبة الإنجاز الحقيقية)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(
                    db.subjectDao().observeById(subjectId),
                    db.taskDao().observeBySubject(subjectId)
                ) { subject, tasks -> subject to tasks }
                    .collect { (subject, tasks) ->
                        if (subject == null) {   // المادة اتمسحت
                            finish()
                            return@collect
                        }
                        val pct = if (tasks.isEmpty()) 0 else tasks.count { it.isDone } * 100 / tasks.size
                        binding.tvSubjectHeader.text = "${subject.name}   $pct%"
                        adapter.submitList(tasks)
                    }
            }
        }
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
            binding.spPriority.setSelection(1)
            binding.spStatus.setSelection(0)
        }
    }

    private fun confirmDelete(task: Task) {
        AlertDialog.Builder(this)
            .setTitle("Delete task?")
            .setMessage("\"${task.title}\" will be removed.")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch { db.taskDao().delete(task) }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /** تعديل العنوان + الأولوية + الحالة */
    private fun showEditDialog(task: Task) {
        val pad = (16 * resources.displayMetrics.density).toInt()
        val input = EditText(this).apply { setText(task.title) }
        val spPriority = Spinner(this).apply {
            adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, priorities)
            setSelection(priorities.indexOf(task.priority).coerceAtLeast(0))
        }
        val spStatus = Spinner(this).apply {
            adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, statuses)
            setSelection(statuses.indexOf(task.status).coerceAtLeast(0))
        }
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad / 2, pad, 0)
            addView(input, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            addView(spPriority, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            addView(spStatus, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }
        AlertDialog.Builder(this)
            .setTitle("Edit task")
            .setView(container)
            .setPositiveButton("Save") { _, _ ->
                val newTitle = input.text.toString().trim()
                if (newTitle.isEmpty()) {
                    Toast.makeText(this, "Title can't be empty", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val newStatus = spStatus.selectedItem as String
                lifecycleScope.launch {
                    db.taskDao().update(
                        task.copy(
                            title = newTitle,
                            priority = spPriority.selectedItem as String,
                            status = newStatus,
                            isDone = newStatus == "Done"
                        )
                    )
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
