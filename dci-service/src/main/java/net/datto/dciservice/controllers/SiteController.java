package net.datto.dciservice.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datto.dci.api.dto.DattoContinuityDevice;
import net.datto.dciservice.dynamodb.AccountMapping;
import net.datto.dciservice.dynamodb.SiteMapping;
import net.datto.dciservice.services.AccountMappingProcessor;
import net.datto.dciservice.services.DciService;
import net.datto.dciservice.services.MetricsService;
import net.datto.dciservice.services.SiteMappingProcessor;
import net.datto.dciservice.utils.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
public class SiteController {
    private final AccountMappingProcessor accountMappingProcessor;
    private final SiteMappingProcessor siteMappingProcessor;
    private final DciService dciService;
    private final MetricsService metricsService;


    /**
     * Save site mappings.
     */
    @PostMapping("/{accountUid}/site-mappings")
    @Operation(summary = "Save site mappings for the authenticated user's account.")
    public ResponseEntity<?> saveSiteMappings(@PathVariable("accountUid") String accountUid, @RequestBody List<SiteMapping> siteMappings) throws Exception {
        log.debug("Started saveSiteMappings call for accountUid: {}, siteUid: {}", accountUid, siteMappings);

        AccountMapping accountMapping = accountMappingProcessor.checkAccountMapping(accountUid);

        for (SiteMapping siteMapping : siteMappings) {
            try {
                DattoContinuityDevice dattoContinuityDevice = metricsService.startTimer(this.getClass(), "saveSiteMappings.getDciDeviceBySerialNumber")
                        .recordCallable(() -> dciService.getDciDeviceBySerialNumber(accountMapping, siteMapping.getSerialNumber()));
                siteMapping.setRmmAccountUid(accountUid);
                siteMapping.setClientCompanyName(dattoContinuityDevice.getClientCompanyName());
                siteMapping.setInternalIp(dattoContinuityDevice.getInternalIpAddress());
                siteMapping.setModel(dattoContinuityDevice.getModel());
                siteMapping.setName(dattoContinuityDevice.getName());
                siteMappingProcessor.saveSiteMapping(siteMapping);
                log.debug("Processed saveSiteMappings call for accountUid: {}, siteUid: {}",
                        siteMapping.getRmmAccountUid(), siteMapping.getRmmSiteUid());
            } catch (BadRequestException e) {
                log.error("Error saving SiteMapping for accountUid: {}, siteUid: {}, serialNumber: {}", accountUid, siteMapping.getRmmSiteUid(), siteMapping.getSerialNumber());
                metricsService.markMeter(this.getClass(), "saveSiteMappings.getDciDeviceBySerialNumber.BadRequestException");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/{accountUid}/site-mappings/{serialNumber}")
    @Operation(summary = "Delete site mapping by serialNumber for the authenticated user's account.")
    public ResponseEntity<?> deleteSiteMappingBySerialNumber(@PathVariable("accountUid") String accountUid, @PathVariable("serialNumber") String serialNumber) throws Exception {

        log.debug("Started deleteSiteMappings call for accountUid: {}, siteUid: {}", accountUid, serialNumber);
        accountMappingProcessor.checkAccountMapping(accountUid);

        siteMappingProcessor.deleteSiteMapping(accountUid, serialNumber);
        log.debug("Processed deleteSiteMappings call for accountUid: {}, serialNumber: {}", accountUid, serialNumber);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Unmap site mappings.
     */
    @DeleteMapping("/{accountUid}/site-mappings")
    @Operation(summary = "Delete site mappings for the authenticated user's account.")
    public ResponseEntity<?> deleteSiteMappings(@PathVariable("accountUid") String accountUid, @RequestBody List<SiteMapping> siteMappings) throws Exception {

        log.debug("Started deleteSiteMappings call for accountUid: {}, siteUid: {}", accountUid, siteMappings);

        accountMappingProcessor.checkAccountMapping(accountUid);

        for (SiteMapping siteMapping : siteMappings) {
            siteMappingProcessor.deleteSiteMapping(accountUid, siteMapping.getSerialNumber());
            log.debug("Processed deleteSiteMappings call for accountUid: {}, siteUid: {}",
                    siteMapping.getRmmAccountUid(), siteMapping.getRmmSiteUid());
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Delete all site mappings in the account
     */
    @DeleteMapping("/{accountUid}/all-site-mappings")
    @Operation(summary = "Delete site mappings for the authenticated user's account.")
    public void deleteSiteMappingsByAccount(@PathVariable("accountUid") String accountUid) throws Exception {
        log.debug("Started deleteSiteMappingsByAccount call for accountUid: {}", accountUid);

        accountMappingProcessor.checkAccountMapping(accountUid);
        siteMappingProcessor.deleteSiteMapping(accountUid);
        log.debug("Processed deleteSiteMappingsByAccount call for accountUid: {}", accountUid);
    }

    /**
     * This method either deletes or recreates RMM devices depending on whether the integration is turned off or on
     */
    @PostMapping("/{accountUid}/integration")
    @Operation(summary = "Creates or deletes RMM devices when the integration is toggled")
    public void togglesIntegration(@PathVariable("accountUid") String accountUid, @RequestParam(value = "enabled") Boolean enabled) {
        log.debug("Started togglesIntegration call for accountUid: {}", accountUid);
        List<SiteMapping> siteMappings = siteMappingProcessor.getSiteMappings(accountUid);
        for (SiteMapping siteMapping : siteMappings) {
            if (enabled) {
                //If integration is turned on and there is already a pre-configured account,
                //for each site add DCI device back to RMM
                siteMappingProcessor.createDciDeviceInRmm(siteMapping);
            } else {
                //If integration is turned off and there is already a pre-configured account,
                //for each site remove DCI device from RMM
                siteMappingProcessor.deleteDciDeviceFromRmm(siteMapping);
            }
        }
        log.debug("Processed togglesIntegration call for accountUid: {}", accountUid);
    }

}
