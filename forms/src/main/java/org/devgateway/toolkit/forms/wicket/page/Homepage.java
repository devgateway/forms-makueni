/**
 * Copyright (c) 2015 Development Gateway, Inc and others.
 * <p>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT License (MIT)
 * which accompanies this distribution, and is available at
 * https://opensource.org/licenses/MIT
 * <p>
 * Contributors:
 * Development Gateway - initial API and implementation
 */
/**
 *
 */
package org.devgateway.toolkit.forms.wicket.page;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.devgateway.toolkit.forms.service.SessionMetadataService;
import org.devgateway.toolkit.forms.wicket.page.overview.status.StatusOverviewPage;
import org.devgateway.toolkit.forms.wicket.styles.HomeStyles;
import org.devgateway.toolkit.web.security.SecurityConstants;
import org.wicketstuff.annotation.mount.MountPath;

/**
 * @author mpostelnicu
 *
 */
@AuthorizeInstantiation(SecurityConstants.Roles.ROLE_USER)
@MountPath(value = "/home")
public class Homepage extends BasePage {
    public Homepage(final PageParameters parameters) {
        super(parameters);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        final Link<Void> dataEntryLink = new Link<Void>("dataEntryLink") {
            @Override
            public void onClick() {
                // clear all session data before going to the dashboard
                SessionMetadataService.clearSessionData();
                setResponsePage(StatusOverviewPage.class);
            }
        };
        add(dataEntryLink);

        final Link<Void> publicPortalLink = new Link<Void>("publicPortalLink") {
            @Override
            public void onClick() {
                setResponsePage(PublicPortalPage.class);
            }

            @Override
            protected void onComponentTag(final ComponentTag tag) {
                super.onComponentTag(tag);
                tag.put("target", "_blank");
            }
        };
        add(publicPortalLink);
        
        final Link<Void> corruptionRiskDashboard = new Link<Void>("corruptionRiskDashboard") {
            @Override
            public void onClick() {
                setResponsePage(CorruptionRiskDashboardPage.class);
            }

            @Override
            protected void onComponentTag(final ComponentTag tag) {
                super.onComponentTag(tag);
                tag.put("target", "_blank");
            }
        };
        add(corruptionRiskDashboard); 

        final Link<Void> dataExport = new Link<Void>("dataExport") {
            @Override
            public void onClick() {
                setResponsePage(DataExportPage.class);
            }
        };
        add(dataExport);

    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);

        // Load Home Page Styles
        response.render(CssHeaderItem.forReference(HomeStyles.INSTANCE));
    }
}
