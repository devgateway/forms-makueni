import cn from 'classnames';
import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import PropTypes from 'prop-types';
import { useImmer } from 'use-immer';
import Tender from './Tender';
import PurchaseReq from './PurchaseReq';
import TenderQuotation from './TenderQuotation';
import ProfessionalOpinion from './ProfessionalOpinion';
import Notification from './Notification';
import Award from './Award';
import Contract from './Contract';
import FeedbackMessageList from '../../../feedback/feedbackList';
import AdministratorReport from './AdministratorReport';
import fmConnect from '../../../fm/fm';
import { tCreator } from '../../../translatable';
import { getPurchaseRequisition } from '../../../api/Api';
import InspectionReport from './InspectionReport';
import PMCReport from './PMCReport';
import MEReport from './MEReport';
import PaymentVoucher from './PaymentVoucher';
import { setImmer } from '../../../tools';

const PurchaseReqView = (props) => {
  const [selected, setSelected] = useState(1);
  const [visibleTabs, setVisibleTabs] = useState([]);

  const [data, updateData] = useImmer(undefined);
  const { id } = useParams();
  const { navigate, isFeatureVisible } = props;
  const t = tCreator(props.translations);

  const maybeTrimOcidPrefix = (id) => {
    if (id.includes('ocds-muq5cl-')) {
      return id.replace('ocds-muq5cl-', '');
    }
    return id;
  };

  const tabs = [
    {
      name: t('purchaseReq:tabs:tender'),
      tab: 1,
      fm: 'publicView.tender',
    }, {
      name: t('purchaseReq:tabs:purchaseReqs'),
      tab: 2,
      fm: 'publicView.tenderProcess',
    }, {
      name: t('purchaseReq:tabs:tenderEvaluation'),
      tab: 3,
      fm: 'publicView.tenderQuotationEvaluation',
    }, {
      name: t('purchaseReq:tabs:professionalOpinion'),
      tab: 4,
      fm: 'publicView.professionalOpinions',
    }, {
      name: t('purchaseReq:tabs:notification'),
      tab: 5,
      fm: 'publicView.awardNotification',
    }, {
      name: t('purchaseReq:tabs:award'),
      tab: 6,
      fm: 'publicView.awardAcceptance',
    }, {
      name: t('purchaseReq:tabs:contract'),
      tab: 7,
      fm: 'publicView.contract',
    }, {
      name: t('purchaseReq:tabs:adminReports'),
      tab: 8,
      fm: 'publicView.administratorReport',
    }, {
      name: t('purchaseReq:tabs:inspectionReports'),
      tab: 9,
      fm: 'publicView.inspectionReport',
    }, {
      name: t('purchaseReq:tabs:pmcReports'),
      tab: 10,
      fm: 'publicView.pmcReport',
    }, {
      name: t('purchaseReq:tabs:meReports'),
      tab: 11,
      fm: 'publicView.meReport',
    }, {
      name: t('purchaseReq:tabs:paymentVouchers'),
      tab: 12,
      fm: 'publicView.paymentVoucher',
    },
  ];

  const changeTab = (option) => {
    setSelected(option);
  };

  useEffect(() => {
    getPurchaseRequisition({ id: maybeTrimOcidPrefix(id) }).then(setImmer(updateData));
    setVisibleTabs(tabs.filter((tab) => isFeatureVisible(tab.fm)));
    if (visibleTabs.length > 0 && selected !== visibleTabs[0].tab) {
      changeTab(visibleTabs[0].tab);
    }
  }, [id, isFeatureVisible]);

  const isActive = (option) => {
    if (selected === undefined || selected === '') {
      return false;
    }
    return selected === option;
  };

  const displayTab = () => {
    const { tenderTitle } = data.tenderProcesses.tender[0];

    switch (selected) {
      case 1:
        return (
          <Tender
            data={data.tenderProcesses.tender}
            prId={data._id}
            department={data.department}
            fiscalYear={data.fiscalYear}
            styling={props.styling}
            translations={props.translations}
          />
        );

      case 2:
        return (
          <PurchaseReq
            data={data.tenderProcesses}
            department={data.department}
            fiscalYear={data.fiscalYear}
            styling={props.styling}
            translations={props.translations}
          />
        );

      case 3:
        return (
          <TenderQuotation
            data={data.tenderProcesses.tenderQuotationEvaluation}
            tenderTitle={tenderTitle}
            department={data.department}
            fiscalYear={data.fiscalYear}
            styling={props.styling}
            translations={props.translations}
          />
        );

      case 4:
        return (
          <ProfessionalOpinion
            data={data.tenderProcesses.professionalOpinion}
            tenderTitle={tenderTitle}
            department={data.department}
            fiscalYear={data.fiscalYear}
            styling={props.styling}
            translations={props.translations}
          />
        );

      case 5:
        return (
          <Notification
            data={data.tenderProcesses.awardNotification}
            tenderTitle={tenderTitle}
            department={data.department}
            fiscalYear={data.fiscalYear}
            styling={props.styling}
            translations={props.translations}
          />
        );

      case 6:
        return (
          <Award
            data={data.tenderProcesses.awardAcceptance}
            department={data.department}
            tenderTitle={tenderTitle}
            fiscalYear={data.fiscalYear}
            styling={props.styling}
            translations={props.translations}
          />
        );

      case 7:
        return (
          <Contract
            data={data.tenderProcesses.contract}
            department={data.department}
            tenderTitle={tenderTitle}
            fiscalYear={data.fiscalYear}
            styling={props.styling}
            translations={props.translations}
          />
        );

      case 8:
        return (
          <AdministratorReport
            data={data.tenderProcesses.administratorReports}
            department={data.department}
            tenderTitle={tenderTitle}
            fiscalYear={data.fiscalYear}
            styling={props.styling}
            translations={props.translations}
          />
        );

      case 9:
        return (
          <InspectionReport
            data={data.tenderProcesses.inspectionReports}
            department={data.department}
            tenderTitle={tenderTitle}
            fiscalYear={data.fiscalYear}
            styling={props.styling}
            translations={props.translations}
          />
        );

      case 10:
        return (
          <PMCReport
            data={data.tenderProcesses.pmcReports}
            department={data.department}
            tenderTitle={tenderTitle}
            fiscalYear={data.fiscalYear}
            styling={props.styling}
            translations={props.translations}
          />
        );

      case 11:
        return (
          <MEReport
            data={data.tenderProcesses.meReports}
            department={data.department}
            tenderTitle={tenderTitle}
            fiscalYear={data.fiscalYear}
            styling={props.styling}
            translations={props.translations}
          />
        );

      case 12:
        return (
          <PaymentVoucher
            data={data.tenderProcesses.paymentVouchers}
            department={data.department}
            tenderTitle={tenderTitle}
            fiscalYear={data.fiscalYear}
            styling={props.styling}
            translations={props.translations}
          />
        );

      default:
        throw new Error('Tab not implemented');
    }
  };

  return (data !== undefined && (
  <div className="tender makueni-form">
    <div className="row">
      <div className="col-md-12 navigation-view" data-step="9" data-intro={t('purchaseReq:nav:dataIntro')}>
        {
          visibleTabs.map((tab) => (
            <a
              key={tab.tab}
              className={cn('', { active: isActive(tab.tab) })}
              onClick={() => changeTab(tab.tab)}
            >
              {tab.name}
            </a>
          ))
        }
      </div>
    </div>

    <div className="row">
      <a href="#!/tender" onClick={() => navigate()} className="back-link col-md-3">
        <span className="back-icon">
          <span className="previous">&#8249;</span>
        </span>
        <span>
          {t('general:goBack')}
        </span>
      </a>

      <div className="col-md-offset-5 col-md-4">
        <button
          className="btn btn-subscribe pull-right"
          type="submit"
          data-step="11"
          data-intro={t('purchaseReq:receiveAlerts:dataIntro')}
          onClick={() => props.onSwitch('alerts', data._id, data.tenderProcesses.tender[0].tenderTitle)}
        >
          {t('purchaseReq:receiveAlerts:caption')}
        </button>
      </div>
    </div>
    {
      displayTab()
    }

    <div className="row">
      <p />
      <FeedbackMessageList department={data.department} />
    </div>
  </div>
  ));
};

PurchaseReqView.propTypes = {
  navigate: PropTypes.func.isRequired,
  isFeatureVisible: PropTypes.func.isRequired,
  styling: PropTypes.object.isRequired,
  translations: PropTypes.object.isRequired,
  onSwitch: PropTypes.func.isRequired,
};

export default fmConnect(PurchaseReqView);
