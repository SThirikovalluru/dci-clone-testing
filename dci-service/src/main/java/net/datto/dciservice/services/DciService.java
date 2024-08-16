package net.datto.dciservice.services;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import net.datto.dci.api.dto.DattoContinuityDevice;
import net.datto.dci.api.dto.DeviceResponse;
import net.datto.dci.api.dto.SaasDomain;
import net.datto.dci.api.dto.agent.AgentDto;
import net.datto.dciservice.dynamodb.AccountMapping;
import net.datto.dciservice.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for requesting BCDR/Datto Continuity objects.
 */
@Service
public class DciService extends DciApi {
    private static final Logger logger = LoggerFactory.getLogger(DciService.class);

    @Autowired
    private MetricsService metricsService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Gets a page of Datto Continuity Devices from the BCDR API
     *
     * @param accountMapping
     * @param page
     * @param size
     * @return
     * @throws InternalServerErrorException
     * @throws NotFoundException
     * @throws UnauthorizedException
     * @throws BadRequestException
     */
    public DeviceResponse<DattoContinuityDevice> getDciDevices(AccountMapping accountMapping, String page, String size)
            throws InternalServerErrorException, NotFoundException, UnauthorizedException, BadRequestException {
        try {
            String devicesEndpoint = getDevicesEndpoint(page, size);
            DeviceResponse<DCIDevice> deviceResponseTmp = requestPortal(accountMapping, HttpMethod.GET, devicesEndpoint, new ParameterizedTypeReference<DeviceResponse<DCIDevice>>() {
            }).getBody();
            DeviceResponse<DattoContinuityDevice> deviceResponse = objectMapper.readValue(objectMapper.writeValueAsString(deviceResponseTmp), new TypeReference<DeviceResponse<DattoContinuityDevice>>() {
            });
            return deviceResponse;
        } catch (HttpClientErrorException | HttpServerErrorException httpClientOrServerExc) {
            if ((httpClientOrServerExc instanceof HttpClientErrorException.Unauthorized)
                    || (httpClientOrServerExc instanceof HttpClientErrorException.Forbidden)) {
                logger.info(
                        "Unauthorised request to Rest API for RMM accountUid: {}, publicKey: {}, Exception: {}",
                        accountMapping.getRmmAccountUid(), accountMapping.getPortalPublicKey(), httpClientOrServerExc.getMessage());
                metricsService.markMeter(this.getClass(), "getDciDevices.Unauth." + httpClientOrServerExc);
            } else {
                logger.error(
                        "Failed to read response while getting devices from Rest API for RMM accountUid: {}, publicKey: {}, Exception: {}",
                        accountMapping.getRmmAccountUid(), accountMapping.getPortalPublicKey(), httpClientOrServerExc.getMessage());
                metricsService.markMeter(this.getClass(), "getDciDevices." + httpClientOrServerExc);
            }
            DciResponseUtil.handleHttpExceptionError(httpClientOrServerExc);

        } catch (JsonProcessingException ex) {
            logger.error(
                    "Failed to read response while getting devices from Rest API for RMM accountUid: {}, publicKey: {}, Exception: ",
                    accountMapping.getRmmAccountUid(), accountMapping.getPortalPublicKey(), ex);
            metricsService.markMeter(this.getClass(), "getDciDevices." + ex);
            throw new InternalServerErrorException(
                    String.format("Failed to read response while getting devices from Rest API for RMM accountUid: {%s}", accountMapping.getRmmAccountUid())
            );
        }

        return null;
    }

    /**
     * Gets a DCI Device based on its Serial Number
     *
     * @param accountMapping
     * @param serialNumber
     * @return DattoContinuityDevice
     * @throws InternalServerErrorException
     * @throws NotFoundException
     * @throws UnauthorizedException
     * @throws BadRequestException
     */
    public DattoContinuityDevice getDciDeviceBySerialNumber(AccountMapping accountMapping, String serialNumber)
            throws InternalServerErrorException, NotFoundException, UnauthorizedException, BadRequestException {
        try {
            String deviceEndpoint = getDeviceEndpoint(serialNumber);
            DattoContinuityDevice device = requestPortal(accountMapping, HttpMethod.GET, deviceEndpoint, new ParameterizedTypeReference<DattoContinuityDevice>() {}).getBody();
            return device;
        } catch (HttpClientErrorException | HttpServerErrorException  httpClientOrServerExc) {
            if ((httpClientOrServerExc instanceof HttpClientErrorException.Unauthorized)
                    || (httpClientOrServerExc instanceof HttpClientErrorException.Forbidden)) {
                logger.info(
                        "Unauthorised request to Rest API for RMM accountUid: {}, publicKey: {}, Exception: {}",
                        accountMapping.getRmmAccountUid(), accountMapping.getPortalPublicKey(), httpClientOrServerExc.getMessage());
                metricsService.markMeter(this.getClass(), "getDciDeviceBySerialNumber.Unauth." + httpClientOrServerExc);
            } else {
                logger.error(
                        "Failed to read response while getting devices from Rest API for RMM accountUid: {}, publicKey: {}, Exception: {}",
                        accountMapping.getRmmAccountUid(), accountMapping.getPortalPublicKey(), httpClientOrServerExc.getMessage());
                metricsService.markMeter(this.getClass(), "getDciDeviceBySerialNumber." + httpClientOrServerExc);
            }
            DciResponseUtil.handleHttpExceptionError(httpClientOrServerExc);
        }
        return null;
    }

    /**
     * Get a page of agents of DCI Device based on its Serial Number
     *
     * @param accountMapping
     * @param serialNumber
     * @param pageNumber
     * @param pageSize
     * @return
     * @throws InternalServerErrorException
     * @throws NotFoundException
     * @throws UnauthorizedException
     * @throws BadRequestException
     */
    public DeviceResponse<AgentDto> getDeviceAgents(AccountMapping accountMapping, String serialNumber, String pageNumber, String pageSize)
            throws InternalServerErrorException, NotFoundException, UnauthorizedException, BadRequestException {

        try {
            String agentsEndpoint = getAgentsEndpoint(serialNumber, pageNumber, pageSize);
            DeviceResponse<AgentDto> deviceResponse = requestPortal(accountMapping, HttpMethod.GET, agentsEndpoint, new ParameterizedTypeReference<DeviceResponse<AgentDto>>() {
            }).getBody();
            return deviceResponse;

        } catch (HttpClientErrorException | HttpServerErrorException httpClientOrServerExc) {
            metricsService.markMeter(this.getClass(), "getDeviceAgents." + httpClientOrServerExc);
            DciResponseUtil.handleHttpExceptionError(httpClientOrServerExc);
        }
        return null;
    }

    /**
     *  Excluding Duplicated Agents from the List, Archived Agents with lowest Agent Version have priority to be excluded.
     * @param agents
     * @return
     */
    public List<AgentDto> excludeDuplicatedAgents(List<AgentDto> agents) {
        // Detecting Duplicated Agents for Excluding them.
        Map<String, Integer> agentsMap = new HashMap();
        for (AgentDto agentDto : agents) {
            String agentNameLocalIp = agentDto.getName() + agentDto.getLocalIp();
            Integer nrOfDuplicates = agentsMap.get(agentNameLocalIp);
            if (nrOfDuplicates == null) {
                agentsMap.put(agentNameLocalIp, 0);
            } else {
                agentsMap.put(agentNameLocalIp, ++nrOfDuplicates);
            }
        }

        // Excluding Duplicated Agents List should have Agents with highest Agent Version.
        // So after sorting, bellow logic will exclude first lowest Agent Version and will remain only highest Versions.
        agents = agents.stream().sorted(Comparator.comparing(AgentDto::getAgentVersion)).collect(Collectors.toList());
        // Excluding Duplicated Agents, if Agents List has duplicates then we start to exclude from the list Archived Agent with lowest Agent Version.
        ListIterator<AgentDto> iter = agents.listIterator();
        while (iter.hasNext()) {
            AgentDto agentDto = iter.next();
            String agentNameLocalIp = agentDto.getName() + agentDto.getLocalIp();
            Integer nrOfDuplicates = agentsMap.get(agentNameLocalIp);
            if (nrOfDuplicates > 0 && agentDto.getIsArchived()) {
                agentsMap.put(agentNameLocalIp, --nrOfDuplicates);
                iter.remove();
            }
        }

        return agents;
    }

    /**
     * Gets Saas Domains from the BCDR API
     *
     * @param accountMapping
     * @return
     * @throws InternalServerErrorException
     * @throws NotFoundException
     * @throws UnauthorizedException
     * @throws BadRequestException
     */
    public Set<SaasDomain> getSaasDomains(AccountMapping accountMapping)
            throws InternalServerErrorException, NotFoundException, UnauthorizedException, BadRequestException {
        try {
            String saasDomainsEndpoint = getSaasEndpoint();
            Set<DCISaasDomain> saasResponseTmp = requestPortal(accountMapping, HttpMethod.GET, saasDomainsEndpoint, new ParameterizedTypeReference<Set<DCISaasDomain>>() {
            }).getBody();
            Set<SaasDomain> sassResponse = objectMapper.readValue(objectMapper.writeValueAsString(saasResponseTmp), new TypeReference<Set<SaasDomain>>() {
            });
            return sassResponse;
        } catch (HttpClientErrorException | HttpServerErrorException httpClientOrServerExc) {
            if ((httpClientOrServerExc instanceof HttpClientErrorException.Unauthorized)
                    || (httpClientOrServerExc instanceof HttpClientErrorException.Forbidden)) {
                logger.info(
                        "Unauthorised request to Rest API for RMM accountUid: {}, publicKey: {}, Exception: {}",
                        accountMapping.getRmmAccountUid(), accountMapping.getPortalPublicKey(), httpClientOrServerExc.getMessage());
                metricsService.markMeter(this.getClass(), "getSaasDomains.Unauth." + httpClientOrServerExc);
            } else {
                logger.error(
                        "Failed to read response while getting Saas Domains from Rest API for RMM accountUid: {}, publicKey: {}, Exception: {}",
                        accountMapping.getRmmAccountUid(), accountMapping.getPortalPublicKey(), httpClientOrServerExc.getMessage());
                metricsService.markMeter(this.getClass(), "getSaasDomains." + httpClientOrServerExc);
            }
            DciResponseUtil.handleHttpExceptionError(httpClientOrServerExc);

        } catch (JsonProcessingException ex) {
            logger.error(
                    "Failed to read response while getting Saas Domains from Rest API for RMM accountUid: {}, publicKey: {}, Exception: ",
                    accountMapping.getRmmAccountUid(), accountMapping.getPortalPublicKey(), ex);
            metricsService.markMeter(this.getClass(), "getSaasDomains." + ex);
            throw new InternalServerErrorException(
                    String.format("Failed to read response while getting Saas Domains from Rest API for RMM accountUid: {%s}", accountMapping.getRmmAccountUid())
            );
        }

        return null;
    }


}
