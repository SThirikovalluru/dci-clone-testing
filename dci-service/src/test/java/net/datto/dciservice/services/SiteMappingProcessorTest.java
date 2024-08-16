package net.datto.dciservice.services;

import net.datto.dci.api.dto.DattoContinuityDevice;
import net.datto.dci.api.dto.DattoContinuityDeviceDto;
import net.datto.dciservice.dynamodb.DynamoDbSiteMappingDao;
import net.datto.dciservice.dynamodb.SiteMapping;
import net.datto.dciservice.queues.DattoContinuityDeviceSqsSender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static net.datto.dci.api.dto.Action.CREATE;
import static net.datto.dci.api.dto.Action.DELETE;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SiteMappingProcessorTest {
    private static final SiteMapping SITE_MAPPING = SiteMapping.builder()
            .rmmAccountUid("1")
            .serialNumber("45369874")
            .rmmSiteUid("741")
            .name("name")
            .model("schelby")
            .internalIp("127.1.0.8")
            .clientCompanyName("kaseya")
            .build();

    @Mock
    private DynamoDbSiteMappingDao dynamoDbSiteMappingDaoMock;
    @Mock
    private DattoContinuityDeviceSqsSender dattoContinuityDeviceSqsSenderMock;
    @InjectMocks
    private SiteMappingProcessor siteMappingProcessor;

    @Test
    void shouldNotSaveMapping() {
        // when
        siteMappingProcessor.saveSiteMapping(null);

        // then
        verifyNoInteractions(dynamoDbSiteMappingDaoMock, dattoContinuityDeviceSqsSenderMock);
    }

    @Test
    void shouldSaveMapping() {
        // when
        siteMappingProcessor.saveSiteMapping(SITE_MAPPING);

        // then
        var dattoContinuityDevice = DattoContinuityDeviceDto.builder()
                .action(CREATE)
                .rmmAccountUid(SITE_MAPPING.getRmmAccountUid())
                .dciDevice(DattoContinuityDevice.builder()
                        .serialNumber(SITE_MAPPING.getSerialNumber())
                        .name(SITE_MAPPING.getName())
                        .model(SITE_MAPPING.getModel())
                        .internalIpAddress(SITE_MAPPING.getInternalIp())
                        .clientCompanyName(SITE_MAPPING.getClientCompanyName())
                        .rmmSiteUid(SITE_MAPPING.getRmmSiteUid())
                        .build())
                .build();

        verify(dynamoDbSiteMappingDaoMock).storeSiteMapping(SITE_MAPPING);
        verify(dattoContinuityDeviceSqsSenderMock).send(dattoContinuityDevice);
    }

    @Test
    void shouldNotDeleteSiteMapping() {
        // given
        var invalidAccountUid = "invalid";
        var invalidSerialNumber = "number";

        // when
        siteMappingProcessor.deleteSiteMapping(invalidAccountUid, invalidSerialNumber);

        // then
        verify(dynamoDbSiteMappingDaoMock, never()).deleteSiteMapping(SITE_MAPPING);
    }

    @Test
    void shouldDeleteSiteMapping() {
        // given
        var accountUid = "accountId";
        var serialNumber = "number";

        when(dynamoDbSiteMappingDaoMock.getSiteMapping(accountUid, serialNumber)).thenReturn(SITE_MAPPING);

        // when
        siteMappingProcessor.deleteSiteMapping(accountUid, serialNumber);

        // then
        var dattoContinuityDevice = DattoContinuityDeviceDto.builder()
                .action(DELETE)
                .rmmAccountUid(SITE_MAPPING.getRmmAccountUid())
                .dciDevice(DattoContinuityDevice.builder()
                        .serialNumber(SITE_MAPPING.getSerialNumber())
                        .name(SITE_MAPPING.getName())
                        .model(SITE_MAPPING.getModel())
                        .internalIpAddress(SITE_MAPPING.getInternalIp())
                        .clientCompanyName(SITE_MAPPING.getClientCompanyName())
                        .rmmSiteUid(SITE_MAPPING.getRmmSiteUid())
                        .build())
                .build();

        verify(dynamoDbSiteMappingDaoMock).deleteSiteMapping(SITE_MAPPING);
        verify(dattoContinuityDeviceSqsSenderMock).send(dattoContinuityDevice);
    }
}