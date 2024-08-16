package net.datto.dciservice.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datto.dci.api.dto.SaasDomain;
import net.datto.dciservice.dynamodb.AccountMapping;
import net.datto.dciservice.services.AccountMappingProcessor;
import net.datto.dciservice.services.DciService;
import net.datto.dciservice.services.MetricsService;
import net.datto.dciservice.services.SiteMappingProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@RestController
public class SaasController {
	private final AccountMappingProcessor accountMappingProcessor;
	private final DciService dciService;

	/**
	 * Fetches Saas Domains for an account
	 * @param accountUid
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/{accountUid}/saas/domains")
	@Operation(summary = "Fetches Sass Domains for the account.")
	public ResponseEntity<Set<SaasDomain>> getSaasDomains(@PathVariable("accountUid") String accountUid) throws Exception {
		log.debug("Started getSaasDomains call for accountUid: {}", accountUid);

		AccountMapping accountMapping = accountMappingProcessor.checkAccountMapping(accountUid);
		Set<SaasDomain> saasResponse = dciService.getSaasDomains(accountMapping);

		log.debug("Processed getSaasDomains call for accountUid: {}", accountUid);
		return ResponseEntity.status(HttpStatus.OK).body(saasResponse);
	}
}
