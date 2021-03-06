import React from 'react';
import { pluck } from '../../../../tools';
import Donut from '../../../donut';
import PropTypes from 'prop-types';

class CenterText extends React.PureComponent {
  format(number) {
    const formatted = this.props.styling.charts.hoverFormatter(number) || '';
    return (
      <>
        {formatted.slice(0, -1)}
        <span className="multiplier">{formatted.slice(-1)}</span>
      </>
    );
  }

  render() {
    const { data, t } = this.props;
    const [fst, snd] = data.map(pluck('value'));
    return (
      <div className="center-text two-rows">
        <div>
          {this.format(fst)}
          <div className="secondary">
            {this.format(snd)}
            {' '}
            {t('crd:supplier:amountLostVsWon:Lost')}
          </div>
        </div>
      </div>
    );
  }
}

CenterText.propTypes = {
  t: PropTypes.func.isRequired,
};

class AmountWonVsLost extends React.Component {
  transformNewData(path, data) {
    const { styling, t } = this.props;
    const won = data.getIn([0, 'won', 'totalAmount']);
    const lost = data.getIn([0, 'lostAmount']);

    this.props.requestNewData(path, [{
      color: '#2e833a',
      label: t('crd:supplier:amountLostVsWon:won')
        .replace('$#$', styling.charts.hoverFormatter(won)),
      value: won,
    }, {
      color: '#72c47e',
      label: t('crd:supplier:amountLostVsWon:lost')
        .replace('$#$', styling.charts.hoverFormatter(lost)),
      value: lost,
    }]);
  }

  render() {
    const { data, t } = this.props;
    return (
      <Donut
        {...this.props}
        requestNewData={this.transformNewData.bind(this)}
        data={data || []}
        CenterText={CenterText}
        title={t('crd:supplier:amountLostVsWon:title')}
        subtitle={t('crd:supplier:amountLostVsWon:subtitle')}
        endpoint="procurementsWonLost"
      />
    );
  }
}

AmountWonVsLost.propTypes = {
  t: PropTypes.func.isRequired,
};

export default AmountWonVsLost;
