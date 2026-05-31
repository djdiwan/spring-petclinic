import React, { Component } from 'react';

class TaskItem extends Component {
  render() {
    const { task, onToggle, onDelete } = this.props;

    return (
      <li className={`task-item ${task.completed ? 'completed' : ''}`}>
        <label className="task-label">
          <input
            type="checkbox"
            checked={task.completed}
            onChange={() => onToggle(task.id)}
          />
          <span className="task-title">{task.title}</span>
        </label>
        <button
          className="btn-delete"
          onClick={() => onDelete(task.id)}
          title="Delete task"
        >
          &times;
        </button>
      </li>
    );
  }
}

export default TaskItem;
