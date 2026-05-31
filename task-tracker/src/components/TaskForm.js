import React, { Component } from 'react';

class TaskForm extends Component {
  constructor(props) {
    super(props);
    this.state = { title: '' };
    this.handleSubmit = this.handleSubmit.bind(this);
    this.handleChange = this.handleChange.bind(this);
  }

  handleChange(e) {
    this.setState({ title: e.target.value });
  }

  handleSubmit(e) {
    e.preventDefault();
    const { title } = this.state;
    if (!title.trim()) return;
    this.props.onAdd(title.trim());
    this.setState({ title: '' });
  }

  render() {
    return (
      <form className="task-form" onSubmit={this.handleSubmit}>
        <input
          type="text"
          className="task-input"
          placeholder="Add a new task..."
          value={this.state.title}
          onChange={this.handleChange}
        />
        <button type="submit" className="btn-add">
          Add
        </button>
      </form>
    );
  }
}

export default TaskForm;
