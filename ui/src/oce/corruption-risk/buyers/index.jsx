import React from 'react';
import { List } from 'immutable';
import CRDPage from '../page';
import PaginatedTable from '../paginated-table';
import Archive from '../archive';
import { wireProps } from '../tools';
import { getBuyersTenderAndAwardCounts } from './api';
import BootstrapTableWrapper from '../archive/bootstrap-table-wrapper';

const mkLink = (navigate) => (content, { id }) => (
  <a href={`#!/crd/buyer/${id}`} onClick={() => navigate('buyer', id)}>{content}</a>
);

class BuyerList extends PaginatedTable {
  constructor(...args) {
    super(...args);
    this.state = this.state || {};
    this.state.tenders = {};
    this.state.awards = {};
  }

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

    const { data } = this.props;
    if (prevProps.data !== data) {
      this.fetchCounts();
    }
  }

  fetchCounts() {
    const {
      filters, years, months, data,
    } = this.props;

    const buyerIds = data
      .get('data', List())
      .map((datum) => datum.get('buyerId'))
      .toArray();

    const buyerFilters = {
      ...filters,
      year: years,
      month: months,
      buyerId: buyerIds,
    };

    if (buyerFilters.buyerId.length > 0) {
      getBuyersTenderAndAwardCounts(buyerFilters)
        .then(
          ([tenders, awards]) => this.setState({ tenders, awards }),
          () => this.setState({ tenders: {}, awards: {} }),
        );
    }
  }

  render() {
    const { data, navigate } = this.props;

    const count = data.get('count', 0);

    const {
      pageSize, page, tenders, awards,
    } = this.state;

    const jsData = data.get('data', List()).map((supplier) => {
      const id = supplier.get('buyerId');
      return {
        id,
        name: supplier.get('buyerName'),
        nrTenders: tenders[id],
        nrAwards: awards[id],
        nrFlags: supplier.get('countFlags'),
      };
    }).toJS();

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
            text: this.t('crd:suppliers:ID'),
            dataField: 'id',
            fm: 'crd.buyers.col.id',
            formatter: mkLink(navigate),
          },
          {
            text: this.t('crd:suppliers:name'),
            dataField: 'name',
            fm: 'crd.buyers.col.name',
            formatter: mkLink(navigate),
          },
          {
            text: this.t('crd:procuringEntities:noOfTenders'),
            dataField: 'nrTenders',
            fm: 'crd.buyers.col.nrTenders',
          },
          {
            text: this.t('crd:procuringEntities:noOfAwards'),
            dataField: 'nrAwards',
            fm: 'crd.buyers.col.nrAwards',
          },
          {
            text: this.t('crd:procurementsTable:noOfFlags'),
            dataField: 'nrFlags',
            fm: 'crd.buyers.col.nrFlags',
          },
        ]}
      />
    );
  }
}

class Buyers extends CRDPage {
  requestNewData(path, newData) {
    this.props.requestNewData(
      path,
      newData.set('count', newData.getIn(['count', 0, 'count'], 0)),
    );
  }

  render() {
    const { navigate, searchQuery, doSearch } = this.props;
    return (
      <Archive
        {...wireProps(this)}
        requestNewData={this.requestNewData.bind(this)}
        searchQuery={searchQuery}
        doSearch={doSearch}
        navigate={navigate}
        className="procuring-entities-page"
        topSearchPlaceholder={this.t('crd:buyers:top-search')}
        List={BuyerList}
        dataEP="buyersByFlags"
        countEP="buyersByFlags/count"
      />
    );
  }
}

export default Buyers;
