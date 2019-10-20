import React, { useState } from 'react';
import TeamForm from './TeamForm';


const ManageTeamPage = ({
    ...props
}) => {
    
    const [users, setUsers] = useState([...props.usernameList]); // this will get populated with useEffect
    const [team, setTeam] = useState({...props.team});

    const handleChange = (event) => {
        const { name, value } = event.target;
        
        // if it is selected, automatically add to the team and create a new selection input
        // this can combine because the how the state is design in the component
        // name and value representing the property of the state
        if (name === 'admins' || name === 'agents') {
            const newUserObj = users.find(user => user.id === Number(value));
            setTeam(prevTeam => ({
                ...prevTeam,
                [name]: prevTeam[name].concat(newUserObj),
            }))
        }

        // changing team name
        else if (name === 'name') {
            setTeam(prevTeam => ({
                ...prevTeam,
                [name]: value,
            }));
        }
    }

    const handleDeleteClick = (authority, id) => (event) => {
        setTeam(prevTeam => ({
            ...prevTeam,
            [authority]: prevTeam[authority].filter(user => user.id !== id),
        }));
    }
    const handleSubmit = (event) => {
        event.preventDefault();
        console.log(team);
    }

    // you don't need to re-add anything field because this function will automatically filtered it out
    // filter out the users that is not in team or that is not selected 
    const usernameList = getUsersNotInTeam(users, team);
    return (
        <>
            <TeamForm team={team}
                usernameList={usernameList}
                handleSubmit={handleSubmit}
                handleChange={handleChange}
                handleDeleteClick={handleDeleteClick}
            />
        </>
    )
}

export const getUsersNotInTeam = (usersList, team) => {
    const { admins = [], agents = [] } = team;
    return usersList.filter(user => {
        return !(admins.find(u => u.id === user.id) ||
            agents.find(u => u.id === user.id));
    });
}


export default ManageTeamPage;