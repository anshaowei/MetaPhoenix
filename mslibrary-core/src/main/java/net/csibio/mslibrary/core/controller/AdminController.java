package net.csibio.mslibrary.core.controller;

import lombok.extern.slf4j.Slf4j;
import net.csibio.aird.bean.common.IdName;
import net.csibio.mslibrary.client.constants.enums.ResultCode;
import net.csibio.mslibrary.client.domain.Result;
import net.csibio.mslibrary.client.domain.bean.common.LabelValue;
import net.csibio.mslibrary.client.domain.db.LibraryDO;
import net.csibio.mslibrary.client.domain.query.LibraryQuery;
import net.csibio.mslibrary.client.domain.query.SpectrumQuery;
import net.csibio.mslibrary.client.domain.vo.LibraryUploadVO;
import net.csibio.mslibrary.client.exceptions.XException;
import net.csibio.mslibrary.client.service.*;
import net.csibio.mslibrary.core.config.VMProperties;
import net.csibio.mslibrary.core.parser.HmdbParser;
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
@RequestMapping("admin")
public class AdminController {

    @Autowired
    LibraryParserService libraryParserService;
    @Autowired
    VMProperties vmProperties;

    @RequestMapping(value = "/async")
    Result async() {
        String path = vmProperties.getRepository()+"/metabolomics/hmdb_metabolites.xml";
        new HmdbParser().parse(path);
//        libraryParserService.parseMassBank();
//        libraryParserService.parseGNPS();
        return Result.OK();
    }

}
