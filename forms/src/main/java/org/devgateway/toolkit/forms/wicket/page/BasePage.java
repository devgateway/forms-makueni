/*******************************************************************************
 * Copyright (c) 2015 Development Gateway, Inc and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT License (MIT)
 * which accompanies this distribution, and is available at
 * https://opensource.org/licenses/MIT
 *
 * Contributors:
 * Development Gateway - initial API and implementation
 *******************************************************************************/
package org.devgateway.toolkit.forms.wicket.page;

import de.agilecoders.wicket.core.markup.html.bootstrap.behavior.CssClassNameAppender;
import de.agilecoders.wicket.core.markup.html.bootstrap.button.BootstrapBookmarkablePageLink;
import de.agilecoders.wicket.core.markup.html.bootstrap.button.dropdown.MenuBookmarkablePageLink;
import de.agilecoders.wicket.core.markup.html.bootstrap.button.dropdown.MenuDivider;
import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import de.agilecoders.wicket.core.markup.html.bootstrap.html.HtmlTag;
import de.agilecoders.wicket.core.markup.html.bootstrap.image.IconType;
import de.agilecoders.wicket.core.markup.html.bootstrap.navbar.Navbar;
import de.agilecoders.wicket.core.markup.html.bootstrap.navbar.NavbarButton;
import de.agilecoders.wicket.core.markup.html.bootstrap.navbar.NavbarComponents;
import de.agilecoders.wicket.core.markup.html.bootstrap.navbar.NavbarDropDownButton;
import de.agilecoders.wicket.core.markup.html.references.RespondJavaScriptReference;
import de.agilecoders.wicket.core.markup.html.themes.bootstrap.BootstrapCssReference;
import de.agilecoders.wicket.core.util.CssClassNames;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.icon.FontAwesomeCssReference;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.icon.FontAwesomeIconType;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.authroles.authorization.strategies.role.metadata.MetaDataRoleAuthorizationStrategy;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.filter.HeaderResponseContainer;
import org.apache.wicket.markup.html.GenericWebPage;
import org.apache.wicket.markup.html.TransparentWebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.resource.JQueryResourceReference;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.string.StringValue;
import org.devgateway.ocds.forms.wicket.FormSecurityUtil;
import org.devgateway.ocds.web.util.SettingsUtils;
import org.devgateway.toolkit.forms.WebConstants;
import org.devgateway.toolkit.forms.fm.DgFmAttachingVisitor;
import org.devgateway.toolkit.forms.fm.DgFmFormComponentSubject;
import org.devgateway.toolkit.forms.wicket.components.GoogleAnalyticsTracker;
import org.devgateway.toolkit.forms.wicket.page.edit.EditAdminSettingsPage;
import org.devgateway.toolkit.forms.wicket.page.lists.AbstractListPage;
import org.devgateway.toolkit.forms.wicket.page.lists.ListFiscalYearBudgetPage;
import org.devgateway.toolkit.forms.wicket.page.lists.ListFiscalYearPage;
import org.devgateway.toolkit.forms.wicket.page.lists.ListUserPage;
import org.devgateway.toolkit.forms.wicket.page.lists.alerts.ListAlertPage;
import org.devgateway.toolkit.forms.wicket.page.lists.alerts.ListAlertsStatisticsPage;
import org.devgateway.toolkit.forms.wicket.page.lists.category.ListChargeAccountPage;
import org.devgateway.toolkit.forms.wicket.page.lists.category.ListContractDocumentTypePage;
import org.devgateway.toolkit.forms.wicket.page.lists.category.ListDepartmentPage;
import org.devgateway.toolkit.forms.wicket.page.lists.category.ListDesignationPage;
import org.devgateway.toolkit.forms.wicket.page.lists.category.ListItemPage;
import org.devgateway.toolkit.forms.wicket.page.lists.category.ListPMCStaffPage;
import org.devgateway.toolkit.forms.wicket.page.lists.category.ListProcurementMethodRationalePage;
import org.devgateway.toolkit.forms.wicket.page.lists.category.ListProcuringEntityPage;
import org.devgateway.toolkit.forms.wicket.page.lists.category.ListStaffPage;
import org.devgateway.toolkit.forms.wicket.page.lists.category.ListSubWardPage;
import org.devgateway.toolkit.forms.wicket.page.lists.category.ListSubcountyPage;
import org.devgateway.toolkit.forms.wicket.page.lists.category.ListSupplierPage;
import org.devgateway.toolkit.forms.wicket.page.lists.category.ListTargetGroupPage;
import org.devgateway.toolkit.forms.wicket.page.lists.category.ListUnitPage;
import org.devgateway.toolkit.forms.wicket.page.lists.category.ListWardPage;
import org.devgateway.toolkit.forms.wicket.page.lists.feedback.ListFeedbackMessagePage;
import org.devgateway.toolkit.forms.wicket.page.lists.flags.ListFlagHistoryPage;
import org.devgateway.toolkit.forms.wicket.page.lists.fm.ListFeaturesPage;
import org.devgateway.toolkit.forms.wicket.page.lists.form.ListAdministratorReportPage;
import org.devgateway.toolkit.forms.wicket.page.lists.form.ListAwardAcceptancePage;
import org.devgateway.toolkit.forms.wicket.page.lists.form.ListAwardNotificationPage;
import org.devgateway.toolkit.forms.wicket.page.lists.form.ListCabinetPaperPage;
import org.devgateway.toolkit.forms.wicket.page.lists.form.ListContractPage;
import org.devgateway.toolkit.forms.wicket.page.lists.form.ListInspectionReportPage;
import org.devgateway.toolkit.forms.wicket.page.lists.form.ListMEReportPage;
import org.devgateway.toolkit.forms.wicket.page.lists.form.ListPMCReportPage;
import org.devgateway.toolkit.forms.wicket.page.lists.form.ListPaymentVoucherPage;
import org.devgateway.toolkit.forms.wicket.page.lists.form.ListProcurementPlanPage;
import org.devgateway.toolkit.forms.wicket.page.lists.form.ListProfessionalOpinionPage;
import org.devgateway.toolkit.forms.wicket.page.lists.form.ListProjectPage;
import org.devgateway.toolkit.forms.wicket.page.lists.form.ListPurchaseRequisitionGroupPage;
import org.devgateway.toolkit.forms.wicket.page.lists.form.ListTenderPage;
import org.devgateway.toolkit.forms.wicket.page.lists.form.ListTenderQuotationEvaluationPage;
import org.devgateway.toolkit.forms.wicket.page.user.EditUserPage;
import org.devgateway.toolkit.forms.wicket.page.user.LogoutPage;
import org.devgateway.toolkit.forms.wicket.styles.BaseStyles;
import org.devgateway.toolkit.persistence.dao.Person;
import org.devgateway.toolkit.persistence.fm.service.DgFmService;
import org.devgateway.toolkit.web.security.SecurityConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.devgateway.toolkit.web.security.SecurityConstants.Roles.ROLE_ADMIN;
import static org.devgateway.toolkit.web.security.SecurityConstants.Roles.ROLE_PMC_ADMIN;
import static org.devgateway.toolkit.web.security.SecurityConstants.Roles.ROLE_PROCUREMENT_USER;
import static org.devgateway.toolkit.web.security.SecurityConstants.Roles.ROLE_USER;

/**
 * Base wicket-bootstrap {@link org.apache.wicket.Page}
 *
 * @author miha
 */
public abstract class BasePage extends GenericWebPage<Void> implements DgFmFormComponentSubject  {
    private static final long serialVersionUID = -4179591658828697452L;

    protected static final Logger logger = LoggerFactory.getLogger(BasePage.class);

    private TransparentWebMarkupContainer mainContainer;

    private Header mainHeader;

    private Footer mainFooter;

    protected Label pageTitle;

    private Navbar navbar;

    protected NotificationPanel feedbackPanel;

    private GoogleAnalyticsTracker googleAnalyticsTracker;

    @SpringBean
    private SettingsUtils settingsUtils;


    protected void createGoogleAnalyticsTracker() {
        googleAnalyticsTracker = new GoogleAnalyticsTracker(
                "googleAnalyticsTracker", settingsUtils.getGoogleAnalyticsTrackingId());
        googleAnalyticsTracker.setVisibilityAllowed(settingsUtils.getGoogleAnalyticsTrackingId() != null);
        add(googleAnalyticsTracker);
    }

    @SpringBean
    protected DgFmService fmService;

    @Override
    public DgFmService getFmService() {
        return fmService;
    }

    @Override
    public boolean isEnabled() {
        return isFmEnabled(super::isEnabled);
    }

    @Override
    public boolean isVisible() {
        return isFmVisible(super::isVisible);
    }

//    @Override
//    public MarkupContainer add(Component... children) {
//        MarkupContainer ret = super.add(children);
//        attachFmForChildren(children);
//        return ret;
//    }

    /**
     * Determines if this page has a fluid container for the content or not.
     */
    public Boolean fluidContainer() {
        return true;
    }

    public static class HALRedirectPage extends RedirectPage {
        private static final long serialVersionUID = -750983217518258464L;

        public HALRedirectPage() {
            super(WebApplication.get().getServletContext().getContextPath() + "/api/browser/");
        }

    }

    public static class JminixRedirectPage extends RedirectPage {
        private static final long serialVersionUID = -750983217518258464L;

        public JminixRedirectPage() {
            super(WebApplication.get().getServletContext().getContextPath() + "/jminix/");
        }

    }

    public static class UIRedirectPage extends RedirectPage {
        private static final long serialVersionUID = -750983217518258464L;

        public UIRedirectPage() {
            super(WebApplication.get().getServletContext().getContextPath() + "/ui/index.html");
        }
    }

    /**
     * Selects/changes the default language in the current session. If the
     * {@link WebConstants#LANGUAGE_PARAM} is found in the
     * {@link PageParameters} then its contents is set as language in the
     * session object.
     */
    protected void selectLanguage() {
        StringValue lang = this.getPageParameters().get(WebConstants.LANGUAGE_PARAM);
        if (!lang.isEmpty()) {
            WebSession.get().setLocale(new Locale(lang.toString()));
        }
    }

    /**
     * Construct.
     *
     * @param parameters current page parameters
     */
    public BasePage(final PageParameters parameters) {
        super(parameters);

        selectLanguage();

        add(new HtmlTag("html"));

        // Add javascript files.
        add(new HeaderResponseContainer("scripts-container", "scripts-bucket"));

        feedbackPanel = createFeedbackPanel();
        add(feedbackPanel);

        mainContainer = new TransparentWebMarkupContainer("mainContainer");
        add(mainContainer);

        // Set the bootstrap container class.
        // @see https://getbootstrap.com/css/#grid
        if (fluidContainer()) {
            mainContainer.add(new CssClassNameAppender(CssClassNames.Grid.containerFluid));
        } else {
            mainContainer.add(new CssClassNameAppender(CssClassNames.Grid.container));
        }

        mainHeader = new Header("mainHeader", this.getPageParameters());
        add(mainHeader);

        navbar = newNavbar("navbar");
        mainHeader.add(navbar);

        // Add information about navbar position on mainHeader element.
        if (navbar.getPosition().equals(Navbar.Position.DEFAULT)) {
            mainHeader.add(new CssClassNameAppender("with-navbar-default"));
        } else {
            mainHeader.add(new CssClassNameAppender("with-" + navbar.getPosition().cssClassName()));
        }

        mainFooter = new Footer("mainFooter");
        add(mainFooter);

        pageTitle = new Label("pageTitle", new ResourceModel("page.title"));
        add(pageTitle);

        createGoogleAnalyticsTracker();
    }

    private NotificationPanel createFeedbackPanel() {
        final NotificationPanel notificationPanel = new NotificationPanel("feedback");
        notificationPanel.setOutputMarkupId(true);
        return notificationPanel;
    }

    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();
        visitChildren(new DgFmAttachingVisitor());
    }

    public NavbarDropDownButton newLanguageMenu() {
        final NavbarDropDownButton languageDropDown =
                new NavbarDropDownButton(new StringResourceModel("navbar.lang", this, null)) {

                    private static final long serialVersionUID = 319842753824102674L;

                    @Override
                    protected List<AbstractLink> newSubMenuButtons(final String buttonMarkupId) {
                        final List<AbstractLink> list = new ArrayList<>();

                        for (final Locale l : WebConstants.AVAILABLE_LOCALES) {
                            final PageParameters params = new PageParameters(BasePage.this.getPageParameters());
                            params.set(WebConstants.LANGUAGE_PARAM, l.getLanguage());
                            list.add(new MenuBookmarkablePageLink<Page>(BasePage.this.getPageClass(), params,
                                    Model.of(l.getDisplayName())
                            ));
                        }

                        return list;
                    }
                };
        languageDropDown.setIconType(FontAwesomeIconType.flag);
        return languageDropDown;
    }

    private NavbarButton<LogoutPage> newLogoutMenu() {
        // logout menu
        final NavbarButton<LogoutPage> logoutMenu =
                new NavbarButton<LogoutPage>(LogoutPage.class, new StringResourceModel("navbar.logout", this, null));
        logoutMenu.setIconType(FontAwesomeIconType.sign_out);
        MetaDataRoleAuthorizationStrategy.authorize(logoutMenu, Component.RENDER, SecurityConstants.Roles.ROLE_USER);

        return logoutMenu;
    }

    private NavbarButton<EditUserPage> newAccountMenu() {
        final PageParameters pageParametersForAccountPage = new PageParameters();
        final Person person = FormSecurityUtil.getCurrentAuthenticatedPerson();

        // account menu
        Model<String> account = null;
        if (person != null) {
            account = Model.of(person.getFirstName());
            pageParametersForAccountPage.add(WebConstants.PARAM_ID, person.getId());
        }

        final NavbarButton<EditUserPage> accountMenu =
                new NavbarButton<>(EditUserPage.class, pageParametersForAccountPage, account);
        accountMenu.setIconType(FontAwesomeIconType.user);
        MetaDataRoleAuthorizationStrategy.authorize(accountMenu, Component.RENDER, SecurityConstants.Roles.ROLE_USER);

        return accountMenu;
    }

    private NavbarButton<Homepage> newHomeMenu() {
        // home
        final NavbarButton<Homepage> homeMenu = new NavbarButton<>(Homepage.class,
                new StringResourceModel("navbar.home", this));
        homeMenu.setIconType(FontAwesomeIconType.home);
        MetaDataRoleAuthorizationStrategy.authorize(homeMenu, Component.RENDER, SecurityConstants.Roles.ROLE_USER);
        return homeMenu;
    }


    private <L extends AbstractListPage> BootstrapBookmarkablePageLink<L>
    createListMenu(Class<L> clazz, String resourceKey, IconType iconType) {
        return new MenuBookmarkablePageLink<L>(clazz, null,
                new StringResourceModel(resourceKey, this, null))
                .setIconType(iconType);
    }

    private <L extends AbstractListPage> BootstrapBookmarkablePageLink<L>
    createAddListMenu(List<AbstractLink> list, Class<L> clazz, String resourceKey, IconType iconType) {
        BootstrapBookmarkablePageLink<L> menu = new MenuBookmarkablePageLink<L>(clazz, null,
                new StringResourceModel(resourceKey, this, null))
                .setIconType(iconType);
        list.add(menu);
        return menu;
    }

    private <L extends AbstractListPage> void
    createAddListMenuWithRole(List<AbstractLink> list, String role, Class<L> clazz, String resourceKey,
                              IconType iconType) {
        BootstrapBookmarkablePageLink<L> menu = createAddListMenu(list, clazz, resourceKey, iconType);
        MetaDataRoleAuthorizationStrategy.authorize(menu, Component.RENDER, role);
    }


    private NavbarDropDownButton newMetadataMenu() {
        // metadata menu
        final NavbarDropDownButton metadataMenu = new NavbarDropDownButton(
                new StringResourceModel("navbar.metadata", this, null)) {

            @Override
            protected List<AbstractLink> newSubMenuButtons(final String arg0) {
                final List<AbstractLink> list = new ArrayList<>();


                createAddListMenuWithRole(list, ROLE_ADMIN, ListDepartmentPage.class,
                        "navbar.departments", FontAwesomeIconType.bank
                );

                createAddListMenuWithRole(list, ROLE_ADMIN, ListProcurementMethodRationalePage.class,
                        "navbar.procurementMethodRationale", FontAwesomeIconType.bug
                );

                createAddListMenuWithRole(list, ROLE_ADMIN, ListFiscalYearBudgetPage.class,
                        "navbar.fiscalYearBudget", FontAwesomeIconType.money
                );

                createAddListMenuWithRole(list, ROLE_ADMIN, ListFiscalYearPage.class,
                        "navbar.fiscalyear", FontAwesomeIconType.calendar_times_o
                );

                createAddListMenuWithRole(list, ROLE_ADMIN, ListTargetGroupPage.class,
                        "navbar.targetgroup", FontAwesomeIconType.object_group
                );

                createAddListMenuWithRole(list, ROLE_ADMIN, ListContractDocumentTypePage.class,
                        "navbar.ContractDocumentType", FontAwesomeIconType.file
                );

                createAddListMenuWithRole(list, ROLE_ADMIN, ListItemPage.class,
                        "navbar.items", FontAwesomeIconType.list
                );

                createAddListMenuWithRole(list, ROLE_ADMIN, ListChargeAccountPage.class,
                        "navbar.chargeaccounts", FontAwesomeIconType.money
                );

                createAddListMenuWithRole(list, ROLE_PROCUREMENT_USER, ListSupplierPage.class,
                        "navbar.suppliers", FontAwesomeIconType.list
                );

                createAddListMenuWithRole(list, ROLE_ADMIN, ListStaffPage.class,
                        "navbar.stafflist", FontAwesomeIconType.user_times
                );

                createAddListMenuWithRole(list, ROLE_PMC_ADMIN, ListPMCStaffPage.class,
                        "navbar.pmcStaffList", FontAwesomeIconType.user_times
                );

                createAddListMenuWithRole(list, ROLE_PMC_ADMIN, ListDesignationPage.class,
                        "navbar.designations", FontAwesomeIconType.certificate
                );

                createAddListMenuWithRole(list, ROLE_ADMIN, ListProcuringEntityPage.class,
                        "navbar.procuringentitylist", FontAwesomeIconType.list
                );

                createAddListMenuWithRole(list, ROLE_ADMIN, ListSubcountyPage.class,
                        "navbar.subcountylist", FontAwesomeIconType.flag
                );

                createAddListMenuWithRole(list, ROLE_ADMIN, ListWardPage.class,
                        "navbar.wardlist", FontAwesomeIconType.flag
                );

                createAddListMenuWithRole(list, ROLE_ADMIN, ListSubWardPage.class,
                        "navbar.subwardlist", FontAwesomeIconType.flag
                );


                createAddListMenuWithRole(list, ROLE_ADMIN, ListUnitPage.class,
                        "navbar.unitlist", FontAwesomeIconType.list
                );

                return list;
            }
        };

        metadataMenu.setIconType(FontAwesomeIconType.code);
        MetaDataRoleAuthorizationStrategy.authorize(metadataMenu, Component.RENDER, ROLE_PROCUREMENT_USER);
        MetaDataRoleAuthorizationStrategy.authorize(metadataMenu, Component.RENDER, ROLE_PMC_ADMIN);
        return metadataMenu;
    }

    private NavbarDropDownButton newImplementationFormMenu() {
        // form menu
        final NavbarDropDownButton formMenu = new NavbarDropDownButton(
                new StringResourceModel("navbar.implementationForms", this, null)) {

            @Override
            protected List<AbstractLink> newSubMenuButtons(final String arg0) {
                final List<AbstractLink> list = new ArrayList<>();

                addFormMenuItem(list, ListAdministratorReportPage.class,
                        "navbar.administratorReport", "navbar.administratorReport");

                addFormMenuItem(list, ListInspectionReportPage.class,
                        "navbar.inspectionReport", "navbar.inspectionReport");

                addFormMenuItem(list, ListPMCReportPage.class,
                        "navbar.pmcReport", "navbar.pmcReport");

                addFormMenuItem(list, ListMEReportPage.class,
                        "navbar.meReport", "navbar.meReport");

                addFormMenuItem(list, ListPaymentVoucherPage.class,
                        "navbar.paymentVoucher", "navbar.paymentVoucher");

                return list;
            }
        };

        formMenu.setIconType(FontAwesomeIconType.wpforms);
        MetaDataRoleAuthorizationStrategy.authorize(formMenu, Component.RENDER, ROLE_USER);

        return formMenu;
    }

    private NavbarDropDownButton newProcurementFormMenu() {
        // form menu
        final NavbarDropDownButton formMenu = new NavbarDropDownButton(
                new StringResourceModel("navbar.forms", this, null)) {

            @Override
            protected List<AbstractLink> newSubMenuButtons(final String arg0) {
                final List<AbstractLink> list = new ArrayList<>();

                addFormMenuItem(list, ListProcurementPlanPage.class,
                        "navbar.procurementPlan", "navbar.procurementPlan");

                addFormMenuItem(list, ListCabinetPaperPage.class,
                        "navbar.cabinetpapers", "navbar.cabinetPapers");

                addFormMenuItem(list, ListProjectPage.class,
                        "navbar.project", "navbar.project");

                addFormMenuItem(list, ListPurchaseRequisitionGroupPage.class,
                        "navbar.tenderProcess", "navbar.tenderProcess");

                addFormMenuItem(list, ListTenderPage.class,
                        "navbar.tenderdocument", "navbar.tenderDocument");

                addFormMenuItem(list, ListTenderQuotationEvaluationPage.class,
                        "navbar.tenderquotationevaluation", "navbar.tenderQuotationEvaluation");

                addFormMenuItem(list, ListProfessionalOpinionPage.class,
                        "navbar.professionalopinion", "navbar.professionalOpinion");

                addFormMenuItem(list, ListAwardNotificationPage.class,
                        "navbar.awardnotification", "navbar.awardNotification");

                addFormMenuItem(list, ListAwardAcceptancePage.class,
                        "navbar.awardacceptance", "navbar.awardAcceptance");

                addFormMenuItem(list, ListContractPage.class,
                        "navbar.contract", "navbar.contract");

                return list;
            }
        };

        formMenu.setIconType(FontAwesomeIconType.wpforms);
        MetaDataRoleAuthorizationStrategy.authorize(formMenu, Component.RENDER, ROLE_USER);

        return formMenu;
    }

    private <T extends BasePage> void addFormMenuItem(List<AbstractLink> list, Class<T> pageClass, String resourceKey,
            String featureName) {
        StringResourceModel label = new StringResourceModel(resourceKey, this);
        MenuBookmarkablePageLink<T> link = new MenuBookmarkablePageLink<>(pageClass, label);
        link.setIconType(FontAwesomeIconType.file_text_o);

        if (fmService.isFeatureVisible(featureName)) {
            list.add(link);
        }
    }

    private NavbarDropDownButton newAdminMenu() {
        // admin menu
        final NavbarDropDownButton adminMenu = new NavbarDropDownButton(
                new StringResourceModel("navbar.admin", this, null)) {
            @Override
            protected List<AbstractLink> newSubMenuButtons(final String arg0) {
                final List<AbstractLink> list = new ArrayList<>();
                BootstrapBookmarkablePageLink<ListUserPage> users =
                        new MenuBookmarkablePageLink<ListUserPage>(ListUserPage.class, null,
                                new StringResourceModel("navbar.users", this, null))
                                .setIconType(FontAwesomeIconType.users);
                FormSecurityUtil.authorizeRender(users, ROLE_PMC_ADMIN, ROLE_ADMIN);
                list.add(users);

                /*
                list.add(new MenuBookmarkablePageLink<ListTestFormPage>(ListTestFormPage.class, null,
                        new StringResourceModel("navbar.testcomponents", this, null))
                        .setIconType(FontAwesomeIconType.android));

                list.add(new MenuDivider());

                final BootstrapBookmarkablePageLink swagger = new MenuBookmarkablePageLink<Void>(
                        SwaggerPage.class,
                        new StringResourceModel("navbar.swagger", BasePage.this, null))
                        .setIconType(FontAwesomeIconType.code);
                MetaDataRoleAuthorizationStrategy.authorize(swagger, Component.RENDER,
                        SecurityConstants.Roles.ROLE_ADMIN);
                list.add(swagger);

                final BootstrapBookmarkablePageLink javamelody = new MenuBookmarkablePageLink<Void>(
                        JavamelodyPage.class, new StringResourceModel("navbar.javamelody",
                        BasePage.this, null)).setIconType(FontAwesomeIconType.eye);
                MetaDataRoleAuthorizationStrategy.authorize(javamelody, Component.RENDER,
                        SecurityConstants.Roles.ROLE_ADMIN);
                list.add(javamelody);

                list.add(new MenuBookmarkablePageLink<SpringEndpointsPage>(SpringEndpointsPage.class, null,
                        new StringResourceModel("navbar.springendpoints", this, null))
                        .setIconType(FontAwesomeIconType.anchor));

                list.add(new MenuBookmarkablePageLink<JminixRedirectPage>(JminixRedirectPage.class, null,
                        new StringResourceModel("navbar.jminix", this, null))
                        .setIconType(FontAwesomeIconType.bug));

                final MenuBookmarkablePageLink<HalBrowserHALRedirectPage> halBrowserLink =
                        new MenuBookmarkablePageLink<HALRedirectPage>(HALRedirectPage.class, null,
                                new StringResourceModel("navbar.halbrowser", this, null)) {
                            private static final long serialVersionUID = 1L;

                            @Override
                            protected void onComponentTag(final ComponentTag tag) {
                                super.onComponentTag(tag);
                                tag.put("target", "_blank");
                            }
                        };
                halBrowserLink.setIconType(FontAwesomeIconType.rss).setEnabled(true);

                list.add(halBrowserLink);

                final MenuBookmarkablePageLink<UIRedirectPage> uiBrowserLink =
                        new MenuBookmarkablePageLink<UIRedirectPage>(
                                UIRedirectPage.class, null, new StringResourceModel("navbar.ui", this, null)) {
                            private static final long serialVersionUID = 1L;

                            @Override
                            protected void onComponentTag(final ComponentTag tag) {
                                super.onComponentTag(tag);
                                tag.put("target", "_blank");
                            }
                        };
                uiBrowserLink.setIconType(FontAwesomeIconType.rocket).setEnabled(true);
                list.add(uiBrowserLink);
                */
                list.add(new MenuDivider());

                BootstrapBookmarkablePageLink<Void> adminSettings = new MenuBookmarkablePageLink<Void>(
                        EditAdminSettingsPage.class,
                        new StringResourceModel("navbar.adminSettings", BasePage.this, null)
                ).setIconType(FontAwesomeIconType.briefcase);
                MetaDataRoleAuthorizationStrategy.authorize(adminSettings, Component.RENDER, ROLE_ADMIN);
                list.add(adminSettings);

                BootstrapBookmarkablePageLink<ListAlertPage> feedbackMessages
                        = new MenuBookmarkablePageLink<ListAlertPage>(ListFeedbackMessagePage.class,
                        new StringResourceModel("navbar.feedbackMessages", BasePage.this, null)
                ).setIconType(FontAwesomeIconType.warning);
                MetaDataRoleAuthorizationStrategy.authorize(feedbackMessages, Component.RENDER, ROLE_ADMIN);
                list.add(feedbackMessages);


                BootstrapBookmarkablePageLink<ListAlertPage> navbarAlerts = new MenuBookmarkablePageLink<ListAlertPage>(
                        ListAlertPage.class,
                        new StringResourceModel("navbar.alerts", BasePage.this, null)
                ).setIconType(FontAwesomeIconType.envelope);
                MetaDataRoleAuthorizationStrategy.authorize(navbarAlerts, Component.RENDER, ROLE_ADMIN);
                list.add(navbarAlerts);

                BootstrapBookmarkablePageLink<ListFlagHistoryPage> redFlagHistory =
                        new MenuBookmarkablePageLink<ListFlagHistoryPage>(ListFlagHistoryPage.class,
                                new StringResourceModel("navbar.redFlagHistory", BasePage.this, null)
                        ).setIconType(FontAwesomeIconType.flag);
                MetaDataRoleAuthorizationStrategy.authorize(redFlagHistory, Component.RENDER, ROLE_ADMIN);
                list.add(redFlagHistory);

                BootstrapBookmarkablePageLink<ListAlertsStatisticsPage> alertsStatistics =
                        new MenuBookmarkablePageLink<ListAlertsStatisticsPage>(ListAlertsStatisticsPage.class,
                                new StringResourceModel("navbar.alertsStatistics", BasePage.this, null)
                        ).setIconType(FontAwesomeIconType.mail_reply_all);
                MetaDataRoleAuthorizationStrategy.authorize(alertsStatistics, Component.RENDER, ROLE_ADMIN);
                list.add(alertsStatistics);

                if (WebApplication.get().usesDevelopmentConfig()) {
                    BootstrapBookmarkablePageLink<ListFeaturesPage> features =
                            new MenuBookmarkablePageLink<ListFeaturesPage>(ListFeaturesPage.class,
                                    new StringResourceModel("navbar.features", BasePage.this, null)
                            ).setIconType(FontAwesomeIconType.list);
                    MetaDataRoleAuthorizationStrategy.authorize(features, Component.RENDER, ROLE_ADMIN);
                    list.add(features);
                }

                return list;
            }
        };

        adminMenu.setIconType(FontAwesomeIconType.cog);
        FormSecurityUtil.authorizeRender(adminMenu, ROLE_ADMIN, ROLE_PMC_ADMIN);
        return adminMenu;
    }

    /**
     * creates a new {@link Navbar} instance
     *
     * @param markupId The components markup id.
     * @return a new {@link Navbar} instance
     */
    protected Navbar newNavbar(final String markupId) {
        final Navbar navbar = new Navbar(markupId);

        /**
         * Make sure to update the BaseStyles when the navbar position changes.
         *
         * @see org.devgateway.toolkit.forms.wicket.styles.BaseStyles
         */
        navbar.setPosition(Navbar.Position.TOP);
        navbar.setInverted(true);

        // add brand image
        navbar.setBrandImage(new PackageResourceReference(BaseStyles.class, "assets/img/logo.png"),
                new StringResourceModel("brandImageAltText", this, null));
        navbar.setBrandName(new StringResourceModel("brandName", this, null));

        navbar.addComponents(
                NavbarComponents.transform(Navbar.ComponentPosition.RIGHT, /*newHomeMenu(),*/ newProcurementFormMenu(),
                        newImplementationFormMenu(),
                        newMetadataMenu(), newAdminMenu(), newAccountMenu(), newLogoutMenu()
                ));

        // navbar.addComponents(NavbarComponents.transform(Navbar.ComponentPosition.LEFT, newLanguageMenu()));

        return navbar;
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);

        // Load Styles.
        response.render(CssHeaderItem.forReference(BaseStyles.INSTANCE));
        response.render(CssHeaderItem.forReference(BootstrapCssReference.instance()));
        response.render(CssHeaderItem.forReference(FontAwesomeCssReference.instance()));

        // Load Scripts.
        response.render(RespondJavaScriptReference.headerItem());
        response.render(JavaScriptHeaderItem.forReference(JQueryResourceReference.getV2()));

        // file upload improvement script
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(
                BaseStyles.class, "assets/js/fileupload.js")));
    }
}
