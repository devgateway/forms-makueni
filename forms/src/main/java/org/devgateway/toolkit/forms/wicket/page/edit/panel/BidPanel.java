package org.devgateway.toolkit.forms.wicket.page.edit.panel;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.validation.IFormValidator;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.RangeValidator;
import org.devgateway.toolkit.forms.validators.NonZeroBigDecimalValidator;
import org.devgateway.toolkit.forms.wicket.components.ListViewSectionPanel;
import org.devgateway.toolkit.forms.wicket.components.form.GenericSleepFormComponent;
import org.devgateway.toolkit.forms.wicket.components.form.Select2ChoiceBootstrapFormComponent;
import org.devgateway.toolkit.forms.wicket.components.util.ComponentUtil;
import org.devgateway.toolkit.forms.wicket.providers.GenericChoiceProvider;
import org.devgateway.toolkit.persistence.dao.DBConstants;
import org.devgateway.toolkit.persistence.dao.categories.Supplier;
import org.devgateway.toolkit.persistence.dao.form.Bid;
import org.devgateway.toolkit.persistence.dao.form.TenderQuotationEvaluation;
import org.devgateway.toolkit.persistence.service.category.SupplierService;

import java.math.BigDecimal;
import java.util.List;

public class BidPanel extends ListViewSectionPanel<Bid, TenderQuotationEvaluation> {
    @SpringBean
    protected SupplierService supplierService;

    private GenericSleepFormComponent supplierID = null;

    public BidPanel(final String id) {
        super(id);
    }

    protected class ListItemsValidator implements IFormValidator {
        @Override
        public FormComponent<?>[] getDependentFormComponents() {
            return new FormComponent[0];
        }

        @Override
        public void validate(Form<?> form) {
            List<Bid> bids = BidPanel.this.getModelObject();
            if (bids.size() == 0) {
                form.error(getString("atLeastOneBid"));
            }

        }
    }


    @Override
    protected void onInitialize() {
        super.onInitialize();

        final Form form = (Form) getParent();
        if (form != null) {
            form.add(new BidPanel.ListItemsValidator());
        }
    }

    @Override
    public Bid createNewChild(final IModel<TenderQuotationEvaluation> parentModel) {
        final Bid child = new Bid();
        child.setParent(parentModel.getObject());
        child.setExpanded(true);
        child.setEditable(true);

        return child;
    }

    @Override
    public void populateCompoundListItem(final ListItem<Bid> item) {
        Select2ChoiceBootstrapFormComponent<Supplier> supplier = ComponentUtil.addSelect2ChoiceField(item, "supplier",
                supplierService);
        supplier.required();
        supplier.getField().add(new SupplierUpdatingBehavior("change"));
        supplierID = new GenericSleepFormComponent<>("supplierID", (IModel<String>) () -> {
            if (supplier.getModelObject() != null) {
                return supplier.getModelObject().getCode();
            }
            return null;
        });
        supplierID.setOutputMarkupId(true);
        item.add(supplierID);

        ComponentUtil.addBigDecimalField(item, "quotedAmount")
                .getField().add(RangeValidator.minimum(BigDecimal.ZERO), new NonZeroBigDecimalValidator());

        Select2ChoiceBootstrapFormComponent<String> responsiveness = new Select2ChoiceBootstrapFormComponent<>(
                "supplierResponsiveness",
                new GenericChoiceProvider<>(DBConstants.SupplierResponsiveness.ALL_LIST));
        responsiveness.required();
        item.add(responsiveness);
    }

    @Override
    protected boolean filterListItem(final Bid tenderItem) {
        return true;
    }

    private class SupplierUpdatingBehavior extends AjaxFormComponentUpdatingBehavior {
        SupplierUpdatingBehavior(final String event) {
            super(event);
        }

        @Override
        protected void onUpdate(final AjaxRequestTarget target) {
            target.add(supplierID);

        }
    }
}