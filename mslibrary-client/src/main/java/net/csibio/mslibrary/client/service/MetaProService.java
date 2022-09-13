package net.csibio.mslibrary.client.service;

import net.csibio.mslibrary.client.domain.Result;
import net.csibio.mslibrary.client.domain.bean.identification.Feature;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface MetaProService {

    public Result<List<Feature>> getAllFeatureByOverviewId(String overviewId, String projectId);

}
