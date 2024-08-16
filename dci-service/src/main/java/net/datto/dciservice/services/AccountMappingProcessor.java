package net.datto.dciservice.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datto.dciservice.dynamodb.AccountMapping;
import net.datto.dciservice.dynamodb.DynamoDbAccountMappingDao;
import net.datto.dciservice.utils.DciResponseUtil;
import net.datto.dciservice.utils.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

/**
 * Service class for managing the AccountMapping objects
 */

@Slf4j
@RequiredArgsConstructor
@Service
public class AccountMappingProcessor extends DciApi {

    private final DynamoDbAccountMappingDao dynamoDbAccountMappingDao;
    private final DciService dciService;
    private final MetricsService metricsService;

    public void saveAccountMapping(AccountMapping accountMapping) {
        if (accountMapping == null) {
            return;
        }
        dynamoDbAccountMappingDao.storeAccountMapping(accountMapping);
    }

    public AccountMapping getAccountMapping(String accountUid) {
        return dynamoDbAccountMappingDao.getAccountMapping(accountUid);
    }


    public void deleteAccountMapping(String accountUid) {
        dynamoDbAccountMappingDao.deleteAccountMapping(accountUid);
    }

    /**
     * This method tests if the keypair accountMapping.publicKey
     * accountMapping.privateKey are valid for a DATTO Portal account and returns
     * results from the networking api
     *
     * @param accountMapping - AccountMapping object which has to be authenticated
     * @return Datto Account ID
     */
    public boolean doAuthentication(AccountMapping accountMapping)
            throws Exception {
        log.info("Started authenticate call for accountUid {}", accountMapping.getRmmAccountUid());
        String pageNumber = "1";
        String pageSize = "1";
        try {
            metricsService.startTimer(this.getClass(), "doAuthentication.getDciDevices")
                    .recordCallable(() -> dciService.getDciDevices(accountMapping, pageNumber, pageSize));
            log.info("Processed authenticate call for accountUid {}", accountMapping.getRmmAccountUid());
            return true;
        } catch (HttpClientErrorException | HttpServerErrorException httpClientOrServerExc) {
            metricsService.markMeter(this.getClass(), "doAuthentication.rmmAccUID." + accountMapping.getRmmAccountUid() + "." + httpClientOrServerExc);
            DciResponseUtil.handleHttpExceptionError(httpClientOrServerExc);
        }
        return false;
    }

    /**
     * Retrieves the AccountMapping from DynamoDB - if it doesn't exist, throw an exception
     *
     * @param accountUid
     * @return
     * @throws NotFoundException
     */
    public AccountMapping checkAccountMapping(String accountUid) throws NotFoundException {
        AccountMapping accountMapping = getAccountMapping(accountUid);

        if (accountMapping == null) {
            log.warn("Account information not found for accountUid {} ", accountUid);
            metricsService.markMeter(this.getClass(), "checkAccountMapping.rmmAccUID.notFound." + accountUid);
            throw new NotFoundException("Account information not found for accountUid: " + accountUid);
        }
        return accountMapping;
    }

}
