package org.devgateway.toolkit.forms.wicket.page.edit.form;

import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.devgateway.toolkit.forms.wicket.components.form.GenericSleepFormComponent;
import org.devgateway.toolkit.persistence.dao.form.AbstractTenderProcessMakueniEntity;

/**
 * @author mpostelnicu
 */
public abstract class EditAbstractImplTenderProcessEntity<T extends AbstractTenderProcessMakueniEntity>
        extends EditAbstractTenderProcessMakueniEntity<T> {

    protected Fragment abstractImplExtraFields;

    public EditAbstractImplTenderProcessEntity(PageParameters parameters) {
        super(parameters);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        abstractImplExtraFields = new Fragment("extraReadOnlyFields", "abstractImplExtraFields", this);
        abstractImplExtraFields.add(new GenericSleepFormComponent<>("tenderProcess.tender.iterator.next.tenderTitle"));
        abstractImplExtraFields.add(new GenericSleepFormComponent<>("tenderProcess.singleContract.awardee"));

        final Fragment fragment = new Fragment("childExtraFields", "noChildExtraFields", this);
        abstractImplExtraFields.add(fragment);

        editForm.replace(abstractImplExtraFields);
    }
}