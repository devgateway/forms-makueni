import {createSlice} from "@reduxjs/toolkit";
import { defaultMemoize } from 'reselect';

export const metadataSlice = createSlice({
    name: 'metadata',
    initialState: {
        ref: {},
        refById: {}
    },
    reducers: {
        loadSuccess: (state, action) => {
            state.ref = action.payload
            state.refById = getRefById(action.payload)
        }
    }
});

export const {loadSuccess} = metadataSlice.actions;

export const selectMetadata = state => state.metadata;

export default metadataSlice.reducer;

export const metadataStateFrom = metadata => {
    if (!metadata) {
        return undefined
    }
    return {
        ref: metadata,
        refById: getRefById(metadata)
    }
}

const getRefById = ref => {
    let refById = {}
    for (const [key, value] of Object.entries(ref)) {
        refById[key] = value.reduce((byId, entity) => {
            byId[entity.id] = entity
            return byId
        }, {})
    }
    return refById
}

export const selectTendersById = defaultMemoize((metadata) => metadata.refById["Tender"] || {});
