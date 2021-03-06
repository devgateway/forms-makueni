import frontendDateFilterable from '../frontend-date-filterable';
import Chart from './index';

class FrontendYearFilterableChart extends frontendDateFilterable(Chart) {
  hasNoData() {
    const data = super.getData();
    return data && data.isEmpty();
  }
}

FrontendYearFilterableChart.UPDATABLE_FIELDS = ['data', 'years', 'months', 'monthly'];

export default FrontendYearFilterableChart;
