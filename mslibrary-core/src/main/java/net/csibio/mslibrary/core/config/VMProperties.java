package net.csibio.mslibrary.core.config;

import jakarta.annotation.PostConstruct;
import net.csibio.mslibrary.client.utils.RepositoryUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

@Component("vmProperties")
public class VMProperties {
    @Value("${repository}")
    private String repository;

    @Value("${siriusPath}")
    private String siriusPath;

    @Value("${siriusProjectSpace}")
    private String siriusProjectSpace;

    @PostConstruct
    public void init() {
        String repositoryPath = getRepository();
        File repositoryDir = new File(repositoryPath);
        if (!repositoryDir.exists()) {
            repositoryDir.mkdirs();
        }
        RepositoryUtil.repository = repositoryPath;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public String getRepository() {
        if (StringUtils.isEmpty(repository)) {
            return "/nas/data";
        }
        return repository;
    }

    public String getSiriusPath() {
        return siriusPath;
    }

    public String getSiriusProjectSpace() {
        return siriusProjectSpace;
    }
}
