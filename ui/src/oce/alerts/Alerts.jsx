import React, { useEffect, useState } from 'react';
import Header from '../layout/header';
import {
  Alert, FormControl, FormGroup, ControlLabel, HelpBlock,
} from 'react-bootstrap';
import { Typeahead } from 'react-bootstrap-typeahead';
import './alerts.scss';
import PropTypes from 'prop-types';
import { useImmer } from 'use-immer';
import { useTranslation } from 'react-i18next';
import { useParams } from 'react-router-dom';
import { setImmer } from '../tools';
import { fetch, subscribeToAlerts } from '../api/Api';

const emailPattern = /^([\w.%+-]+)@([\w-]+\.)+([\w]{2,})$/i;

const Alerts = (props) => {
  const [fetchedDepartments, updateFetchedDepartments] = useImmer([]);
  const [fetchedItems, updateFetchedItems] = useImmer([]);
  const [serverResponse, setServerResponse] = useState();
  const [showFormErrors, setShowFormErrors] = useState(false);
  const [formData, setFormData] = useImmer({
    email: '',
    departments: [],
    items: [],
  });
  const { departments, items, email } = formData;

  const { purchaseReqId } = useParams();

  useEffect(() => {
    if (!purchaseReqId) {
      fetch('/makueni/filters/departments/all')
        .then(setImmer(updateFetchedDepartments));

      fetch('/makueni/filters/items/all')
        .then(setImmer(updateFetchedItems));
    }

    window.scrollTo(0, 0);
  }, [purchaseReqId]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((draft) => {
      draft[name] = value;
    });
    setShowFormErrors(false);
  };

  const emailValid = email.match(emailPattern);

  const getEmailValidationState = () => {
    if (!email) {
      return null;
    } if (emailValid) {
      return 'success';
    }
    return 'error';
  };

  const formValid = (purchaseReqId || departments.length > 0 || items.length > 0)
    && emailValid;

  const submit = () => {
    setShowFormErrors(true);

    if (formValid) {
      setServerResponse(undefined);

      subscribeToAlerts({
        email,
        departments: departments.map((item) => item.id),
        items: items.map((item) => item.id),
        purchaseReqId,
      }).then((data) => setServerResponse(data));
    }
  };

  const { t } = useTranslation();

  return (
    <div className="container-fluid dashboard-default">

      <Header
        styling={props.styling}
        selected=""
      />

      <div className="alerts content row">
        <div className="col-md-10 col-md-offset-1">
          <div className="row title">
            <div className="col-md-12">
              <h2>{t('alerts:title')}</h2>
            </div>
          </div>

          <div className="row">
            <div className="col-md-12">
              <h4 className="sub-title">{t('alerts:subtitle')}</h4>
            </div>

            <div className="col-md-6">
              <FormGroup validationState={getEmailValidationState()} bsSize="large">
                <ControlLabel>{t('alerts:email')}</ControlLabel>
                <FormControl
                  type="email"
                  name="email"
                  value={email}
                  placeholder="email address"
                  onChange={handleChange}
                />
                <FormControl.Feedback />
                {
                  email && !emailValid && <HelpBlock>{t('alerts:invalidEmail')}</HelpBlock>
                }
              </FormGroup>
            </div>
          </div>

          {
            purchaseReqId !== undefined
              ? (
                <div>
                  <div className="row">
                    <div className="col-md-12">
                      <h4 className="sub-title">{t('alerts:alertPreferences')}</h4>
                    </div>
                  </div>

                  <div className="row">
                    <div className="col-md-12">
                      {t('alerts:alertsForTender')}
                      {' '}
                      <b>{purchaseReqId}</b>
                    </div>
                  </div>
                </div>
              )

              : (
                <div>
                  <h4 className="sub-title">{t('alerts:alertPreferences')}</h4>
                  <h5 className="sub-title">{t('alerts:onNewTender')}</h5>
                  <div className="row">
                    <div className="col-md-6">
                      <ControlLabel>{t('alerts:alertsForTendersFromDepartment')}</ControlLabel>
                      <Typeahead
                        id="departments"
                        onChange={(selected) => handleChange({
                          target: {
                            name: 'departments',
                            value: selected,
                          },
                        })}
                        options={fetchedDepartments === undefined ? [] : fetchedDepartments}
                        clearButton
                        placeholder={t('alerts:departmentsPlaceholder')}
                        selected={departments}
                        multiple
                        isLoading={fetchedDepartments === undefined}
                        bsSize="large"
                        highlightOnlyResult
                      />
                    </div>
                    <div className="col-md-6">
                      <ControlLabel>{t('alerts:alertsForItemsFromDepartment')}</ControlLabel>
                      <Typeahead
                        id="items"
                        onChange={(selected) => handleChange({
                          target: {
                            name: 'items',
                            value: selected,
                          },
                        })}
                        options={fetchedItems === undefined ? [] : fetchedItems}
                        clearButton
                        placeholder={t('alerts:itemsPlaceholder')}
                        selected={items}
                        multiple
                        isLoading={fetchedItems === undefined}
                        bsSize="large"
                        highlightOnlyResult
                      />
                    </div>
                  </div>
                </div>
              )
          }

          <div className="row apply-button">
            <div className="col-md-6">
              <button
                className="btn btn-info btn-lg submit"
                type="submit"
                onClick={submit}
              >
                {t('alerts:subscribe')}
              </button>
            </div>
          </div>

          {
            showFormErrors && !formValid
              ? (
                <div className="row validation-message">
                  <div className="col-md-12">
                    <Alert bsStyle="danger">
                      <i className="glyphicon glyphicon-exclamation-sign" />
&nbsp;
                      {
                      purchaseReqId === undefined
                        ? <span>{t('alerts:formErrorGeneral')}</span>
                        : <span>{t('alerts:formErrorForTender')}</span>
                    }
                    </Alert>
                  </div>
                </div>
              )
              : null
          }

          {
            (serverResponse !== undefined)
              ? (
                <div className="row validation-message">
                  <div className="col-md-12">
                    <h4>
                      {serverResponse.status === true
                        ? (
                          <Alert bsStyle="info">
                            {t('alerts:subscribedSuccessfully:line1').replace('$#$', email)}
                            <br />
                            {t('alerts:subscribedSuccessfully:line2')}
                          </Alert>
                        )
                        : (
                          <Alert bsStyle="danger">
                            {t('alerts:subscribeFailed')}
                            <br />
                            {serverResponse.status === false ? serverResponse.message : t('alerts:unknownError')}
                          </Alert>
                        )}
                    </h4>
                  </div>
                </div>
              )
              : null
          }
        </div>
      </div>
    </div>
  );
};

Alerts.propTypes = {
  styling: PropTypes.object.isRequired,
};

export default Alerts;
