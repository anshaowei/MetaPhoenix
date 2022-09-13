package net.csibio.mslibrary.core.service.impl;

import net.csibio.mslibrary.client.domain.Result;
import net.csibio.mslibrary.client.service.MetaProService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class MetaProServiceImpl implements MetaProService {

    @Value("${metapro.url}")
    private String url;

    @Autowired
    RestTemplate restTemplate;

    public Result getAllFeatureByOverviewId(String overviewId, String projectId) {
        String request = url + "/feature/getAll?overviewId=" + overviewId + "&projectId=" + projectId;
        return this.restTemplate.getForObject(request, Result.class);
    }

}
