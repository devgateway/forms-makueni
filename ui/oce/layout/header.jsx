import React from 'react';
import translatable from '../translatable';
import cn from 'classnames';
import URI from 'urijs';
import Cookies from 'universal-cookie';

import * as introJs from 'intro.js/intro.js';
import 'intro.js/introjs.css';
import './header.less';
import { statsInfo } from './state';

export default class Header extends translatable(React.Component) {
  constructor(props) {
    super(props);

    this.state = {
      selected: props.selected || '',
    };

    this.tabs = [
      {
        name: 'tender',
        title: 'Tenders',
        step:2,
        intro: "The portal opens by default on the Tender page. It shows all the tenders for each " +
          "financial year for each department in the County Government of Makueni.",
        icon: 'assets/icons/efficiency.svg'
      },
      {
        name: 'procurement-plan',
        title: 'Procurement Plan',
        step:3,
        intro: "Click to view all the procurement plans for each financial year for each department in" +
          " the County Government of Makueni.",
        icon: 'assets/icons/compare.svg'
      },
      {
        name: 'm-and-e',
        title: 'Charts',
        step:4,
        intro: "Click the charts button to view charts that provide an overview of the procurement " +
          "process, and highlight the competitiveness, and efficiency of the procurement process.",
        icon: 'assets/icons/eprocurement.svg'
      }
    ];

    this.changeOption = this.changeOption.bind(this);
    this.isActive = this.isActive.bind(this);
  }

  componentDidMount() {
    statsInfo.addListener('Header', () => {
      statsInfo.getState()
      .then(data => {
        this.setState({ data: data });
        const cookies = new Cookies();
        if (cookies.get("introjs") === undefined ) {
          const current = new Date();
          const nextYear = new Date();
          nextYear.setFullYear(current.getFullYear() + 1);
          cookies.set("introjs","introjs", {path:'/', expires: nextYear});
          this.showIntroJs();
        }
      });
    });
  }

  componentWillUnmount() {
    statsInfo.removeListener('Header');
  }

  changeOption(option) {
    this.setState({ selected: option });
    this.props.onSwitch(option);
  }

  isActive(option) {
    const { selected } = this.state;
    if (selected === '') {
      return false;
    }
    return selected === option;
  }

  showIntroJs() {
    window.scrollTo(0, 0);
    introJs.introJs()
      .setOption('overlayOpacity', 0.7)
      .setOption('showProgress', true)
      .setOption("scrollToElement", true)
      .start();
  }

  exportBtn() {
    const excelURL = new URI('/api/makueni/excelExport');
    const jsonURL = new URI('/api/ocds/package/all');

    return (<div>
        <span className="help-title">
          <a onClick={()=> {
            this.showIntroJs();
          }
          }>HELP</a>
        </span>
        <span className="download-title" data-step="7" data-intro="Download the data in either XLS,
        which uses the standard government terminology, or in JSON which uses the Open Contracting
         Data Standard.">
          Download the Data
        </span>
        <div className="export-btn">
          <a href={excelURL} download="export.zip">
            <button className="xls"></button>
          </a>
          <a href={jsonURL} target="_blank">
            <button className="json"></button>
          </a>
        </div>
      </div>
    );
  }

  render() {
    const { data } = this.state;
    const currencyFormatter = this.props.styling.tables.currencyFormatter;

    return (<div>
      <header className="branding row">
        <div className="col-md-8 col-sm-6 col-xs-12">
          <a className="portal-logo-wrapper" href="#!/">
              <img src="assets/makueni-logo.png" alt="Makueni"/>
              <span data-step="1" data-intro="Welcome to the Government of Makueni County Open Contracting Portal.
              The procurement data has been provided and validated by the Government of Makueni county.">
                Government of Makueni County Open Contracting Portal</span>
          </a>
        </div>

        <div className="col-md-4 col-sm-6 col-xs-12">
          <div className="row">
            <div className="navigation">
              {
                this.tabs.map(tab => {
                  return (<a
                      key={tab.name}
                      href="javascript:void(0);"
                      className={cn('', { active: this.isActive(tab.name) })}
                      onClick={() => this.changeOption(tab.name)}
                    ><span data-intro={tab.intro} data-step={tab.step}>
                      {tab.title}
                    </span>
                    </a>
                  );
                })
              }
            </div>
          </div>
        </div>
      </header>

      <div className="header-tools row">
        {
          data !== undefined
            ? <div>
              <div className="col-lg-3 col-md-3 col-sm-6 total-item" data-step="5" data-intro="This
              shows the total number of tenders that have been contracted that are published on the
               portal." data-position="right">
                <span className="total-label">Total Contracts</span>
                <span className="total-number">{data.count}</span>
              </div>
              <div className="col-lg-4 col-md-5 col-sm-6 total-item" data-step="6"
                   data-intro="This shows the total contracted amount."
                   data-position="right">
                <span className="total-label">Total Contract Amount</span>
                <span className="total-number">{currencyFormatter(data.value)}</span>
              </div>
            </div>
            : null
        }

        <div className="col-md-4 col-sm-12 col-xs-12 export">
          {this.exportBtn()}
        </div>
      </div>
    </div>);
  }
}
