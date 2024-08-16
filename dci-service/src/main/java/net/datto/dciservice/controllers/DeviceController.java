package net.datto.dciservice.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datto.dci.api.dto.DattoContinuityDevice;
import net.datto.dci.api.dto.DeviceResponse;
import net.datto.dci.api.dto.agent.AgentDto;
import net.datto.dciservice.dynamodb.AccountMapping;
import net.datto.dciservice.dynamodb.DeviceMapping;
import net.datto.dciservice.dynamodb.DeviceMappingProcessor;
import net.datto.dciservice.dynamodb.SiteMapping;
import net.datto.dciservice.services.AccountMappingProcessor;
import net.datto.dciservice.services.DciService;
import net.datto.dciservice.services.MetricsService;
import net.datto.dciservice.services.SiteMappingProcessor;
import net.datto.dciservice.utils.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Slf4j
@RequiredArgsConstructor
@RestController
public class DeviceController {
    private final AccountMappingProcessor accountMappingProcessor;
    private final SiteMappingProcessor siteMappingProcessor;
    private final DeviceMappingProcessor deviceMappingProcessor;
    private final DciService dciService;
    private final MetricsService metricsService;


    /**
     * Would return the following: (if its mapped rmmSiteUid is populated, it its not mapped rmmSiteUid is null)
     * <p>
     * [
     * {"rmmSiteUid":"2381f255-8d8c-42ba-942a-a22e9753b750","serialNumber":"00012E94105B","name":"TheOnlyWayIsUp",
     * "model":"S4X1","internalIP":"10.9.70.71","clientCompanyName":"Freddie, Inc"},
     * {"rmmSiteUid":null,"serialNumber":"54B203707425","name":"UpUpAndAway",
     * "model":"L3A2000","internalIP":"10.9.70.79","clientCompanyName":"Freddie, Inc"}]
     *
     * @param accountUid
     * @return
     * @throws NotFoundException
     * @throws Exception
     */
    @GetMapping("/{accountUid}/devices")
    @Operation(summary = "Fetches all Datto Continuity Devices that are mapped/unmapped for the authenticated user's account.")
    public ResponseEntity<List<DattoContinuityDevice>> getAllDattoContinuityDevices(@PathVariable("accountUid") String accountUid,
                                                                                    @RequestParam(value = "mapped", required = false) Boolean mapped) throws Exception {
        log.debug("Started getAllDattoContinuityDevice call for accountUid: {}", accountUid);

        AccountMapping accountMapping = accountMappingProcessor.checkAccountMapping(accountUid);

        //Get all Datto Continuity Devices currently mapped
        List<SiteMapping> allDevices = siteMappingProcessor.getSiteMappings(accountUid);

        String pageNumber = "1";
        String pageSize = "100";
        //Get all Datto Continuity Devices
        List<DattoContinuityDevice> dattoContinuityDevices = metricsService.startTimer(this.getClass(), "getDciDevices")
                .recordCallable(() -> dciService.getDciDevices(accountMapping, pageNumber, pageSize).getItems());

        //Do filtering so sets the siteUids to the ones that are mapped
        Iterator<DattoContinuityDevice> iterator = dattoContinuityDevices.iterator();
        while (iterator.hasNext()) {
            DattoContinuityDevice dattoContinuityDevice = iterator.next();
            Optional<SiteMapping> siteMapping = allDevices.stream()
                    .filter(o -> o.getSerialNumber().equals(dattoContinuityDevice.getSerialNumber()))
                    .findAny();
            //If unmapped ones are only required
            if (mapped != null && !mapped && !siteMapping.isEmpty()) {
                iterator.remove();
            } else if (mapped != null && mapped && siteMapping.isEmpty()) {
                //If mapped ones are only required
                iterator.remove();
            } else if (!siteMapping.isEmpty()) {
                //If all are required
                dattoContinuityDevice.setRmmSiteUid(siteMapping.get().getRmmSiteUid());
            }
        }
        // Do one more check, loop around what we have currently mapped to see to see if
        // that device in siteMapping actually still exists in BCDR
        for (SiteMapping siteMapping : allDevices) {
            try {
                if (dattoContinuityDevices.stream()
                        .noneMatch(it -> siteMapping.getSerialNumber().equals(it.getSerialNumber()))) {
                    log.info(
                            "Device with serial number {}, siteUid {}, accountUid {} doesn't exist in BCDR - initiating deleting",
                            siteMapping.getSerialNumber(), siteMapping.getRmmSiteUid(), siteMapping.getRmmAccountUid());
                    siteMappingProcessor.deleteSiteMapping(siteMapping.getRmmAccountUid(), siteMapping.getSerialNumber());
                }
            } catch (NullPointerException e) {
                log.error("Unable to delete siteMapping {}", siteMapping);
                metricsService.markMeter(this.getClass(), "deleteSiteMapping.NullPointerException");
            }
        }
        log.debug("Processed getAllDattoContinuityDevice call for accountUid: {}", accountUid);
        return ResponseEntity.status(HttpStatus.OK).body(dattoContinuityDevices);
    }

    /**
     * Gets a page of Datto Continuity Devices from the BCDR API
     * <p>
     * If device is mapped then rmmSiteUid is populated, it its not mapped rmmSiteUid is null
     * [
     * {"rmmSiteUid":"2381f255-8d8c-42ba-942a-a22e9753b750","serialNumber":"00012E94105B","name":"TheOnlyWayIsUp",
     * "model":"S4X1","internalIP":"10.9.70.71","clientCompanyName":"Freddie, Inc"},
     * {"rmmSiteUid":null,"serialNumber":"54B203707425","name":"UpUpAndAway",
     * "model":"L3A2000","internalIP":"10.9.70.79","clientCompanyName":"Freddie, Inc"}]
     *
     * @param accountUid
     * @param page
     * @param size
     * @return
     * @throws Exception
     */
    @GetMapping("/v2/{accountUid}/devices")
    @Operation(summary = "Fetches all and paginate of Datto Continuity Devices that are mapped/unmapped for the authenticated user's account.")
    public ResponseEntity<DeviceResponse<DattoContinuityDevice>> getAllDattoContinuityDeviceV2(@PathVariable("accountUid") String accountUid, @RequestParam(value = "page", required = false, defaultValue = "1") String page, @RequestParam(value = "size", required = false, defaultValue = "50") String size) throws Exception {
        AccountMapping accountMapping = accountMappingProcessor.checkAccountMapping(accountUid);
        List<SiteMapping> siteMappings = siteMappingProcessor.getSiteMappings(accountUid);
        Map<String, String> deviceSiteMap = siteMappings.stream().collect(Collectors.toMap(SiteMapping::getSerialNumber, SiteMapping::getRmmSiteUid));

        DeviceResponse<DattoContinuityDevice> deviceResponse = dciService.getDciDevices(accountMapping, page, size);
        List<DattoContinuityDevice> dciDevices = deviceResponse.getItems();
        dciDevices.forEach(device -> device.setRmmSiteUid(deviceSiteMap.get(device.getSerialNumber())));

        Map<String, String> orphanedDeviceSiteMap = deviceSiteMap;
        dciDevices.forEach(device -> orphanedDeviceSiteMap.remove(device.getSerialNumber())); // Removing siteMappings for existing devices, to get orphaned mappings
        if (orphanedDeviceSiteMap.size() > 0) {
            List<DattoContinuityDevice> orphanedDCiDevices = new ArrayList<>();
            String pageNumber = "1";
            DeviceResponse<DattoContinuityDevice> devices = dciService.getDciDevices(accountMapping, pageNumber, String.valueOf(deviceResponse.getPagination().getCount()));
            devices.getItems().forEach(device -> orphanedDeviceSiteMap.remove(device.getSerialNumber())); // Removing siteMappings for all existing devices, to get orphaned mappings

            for (SiteMapping siteMapping : siteMappings) {
                if (orphanedDeviceSiteMap.containsKey(siteMapping.getSerialNumber())) { // Creating orphaned DCI devices only for orphaned mappings
                    DattoContinuityDevice device = new DattoContinuityDevice();
                    device.setRmmSiteUid(siteMapping.getRmmSiteUid());
                    device.setSerialNumber(siteMapping.getSerialNumber());
                    device.setName(siteMapping.getName());
                    device.setModel(siteMapping.getModel());
                    device.setInternalIpAddress(siteMapping.getInternalIp());
                    device.setClientCompanyName(siteMapping.getClientCompanyName());
                    orphanedDCiDevices.add(device);
                }
            }

            if (orphanedDCiDevices.size() > 0) {
                int responsePage = deviceResponse.getPagination().getPage();
                int responsePerPage = deviceResponse.getPagination().getPerPage();
                int responseItemsSize = deviceResponse.getItems().size();
                int responseTotalPages = deviceResponse.getPagination().getTotalPages();
                int responseDevicesCount = deviceResponse.getPagination().getCount();

                if (responseItemsSize < responsePerPage) {   // We will add orphaned items at the end of page/pages (RMM-13679)
                    if (responsePage == responseTotalPages) {
                        if ((responsePerPage - responseItemsSize) < orphanedDCiDevices.size()) {
                            deviceResponse.getItems().addAll(orphanedDCiDevices.subList(0, (responsePerPage - responseItemsSize)));
                        } else {
                            deviceResponse.getItems().addAll(orphanedDCiDevices);
                        }
                    } else {
                        int startIndex = (responseTotalPages * responsePerPage - responseDevicesCount) + (responsePage - responseTotalPages - 1) * responsePerPage;
                        int endIndex = (startIndex + responsePerPage) <= orphanedDCiDevices.size() ? (startIndex + responsePerPage) : orphanedDCiDevices.size();
                        if (startIndex >= 0 && startIndex < orphanedDCiDevices.size()) {
                            deviceResponse.getItems().addAll(orphanedDCiDevices.subList(startIndex, endIndex));
                        }
                    }
                }

                int newDevicesCount = deviceResponse.getPagination().getCount() + orphanedDCiDevices.size();
                int newTotalPages = (int) Math.ceil((float) newDevicesCount / responsePerPage);
                deviceResponse.getPagination().setCount(newDevicesCount);
                deviceResponse.getPagination().setTotalPages(newTotalPages);
            }
        }

        log.debug("Processed getAllDattoContinuityDeviceV2 call for accountUid: {}", accountUid);
        return ResponseEntity.status(HttpStatus.OK).body(deviceResponse);
    }

    /**
     * API to get all agents/protected devices of Dci Device based on its Serial Number.
     * This API is used by CSM.
     *
     * @param accountUid   RMM account Uid
     * @param serialNumber Datto Continuity Device Serial Number
     * @param hostName     field used to determine protected devices
     * @param internalIp   field used to determine protected devices
     * @return
     * @throws Exception
     */
    @GetMapping("/{accountUid}/devices/{serialNumber}/agents")
    @Operation(summary = "Fetches all agents/protected devices of Dci Device based on its Serial Number for the authenticated user's account.")
    public ResponseEntity<List<AgentDto>> getAgentsBySerialNumber(@PathVariable("accountUid") String accountUid, @PathVariable("serialNumber") String serialNumber, @RequestParam(required = false) String hostName, @RequestParam(required = false) String internalIp) throws Exception {
        log.debug("Started getAgentsBySerialNumber call for accountUid: {}", accountUid);

        AccountMapping accountMapping = accountMappingProcessor.checkAccountMapping(accountUid);
        String pageNumber = "1";
        String pageSize = "200";
        List<AgentDto> agents = metricsService.startTimer(this.getClass(), "getAgentsBySerialNumber.getDeviceAgents")
                .recordCallable(() -> dciService.getDeviceAgents(accountMapping, serialNumber, pageNumber, pageSize).getItems());

        agents = dciService.excludeDuplicatedAgents(agents);

        // Getting protected devices from agents list by filtering (name=hostName) and (localIp=internalIp)
        if (isNotEmpty(hostName) && isNotEmpty(internalIp)) {
            List<AgentDto> filteredAgents = agents.stream().filter(agent -> hostName.equalsIgnoreCase(agent.getName()) && internalIp.equals(agent.getLocalIp())).collect(Collectors.toList());
            return ResponseEntity.status(HttpStatus.OK).body(filteredAgents);
        }
        log.debug("Processed getAgentsBySerialNumber call for accountUid: {}", accountUid);
        return ResponseEntity.status(HttpStatus.OK).body(agents);
    }

    /**
     * API to get agents page of Dci Device based on its Serial Number.
     * This API is used by FE-API.
     *
     * @param accountUid
     * @param serialNumber
     * @param page
     * @param size
     * @return
     * @throws Exception
     */
    @GetMapping("/v2/{accountUid}/devices/{serialNumber}/agents")
    @Operation(summary = "Fetches agents page of Dci Device based on its Serial Number for the authenticated user's account.")
    public ResponseEntity<DeviceResponse<AgentDto>> getAgentsBySerialNumberV2(@PathVariable("accountUid") String accountUid, @PathVariable("serialNumber") String serialNumber, @RequestParam(value = "page", required = false, defaultValue = "1") String page, @RequestParam(value = "size", required = false, defaultValue = "50") String size) throws Exception {
        log.debug("Started getAgentsBySerialNumberV2 call for accountUid: {}", accountUid);

        AccountMapping accountMapping = accountMappingProcessor.checkAccountMapping(accountUid);

        DeviceResponse<AgentDto> deviceResponse = metricsService.startTimer(this.getClass(), "getAgentsBySerialNumberV2.getDeviceAgents")
                .recordCallable(() -> dciService.getDeviceAgents(accountMapping, serialNumber, page, size));

        log.debug("Processed getAgentsBySerialNumberV2 call for accountUid: {}", accountUid);
        return ResponseEntity.status(HttpStatus.OK).body(deviceResponse);
    }

    /**
     * API to get protected device of Dci Device based on its Serial Number.
     *
     * @param accountUid   RMM account Uid
     * @param serialNumber Datto Continuity Device Serial Number
     * @param hostName     field used to determine protected device
     * @param internalIp   field used to determine protected device
     * @return
     * @throws Exception
     */
    @GetMapping("/{accountUid}/devices/{serialNumber}/protected")
    @Operation(summary = "Fetches protected device of DCI Device based on its Serial Number for the authenticated user's account.")
    public ResponseEntity<AgentDto> getProtectedDeviceBySerialNumber(@PathVariable("accountUid") String accountUid, @PathVariable("serialNumber") String serialNumber, @RequestParam String hostName, @RequestParam String internalIp) throws Exception {
        log.debug("Started getProtectedDeviceBySerialNumber call for accountUid: {}", accountUid);

        AccountMapping accountMapping = accountMappingProcessor.checkAccountMapping(accountUid);

        DeviceResponse<AgentDto> deviceResponse;
        AgentDto protectedDevice;
        Integer pageNumber = 0;
        String bcdrPageSize = "200";
        do {
            String bcdrPageNumber = String.valueOf(++pageNumber);
            deviceResponse = metricsService.startTimer(this.getClass(), "getProtectedDeviceBySerialNumber.getDeviceAgents")
                    .recordCallable(() -> dciService.getDeviceAgents(accountMapping, serialNumber, bcdrPageNumber, bcdrPageSize));
            List<AgentDto> agents = deviceResponse.getItems();
            protectedDevice = agents.stream().filter(agent -> hostName.equalsIgnoreCase(agent.getName()) && Pattern.compile("\\b" + Pattern.quote(agent.getLocalIp()) + "\\b").matcher(internalIp).find())
                    .findFirst().orElse(null);
        } while (pageNumber < deviceResponse.getPagination().getTotalPages() && protectedDevice == null);

        log.debug("Processed getProtectedDeviceBySerialNumber call for accountUid: {}", accountUid);
        return ResponseEntity.status(HttpStatus.OK).body(protectedDevice);
    }

    /**
     * API to create/update mapped devices.
     *
     * @param deviceMapping
     * @return
     * @throws Exception
     */
    @PostMapping("/{accountUid}/devices/mapped-devices")
    @Operation(summary = "Save mapped Dci device to protected device for the authenticated user's account.")
    public ResponseEntity<Object> mapDciToProtectedDevice(@PathVariable("accountUid") String accountUid, @RequestBody DeviceMapping deviceMapping) throws Exception {
        log.debug("Started mapDciToProtectedDevice call for accountUid: {}", accountUid);

        accountMappingProcessor.checkAccountMapping(accountUid);
        deviceMappingProcessor.saveDeviceMapping(deviceMapping);

        log.debug("Processed mapDciToProtectedDevice call for accountUid: {}", accountUid);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * API to remove mapped device based on DCI device UID.
     *
     * @param accountUid
     * @param dciDeviceUid
     * @return
     * @throws Exception
     */
    @DeleteMapping("/{accountUid}/devices/mapped-devices/{dciDeviceUid}")
    @Operation(summary = "Delete mapped devices.")
    public ResponseEntity<Object> deleteMappedDevices(@PathVariable("accountUid") String accountUid, @PathVariable("dciDeviceUid") String dciDeviceUid) throws Exception {
        log.debug("Started deleteMappedDevices call for accountUid: {}", dciDeviceUid);

        accountMappingProcessor.checkAccountMapping(accountUid);
        deviceMappingProcessor.deleteDeviceMapping(dciDeviceUid);

        log.debug("Processed deleteMappedDevices call for accountUid: {}", dciDeviceUid);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Would return the following:
     * <p>
     * [
     * "rmmSiteUid": null,
     * "serialNumber": "00012E94105B",
     * "name": "TheOnlyWayIsUp",
     * "model": "S4X1",
     * "internalIP": "10.9.70.71",
     * "clientCompanyName": "Freddie, Inc",
     * "lastSeenDate": "2020-03-06T10:31:38+00:00"
     * ]
     *
     * @param accountUid
     * @return
     * @throws NotFoundException
     * @throws Exception
     */
    @GetMapping("/{accountUid}/devices/{serialNumber}")
    @Operation(summary = "Fetches a Datto Continuity Devices by its serial number for the authenticated user's account.")
    public ResponseEntity<DattoContinuityDevice> getDciDeviceBySerialNumber(@PathVariable("accountUid") String accountUid,
                                                                            @PathVariable("serialNumber") String serialNumber) throws NotFoundException, Exception {
        log.debug("Started getDciDeviceBySerialNumber call for accountUid: {}", accountUid);

        AccountMapping accountMapping = accountMappingProcessor.checkAccountMapping(accountUid);
        DattoContinuityDevice device = metricsService.startTimer(this.getClass(), "getDciDeviceBySerialNumber.getDciDeviceBySerialNumber")
                .recordCallable(() -> dciService.getDciDeviceBySerialNumber(accountMapping, serialNumber));

        log.debug("Processed getDciDeviceBySerialNumber call for accountUid: {}", accountUid);
        return ResponseEntity.status(HttpStatus.OK).body(device);
    }

    /**
     * API to delete a protected device
     *
     * @param accountUid
     * @return
     * @throws Exception
     */
    @DeleteMapping("/{accountUid}/devices/{dciDeviceUid}/protectedDevices/{protectedDeviceUid}")
    @Operation(summary = "delete a protected device mapping")
    public ResponseEntity<Object> deleteProtectedDeviceFromDciDevice(@PathVariable("accountUid") String accountUid,
                                                                     @PathVariable("dciDeviceUid") String dciDeviceUid,
                                                                     @PathVariable("protectedDeviceUid") String protectedDeviceUid) throws Exception {
        log.debug("Started deleteProtectedDeviceFromDci call for accountUid: {}", accountUid);

        accountMappingProcessor.checkAccountMapping(accountUid);
        deviceMappingProcessor.deleteDeviceMapping(dciDeviceUid, protectedDeviceUid);

        log.debug("Processed deleteDeviceMappings call for accountUid: {}, dciDeviceUid: {}, protectedDeviceUid: {}",
                accountUid, dciDeviceUid, protectedDeviceUid);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
