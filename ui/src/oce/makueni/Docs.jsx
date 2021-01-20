import CRDPage from '../corruption-risk/page';
import Header from '../layout/header';
import './makueni.scss';
import React from 'react';
import Footer from '../layout/footer';

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
            <h1>{this.t('docs:resources')}</h1>
          </div>

          <div className="col-md-6 col-md-offset-3">
            <div className="list-group">
              <a href="#!/portal-videos" className="list-group-item">
                <h3 className="list-group-item-heading">{this.t('docs:portalVideos:heading')}</h3>
              </a>
              <a href="/swagger-ui/" className="list-group-item">
                <h3 className="list-group-item-heading">{this.t('docs:allEndpoints:heading')}</h3>
                <p className="list-group-item-text">{this.t('docs:allEndpoints:text')}</p>
              </a>
              <a href="/swagger-ui/#/makueni-data-controller" className="list-group-item">
                <h3 className="list-group-item-heading">{this.t('docs:makueniEndpoints:heading')}</h3>
                <p className="list-group-item-text">{this.t('docs:makueniEndpoints:text')}</p>
              </a>
              <a href="/swagger-ui/#/ocds-controller" className="list-group-item">
                <h3 className="list-group-item-heading">{this.t('docs:ocdsEndpoints:heading')}</h3>
                <p className="list-group-item-text">{this.t('docs:ocdsEndpoints:text')}</p>
              </a>
              <a href="https://standard.open-contracting.org/1.1/en/" className="list-group-item">
                <h3 className="list-group-item-heading">{this.t('docs:ocdsDocs:heading')}</h3>
                <p className="list-group-item-text">{this.t('docs:ocdsDocs:text')}</p>
              </a>
              <a href="https://github.com/devgateway/forms-makueni" className="list-group-item">
                <h3 className="list-group-item-heading">{this.t('docs:sourceCode:heading')}</h3>
                <p className="list-group-item-text">{this.t('docs:sourceCode:text')}</p>
              </a>
              <a href="https://github.com/devgateway/forms-makueni/blob/develop/LICENSE" className="list-group-item">
                <h3 className="list-group-item-heading">{this.t('docs:license:heading')}</h3>
                <p className="list-group-item-text">{this.t('docs:license:text')}</p>
              </a>
              <a href="#!/publication-policy" className="list-group-item">
                <h3 className="list-group-item-heading">{this.t('docs:policy:heading')}</h3>
                <p className="list-group-item-text">{this.t('docs:policy:text')}</p>
              </a>
            </div>
          </div>
        </div>
        <Footer translations={this.props.translations} />
      </div>
    );
  }
}

export default Docs;
