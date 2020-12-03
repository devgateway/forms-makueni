import FilterItemDep from './FilterItemDep';
import FilterItemFY from './FilterItemFY';
import FilterItems from './FilterItems';
import FiltersWrapper from './FiltersWrapper';
import FilterSubcounties from './FilterSubcounties';
import FilterWards from './FilterWards';
import FilterAmount from './FilterAmount';
import FilterTitle from './FilterTitle';
import FilterTenderDate from './FilterTenderDate';
import fmConnect from "../../fm/fm";

/**
 * Filter used for the Tender table.
 */
class FiltersTendersWrapper extends FiltersWrapper {

}

FiltersTendersWrapper.ITEMS = [FilterTitle, FilterItemDep, FilterItemFY, FilterItems,
  FilterSubcounties, FilterWards, FilterAmount, FilterTenderDate];

FiltersTendersWrapper.CLASS = ['title-search', 'department', 'fiscal-year', 'items',
  'subcounties', 'wards', 'amount', 'date'];

FiltersTendersWrapper.FM = ['publicView.filter.titleSearch', 'publicView.filter.department',
  'publicView.filter.fiscalYear', 'publicView.filter.items', 'publicView.filter.subcounties',
  'publicView.filter.wards', 'publicView.filter.amount', 'publicView.filter.date'];

export default fmConnect(FiltersTendersWrapper);