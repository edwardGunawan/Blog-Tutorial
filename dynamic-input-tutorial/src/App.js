import React from 'react';
import ManageTeamPage from './ManageTeamPage';
import './App.css';

function App() {
  const teamObj = {
    name: '',
    admins: [],
    agents: [],
  }

  const usernameList = [
    { id: 0, username: 'Options' },
    { id: 1, username: 'Ed' },
    { id: 2, username: 'Bob' },
    { id: 3, username: 'Joe' },
    { id: 4, username: 'Harrison' },
    { id: 5, username: 'Steve' },
    { id: 6, username: 'John Doe'},
  ]
  return (
    <div className="App">
      <h2>Dynamic Input Tutorial</h2>
      <ManageTeamPage team={teamObj} usernameList={usernameList}/>
    </div>
  );
}

export default App;
