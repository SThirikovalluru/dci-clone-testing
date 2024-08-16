package net.datto.dciservice.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datto.dciservice.dynamodb.AccountMapping;
import net.datto.dciservice.dynamodb.SiteMapping;
import net.datto.dciservice.services.AccountMappingProcessor;
import net.datto.dciservice.services.MetricsService;
import net.datto.dciservice.services.SiteMappingProcessor;
import net.datto.dciservice.utils.AESCodec;
import net.datto.dciservice.utils.BadRequestException;
import net.datto.dciservice.utils.InternalServerErrorException;
import net.datto.dciservice.utils.NotFoundException;
import net.datto.dciservice.utils.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;

/**
 * AccountController - uses DATTO Portal RestAPI to authenticate users accounts
 */

@Slf4j
@RequiredArgsConstructor
@RestController
public class AccountController {
    private final AccountMappingProcessor accountMappingProcessor;
    private final SiteMappingProcessor siteMappingProcessor;
    private final AESCodec aesCodec;
    private final MetricsService metricsService;


    @GetMapping("/authentication")
    @Operation(summary = "Authenticate using Portal Api Credentials")
    public ResponseEntity<?> authenticate(@RequestParam(value = "accountUid") String accountUid,
                                       @RequestParam(value = "publicKey") String publicKey,
                                       @RequestParam(value = "privateKey") String privateKey) throws Exception {
        log.info("Started authenticate call for accountUid: {}, publicKey: {}", accountUid, publicKey);

        AccountMapping accountMapping = accountMappingProcessor.getAccountMapping(accountUid);

        try {
            if (accountMapping == null) {
                //initiate a new record for datto_integrations_info record for the account
                accountMapping = new AccountMapping(accountUid, publicKey, aesCodec.encode(privateKey));
            } else {
                accountMapping.setPortalPublicKey(publicKey);
                accountMapping.setPortalSecretKey(aesCodec.encode(privateKey));
            }

            accountMappingProcessor.doAuthentication(accountMapping);
            accountMappingProcessor.saveAccountMapping(accountMapping);

            log.info("Processed authenticate call for accountUid: {}", accountUid);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (InternalServerErrorException e) {
            log.error("Authentication failed for RMM accountUid: {}, publicKey: {}, Exception: ", accountMapping.getRmmAccountUid(), accountMapping.getPortalPublicKey(), e);
            metricsService.markMeter(this.getClass(), "Authentication failed.rmmAccUID." + accountMapping.getRmmAccountUid());
            throw new InternalServerErrorException("Authentication failed for RMM accountUid: " + accountMapping.getRmmAccountUid() + " with Exception: " + e.getMessage());
        }
    }

    /**
     * This method returns AccountMapping by accountUid from DynamoDb if it present, if not we return null.
     */
    @GetMapping("/{accountUid}/account-mappings")
    @Operation(summary = "Fetches Datto Integration by Account Uid.")
    public AccountMapping getAccountMapping(@PathVariable("accountUid") String accountUid) throws NotFoundException {
        log.debug("Started getAccountMapping call for accountUid: {}", accountUid);


        AccountMapping accountMapping = accountMappingProcessor.checkAccountMapping(accountUid);
        //Dont want to send the private key as its private
        accountMapping.setPortalSecretKey("");

        log.debug("Processed getAccountMapping call for accountUid: {}", accountUid);
        return accountMapping;
    }

    /**
     * This method checks/validate the accountMapping by accountUid from DynamoDb
     * and if it is present and the keys are valid and the portal is available return the accountMapping.
     */
    @GetMapping("/{accountUid}/validate-account-mappings")
    @Operation(summary = "Fetches Datto Integration by Account Uid and checks the public and private keys validity and the availability of CT.")
    public AccountMapping checkAccountMapping(@PathVariable("accountUid") String accountUid) throws NotFoundException, InternalServerErrorException, UnauthorizedException, BadRequestException, Exception {
        log.info("Started checkAccountMapping call for accountUid: {}", accountUid);

        AccountMapping accountMapping = accountMappingProcessor.checkAccountMapping(accountUid);

        metricsService.startTimer(this.getClass(), "checkAccountMapping.doAuthentication")
                .recordCallable(() -> accountMappingProcessor.doAuthentication(accountMapping));
        //Dont want to send the private key as its private
        accountMapping.setPortalSecretKey("");

        log.info("Processed checkAccountMapping call for accountUid: {}", accountUid);
        return accountMapping;
    }

    /**
     * Check if exists AccountMapping and retrieves SiteMapping from DynamoDB based on accountUid and returns true if does exist mappings.
     */
    @GetMapping("/{accountUid}/sync-account-allowed")
    @Operation(summary = "Check if exists AccountMapping and SiteMappings in DynamoDB based on accountUid.")
    public HashMap<String, Boolean> isSyncAccountAllowed(@PathVariable("accountUid") String accountUid) {
        log.debug("Started isSyncAccountAllowed call for accountUid: {}", accountUid);
        HashMap<String, Boolean> returnMap = new HashMap<>();
        AccountMapping accountMapping = accountMappingProcessor.getAccountMapping(accountUid);
        List<SiteMapping> siteMappings = siteMappingProcessor.getSiteMappings(accountUid);

        log.debug("Processed isSyncAccountAllowed call for accountUid: {}", accountUid);
        returnMap.put("accountAllowed", accountMapping != null && siteMappings.size() > 0);

        return returnMap;
    }

    /**
     * This method delete AccountMapping by accountUid from DynamoDb if it is present.
     */
    @DeleteMapping("/{accountUid}/account-mappings")
    @Operation(summary = "Remove Account Mappings by Account Uid.")
    public void deleteAccountMapping(@PathVariable("accountUid") String accountUid) throws NotFoundException {
        log.debug("Started deleteAccountMapping call for accountUid: {}", accountUid);

        accountMappingProcessor.deleteAccountMapping(accountUid);

        log.debug("Processed deleteAccountMapping call for accountUid: {}", accountUid);
    }

}

