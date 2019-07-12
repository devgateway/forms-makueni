import { API_ROOT, OCE } from '../../state/oce-state';
import { Map } from 'immutable';

// makeuni tenders state
export const mtState = OCE.substate({ name: 'makueniTenders' });

// makeuni tenders filters
export const mtFilters = mtState.input({
  name: 'mtFilters',
  initial: Map(),
});

export const page = mtState.input({
  name: 'page',
  initial: 1,
});

export const pageSize = mtState.input({
  name: 'pageSize',
  initial: 20,
});

const filters = mtState.mapping({
  name: 'filters',
  deps: [mtFilters, page, pageSize],
  mapper: (mtFilters, page, pageSize) => {
    return mtFilters
    .set('pageSize', pageSize)
    .set('pageNumber', page === 0 ? page : page - 1);
  }
});


const mtEP = mtState.input({
  name: 'makueniTendersEP',
  initial: `${API_ROOT}/makueni/tenders`,
});

const tendersRemote = mtState.remote({
  name: 'tendersRemote',
  url: mtEP,
  params: filters,
});

export const tendersData = mtState.mapping({
  name: 'tendersData',
  deps: [tendersRemote],
  mapper: raw =>
    raw.map(datum => {
      return {
        id: datum.id,
        department: datum.department.label,
        fiscalYear: datum.fiscalYear.label,
      };
    })
});

const mtCountEP = mtState.input({
  name: 'makueniTendersCountEP',
  initial: `${API_ROOT}/makueni/tendersCount`,
});

export const tendersCountRemote = mtState.remote({
  name: 'tendersCountRemote',
  url: mtCountEP,
  params: filters,
});
