import React, { useEffect } from 'react';
import PropTypes from 'prop-types';
import { useImmer } from 'use-immer';
import { useParams } from 'react-router-dom';
import { Item } from './Item';
import fmConnect from '../../../fm/fm';
import FileDownloadLinks from './FileDownloadLinks';
import { getProject } from '../../../api/Api';
import { tCreator } from '../../../translatable';
import { setImmer } from '../../../tools';

const Project = (props) => {
  const [data, updateData] = useImmer(undefined);
  const { navigate, isFeatureVisible } = props;
  const { currencyFormatter, formatDate } = props.styling.tables;
  const t = tCreator(props.translations);
  const { id } = useParams();

  useEffect(() => {
    getProject({ id }).then(setImmer(updateData));
  }, []);

  // eslint-disable-next-line no-unused-vars
  const getFeedbackSubject = () => {
    let metadata;
    if (data !== undefined) {
      metadata = ` - ${data.projects.projectTitle
      } - ${data.department.label
      } - ${data.fiscalYear.name}`;
    }
    return escape(t('project:feedbackSubject') + metadata);
  };

  return (
    <div className="project makueni-form">
      <div className="row">
        <a href="#!/tender" onClick={() => navigate()} className="back-link col-md-3">
          <span className="back-icon">
            <span className="previous">&#8249;</span>
          </span>
          <span className="back-text">
            {t('general:goBack')}
          </span>
        </a>
      </div>

      <div className="row padding-top-10">
        <div className="col-md-12">
          <h1 className="page-title">{t('project:pageTitle')}</h1>
        </div>
      </div>

      {
      data !== undefined
        ? (
          <div>
            <div className="row display-flex">
              {isFeatureVisible('publicView.project.projectTitle')
              && <Item label={t('project:title')} value={data.projects.projectTitle} col={6} />}

              {isFeatureVisible('publicView.project.cabinetPapers')
              && (
              <Item label={t('project:title')} col={6}>
                <FileDownloadLinks files={data.projects.cabinetPapers.flatMap((cp) => cp.formDocs)} useDash />
              </Item>
              )}

              {isFeatureVisible('publicView.project.amountBudgeted')
              && <Item label={t('project:amountBudgeted')} value={currencyFormatter(data.projects.amountBudgeted)} col={6} />}

              {isFeatureVisible('publicView.project.amountRequested')
              && <Item label={t('project:amountRequested')} value={currencyFormatter(data.projects.amountRequested)} col={6} />}

              {isFeatureVisible('publicView.project.subcounties')
              && (
              <Item
                label={t('project:subcounties')}
                value={data.projects.subcounties.map((item) => item.label).join(', ')}
                col={6}
              />
              )}

              {isFeatureVisible('publicView.project.wards')
              && (
              <Item
                label={t('project:wards')}
                value={data.projects.wards && data.projects.wards.map((item) => item.label).join(', ')}
                col={6}
              />
              )}

              {isFeatureVisible('publicView.project.approvedDate')
              && <Item label={t('project:approvedDate')} value={formatDate(data.projects.approvedDate)} col={6} />}
            </div>
          </div>
        )
        : null
    }
    </div>
  );
};

Project.propTypes = {
  navigate: PropTypes.func.isRequired,
  isFeatureVisible: PropTypes.func.isRequired,
  styling: PropTypes.object.isRequired,
  translations: PropTypes.object.isRequired,
};

export default fmConnect(Project);
