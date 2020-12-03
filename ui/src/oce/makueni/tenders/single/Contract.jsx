import { OverlayTrigger, Tooltip } from 'react-bootstrap';
import NoDataMessage from './NoData';
import React from 'react';
import translatable from "../../../translatable";

class Contract extends translatable(React.Component) {
  getFeedbackSubject() {
    const { tenderTitle, department, fiscalYear } = this.props;

    let metadata;
    if (department !== undefined) {
      metadata = " - " + tenderTitle
        + " - " + department.label
        + " - " + fiscalYear.name;
    }
    return escape(this.t("contract:subject") + metadata);
  }

  render() {
    const { data } = this.props;
    const { currencyFormatter, formatDate } = this.props.styling.tables;

    if (data === undefined) {
      return (<NoDataMessage translations={this.props.translations}/>);
    }

    const contract = data[0];

    return (<div>
      <div className="row padding-top-10">
        <div className="col-md-4">
          <div className="item-label">{this.t("contract:referenceNumber")}</div>
          <div className="item-value">{contract.referenceNumber}</div>
        </div>
        <div className="col-md-4">
          <div className="item-label">{this.t("contract:contractDate")}</div>
          <div className="item-value">{formatDate(contract.contractDate)}</div>
        </div>
        <div className="col-md-4">
          <div className="item-label">{this.t("contract:expiryDate")}</div>
          <div className="item-value">{formatDate(contract.expiryDate)}</div>
        </div>
      </div>

      <div className="row padding-top-10">
        <div className="col-md-6">
          <div className="item-label">{this.t("contract:awardee:label")}</div>
          <div className="item-value">{contract.awardee.label}</div>
        </div>
        <div className="col-md-6">
          <div className="item-label">{this.t("contract:awardee:address")}</div>
          <div className="item-value">{contract.awardee.address}</div>
        </div>
      </div>

      <div className="row padding-top-10">
        <div className="col-md-4">
          <div className="item-label">{this.t("contract:procuringEntity:label")}</div>
          <div className="item-value">{contract.procuringEntity.label}</div>
        </div>
        <div className="col-md-4">
          <div className="item-label">{this.t("contract:contractValue")}</div>
          <div className="item-value">{currencyFormatter(contract.contractValue)}</div>
        </div>
        <div className="col-md-4">
          <div className="item-label">{this.t("contract:contractApprovalDate")}</div>
          <div className="item-value">{formatDate(contract.contractApprovalDate)}</div>
        </div>
      </div>

      {
        contract.contractDocs !== undefined
          ? <div>
            <div className="row padding-top-10">
              <div className="col-md-12 sub-title">{this.t("contract:contractDocs")}
                ({contract.contractDocs.length})
              </div>
            </div>

            {
              contract.contractDocs.map(contractDoc => <div key={contractDoc._id} className="box">
                <div className="row">
                  <div className="col-md-6">
                    <div className="item-label">{this.t("contract:contractDocs:type")}</div>
                    <div className="item-value">{contractDoc.contractDocumentType.label}</div>
                  </div>
                  <div className="col-md-6">
                    <div className="item-label">{this.t("contract:contractDocs")}</div>

                    {
                      contractDoc.formDocs.map(doc => <div key={doc._id}>
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
              </div>)
            }
          </div>
          : null
      }
    </div>);
  }
}

export default Contract;