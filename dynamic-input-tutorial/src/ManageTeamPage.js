import React, { useState } from 'react';
import TeamForm from './TeamForm';


const ManageTeamPage = ({
    ...props
}) => {
    
    const [users, setUsers] = useState([...props.usernameList]); // this will get populated with useEffect
    const [team, setTeam] = useState({...props.team});

    const handleChange = (event) => {
        const { name, value } = event.target;
        console.log('name, value', name, value)
        
        // console.log('name', name, 'value', value);
        // if it is selected, automatically add to the team and create a new selection input
        // this can combine because the how the state is design in the component
        // name and value representing the property of the state
        if (name === 'admins' || name === 'agents') {
            const newUserObj = users.find(user => user.id === Number(value));
            console.log('what is newUserObj', newUserObj);
            console.log(name);
            setTeam(prevTeam => ({
                ...prevTeam,
                [name]: prevTeam[name].concat(newUserObj),
            }))
            // console.log('here')
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

        // if(team.id) {
        //     updateTeam(team.id, team)
        //     .then(() => history.push('/teams'))
        //     .catch(err => alert(err));
        // }else {
        //     createNewTeam(team)
        //     .then(() => history.push('/teams'))
        //     .catch(err => alert(err));
        // }
        console.log(team);
    }

    console.log(team, users);

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
    console.log('here', admins, agents);
    return usersList.filter(user => {
        console.log(' here in getuserNotInTeam', user);
        return !(admins.find(u => u.id === user.id) ||
            agents.find(u => u.id === user.id));
    });
}


export default ManageTeamPage;