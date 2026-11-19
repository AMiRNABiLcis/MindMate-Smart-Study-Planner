package com.example.mindmate

class TaskFilter {
    fun filterTasks(
        list: List<Task>,
        predicate: (Task) -> Boolean
    ): List<Task> {
        return list.filter(predicate)
    }
}
