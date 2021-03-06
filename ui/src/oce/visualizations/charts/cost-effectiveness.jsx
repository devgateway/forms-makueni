import { Map } from 'immutable';
import FrontendDateFilterableChart from './frontend-date-filterable';
import fmConnect from '../../fm/fm';
import PropTypes from 'prop-types';
import { tMonth } from '../../translatable';

class CostEffectiveness extends FrontendDateFilterableChart {
  mkTrace(name, colorIndex) {
    const { traceColors, hoverFormatter } = this.props.styling.charts;
    const trace = {
      x: [],
      y: [],
      text: [],
      name,
      type: 'bar',
      marker: {
        color: traceColors[colorIndex],
      },
    };

    if (hoverFormatter) trace.hoverinfo = 'text+name';

    return trace;
  }

  getData() {
    const data = super.getData();
    if (!data) return [];
    const { t } = this.props;
    const traces = [
      this.mkTrace(t('charts:costEffectiveness:traces:tenderPrice'), 0),
      this.mkTrace(t('charts:costEffectiveness:traces:awardPrice'), 1),
    ];

    const { hoverFormatter } = this.props.styling.charts;
    const { years } = this.props;

    data.forEach((datum) => {
      const date = datum.has('month')
        ? tMonth(t, datum.get('month'), years)
        : datum.get('year');
      traces.forEach((trace) => trace.x.push(date));
      const tender = datum.get('totalTenderAmount');
      const award = datum.get('totalAwardAmount');
      traces[0].y.push(tender);
      traces[1].y.push(award);
      if (hoverFormatter) {
        traces[0].text.push(hoverFormatter(tender));
        traces[1].text.push(hoverFormatter(award));
      }
    });

    return traces;
  }

  getLayout() {
    const { t } = this.props;
    return {
      barmode: 'group',
      xaxis: {
        title: this.props.monthly ? t('general:month') : t('general:year'),
        type: 'category',
      },
      yaxis: {
        title: t('charts:costEffectiveness:yAxisTitle'),
        tickprefix: '   ',
      },
    };
  }
}

CostEffectiveness.getName = (t) => t('charts:costEffectiveness:title');
CostEffectiveness.endpoint = 'costEffectivenessTenderAwardAmount';
CostEffectiveness.excelEP = 'costEffectivenessExcelChart';
CostEffectiveness.getFillerDatum = (seed) => Map(seed).set('totalTenderAmount', 0).set('diffTenderAwardAmount', 0);

CostEffectiveness.getMaxField = (imm) => imm.get('totalTenderAmount') + imm.get('diffTenderAwardAmount');

CostEffectiveness.propTypes = {
  t: PropTypes.func.isRequired,
};

export default fmConnect(CostEffectiveness, 'viz.me.chart.costEffectiveness');
