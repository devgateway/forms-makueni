/**
 *
 */
package org.devgateway.toolkit.forms.wicket.page.edit.category;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.devgateway.toolkit.forms.wicket.components.form.TextFieldBootstrapFormComponent;
import org.devgateway.toolkit.forms.wicket.components.util.ComponentUtil;
import org.devgateway.toolkit.forms.wicket.page.lists.category.ListPMCStaffPage;
import org.devgateway.toolkit.persistence.dao.categories.PMCStaff;
import org.devgateway.toolkit.persistence.service.category.PMCStaffService;
import org.wicketstuff.annotation.mount.MountPath;

import static org.devgateway.toolkit.web.security.SecurityConstants.Roles.ROLE_IMPLEMENTATION_USER;
import static org.devgateway.toolkit.web.security.SecurityConstants.Roles.ROLE_PMC_ADMIN;
import static org.devgateway.toolkit.web.security.SecurityConstants.Roles.ROLE_PMC_VALIDATOR;

/**
 * @author mpostlenicu
 *
 */
@AuthorizeInstantiation({ROLE_PMC_ADMIN, ROLE_PMC_VALIDATOR, ROLE_IMPLEMENTATION_USER })
@MountPath
public class EditPMCStaffPage extends AbstractCategoryEditPage<PMCStaff> {
    @SpringBean
    private PMCStaffService staffService;

    public EditPMCStaffPage(final PageParameters parameters) {
        super(parameters);
        jpaService = staffService;
        listPageClass = ListPMCStaffPage.class;
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        final TextFieldBootstrapFormComponent<String> phone = ComponentUtil.addTextField(editForm, "phone");
        phone.required();
    }
}
