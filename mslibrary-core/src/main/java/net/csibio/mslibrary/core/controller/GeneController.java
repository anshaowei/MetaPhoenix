package net.csibio.mslibrary.core.controller;

import net.csibio.mslibrary.client.constants.enums.ResultCode;
import net.csibio.mslibrary.client.domain.Result;
import net.csibio.mslibrary.client.domain.db.GeneDO;
import net.csibio.mslibrary.client.domain.db.ProteinDO;
import net.csibio.mslibrary.client.domain.query.GeneQuery;
import net.csibio.mslibrary.client.domain.query.ProteinQuery;
import net.csibio.mslibrary.client.domain.vo.GeneUpdateVO;
import net.csibio.mslibrary.client.domain.vo.ProteinUpdateVO;
import net.csibio.mslibrary.client.exceptions.XException;
import net.csibio.mslibrary.client.service.GeneService;
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
@RequestMapping("gene")
public class GeneController {

    @Autowired
    GeneService geneService;
    @Autowired
    LibraryService libraryService;

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    Result add(GeneUpdateVO geneUpdate) throws XException {
        libraryService.tryGetById(geneUpdate.getLibraryId(), ResultCode.LIBRARY_NOT_EXISTED);
        GeneDO newGene = new GeneDO();
        BeanUtils.copyProperties(geneUpdate, newGene);
        return geneService.insert(newGene);
    }

    @RequestMapping(value = "/update")
    Result update(GeneUpdateVO geneUpdate) throws XException {
        GeneDO geneInDB = geneService.tryGetById(geneUpdate.getId(), ResultCode.GENE_NOT_EXISTED);
        BeanUtils.copyProperties(geneUpdate, geneInDB);
        return geneService.update(geneInDB);
    }

    @RequestMapping(value = "/list")
    Result list(GeneQuery query) {
        Result<List<GeneDO>> result = geneService.getList(query);
        return result;
    }

    @RequestMapping(value = "/detail")
    Result detail(@RequestParam(value = "id") String geneId) throws XException {
        GeneDO gene = geneService.tryGetById(geneId, ResultCode.GENE_NOT_EXISTED);
        return Result.OK(gene);
    }

    @RequestMapping("/remove")
    Result remove(@RequestParam(value = "ids") String[] ids) {
        Result<List<String>> result = new Result<List<String>>();
        List<String> errorList = new ArrayList<>();
        List<String> deletedIds = new ArrayList<>();
        for (String id : ids) {
            Result removeResult = geneService.remove(id);
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
