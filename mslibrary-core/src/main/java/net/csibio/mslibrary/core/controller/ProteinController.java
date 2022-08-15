package net.csibio.mslibrary.core.controller;

import net.csibio.mslibrary.client.constants.enums.ResultCode;
import net.csibio.mslibrary.client.domain.Result;
import net.csibio.mslibrary.client.domain.db.ProteinDO;
import net.csibio.mslibrary.client.domain.query.ProteinQuery;
import net.csibio.mslibrary.client.domain.vo.ProteinUpdateVO;
import net.csibio.mslibrary.client.exceptions.XException;
import net.csibio.mslibrary.client.service.LibraryService;
import net.csibio.mslibrary.client.service.ProteinService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("protein")
public class ProteinController {

    @Autowired
    ProteinService proteinService;
    @Autowired
    LibraryService libraryService;

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    Result add(ProteinUpdateVO proteinUpdate) throws XException {
        libraryService.tryGetById(proteinUpdate.getLibraryId(), ResultCode.LIBRARY_NOT_EXISTED);
        ProteinDO newProtein = new ProteinDO();
        BeanUtils.copyProperties(proteinUpdate, newProtein);
        return proteinService.insert(newProtein);
    }

    @RequestMapping(value = "/update")
    Result update(ProteinUpdateVO proteinUpdate) throws XException {
        ProteinDO proteinInDB = proteinService.tryGetById(proteinUpdate.getId(), ResultCode.PROTEIN_NOT_EXISTED);
        BeanUtils.copyProperties(proteinUpdate, proteinInDB);
        return proteinService.update(proteinInDB);
    }

    @RequestMapping(value = "/list")
    Result list(ProteinQuery query) {
        Result<List<ProteinDO>> result = proteinService.getList(query);
        return result;
    }

    @RequestMapping(value = "/detail")
    Result detail(@RequestParam(value = "id") String proteinId) throws XException {
        ProteinDO protein = proteinService.tryGetById(proteinId, ResultCode.PROTEIN_NOT_EXISTED);
        return Result.OK(protein);
    }

    @RequestMapping("/remove")
    Result remove(@RequestParam(value = "ids") String[] ids) {
        Result<List<String>> result = new Result<List<String>>();
        List<String> errorList = new ArrayList<>();
        List<String> deletedIds = new ArrayList<>();
        for (String id : ids) {
            Result removeResult = proteinService.remove(id);
            if (removeResult.isSuccess()) {
                deletedIds.add(id);
            } else {
                errorList.add(removeResult.getMsgInfo());
            }
        }
        if (deletedIds.size() != 0) {
            result.setData(deletedIds);
            result.setSuccess(true);
        }
        if (errorList.size() != 0) {
            result.setErrorList(errorList);
        }
        return result;
    }
}
