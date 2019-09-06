import FilterBox from "./box";
import ProcuringEntity from "../../filters/procuring-entity";
import Supplier from "../../filters/supplier";
import {Set} from "immutable";
import Buyer from '../../filters/buyer';

class Organizations extends FilterBox{
  isActive(){
    const {appliedFilters} = this.props;
    return appliedFilters.get('procuringEntityId', Set()).count() > 0 ||
           appliedFilters.get('buyerId', Set()).count() > 0 ||
           appliedFilters.get('supplierId', Set()).count() > 0;
  }

  reset(){
    const {onApply, state} = this.props;
    onApply(state.delete('procuringEntityId').delete('supplierId').delete('buyerId'));
  }

  getTitle(){
    return this.t('filters:tabs:organizations:title');
  }

  getBox(){
    return (
      <div className="box-content">
        {this.renderChild(ProcuringEntity, 'procuringEntityId')}
        {this.renderChild(Supplier, 'supplierId')}
        {this.renderChild(Buyer, 'buyerId')}
      </div>
    )
  }
}

export default Organizations;
