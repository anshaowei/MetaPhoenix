package net.csibio.metaphoenix.core.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.csibio.metaphoenix.client.domain.db.MethodDO;
import net.csibio.metaphoenix.client.domain.query.MethodQuery;
import net.csibio.metaphoenix.client.exceptions.XException;
import net.csibio.metaphoenix.client.service.IDAO;
import net.csibio.metaphoenix.client.service.MethodService;
import net.csibio.metaphoenix.core.dao.MethodDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service("methodService")
@Slf4j
public class MethodServiceImpl implements MethodService {
    @Autowired
    MethodDAO methodDAO;

    @Override
    public IDAO<MethodDO, MethodQuery> getBaseDAO() {
        return methodDAO;
    }

    @Override
    public void beforeInsert(MethodDO method) throws XException {
        method.setId(method.getName());
        method.setCreateDate(new Date());
        method.setLastModifiedDate(new Date());
    }

    @Override
    public void beforeUpdate(MethodDO method) throws XException {
        method.setLastModifiedDate(new Date());
    }

    @Override
    public void beforeRemove(String id) throws XException {

    }
}
