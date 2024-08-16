package net.datto.dciservice.status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides the status of the DCI service.
 */
@RestController()
@RequestMapping("/status")
public class StatusResource {

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm");

    @Autowired
    private BuildProperties buildProperties;

    @RequestMapping(
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Map<String, Object> status() {
        HashMap<String, Object> returnMap = new HashMap<>();
        returnMap.put("status", "OK");
        returnMap.put("version", buildProperties.getVersion());
        returnMap.put("started", dateFormat.format(new Date(ManagementFactory.getRuntimeMXBean().getStartTime()).getTime()));
        return returnMap;
    }
}

