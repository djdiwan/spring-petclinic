import React, { Component } from 'react';
import TaskList from './TaskList';
import TaskForm from './TaskForm';
import { fetchTasks, addTask, updateTask, deleteTask } from '../api';

class App extends Component {
  constructor(props) {
    super(props);
    this.state = {
      tasks: [],
      loading: true,
      error: null,
    };
    this.handleAddTask = this.handleAddTask.bind(this);
    this.handleToggleComplete = this.handleToggleComplete.bind(this);
    this.handleDeleteTask = this.handleDeleteTask.bind(this);
  }

  componentDidMount() {
    this.loadTasks();
  }

  loadTasks() {
    fetchTasks()
      .then(tasks => this.setState({ tasks, loading: false }))
      .catch(() => this.setState({ error: 'Failed to load tasks. Please try again.', loading: false }));
  }

  handleAddTask(title) {
    const newTask = { title, completed: false };
    addTask(newTask)
      .then(task => {
        this.setState(prevState => ({
          tasks: [...prevState.tasks, task],
        }));
      })
      .catch(() => {
        this.setState({ error: 'Failed to add task. Please try again.' });
      });
  }

  handleToggleComplete(id) {
    const task = this.state.tasks.find(t => t.id === id);
    if (!task) return;
    updateTask(id, { completed: !task.completed })
      .then(updated => {
        this.setState(prevState => ({
          tasks: prevState.tasks.map(t => (t.id === id ? updated : t)),
        }));
      })
      .catch(() => {
        this.setState({ error: 'Failed to update task. Please try again.' });
      });
  }

  handleDeleteTask(id) {
    deleteTask(id)
      .then(() => {
        this.setState(prevState => ({
          tasks: prevState.tasks.filter(t => t.id !== id),
        }));
      })
      .catch(() => {
        this.setState({ error: 'Failed to delete task. Please try again.' });
      });
  }

  render() {
    const { tasks, loading, error } = this.state;

    return (
      <div className="app">
        <h1>Task Tracker</h1>
        <TaskForm onAdd={this.handleAddTask} />
        {loading && <p className="status">Loading tasks...</p>}
        {error && <p className="status error">Error: {error}</p>}
        {!loading && !error && (
          <TaskList
            tasks={tasks}
            onToggle={this.handleToggleComplete}
            onDelete={this.handleDeleteTask}
          />
        )}
      </div>
    );
  }
}

export default App;
