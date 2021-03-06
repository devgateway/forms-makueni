import React from 'react';
import URI from 'urijs';
import { List, Map } from 'immutable';
import PropTypes from 'prop-types';
import { Link } from 'react-router-dom';
import CRDPage from '../page';
import PaginatedTable from '../paginated-table';
import Archive from '../archive';
import { wireProps } from '../tools';
import { fetchEP, pluckImm, cacheFn } from '../../tools';
import BackendDateFilterable from '../backend-date-filterable';
import BootstrapTableWrapper from '../archive/bootstrap-table-wrapper';

export const mkLink = (content, { id }) => (
  <Link
    to={`/portal/crd/supplier/${id}`}
  >
    {content}
  </Link>
);

class SList extends PaginatedTable {
  getCustomEP() {
    const { searchQuery } = this.props;
    const eps = super.getCustomEP();
    return searchQuery
      ? eps.map((ep) => ep.addSearch('text', decodeURIComponent(searchQuery)))
      : eps;
  }

  componentDidUpdate(prevProps, prevState) {
    const propsChanged = ['filters', 'searchQuery'].some((key) => this.props[key] !== prevProps[key]);
    if (propsChanged) {
      this.fetch();
    } else {
      super.componentDidUpdate(prevProps, prevState);
    }
  }

  render() {
    const { data, t } = this.props;

    const count = data.get('count', 0);

    const { pageSize, page } = this.state;

    const jsData = data.get('data', List()).map((supplier) => ({
      id: supplier.get('supplierId'),
      name: supplier.get('supplierName'),
      wins: supplier.get('wins'),
      winAmount: supplier.get('winAmount'),
      losses: supplier.get('losses'),
      flags: supplier.get('countFlags'),
    })).toJS();

    return (
      <BootstrapTableWrapper
        data={jsData}
        count={count}
        page={page}
        onPageChange={(newPage) => this.setState({ page: newPage })}
        pageSize={pageSize}
        onSizePerPageList={(newPageSize) => this.setState({ pageSize: newPageSize })}
        columns={[
          {
            text: t('crd:suppliers:name'),
            dataField: 'name',
            fm: 'crd.suppliers.col.name',
            formatter: mkLink,
          },
          {
            text: t('crd:suppliers:ID'),
            dataField: 'id',
            fm: 'crd.suppliers.col.id',
            formatter: mkLink,
          },
          {
            text: t('crd:suppliers:wins'),
            dataField: 'wins',
            fm: 'crd.suppliers.col.wins',
          },
          {
            text: t('crd:suppliers:losses'),
            dataField: 'losses',
            fm: 'crd.suppliers.col.losses',
          },
          {
            text: t('crd:suppliers:totalWon'),
            dataField: 'winAmount',
            fm: 'crd.suppliers.col.totalWon',
          },
          {
            text: t('crd:suppliers:nrFlags'),
            dataField: 'flags',
            fm: 'crd.suppliers.col.nrFlags',
          },
        ]}
      />
    );
  }
}

SList.propTypes = {
  t: PropTypes.func.isRequired,
};

class Suppliers extends CRDPage {
  constructor(...args) {
    super(...args);
    this.state = this.state || {};
    this.state.winLossFlagInfo = Map();
    this.injectWinLossData = cacheFn((data, winLossFlagInfo) => data.update('data', List(), (list) => list.map((supplier) => {
      const id = supplier.get('supplierId');
      if (!winLossFlagInfo.has(id)) return supplier;
      const info = winLossFlagInfo.get(id);
      return supplier
        .set('wins', info.won.count)
        .set('winAmount', info.won.totalAmount)
        .set('losses', info.lostCount)
        .set('flags', info.applied.countFlags);
    })));
  }

  onNewDataRequested(path, newData) {
    const supplierIds = newData.get('data').map(pluckImm('supplierId'));
    this.setState({ winLossFlagInfo: Map() });
    if (supplierIds.toJS().length !== 0) {
      fetchEP(new URI('/api/procurementsWonLost').addSearch({
        bidderId: supplierIds.toJS(),
      }))
        .then((result) => {
          this.setState({
            winLossFlagInfo: Map(result.map((datum) => [
              datum.applied._id,
              datum,
            ])),
          });
        });
    }
    this.props.requestNewData(
      path,
      newData.set('count', newData.getIn(['count', 0, 'count'], 0)),
    );
  }

  render() {
    const {
      searchQuery, doSearch, data, t,
    } = this.props;
    const { winLossFlagInfo } = this.state;
    return (
      <BackendDateFilterable
        {...wireProps(this)}
        data={this.injectWinLossData(data, winLossFlagInfo)}
        requestNewData={this.onNewDataRequested.bind(this)}
      >
        <Archive
          searchQuery={searchQuery}
          doSearch={doSearch}
          className="suppliers-page"
          topSearchPlaceholder={t('crd:suppliers:top-search')}
          List={SList}
          dataEP="suppliersByFlags"
          countEP="suppliersByFlags/count"
        />
      </BackendDateFilterable>
    );
  }
}

Suppliers.propTypes = {
  t: PropTypes.func.isRequired,
};

export default Suppliers;
