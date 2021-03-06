import React from 'react';
import PropTypes from 'prop-types';
import { LOGIN_URL } from './constants';
import { debounce } from '../tools';
import logo from '../resources/logo.png';

class LandingPopup extends React.Component {
  constructor(...args) {
    super(...args);
    this.state = {
      top: 0,
    };
  }

  componentDidMount() {
    this.recalcTop();
    this.windowResizeListener = debounce(this.recalcTop.bind(this));
    window.addEventListener('resize', this.windowResizeListener);
  }

  componentWillUnmount() {
    window.removeEventListener('resize', this.windowResizeListener);
  }

  onClose() {
    const { redirectToLogin, requestClosing } = this.props;
    if (redirectToLogin) {
      const hash = encodeURIComponent(window.location.hash);
      window.location.href = `${LOGIN_URL}${hash}`;
    } else {
      requestClosing();
    }
  }

  recalcTop() {
    this.setState({
      top: (window.innerHeight - this.refs.thePopup.offsetHeight) / 2,
    });
  }

  render() {
    const { top } = this.state;
    const { t } = this.props;
    return (
      <div>
        <div className="crd-fullscreen-popup-overlay" onClick={this.onClose.bind(this)} />

        <div className="crd-fullscreen-popup" ref="thePopup" style={{ top }}>
          <div className="container-fluid">
            <div className="row">
              <div className="col-sm-1 text-right">
                <img src={logo} />
              </div>
              <div className="col-sm-9">
                <h4 className="popup-title">{t('crd:title')}</h4>
              </div>
              {/* <div className="col-sm-1 language-switcher"> */}
              {/*  {languageSwitcher()} */}
              {/* </div> */}
              <div className="col-sm-1 text-right">
                <i className="glyphicon glyphicon-remove-circle close-button" onClick={this.onClose.bind(this)} />
              </div>
            </div>
            <div className="row">
              <div className="col-sm-10 col-sm-offset-1 text-column-left">
                {t('crd:landing:introduction:1')}
              </div>
              <div className="col-sm-1" />

              <hr className="col-sm-offset-1 col-sm-10 end separator" />
              <div className="col-sm-1" />

            </div>
            <div className="row">

              <div className="col-sm-1" />

              <div className="col-sm-2 end">
                <button className="btn btn-primary" onClick={this.onClose.bind(this)}>
                  {t('crd:landing:enter')}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }
}

LandingPopup.propTypes = {
  t: PropTypes.func.isRequired,
};

export default LandingPopup;
