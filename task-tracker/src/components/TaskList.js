import React, { Component } from 'react';
import TaskItem from './TaskItem';

class TaskList extends Component {
  render() {
    const { tasks, onToggle, onDelete } = this.props;

    if (tasks.length === 0) {
      return <p className="status">No tasks yet. Add one above!</p>;
    }

    return (
      <ul className="task-list">
        {tasks.map(task => (
          <TaskItem
            key={task.id}
            task={task}
            onToggle={onToggle}
            onDelete={onDelete}
          />
        ))}
      </ul>
    );
  }
}

export default TaskList;
