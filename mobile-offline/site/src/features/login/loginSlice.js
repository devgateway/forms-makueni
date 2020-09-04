import {createSlice} from '@reduxjs/toolkit';
import {loginUser} from "../../api/Api";
import {loadReports, saveUser} from "../../app/db";
import {replaceReports} from "../../features/pmc/pmcReportsSlice";

export const loginStateFromUser = user => {
    if (user === undefined) {
        return undefined
    }
    return {
        authenticated: true,
        tokenValid: true,
        user: user
    }
}

export const loginSlice = createSlice({
    name: 'login',
    initialState: {
        authenticated: false,
        tokenValid: false,
        failed: false,
        user: {},
        loading: false
    },
    reducers: {
        loginInvoked: (state, action) => {
            state.loading = true;
        },
        loginFailure: (state, action) => {
            state.authenticated = false;
            state.loading = false;
            state.failed = true;
        },
        loginSuccess: (state, action) => {
            state.loading = false;
            state.user = action.payload;
            state.authenticated = true;
            state.tokenValid = true;
        },
    },
});

export const {loginInvoked, loginFailure, loginSuccess} = loginSlice.actions;

/**
 * Login a user using (username, password) tuple.
 */
export const performLogin = userPass => dispatch => {
    dispatch(loginInvoked(userPass));

    loginUser(userPass)
        .then(resolve => {
            const {data} = resolve;
            dispatch(loginSuccess(data));
            saveUser(data);
            dispatch(replaceReports(loadReports(data.id)))
        })
        .catch(reject => {
            console.log(JSON.stringify(reject));
            dispatch(loginFailure("Network Error! Ensure Internet is up and retry!"));
        });
};

export const selectLogin = state => state.login;

export default loginSlice.reducer;
