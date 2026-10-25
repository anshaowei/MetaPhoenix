package net.csibio.metaphoenix.core.controller;

import lombok.extern.slf4j.Slf4j;
import net.csibio.aird.bean.common.IdName;
import net.csibio.metaphoenix.client.constants.enums.ResultCode;
import net.csibio.metaphoenix.client.domain.Result;
import net.csibio.metaphoenix.client.domain.bean.common.LabelValue;
import net.csibio.metaphoenix.client.domain.db.LibraryDO;
import net.csibio.metaphoenix.client.domain.query.LibraryQuery;
import net.csibio.metaphoenix.client.domain.query.SpectrumQuery;
import net.csibio.metaphoenix.client.domain.vo.LibraryUploadVO;
import net.csibio.metaphoenix.client.exceptions.XException;
import net.csibio.metaphoenix.client.service.BaseService;
import net.csibio.metaphoenix.client.service.CompoundService;
import net.csibio.metaphoenix.client.service.LibraryService;
import net.csibio.metaphoenix.client.service.SpectrumService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("library")
public class LibraryController extends BaseController<LibraryDO, LibraryQuery> {

    @Autowired
    LibraryService libraryService;
    @Autowired
    CompoundService compoundService;
    @Autowired
    SpectrumService spectrumService;

    @RequestMapping(value = "/list")
    Result list(LibraryQuery query) {
        Result<List<LibraryDO>> res = libraryService.getList(query);
        return res;
    }

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    Result upload(LibraryUploadVO libraryUploadVO) {
        try {
            LibraryDO library = new LibraryDO();
            BeanUtils.copyProperties(libraryUploadVO, library);
            Result result = libraryService.insert(library);
            check(result);
            MultipartFile libFile = libraryUploadVO.getLibFile();
            if (libFile != null) {
                String extraName = libFile.getOriginalFilename();
                String oName = extraName.substring(extraName.lastIndexOf("."));
                if (oName.equals(".xlsx")) {
                    return libraryService.parseAndInsert(library, libFile.getInputStream(), 1);
                } else {
                    return libraryService.parseAndInsert(library, libFile.getInputStream(), 2);
                }
            } else {
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.Error(ResultCode.INSERT_ERROR);
        }
    }

    @RequestMapping(value = "/fetchLibraryLabelValues")
    Result<List<LabelValue>> fetchLibraryLabelValues(@RequestParam(value = "type", required = false) String type) {
        LibraryQuery query = new LibraryQuery();
        if (type != null) {
            query.setType(type);
        }
        List<IdName> libraryList = libraryService.getAll(query, IdName.class);
        List<LabelValue> lvList = new ArrayList<>();
        for (IdName idName : libraryList) {
            lvList.add(new LabelValue(idName.name(), idName.id()));
        }
        Result<List<LabelValue>> result = new Result(true);
        result.setData(lvList);
        return result;
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    Result update(LibraryUploadVO libraryUploadVO) throws XException {
        LibraryDO library = libraryService.tryGetById(libraryUploadVO.getId(), ResultCode.LIBRARY_NOT_EXISTED);
        library.setDescription(libraryUploadVO.getDescription());
        library.setType(libraryUploadVO.getType());
        library.setTags(libraryUploadVO.getTags());
        library.setSpecies(libraryUploadVO.getSpecies());
        library.setMatrix(libraryUploadVO.getMatrix());
        return libraryService.update(library);
    }

    @RequestMapping(value = "/detail")
    Result<LibraryDO> detail(@RequestParam(value = "id", required = false) String id) throws XException {
        Result<LibraryDO> result = new Result();
        LibraryDO library = libraryService.tryGetById(id, ResultCode.LIBRARY_NOT_EXISTED);
        result.setData(library);
        return result;
    }

    @RequestMapping(value = "/countSpectra")
    Result<Long> countSpectra(@RequestParam(value = "id", required = false) String id) {
        Result result = new Result(true);
        Long number = spectrumService.count(new SpectrumQuery(), id);
        result.setData(number);
        return result;
    }

    @Override
    BaseService<LibraryDO, LibraryQuery> getBaseService() {
        return libraryService;
    }
}
