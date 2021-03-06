package org.devgateway.toolkit.forms.wicket.page.edit.form.prequalification;

import org.apache.wicket.Component;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.devgateway.toolkit.forms.WebConstants;
import org.devgateway.toolkit.forms.wicket.components.form.TextFieldBootstrapFormComponent;
import org.devgateway.toolkit.forms.wicket.components.util.ComponentUtil;
import org.devgateway.toolkit.forms.wicket.page.edit.AbstractEditStatusEntityPage;
import org.devgateway.toolkit.forms.wicket.page.lists.form.prequalification.ListPrequalificationSchemaPage;
import org.devgateway.toolkit.persistence.dao.prequalification.PrequalificationSchema;
import org.devgateway.toolkit.persistence.dao.prequalification.PrequalificationSchema_;
import org.devgateway.toolkit.persistence.service.prequalification.PrequalificationSchemaService;
import org.devgateway.toolkit.web.security.SecurityConstants;
import org.wicketstuff.annotation.mount.MountPath;

@AuthorizeInstantiation(SecurityConstants.Roles.ROLE_ADMIN)
@MountPath
public class EditPrequalificationSchemaPage extends AbstractEditStatusEntityPage<PrequalificationSchema> {

    @SpringBean
    private PrequalificationSchemaService prequalificationSchemaService;

    public EditPrequalificationSchemaPage(PageParameters parameters) {
        super(parameters);
        this.jpaService = prequalificationSchemaService;
        this.listPageClass = ListPrequalificationSchemaPage.class;
    }

    public TextFieldBootstrapFormComponent<String> createNameField() {
        final TextFieldBootstrapFormComponent<String> name = ComponentUtil.addTextField(editForm, "name");
        name.getField().add(WebConstants.StringValidators.MAXIMUM_LENGTH_VALIDATOR_STD_DEFAULT_TEXT);
        ComponentUtil.addUniquePropertyEntryValidator(name.getField(), PrequalificationSchema_.name, jpaService,
                editForm.getModel(), getString("uniqueName"));
        return name;
    }

    public TextFieldBootstrapFormComponent<String> createPrefixField() {
        final TextFieldBootstrapFormComponent<String> prefix = ComponentUtil.addTextField(editForm, "prefix");
        prefix.getField().add(WebConstants.StringValidators.MAXIMUM_LENGTH_VALIDATOR_STD_DEFAULT_TEXT);
        ComponentUtil.addUniquePropertyEntryValidator(prefix.getField(), PrequalificationSchema_.prefix, jpaService,
                editForm.getModel(), getString("uniquePrefix"));
        return prefix;
    }

    @Override
    protected void onInitialize() {
        editForm.attachFm("prequalificationSchemaForm");
        super.onInitialize();
        editForm.add(createNameField());
        editForm.add(createPrefixField());
        editForm.add(new PrequalificationSchemaItemPanel("items"));
    }

    @Override
    protected boolean isViewMode() {
        return false;
    }

    @Override
    protected void addTerminateButtonPermissions(Component button) {
        button.setVisibilityAllowed(false);
    }

    @Override
    protected void addApproveButtonPermissions(Component button) {
        button.setVisibilityAllowed(false);
    }

    @Override
    protected void addSaveNextButtonPermissions(Component button) {
        button.setVisibilityAllowed(false);
    }
}
