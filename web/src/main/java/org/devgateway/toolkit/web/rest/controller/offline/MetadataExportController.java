package org.devgateway.toolkit.web.rest.controller.offline;

import org.devgateway.toolkit.persistence.service.category.MetadataExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@RestController
public class MetadataExportController {

    @Autowired
    private MetadataExportService metadataExportService;

    @RequestMapping(value = "/api/metadataExport/{userId}",
            method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json")
    @ResponseBody
    public Map<String, List<Serializable>> metadataExport(@PathVariable Long userId) {
        return metadataExportService.getMetadataMap(userId);
    }
}
