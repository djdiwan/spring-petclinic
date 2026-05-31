const API_URL = '/tasks';

export function fetchTasks() {
  return fetch(API_URL).then(res => res.json());
}

export function addTask(task) {
  return fetch(API_URL, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(task),
  }).then(res => res.json());
}

export function updateTask(id, updates) {
  return fetch(`${API_URL}/${id}`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(updates),
  }).then(res => res.json());
}

export function deleteTask(id) {
  return fetch(`${API_URL}/${id}`, {
    method: 'DELETE',
  });
}
