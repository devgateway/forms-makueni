package org.devgateway.toolkit.forms.wicket.page;

import com.google.common.collect.ImmutableMap;
import de.agilecoders.wicket.core.markup.html.bootstrap.button.Buttons;
import de.agilecoders.wicket.core.markup.html.bootstrap.form.BootstrapForm;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.ladda.LaddaAjaxLink;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.validation.IFormValidator;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.devgateway.toolkit.forms.WebConstants;
import org.devgateway.toolkit.forms.service.SessionMetadataService;
import org.devgateway.toolkit.forms.wicket.components.form.AJAXDownload;
import org.devgateway.toolkit.forms.wicket.components.form.BootstrapSubmitButton;
import org.devgateway.toolkit.forms.wicket.components.form.FileInputBootstrapFormComponent;
import org.devgateway.toolkit.forms.wicket.components.util.ComponentUtil;
import org.devgateway.toolkit.forms.wicket.page.edit.form.EditProcurementPlanPage;
import org.devgateway.toolkit.persistence.dao.FileMetadata;
import org.devgateway.toolkit.persistence.dao.categories.Department;
import org.devgateway.toolkit.persistence.dao.categories.FiscalYear;
import org.devgateway.toolkit.persistence.dao.categories.Item;
import org.devgateway.toolkit.persistence.dao.categories.ProcurementMethod;
import org.devgateway.toolkit.persistence.dao.categories.TargetGroup;
import org.devgateway.toolkit.persistence.dao.categories.Unit;
import org.devgateway.toolkit.persistence.dao.form.PlanItem;
import org.devgateway.toolkit.persistence.dao.form.ProcurementPlan;
import org.devgateway.toolkit.persistence.service.category.ItemService;
import org.devgateway.toolkit.persistence.service.category.ProcurementMethodService;
import org.devgateway.toolkit.persistence.service.category.TargetGroupService;
import org.devgateway.toolkit.persistence.service.category.UnitService;
import org.devgateway.toolkit.persistence.service.form.ProcurementPlanService;
import org.devgateway.toolkit.web.Constants;
import org.devgateway.toolkit.web.security.SecurityConstants;
import org.wicketstuff.annotation.mount.MountPath;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author mpostelnicu
 */
@MountPath("/importProcurementPlanItems")
@AuthorizeInstantiation(SecurityConstants.Roles.ROLE_PROCUREMENT_USER)
public class ImportProcurementPlanItemsPage extends BasePage {

    @SpringBean
    private ItemService itemService;

    @SpringBean
    private UnitService unitService;

    @SpringBean
    protected SessionMetadataService sessionMetadataService;

    @SpringBean
    private ProcurementMethodService procurementMethodService;

    @SpringBean
    private ProcurementPlanService procurementPlanService;

    @SpringBean
    private TargetGroupService targetGroupService;

    protected BootstrapForm<PlanItemFileUpload> form;

    private Map<String, Integer> colIdx = ImmutableMap.<String, Integer>builder()
            .put("No", 0)
            .put("Item Type", 1)
            .put("Item/Service Code", 2)
            .put("Item/Service Description", 3)
            .put("Estd Cost KES", 4)
            .put("Unit", 5)
            .put("Qty", 6)
            .put("Proc Method", 7)
            .put("Account", 8)
            .put("Youth", 9)
            .put("Women", 10)
            .put("PWD", 11)
            .put("Citizen Contractor", 12)
            .put("1st", 13)
            .put("2nd", 14)
            .put("3rd", 15)
            .put("4th", 16).build();


    /**
     * Construct.
     *
     * @param parameters current page parameters
     */
    public ImportProcurementPlanItemsPage(PageParameters parameters) {
        super(parameters);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        createForm();
        createFileUploadComponent();
        createImportButton();
        addDownloadTemplateLink();

    }

    protected class ExcelFormatValidator implements IFormValidator {
        @Override
        public FormComponent<?>[] getDependentFormComponents() {
            return new FormComponent[0];
        }

        @Override
        public void validate(Form<?> form) {
            Boolean contentTypeCheck = checkExcelContentType();
            if (contentTypeCheck != null && !contentTypeCheck) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("encoding",
                        ImportProcurementPlanItemsPage.this.form
                                .getModelObject().getFiles().iterator().next().getContentType());
                map.put("allowed", String.join(" , ", Constants.ContentType.ALL_XLSX));
                form.error(getString("ExcelContentTypeValidator"), map);
                return;
            }

            Boolean checked = checkExcelFormat();
            if (checked != null && !checked) {
                form.error(getString("ExcelFormatValidator"));
                return;
            }
            try {
                createProcurementPlan();
            } catch (Exception e) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("message", e.getMessage());
                form.error(getString("ExcelImportErrorValidator"), map);
            }
        }
    }

    protected void addDownloadTemplateLink() {
        String fileName = "template-governance-procurement-plan.xlsx";
        AJAXDownload download = ComponentUtil.createAJAXDownload(
                fileName, Constants.ContentType.XLSX, getClass());
        add(download);
        final LaddaAjaxLink<Void> downloadLink = new LaddaAjaxLink<Void>("downloadTemplate", Buttons.Type.Link) {
            @Override
            public void onClick(AjaxRequestTarget target) {
                download.initiate(target);
            }
        };
        downloadLink.setLabel(new StringResourceModel("downloadTemplate", this));
        form.add(downloadLink);
    }

    public Boolean checkExcelContentType() {
        if (form.getModelObject().getFiles().size() == 0) {
            return null;
        }
        FileMetadata file = form.getModelObject().getFiles().iterator().next();
        logger.info("Content type was " + file.getContentType());
        return Constants.ContentType.ALL_XLSX.contains(file.getContentType());
    }

    public Boolean checkExcelFormat() {
        if (form.getModelObject().getFiles().size() == 0) {
            return null;
        }
        FileMetadata file = form.getModelObject().getFiles().iterator().next();
        try {
            Workbook wb = WorkbookFactory.create(new ByteArrayInputStream(file.getContent().getBytes()));
            if (wb.getNumberOfSheets() != 1) {
                return false;
            }

            Sheet sh = wb.getSheetAt(0);
            Iterator<Cell> ci = sh.getRow(7).cellIterator();
            int rn = 0;
            while (ci.hasNext()) {
                Cell c = ci.next();
                Integer integer = colIdx.get(c.getStringCellValue().trim());
                if (integer == null || rn != integer) {
                    return false;
                }
                rn++;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public class PlanItemFileUpload implements Serializable {
        private Set<FileMetadata> files;

        public Set<FileMetadata> getFiles() {
            return files;
        }

        public void setFiles(Set<FileMetadata> files) {
            this.files = files;
        }
    }

    protected ProcurementPlan createProcurementPlan() {
        ProcurementPlan pp = new ProcurementPlan();
        final Department department = sessionMetadataService.getSessionDepartment();
        final FiscalYear fiscalYear = sessionMetadataService.getSessionFiscalYear();
        pp.setFiscalYear(fiscalYear);
        pp.setDepartment(department);

        FileMetadata file = form.getModelObject().getFiles().iterator().next();
        int rn = 0;
        try {
            Workbook wb = WorkbookFactory.create(new ByteArrayInputStream(file.getContent().getBytes()));
            Sheet sh = wb.getSheetAt(0);
            for (Row r : sh) {
                if (rn++ < 7 || isEmptyRow(r)) {
                    continue;
                }
                PlanItem pi = new PlanItem();
                String itemCode = toSingleLine(r.getCell(2).getStringCellValue());
                Item item = itemService.findByCode(itemCode);
                if (item == null) {
                    String itemLabel = toSingleLine(r.getCell(3).getStringCellValue());
                    item = createItem(itemCode, itemLabel);
                }
                pi.setItem(item);
                pp.getPlanItems().add(pi);
                pi.setEstimatedCost(BigDecimal.valueOf(r.getCell(4).getNumericCellValue()));

                String unitName = r.getCell(5).getStringCellValue();
                Unit unit = unitService.findByLabelIgnoreCase(unitName);
                if (unit == null) {
                    String message = new StringResourceModel("import.unknownUnit")
                            .setParameters(unitName)
                            .getString();
                    throw new RuntimeException(message);
                }

                pi.setUnitOfIssue(unit);

                pi.setQuantity(BigDecimal.valueOf(r.getCell(6).getNumericCellValue()));
                pi.setProcurementMethod(getProcurementMethod(r));
                pi.setSourceOfFunds(r.getCell(8).getStringCellValue());
                pi.setTargetGroup(getTargetGroup(r));
                pi.setQuarter1st(BigDecimal.valueOf(r.getCell(13).getNumericCellValue()));
                pi.setQuarter2nd(BigDecimal.valueOf(r.getCell(14).getNumericCellValue()));
                pi.setQuarter3rd(BigDecimal.valueOf(r.getCell(15).getNumericCellValue()));
                pi.setQuarter4th(BigDecimal.valueOf(r.getCell(16).getNumericCellValue()));
            }

        } catch (Exception e) {
            String message = new StringResourceModel("import.exceptionAtRow", this)
                    .setParameters((rn + 2), e.toString())
                    .getString();
            throw new RuntimeException(message);
        }

        return pp;
    }

    private boolean isEmptyRow(Row r) {
        short lastCellNum = r.getLastCellNum();
        for (int col = r.getFirstCellNum(); col < lastCellNum; col++) {
            Cell cell = r.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null) {
                return false;
            }
        }
        return true;
    }

    private String toSingleLine(String value) {
        if (value == null) {
            return null;
        }
        return value.replace('\n', ' ')
                .replace("\r", "");
    }

    private ProcurementMethod getProcurementMethod(Row r) {
        String pmText = r.getCell(7).getStringCellValue().trim();

        ProcurementMethod pm = procurementMethodService.findByLabel(pmText);
        if (pm == null) {
            String message = new StringResourceModel("import.unmappedProcurementMethod", this)
                    .setParameters(pmText)
                    .getString();
            throw new RuntimeException(message);
        }
        return pm;
    }

    private TargetGroup getTargetGroup(Row r) {
        TargetGroup t = null;

        //youth
        if (r.getCell(9).getNumericCellValue() == 100) {
            t = targetGroupService.findById(23L).get();
        }

        //women
        if (r.getCell(10).getNumericCellValue() == 100) {
            t = targetGroupService.findById(24L).get();
        }

        //PWD
        if (r.getCell(11).getNumericCellValue() == 100) {
            t = targetGroupService.findById(25L).get();
        }

        //Citizen Contractor
        if (r.getCell(12).getNumericCellValue() == 100) {
            t = targetGroupService.findById(26L).get();
        }

        return t;
    }

    protected Item createItem(String code, String label) {
        Item item = new Item();
        item.setCode(code);
        item.setLabel(label);
        return itemService.save(item);
    }

    protected void createImportButton() {
        StringResourceModel importModel = new StringResourceModel("import", this);
        BootstrapSubmitButton importButton = new BootstrapSubmitButton("import", importModel) {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                ProcurementPlan procurementPlan = createProcurementPlan();
                ProcurementPlan saved = procurementPlanService.save(procurementPlan);
                PageParameters pp = new PageParameters();
                pp.set(WebConstants.PARAM_ID, saved.getId());
                setResponsePage(EditProcurementPlanPage.class, pp);
            }

            @Override
            protected void onError(AjaxRequestTarget target) {
                target.add(feedbackPanel);
            }

        };
        form.add(importButton);
    }

    protected void createForm() {
        form = new BootstrapForm<>(
                "form", CompoundPropertyModel.of(new PlanItemFileUpload()));
        form.setOutputMarkupId(true);
        form.add(new ExcelFormatValidator());
        add(form);
    }

    protected void createFileUploadComponent() {
        final FileInputBootstrapFormComponent doc = new FileInputBootstrapFormComponent("files");
        doc.maxFiles(1);
        doc.required();
        form.add(doc);
    }
}
