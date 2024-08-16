package net.datto.dciservice.dynamodb;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class DynamoDbSiteMappingDao {
    private final DynamoDbSiteMapping dynamoDbSiteMapping;

    public void storeSiteMapping(SiteMapping siteMapping) {
        dynamoDbSiteMapping.save(siteMapping);
    }

    public SiteMapping getSiteMapping(String rmmAccountUid, String serialNumberSiteUid) {
    	  return dynamoDbSiteMapping.getByAccountAndSerialNumber(rmmAccountUid, serialNumberSiteUid);
    }

    public List<SiteMapping> getRmmSitesMapped(String rmmAccountUid) {
        return dynamoDbSiteMapping.getByAccount(rmmAccountUid);
    }
    
    public void deleteSiteMappingByAccountUid(String accountUid) {
        dynamoDbSiteMapping.deleteByAccountUid(accountUid);
    }

    public void deleteSiteMapping(SiteMapping siteMapping) {
        dynamoDbSiteMapping.deleteSiteMapping(siteMapping);
    }

}

