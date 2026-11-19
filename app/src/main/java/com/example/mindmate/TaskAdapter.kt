package com.example.mindmate

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mindmate.data.Task
import com.example.mindmate.databinding.ItemTaskBinding

class TaskAdapter(
    private val onToggle: (Task, Boolean) -> Unit,
    private val onEdit: (Task) -> Unit,
    private val onDelete: (Task) -> Unit
) : ListAdapter<Task, TaskAdapter.VH>(Diff) {

    class VH(val b: ItemTaskBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val task = getItem(position)
        val b = holder.b

        b.tvTitle.text = task.title
        b.tvPriority.text = task.priority
        b.tvStatus.text = task.status
        b.tvTitle.paintFlags =
            if (task.isDone) b.tvTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            else b.tvTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()

        b.cbDone.setOnCheckedChangeListener(null)
        b.cbDone.isChecked = task.isDone
        b.cbDone.setOnCheckedChangeListener { _, checked -> onToggle(task, checked) }

        b.btnEdit.setOnClickListener { onEdit(task) }
        b.btnDelete.setOnClickListener { onDelete(task) }
    }

    private object Diff : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(a: Task, b: Task) = a.id == b.id
        override fun areContentsTheSame(a: Task, b: Task) = a == b
    }
}