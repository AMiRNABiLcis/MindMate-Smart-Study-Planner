package com.example.mindmate

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.mindmate.data.AppDatabase
import com.example.mindmate.data.Subject
import com.example.mindmate.databinding.ActivityEditSubjectBinding
import kotlinx.coroutines.launch

class EditSubjectActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditSubjectBinding
    private val db by lazy { AppDatabase.get(this) }

    private var current: Subject? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditSubjectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val subjectId = intent.getLongExtra("subject_id", -1L)
        if (subjectId != -1L) {
            lifecycleScope.launch {
                db.subjectDao().getById(subjectId)?.let {
                    current = it
                    binding.etSubjectName.setText(it.name)
                    binding.etStudyGoal.setText(it.studyGoal.toString())
                }
            }
        }

        binding.btnBack.setOnClickListener { finish() }
        binding.btnCancel.setOnClickListener { finish() }
        binding.btnSave.setOnClickListener { saveChanges() }
    }

    private fun saveChanges() {
        val name = binding.etSubjectName.text.toString().trim()
        val goal = binding.etStudyGoal.text.toString().toIntOrNull()

        if (name.isEmpty()) {
            binding.etSubjectName.error = "Enter the subject name"
            return
        }
        if (goal == null || goal !in 0..100) {
            binding.etStudyGoal.error = "Enter a number from 0 to 100"
            return
        }

        lifecycleScope.launch {
            val existing = current
            if (existing != null) {
                db.subjectDao().update(existing.copy(name = name, studyGoal = goal))
            } else {
                db.subjectDao().insert(Subject(name = name, studyGoal = goal))
            }
            Toast.makeText(this@EditSubjectActivity, "Saved", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}