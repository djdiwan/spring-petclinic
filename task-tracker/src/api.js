const API_URL = '/tasks';

const MAX_TITLE_LENGTH = 256;

class ApiError extends Error {
  constructor(status, statusText) {
    super(`Request failed: ${status}`);
    this.status = status;
    this.statusText = statusText;
  }
}

function handleResponse(res) {
  if (!res.ok) {
    throw new ApiError(res.status, res.statusText);
  }
  return res;
}

function validateId(id) {
  const numId = Number(id);
  if (!Number.isInteger(numId) || numId <= 0) {
    throw new Error('Invalid task ID');
  }
  return numId;
}

export function sanitizeTitle(title) {
  if (typeof title !== 'string') return '';
  return title.replace(/<[^>]*>/g, '').trim().slice(0, MAX_TITLE_LENGTH);
}

export function fetchTasks() {
  return fetch(API_URL)
    .then(handleResponse)
    .then(res => res.json());
}

export function addTask(task) {
  const sanitized = sanitizeTitle(task.title);
  if (!sanitized) {
    return Promise.reject(new Error('Task title is required'));
  }
  return fetch(API_URL, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ title: sanitized, completed: Boolean(task.completed) }),
  })
    .then(handleResponse)
    .then(res => res.json());
}

export function updateTask(id, updates) {
  const validId = validateId(id);
  const safeUpdates = {};
  if (typeof updates.completed === 'boolean') {
    safeUpdates.completed = updates.completed;
  }
  if (updates.title !== undefined) {
    safeUpdates.title = sanitizeTitle(updates.title);
  }
  return fetch(`${API_URL}/${validId}`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(safeUpdates),
  })
    .then(handleResponse)
    .then(res => res.json());
}

export function deleteTask(id) {
  const validId = validateId(id);
  return fetch(`${API_URL}/${validId}`, {
    method: 'DELETE',
  }).then(handleResponse);
}
