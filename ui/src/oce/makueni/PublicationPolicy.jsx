import CRDPage from '../corruption-risk/page';
import Header from '../layout/header';
import './makueni.scss';
import React from 'react';
import reactStringReplace from 'react-string-replace';

class Docs extends CRDPage {
  render() {
    return (
      <div className="container-fluid dashboard-default">
        <Header
          translations={this.props.translations}
          onSwitch={this.props.onSwitch}
          styling={this.props.styling}
          selected="docs"
        />
        <div className="makueni-procurement-plan content row">

          <div className="col-md-2 col-md-offset-5">
            <h1>{this.t('publicationPolicy:title')}</h1>
          </div>

          <div className="col-md-6 col-md-offset-3 text-justify">
            <h3>{this.t('publicationPolicy:pp:title')}</h3>
            {this.t('publicationPolicy:pp:text')}
            <h3>{this.t('publicationPolicy:pd:title')}</h3>
            {this.t('publicationPolicy:pd:text')}
            <div>
              <img src={`${process.env.PUBLIC_URL}/ocmakueni-architecture.png`} />
              {this.t('publicationPolicy:pd:architecture')}
            </div>
            <h3>{this.t('publicationPolicy:ad:title')}</h3>
            {reactStringReplace(this.t('publicationPolicy:ad:line1'), '$#$',
              (m, i) => {
                if (i === 1) {
                  return (
                    <a
                      key={i}
                      className="download-file"
                      href="https://opencontracting.makueni.go.ke/api/ocds/package/all"
                    >
                      {this.t('publicationPolicy:ad:line1:link1')}
                    </a>
                  );
                }
                return (
                  <a
                    key={i}
                    className="download-file"
                    href="https://opencontracting.makueni.go.ke/swagger-ui/#/ocds-controller"
                  >
                    {this.t('publicationPolicy:ad:line1:link2')}
                  </a>
                );
              })}
            <p />
            {reactStringReplace(this.t('publicationPolicy:ad:line2'), '$#$',
              (m, i) => (
                <a
                  key={i}
                  className="download-file"
                  href="https://opencontracting.makueni.go.ke"
                >
                  opencontracting.makueni.go.ke
                </a>
              ))}
            <p />
            {this.t('publicationPolicy:ad:line3')}
            <p />
            {this.t('publicationPolicy:ad:line4')}

            <h3>{this.t('publicationPolicy:dataScope:title')}</h3>

            <ul>
              <li>
                <b>{this.t('publicationPolicy:dataScope:line1:label')}</b>
                {this.t('publicationPolicy:dataScope:line1:text')}
              </li>
              <li>
                <b>{this.t('publicationPolicy:dataScope:line2:label')}</b>
                {this.t('publicationPolicy:dataScope:line2:text')}
              </li>
              <li>
                <b>{this.t('publicationPolicy:dataScope:line3:label')}</b>
                {this.t('publicationPolicy:dataScope:line3:text')}
              </li>
              <li>
                <b>{this.t('publicationPolicy:dataScope:line4:label')}</b>
                {this.t('publicationPolicy:dataScope:line4:text')}
              </li>
              <li>
                <b>{this.t('publicationPolicy:dataScope:line5:label')}</b>
                {this.t('publicationPolicy:dataScope:line5:text')}
              </li>
              <li>
                <b>{this.t('publicationPolicy:dataScope:line6:label')}</b>
                {this.t('publicationPolicy:dataScope:line6:text')}
              </li>
            </ul>

            <h3>{this.t('publicationPolicy:licensing:title')}</h3>
            {reactStringReplace(this.t('publicationPolicy:licensing:text'), '$#$',
              (m, i) => <a key={i} className="download-file" href="https://creativecommons.org/licenses/by-sa/4.0/">{this.t('publicationPolicy:licensing:text:link')}</a>)}

            <h3>{this.t('publicationPolicy:devPlans:title')}</h3>
            {this.t('publicationPolicy:devPlans:text')}

            <h3>{this.t('publicationPolicy:pubContact:title')}</h3>
            {reactStringReplace(this.t('publicationPolicy:pubContact:text'), '$#$',
              (m, i) => <a key={i} className="download-file" href="mailto:opencontracting@makueni.go.ke">opencontracting@makueni.go.ke</a>)}

            <h3>{this.t('publicationPolicy:disclaimer:title')}</h3>
            {this.t('publicationPolicy:disclaimer:text')}

            <h3>{this.t('publicationPolicy:dataAndExceptions:title')}</h3>
            {this.t('publicationPolicy:dataAndExceptions:text')}
            <p />

            <a
              className="download-file"
              href="https://github.com/devgateway/forms-makueni/tree/master/persistence-mongodb/src/main/resources/extensions/contract_contractor"
            >
              {this.t('publicationPolicy:dataAndExceptions:contractContractor')}
            </a>
            <p />

            <a
              className="download-file"
              href="https://github.com/devgateway/forms-makueni/tree/master/persistence-mongodb/src/main/resources/extensions/first_time_winners"
            >
              {this.t('publicationPolicy:dataAndExceptions:firstTimeWinners')}
            </a>
            <p />
            <a
              className="download-file"
              href="https://github.com/devgateway/forms-makueni/tree/master/persistence-mongodb/src/main/resources/extensions/fiscal_year"
            >
              {this.t('publicationPolicy:dataAndExceptions:fiscalYear')}
            </a>
            <p />
            <a
              className="download-file"
              href="https://github.com/devgateway/forms-makueni/tree/master/persistence-mongodb/src/main/resources/extensions/milestone_delayed_authorization"
            >
              {this.t('publicationPolicy:dataAndExceptions:delayedMilestone')}
            </a>
            <p />
            <a
              className="download-file"
              href="https://github.com/devgateway/forms-makueni/tree/master/persistence-mongodb/src/main/resources/extensions/planning_items"
            >
              {this.t('publicationPolicy:dataAndExceptions:plannedItems')}
            </a>
            <p />
            <a
              className="download-file"
              href="https://github.com/devgateway/forms-makueni/tree/master/persistence-mongodb/src/main/resources/extensions/target_groups"
            >
              {this.t('publicationPolicy:dataAndExceptions:targetGroups')}
            </a>
            <p />
            <a
              className="download-file"
              href="https://github.com/devgateway/forms-makueni/tree/master/persistence-mongodb/src/main/resources/extensions/tender_location"
            >
              {this.t('publicationPolicy:dataAndExceptions:tenderLocation')}
            </a>
            <p />
            <a
              className="download-file"
              href="https://github.com/open-contracting-extensions/ocds_bid_extension"
            >
              {this.t('publicationPolicy:dataAndExceptions:bidExtension')}
            </a>
            <p />
            <a
              className="download-file"
              href="https://github.com/open-contracting-extensions/ocds_budget_breakdown_extension"
            >
              {this.t('publicationPolicy:dataAndExceptions:budgetBreakdownExtensions')}
            </a>
            <p />
            <a
              className="download-file"
              href=" https://github.com/open-contracting-extensions/ocds_location_extension"
            >
              {this.t('publicationPolicy:dataAndExceptions:locationExtension')}
            </a>

            <h3>{this.t('publicationPolicy:ocdsMapping:title')}</h3>
            {reactStringReplace(this.t('publicationPolicy:ocdsMapping:text'), '$#$',
              (m, i) => {
                if (i === 1) {
                  return <a key={i} className="download-file" href={`${process.env.PUBLIC_URL}/Makueni-OCDS-Conversion-Matrix.xlsx`}>{this.t('publicationPolicy:ocdsMapping:text:link1')}</a>;
                }
                return <a key={i} target="_blank" className="download-file" href={`${process.env.PUBLIC_URL}/javadoc/dao/index.html`}>{this.t('publicationPolicy:ocdsMapping:text:link2')}</a>;
              })}
          </div>

        </div>
      </div>
    );
  }
}

export default Docs;
