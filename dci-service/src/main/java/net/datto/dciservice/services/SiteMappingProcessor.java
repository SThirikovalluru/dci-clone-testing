package net.datto.dciservice.services;

import lombok.RequiredArgsConstructor;
import net.datto.dci.api.dto.Action;
import net.datto.dci.api.dto.DattoContinuityDevice;
import net.datto.dci.api.dto.DattoContinuityDeviceDto;
import net.datto.dciservice.dynamodb.DynamoDbSiteMappingDao;
import net.datto.dciservice.dynamodb.SiteMapping;
import net.datto.dciservice.queues.DattoContinuityDeviceSqsSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class SiteMappingProcessor {
    private final DynamoDbSiteMappingDao dynamoDbSiteMappingDao;
    private final DattoContinuityDeviceSqsSender dattoContinuityDeviceSqsSender;

    public void saveSiteMapping(SiteMapping siteMapping) {
        if (siteMapping == null) return;
        dynamoDbSiteMappingDao.storeSiteMapping(siteMapping);
        createDciDeviceInRmm(siteMapping);
    }

    public List<SiteMapping> getSiteMappings(String accountUid) {
        return dynamoDbSiteMappingDao.getRmmSitesMapped(accountUid);
    }

    public void deleteSiteMapping(String rmmAccountUid, String serialNumber) {
    	SiteMapping siteMapping = dynamoDbSiteMappingDao.getSiteMapping(rmmAccountUid, serialNumber);
        if (siteMapping != null) {
            deleteDciDeviceFromRmm(siteMapping);
            dynamoDbSiteMappingDao.deleteSiteMapping(siteMapping);
        }
	}

    public void deleteSiteMapping(String rmmAccountUid) {
        dynamoDbSiteMappingDao.deleteSiteMappingByAccountUid(rmmAccountUid);
    }

    public void createDciDeviceInRmm(SiteMapping siteMapping) {
        configureMessageAndSend(siteMapping, Action.CREATE);
    }

    public void deleteDciDeviceFromRmm(SiteMapping siteMapping) {
        configureMessageAndSend(siteMapping, Action.DELETE);
    }

    public void configureMessageAndSend(SiteMapping siteMapping, Action action) {

        var dciDevice = DattoContinuityDevice.builder()
                .rmmSiteUid(siteMapping.getRmmSiteUid())
                .serialNumber(siteMapping.getSerialNumber())
                .name(siteMapping.getName())
                .model(siteMapping.getModel())
                .internalIpAddress(siteMapping.getInternalIp())
                .clientCompanyName(siteMapping.getClientCompanyName())
                .build();

       var deviceNotification = DattoContinuityDeviceDto.builder()
                .action(action)
                .rmmAccountUid(siteMapping.getRmmAccountUid())
                .dciDevice(dciDevice)
                .build();
        dattoContinuityDeviceSqsSender.send(deviceNotification);
    }

}
