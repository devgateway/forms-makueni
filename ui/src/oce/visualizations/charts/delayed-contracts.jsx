import FrontendDateFilterableChart from './frontend-date-filterable';
import { pluckImm } from '../../tools';
import fmConnect from '../../fm/fm';
import PropTypes from 'prop-types';
import { tMonth } from '../../translatable';

class DelayedContracts extends FrontendDateFilterableChart {
  getRawData() {
    return super.getData();
  }

  getData() {
    const data = super.getData();
    if (!data) return [];
    const { years, t } = this.props;
    const monthly = data.hasIn([0, 'month']);
    const dates = monthly
      ? data.map(pluckImm('month')).map((month) => tMonth(t, month, years)).toArray()
      : data.map(pluckImm('year')).toArray();

    return [{
      x: data.map(pluckImm('countOnTime')).toArray(),
      y: dates,
      name: t('charts:delayedContracts:traces:countOnTime'),
      type: 'bar',
      orientation: 'h',
      marker: {
        color: this.props.styling.charts.traceColors[0],
      },
    }, {
      x: data.map(pluckImm('countDelayed')).toArray(),
      y: dates,
      name: t('charts:delayedContracts:traces:countDelayed'),
      type: 'bar',
      orientation: 'h',
      text: data.map(pluckImm('percentDelayed')).map(this.props.styling.charts.hoverFormatter).map((x) => `${x}% delayed`).toArray(),
      marker: {
        color: this.props.styling.charts.traceColors[1],
      },
    }];
  }

  getLayout() {
    const { t } = this.props;
    const { hoverFormat } = this.props.styling.charts;
    let annotations = [];
    const data = super.getData();
    if (data) {
      annotations = data.map((imm, index) => {
        const sum = imm.reduce((sum, val, key) => (key === 'year' || key === 'month' || key === 'percentDelayed' ? sum : sum + val), 0).toFixed(2);
        return {
          y: index,
          x: sum,
          xanchor: 'left',
          yanchor: 'middle',
          text: `${t('charts:delayedContracts:traces:total')} ${sum}`,
          showarrow: false,
        };
      }).toArray();
    }

    return {
      annotations,
      barmode: 'stack',
      xaxis: {
        title: t('charts:delayedContracts:yAxisTitle'),
        hoverformat: hoverFormat,
      },
      yaxis: {
        title: this.props.monthly ? t('general:month') : t('general:year'),
        type: 'category',
      },
    };
  }
}

DelayedContracts.endpoint = 'delayedContracts';
// BidPeriod.excelEP = 'bidTimelineExcelChart';
DelayedContracts.getName = (t) => t('charts:delayedContracts:title');
DelayedContracts.horizontal = true;

// BidPeriod.getFillerDatum = seed => Map(seed).set('tender', 0).set('award', 0);
// BidPeriod.getMaxField = imm => imm.get('tender', 0) + imm.get('award', 0);

DelayedContracts.propTypes = {
  t: PropTypes.func.isRequired,
};

export default fmConnect(DelayedContracts, 'viz.me.chart.delayedContracts');
