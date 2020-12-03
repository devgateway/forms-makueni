import {OverlayTrigger, Tooltip} from 'react-bootstrap';
import NoDataMessage from './NoData';
import React from 'react';
import translatable from "../../../translatable";

class ImplReport extends translatable(React.Component) {
  getFeedbackSubject() {
    const { tenderTitle, department, fiscalYear } = this.props;

    let metadata;
    if (department !== undefined) {
      metadata = ' - ' + tenderTitle
        + ' - ' + department.label
        + ' - ' + fiscalYear.name;
    }
    return escape(this.getReportName() + metadata);
  }

  getReportName(){

  }

  render() {
    const { fiscalYear, data, tenderTitle } = this.props;
    const { formatDate } = this.props.styling.tables;

    if (data === undefined) {
      return (<NoDataMessage translations={this.props.translations}/>);
    }

    return (<div>
      <div className="row padding-top-10">
        {
          data !== undefined
            ? data.sort((a, b) => new Date(a.approvedDate) - new Date(b.approvedDate))
              .map(i => <div key={i._id} className="box">
              <div className="row padding-top-10">
                <div className="col-md-3">
                  <div className="item-label">{this.t("report:tenderTitle")}</div>
                  <div className="item-value">{tenderTitle}</div>
                </div>
                <div className="col-md-3">
                  <div className="item-label">{this.t("report:awardee:label")}</div>
                  <div className="item-value">{i.contract.awardee.label}</div>
                </div>
                <div className="col-md-3">
                  <div className="item-label">{this.t("report:fiscalYear")}</div>
                  <div className="item-value">{fiscalYear.name}</div>
                </div>
                <div className="col-md-3">
                  <div className="item-label">{this.t("report:approvedDate")}</div>
                  <div className="item-value">{formatDate(i.approvedDate)}</div>
                </div>
              </div>
                {
                  this.childElements(i)
                }
                {
                  i.formDocs ?
                      <div className="row padding-top-10">
                        <div className="col-md-12">
                          <div className="item-label">{this.t("report:uploads").replace("$#$", this.getReportName())}</div>
                          {
                            i.formDocs.map(doc => <div key={doc._id}>
                              <OverlayTrigger
                                  placement="bottom"
                                  overlay={
                                    <Tooltip id="download-tooltip">
                            {this.t("general:downloadFile:tooltip")}
                                    </Tooltip>
                                  }>

                                <a className="item-value download" href={doc.url} target="_blank">
                                  <i className="glyphicon glyphicon-download"/>
                                  <span>{doc.name}</span>
                                </a>
                              </OverlayTrigger>
                            </div>)
                          }
                        </div>
                      </div>
                      : null
                }
              </div>) : null
        }
      </div>
    </div>);
  }

  childElements(i) {

  }
}

export default ImplReport;