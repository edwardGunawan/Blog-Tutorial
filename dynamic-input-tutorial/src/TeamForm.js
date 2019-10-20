import React from 'react';
import uuid from 'uuid';

const TeamForm = ({
    handleSubmit,
    handleChange,
    handleDeleteClick,
    team,
    usernameList,
}) => {
    return (
        <>
            <label htmlFor="admins">Team Name</label>
            <input type="text" value={team.name} onChange={handleChange} name={'name'} placeholder="Team Name" />
            <div>
                <label htmlFor="admins">Admins</label>
                {team.admins && team.admins.map(admin => {
                    return (<div key={admin.id}>
                        <span> {admin.username} </span>
                        <button onClick={handleDeleteClick('admins', admin.id)}> - </button>
                    </div>)
                })}
                <select name={"admins"} value={''} onChange={handleChange}>
                    {usernameList.map(({id, username}) =>
                        <option key={uuid.v4()} value={id}>{username}</option>
                    )}
                </select>
            </div>
            
            <div>
                <label htmlFor="agents">Agents</label>
                {team.agents && team.agents.map(agent => {
                    return (<div key={agent.id}>
                        <span> {agent.username} </span>
                        <button onClick={handleDeleteClick('agents', agent.id)} > - </button>
                    </div>)
                })}
                <select name={"agents"} value={''} onChange={handleChange}>
                    {usernameList.map(({id,username}) =>
                        <option key={uuid.v4()} value={id}>{username}</option>
                    )}
                </select>
            </div>
            <button onClick={handleSubmit}>Submit</button>

        </>
    )
}

export default TeamForm;