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
        binding.root.applySystemBarsPadding()

        val subjectId = intent.getLongExtra("subject_id", -1L)
        if (subjectId == -1L) {
            finish()
            return
        }

        lifecycleScope.launch {
            val subject = db.subjectDao().getById(subjectId)
            if (subject == null) {
                finish()
                return@launch
            }
            current = subject
            binding.etSubjectName.setText(subject.name)
            binding.etStudyGoal.setText(subject.studyGoal.toString())
        }

        binding.btnBack.setOnClickListener { finish() }
        binding.btnCancel.setOnClickListener { finish() }
        binding.btnSave.setOnClickListener { saveChanges() }
    }

    private fun saveChanges() {
        val subject = current ?: return
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
            db.subjectDao().update(subject.copy(name = name, studyGoal = goal))
            Toast.makeText(this@EditSubjectActivity, "Saved", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
