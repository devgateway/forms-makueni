import { Map } from 'immutable';
import PropTypes from 'prop-types';
import Visualization from '../../visualization';
import ReactIgnore from '../../react-ignore';
import { max } from '../../tools';
import './index.scss';
import Plotly from 'plotly.js-basic-dist';
import loadingBubbles from '../../resources/loading-bubbles.svg';

class Chart extends Visualization {
  getData() {
    return super.getData();
  }

  getDecoratedLayout() {
    const {
      title, xAxisRange, yAxisRange, styling, width, height, margin, legend,
    } = this.props;
    const layout = this.getLayout();
    layout.width = width;
    layout.height = height;
    layout.margin = margin;
    if (title) layout.title = title;
    if (xAxisRange) layout.xaxis.range = xAxisRange;
    if (yAxisRange) layout.yaxis.range = yAxisRange;
    if (styling) {
      if (layout.xaxis) {
        layout.xaxis.titlefont = {
          color: styling.charts.axisLabelColor,
        };
      }

      if (layout.yaxis) {
        layout.yaxis.titlefont = {
          color: styling.charts.axisLabelColor,
        };
      }
    }
    if (legend === 'h') {
      layout.legend = layout.legend || {};
      layout.legend.orientation = 'h';
      layout.legend.xanchor = 'right';
      layout.legend.yanchor = 'bottom';
      layout.legend.x = 1;
      layout.legend.y = 1;
    }
    return layout;
  }

  componentDidMount() {
    super.componentDidMount();
    Plotly.newPlot(this.chartContainer, this.getData(), this.getDecoratedLayout(), { responsive: true });
  }

  componentWillUnmount() {
    Plotly.Plots.purge(this.chartContainer);
  }

  componentDidUpdate(prevProps) {
    super.componentDidUpdate(prevProps);
    if (this.constructor.UPDATABLE_FIELDS.some((prop) => prevProps[prop] !== this.props[prop])) {
      this.chartContainer.data = this.getData();
      this.chartContainer.layout = this.getDecoratedLayout();
      setTimeout(() => this.chartContainer && Plotly.redraw(this.chartContainer));
    } else if (['title', 'width', 'xAxisRange', 'yAxisRange'].some((prop) => prevProps[prop] !== this.props[prop])) {
      setTimeout(() => this.chartContainer && Plotly.relayout(this.chartContainer, this.getDecoratedLayout()));
    }
  }

  hasNoData() {
    return this.getData().length === 0;
  }

  render() {
    const { loading } = this.state;
    const { t } = this.props;
    const hasNoData = !loading && this.hasNoData();
    return (
      <div className="chart-container">
        {hasNoData && <div className="message">{t('charts:general:noData')}</div>}
        {loading && (
        <div className="message">
          {t('general:loading')}
          <br />
          <img src={loadingBubbles} alt="" />
        </div>
        )}
        <ReactIgnore>
          <div ref={(c) => {
            this.chartContainer = c;
          }}
          />
        </ReactIgnore>
      </div>
    );
  }
}

Chart.isChart = true;

Chart.getFillerDatum = (seed) => Map(seed);

Chart.getMaxField = (data) => data.flatten()
  .filter((value, key) => value && key !== 'year' && key !== 'month')
  .reduce(max, 0);

Chart.UPDATABLE_FIELDS = ['data'];

Chart.propTypes = {
  ...Chart.propTypes,
  styling: PropTypes.shape({
    charts: PropTypes.shape({
      axisLabelColor: PropTypes.string.isRequired,
      traceColors: PropTypes.arrayOf(PropTypes.string).isRequired,
    }).isRequired,
  }).isRequired,
  t: PropTypes.func.isRequired,
};

Chart.defaultProps = {
  legend: 'h',
};

export default Chart;
