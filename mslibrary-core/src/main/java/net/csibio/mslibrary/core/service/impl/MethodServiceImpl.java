package net.csibio.mslibrary.core.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.csibio.mslibrary.client.domain.db.MethodDO;
import net.csibio.mslibrary.client.domain.query.MethodQuery;
import net.csibio.mslibrary.client.exceptions.XException;
import net.csibio.mslibrary.client.service.IDAO;
import net.csibio.mslibrary.client.service.MethodService;
import net.csibio.mslibrary.core.dao.MethodDAO;
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
