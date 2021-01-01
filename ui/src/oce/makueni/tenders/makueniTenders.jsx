import Header from '../../layout/header';
import BootstrapTableWrapper from '../../corruption-risk/archive/bootstrap-table-wrapper';

import '../makueni.scss';
import Project from './single/Project';
import PurchaseReqView from './single/PurchaseReqView';
import FiltersTendersWrapper from '../filters/FiltersTendersWrapper';
import Footer from '../../layout/footer';
import React, { useEffect } from 'react';
import fmConnect from '../../fm/fm';
import FileDownloadLinks from './single/FileDownloadLinks';
import { getTenders } from '../../api/Api';
import PropTypes from 'prop-types';
import { tCreator } from '../../translatable';
import { useImmer } from 'use-immer';
import { setImmer } from '../../tools';
import { Link, Route, Switch } from 'react-router-dom';

const MakueniTenders = (props) => {
  useEffect(() => window.scrollTo(0, 0), []);

  const [filters, updateFilters] = useImmer({});
  const [page, updatePage] = useImmer(1);
  const [pageSize, updatePageSize] = useImmer(20);
  const [dataWithCount, updateDataWithCount] = useImmer({ data: [], count: 0 });

  useEffect(() => {
    getTenders({ ...filters, pageSize, pageNumber: page - 1 }).then((result) => {
      updateDataWithCount((draft) => {
        draft.data = result.data;
        draft.count = result.count;
      });
    });
  }, [filters, page, pageSize]);

  useEffect(() => {
    updatePage(() => 1);
  }, [filters]);

  let introJsCount = 0; // this should not be part of the state

  const filtersWrapper = () => (
    <FiltersTendersWrapper
      filters={filters}
      applyFilters={setImmer(updateFilters)}
      translations={props.translations}
    />
  );

  const t = tCreator(props.translations);

  const showDataStep = () => {
    introJsCount += 1;
    return introJsCount < 6;
  };

  const tenderLink = (tenderTitle, row) => (
    <div
      className="tender-title"
      data-step={showDataStep() ? 9 : ''}
      data-intro={showDataStep() ? t('tables:tenderLink:dataIntro') : ''}
    >
      {
        row.tender !== undefined
          ? (
            <Link to={`/ui/tender/t/${row.tender.purchaseReqId}`} className="more-details-link">
              {tenderTitle && tenderTitle.toUpperCase()}
            </Link>
          ) : t('tables:tenderLink:noTender')
      }
    </div>
  );

  const projectLink = (project) => (
    <div
      data-step={showDataStep() ? 10 : ''}
      data-intro={showDataStep() ? t('tables:projectLink:dataIntro') : ''}
    >
      {
        project !== undefined
          ? (
            <Link to={`/ui/tender/p/${project._id}`} className="more-details-link">
              {project.projectTitle && project.projectTitle.toUpperCase()}
            </Link>
          ) : t('tables:projectLink:noProject')
      }
    </div>
  );

  const downloadFiles = (tender) => <FileDownloadLinks files={(tender || {}).formDocs} />;

  const downloadLinks = (tender) => (tender && tender.tenderLink
    ? (<a className="download-file" href={tender.tenderLink} target="_blank">{tender.tenderLink}</a>)
    : null);

  const linksAndFiles = () => {
    const { isFeatureVisible } = props;
    return (tender) => (
      <div
        data-step={showDataStep() ? 11 : ''}
        data-intro={showDataStep() ? 'Click to download the tender document hardcopy.' : ''}
      >
        {isFeatureVisible('publicView.tendersProcessList.tenderDocs')
      && downloadFiles(tender)}

        {isFeatureVisible('publicView.tendersProcessList.tenderLink')
      && downloadLinks(tender)}
      </div>
    );
  };

  const { isFeatureVisible } = props;

  const columns = [{
    text: t('tables:tenders:col:title'),
    dataField: 'tender.tenderTitle',
    formatter: tenderLink,
    visible: isFeatureVisible('publicView.tendersProcessList.tenderTitle'),
  }, {
    text: t('tables:tenders:col:department'),
    dataField: 'department',
    visible: isFeatureVisible('publicView.tendersProcessList.department'),
  }, {
    text: t('tables:tenders:col:fiscalYear'),
    dataField: 'fiscalYear',
    visible: isFeatureVisible('publicView.tendersProcessList.fiscalYear'),
  }, {
    text: t('tables:tenders:col:closingDate'),
    dataField: 'tender.closingDate',
    formatter: (closingDate) => (closingDate !== undefined ? new Date(closingDate).toLocaleDateString() : ''),
    visible: isFeatureVisible('publicView.tendersProcessList.closingDate'),
  }, {
    text: t('tables:tenders:col:tenderValue'),
    dataField: 'tender.tenderValue',
    formatter: (tenderValue) => (tenderValue !== undefined ? props.styling.charts.hoverFormatter(tenderValue) : ''),
    visible: isFeatureVisible('publicView.tendersProcessList.tenderValue'),
  }, {
    text: t('tables:tenders:col:project'),
    dataField: 'project',
    formatter: projectLink,
    visible: isFeatureVisible('publicView.tendersProcessList.project'),
  }, {
    text: t('tables:tenders:col:documents'),
    dataField: 'tender',
    formatter: linksAndFiles(),
    visible: isFeatureVisible('publicView.tendersProcessList.tenderLink')
        || isFeatureVisible('publicView.tendersProcessList.tenderDocs'),
  }];

  const visibleColumns = columns.filter((c) => c.visible);

  return (
    <div className="container-fluid dashboard-default">

      <Header
        translations={props.translations}
        styling={props.styling}
        selected="tender"
      />

      <div className="makueni-tenders content row">
        <div className="col-md-3 col-sm-12 filters">
          <div className="row" data-intro={t('tables:tenders:filters:dataIntro')} data-step="8">
            <div className="filters-hint col-md-12">
              {t('filters:hint')}
            </div>
            {filtersWrapper()}
          </div>
        </div>

        <div className="col-md-9 col-sm-12 col-main-content">
          <Switch>
            <Route exact path="/ui/tender">
              <div>
                <h1>{t('tables:tenders:title')}</h1>
                <BootstrapTableWrapper
                  data={dataWithCount.data}
                  page={page}
                  pageSize={pageSize}
                  onPageChange={setImmer(updatePage)}
                  onSizePerPageList={setImmer(updatePageSize)}
                  count={dataWithCount.count}
                  columns={visibleColumns}
                />
              </div>
            </Route>
            <Route path="/ui/tender/t/:id">
              <PurchaseReqView
                onSwitch={props.onSwitch}
                translations={props.translations}
                styling={props.styling}
              />
            </Route>
            <Route path="/ui/tender/p/:id">
              <Project
                translations={props.translations}
                styling={props.styling}
              />
            </Route>
          </Switch>
        </div>
      </div>

      <div className="alerts-container">
        <div className="row alerts-button subscribe">
          <div className="col-md-12">
            <button
              className="btn btn-info btn-lg"
              type="submit"
              onClick={() => props.onSwitch('alerts')}
            >
              {t('general:subscribeToEmailAlerts')}
            </button>
          </div>
        </div>
      </div>
      <div className="smshelp-container">
        <div className="row alerts-button subscribe">
          <div className="col-md-12">
            <button
              className="btn btn-info btn-lg"
              type="submit"
              onClick={() => props.onSwitch('smshelp')}
            >
              {t('general:smsFeedbackHelp')}
            </button>
          </div>
        </div>
      </div>
      <Footer translations={props.translations} />
    </div>
  );
};

MakueniTenders.propTypes = {
  styling: PropTypes.object.isRequired,
};

export default fmConnect(MakueniTenders);
