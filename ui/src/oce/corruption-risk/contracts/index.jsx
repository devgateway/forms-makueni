import React from 'react';
import { List } from 'immutable';
import PropTypes from 'prop-types';
import CRDPage from '../page';
import {
  getAwardAmount, mkContractLink, wireProps, _3LineText,
} from '../tools';
import PaginatedTable from '../paginated-table';
import Archive from '../archive';
import BackendDateFilterable from '../backend-date-filterable';
import BootstrapTableWrapper from '../archive/bootstrap-table-wrapper';

class CList extends PaginatedTable {
  getCustomEP() {
    const { searchQuery } = this.props;
    const eps = super.getCustomEP();
    return searchQuery
      ? eps.map((ep) => ep.addSearch('text', searchQuery))
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

    if (!data) return null;

    const contracts = data.get('data', List());
    const count = data.get('count', 0);

    const { pageSize, page } = this.state;

    const jsData = contracts.map((contract) => {
      const tenderAmount = `${contract.getIn(['tender', 'value', 'amount'], 'N/A')
      } ${
        contract.getIn(['tender', 'value', 'currency'], '')}`;

      const startDate = contract.getIn(['tender', 'tenderPeriod', 'startDate']);

      const ocid = contract.get('ocid');

      const flags = contract.get('flags');

      const flagTypes = flags.get('laggedStats', List())
        .map((flagType) => t(`crd:corruptionType:${flagType.get('type')}:name`))
        .join(', ') || 'N/A';

      return {
        status: contract.getIn(['tender', 'status'], 'N/A'),
        id: ocid,
        ocid,
        title: contract.getIn(['tender', 'title'], 'N/A'),
        PEName: contract.getIn(['tender', 'procuringEntity', 'name'], 'N/A'),
        tenderAmount,
        awardAmount: getAwardAmount(contract),
        startDate: startDate ? new Date(startDate).toLocaleDateString() : 'N/A',
        flagTypes,
        nrFlags: flags.get('totalFlagged'),
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
            text: t('crd:contracts:baseInfo:status'),
            dataField: 'status',
            fm: 'crd.contracts.col.status',
          },
          {
            text: t('crd:procurementsTable:contractID'),
            dataField: 'ocid',
            fm: 'crd.contracts.col.contractId',
            formatter: mkContractLink,
            classes: 'ocid',
            headerClasses: 'ocid',
          },
          {
            text: t('crd:general:contract:title'),
            dataField: 'title',
            fm: 'crd.contracts.col.contractTitle',
            formatter: mkContractLink,
          },
          {
            text: t('crd:contracts:list:procuringEntity'),
            dataField: 'PEName',
            fm: 'crd.contracts.col.procuringEntity',
            formatter: _3LineText,
          },
          {
            text: t('crd:procurementsTable:tenderAmount'),
            dataField: 'tenderAmount',
            fm: 'crd.contracts.col.tenderAmount',
          },
          {
            text: t('crd:contracts:list:awardAmount'),
            dataField: 'awardAmount',
            fm: 'crd.contracts.col.awardAmount',
          },
          {
            text: t('crd:procurementsTable:tenderDate'),
            dataField: 'startDate',
            fm: 'crd.contracts.col.tenderDate',
            classes: 'date',
            headerClasses: 'date',
          },
          {
            text: t('crd:procurementsTable:flagType'),
            dataField: 'flagTypes',
            fm: 'crd.contracts.col.flagType',
          },
          {
            text: t('crd:procurementsTable:noOfFlags'),
            dataField: 'nrFlags',
            fm: 'crd.contracts.col.nrFlags',
          },
        ]}
      />
    );
  }
}

CList.propTypes = {
  t: PropTypes.func.isRequired,
};

export default class Contracts extends CRDPage {
  render() {
    const {
      searchQuery, doSearch, t,
    } = this.props;
    return (
      <BackendDateFilterable
        {...wireProps(this)}
      >
        <Archive
          searchQuery={searchQuery}
          doSearch={doSearch}
          className="contracts-page"
          topSearchPlaceholder={t('crd:contracts:top-search')}
          List={CList}
          dataEP="flaggedRelease/all"
          countEP="flaggedRelease/count"
        />
      </BackendDateFilterable>
    );
  }
}
