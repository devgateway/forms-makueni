import Table from './index';
import { pluckImm } from '../../tools';
import fmConnect from '../../fm/fm';

class Awards extends Table {
  row(entry) {
    const bidNo = entry.getIn(['planning', 'bidNo']);
    const awards = entry.get('awards');
    const value = awards.get('value');

    // TODO - change the key when we have a bidNo
    return (
      <tr key={bidNo + awards}>
        {/* <td>{bidNo}</td> */}
        <td>{new Date(awards.get('date')).toLocaleDateString(undefined, Table.DATE_FORMAT)}</td>
        <td className="supplier-name">
          {awards.get('suppliers')
            .map(pluckImm('name')).map((s) => s.toUpperCase())
            .join(', ')}
        </td>
        <td>
          {this.maybeFormat(value.get('amount'))}
          {' '}
          {value.get('currency')}
        </td>
      </tr>
    );
  }

  render() {
    if (!this.props.data) return null;
    return (
      <table className="table table-striped table-hover awards-table">
        <thead>
          <tr>
            {/* <th>{this.t('tables:top10awards:number')}</th> */}
            <th>{this.t('tables:top10awards:date')}</th>
            <th>{this.t('tables:top10awards:supplier')}</th>
            <th>{this.t('tables:top10awards:value')}</th>
          </tr>
        </thead>
        <tbody>
          {this.props.data.map(this.row.bind(this))}
        </tbody>
      </table>
    );
  }
}

Awards.getName = (t) => t('tables:top10awards:title');
Awards.endpoint = 'topTenLargestAwards';

export default fmConnect(Awards, 'viz.me.table.awards');
