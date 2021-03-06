import React from 'react';
import cn from 'classnames';
import PropTypes from 'prop-types';
import Chart from '../visualizations/charts/frontend-date-filterable';
import ReactIgnore from '../react-ignore';
import { POPUP_HEIGHT, POPUP_WIDTH } from './constants';
import loadingBubbles from '../resources/loading-bubbles.svg';

class CustomPopupChart extends Chart {
  constructor(...args) {
    super(...args);
    this.state = {
      popup: {
        show: false,
        left: 0,
        top: 0,
      },
    };
  }

  componentDidMount() {
    super.componentDidMount();
    const { chartContainer } = this;
    chartContainer.on('plotly_hover', this.showPopup.bind(this));
    chartContainer.on('plotly_unhover', () => this.hidePopup());
  }

  getPopupWidth() {
    return POPUP_WIDTH;
  }

  showPopup(data) {
    const point = data.points[0];
    const year = parseInt(point.x, 10);
    const traceName = point.data.name;
    const traceIndex = point.fullData.index;
    const POPUP_ARROW_SIZE = 8;
    const popupWidth = this.getPopupWidth();

    const { xaxis, yaxis } = point;
    const markerLeft = xaxis.l2p(xaxis._categories.indexOf(point.x)) + xaxis._offset;
    const markerTop = yaxis.l2p(point.y) + yaxis._offset;
    const { left: parentLeft } = this.chartContainer.getBoundingClientRect();
    const toTheLeft = (markerLeft + parentLeft + popupWidth) >= window.innerWidth;
    let top;
    let left;

    if (toTheLeft) {
      top = markerTop - (POPUP_HEIGHT / 2);
      left = markerLeft - popupWidth - (POPUP_ARROW_SIZE * 1.5);
    } else {
      top = markerTop - POPUP_HEIGHT - (POPUP_ARROW_SIZE * 1.5);
      left = markerLeft - (popupWidth / 2);
    }

    this.setState({
      popup: {
        show: true,
        toTheLeft,
        top,
        left,
        year,
        traceName,
        traceIndex,
      },
    });
  }

  hidePopup() {
    this.setState({
      popup: {
        show: false,
      },
    });
  }

  render() {
    const { loading, popup } = this.state;
    const { t } = this.props;
    const hasNoData = !loading && this.hasNoData();
    return (
      <div className={cn('chart-container', { 'popup-left': popup.toTheLeft })}>
        {hasNoData && <div className="message">{t('charts:general:noData')}</div>}
        {loading && (
        <div className="message">
          {t('general:loading')}
          <br />
          <img src={loadingBubbles} alt="" />
        </div>
        )}

        {popup.show && this.getPopup()}

        <ReactIgnore>
          <div ref={(c) => { this.chartContainer = c; }} />
        </ReactIgnore>
      </div>
    );
  }
}

CustomPopupChart.propTypes = {
  t: PropTypes.func.isRequired,
};

export default CustomPopupChart;
