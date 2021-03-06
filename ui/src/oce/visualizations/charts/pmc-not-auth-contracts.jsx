import FrontendDateFilterableChart from './frontend-date-filterable';
import { pluckImm } from '../../tools';
import fmConnect from '../../fm/fm';
import PropTypes from 'prop-types';
import { tMonth } from '../../translatable';

class PmcNotAuthContracts extends FrontendDateFilterableChart {
  getRawData() {
    return super.getData();
  }

  getData() {
    const data = super.getData();
    if (!data) return [];
    const monthly = data.hasIn([0, 'month']);
    const { years, t } = this.props;
    const dates = monthly
      ? data.map(pluckImm('month')).map((month) => tMonth(t, month, years)).toArray()
      : data.map(pluckImm('year')).toArray();

    return [{
      x: data.map(pluckImm('countAuthorized')).toArray(),
      y: dates,
      name: t('charts:pmcNotAuthContracts:traces:countAuthorized'),
      type: 'bar',
      orientation: 'h',
      marker: {
        color: this.props.styling.charts.traceColors[0],
      },
    }, {
      x: data.map(pluckImm('countNotAuthorized')).toArray(),
      y: dates,
      name: t('charts:pmcNotAuthContracts:traces:countNotAuthorized'),
      type: 'bar',
      orientation: 'h',
      text: data.map(pluckImm('percentNotAuthorized')).map(this.props.styling.charts.hoverFormatter).map((x) => `${x}% not authorized`).toArray(),
      marker: {
        color: this.props.styling.charts.traceColors[1],
      },
    }];
  }

  getLayout() {
    const { hoverFormat } = this.props.styling.charts;
    const { t } = this.props;
    let annotations = [];
    const data = super.getData();
    if (data) {
      annotations = data.map((imm, index) => {
        const sum = imm.reduce((sum, val, key) => (key === 'year' || key === 'month' || key === 'percentNotAuthorized' ? sum : sum + val), 0).toFixed(2);
        return {
          y: index,
          x: sum,
          xanchor: 'left',
          yanchor: 'middle',
          text: `${t('charts:pmcNotAuthContracts:traces:total')} ${sum}`,
          showarrow: false,
        };
      }).toArray();
    }

    return {
      annotations,
      barmode: 'stack',
      xaxis: {
        title: t('charts:pmcNotAuthContracts:yAxisTitle'),
        hoverformat: hoverFormat,
      },
      yaxis: {
        title: this.props.monthly ? t('general:month') : t('general:year'),
        type: 'category',
      },
    };
  }
}

PmcNotAuthContracts.endpoint = 'pmcNotAuthContracts';
// BidPeriod.excelEP = 'bidTimelineExcelChart';
PmcNotAuthContracts.getName = (t) => t('charts:pmcNotAuthContracts:title');
PmcNotAuthContracts.horizontal = true;

// BidPeriod.getFillerDatum = seed => Map(seed).set('tender', 0).set('award', 0);
// BidPeriod.getMaxField = imm => imm.get('tender', 0) + imm.get('award', 0);

PmcNotAuthContracts.propTypes = {
  t: PropTypes.func.isRequired,
};

export default fmConnect(PmcNotAuthContracts, 'viz.me.chart.pmcNotAuthContracts');
