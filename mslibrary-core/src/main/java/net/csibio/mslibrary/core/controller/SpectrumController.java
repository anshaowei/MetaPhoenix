package net.csibio.mslibrary.core.controller;

import net.csibio.mslibrary.client.constants.enums.ResultCode;
import net.csibio.mslibrary.client.domain.Result;
import net.csibio.mslibrary.client.domain.bean.spectrum.SpectrumPoint;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import net.csibio.mslibrary.client.domain.query.SpectrumQuery;
import net.csibio.mslibrary.client.exceptions.XException;
import net.csibio.mslibrary.client.service.BaseMultiService;
import net.csibio.mslibrary.client.service.BaseService;
import net.csibio.mslibrary.client.service.CompoundService;
import net.csibio.mslibrary.client.service.SpectrumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("spectrum")
public class SpectrumController {

    @Autowired
    SpectrumService spectrumService;
    @Autowired
    CompoundService compoundService;

    @RequestMapping(value = "/getBaseSpectrum")
    Result<SpectrumPoint> getBaseSpectrum(@RequestParam(value = "spectrumId", required = true) String spectrumId, String libraryId) throws XException {
        SpectrumDO spectrumDO = spectrumService.tryGetById(spectrumId, libraryId, ResultCode.SPECTRA_NOT_EXISTED);
        SpectrumPoint spectrum = new SpectrumPoint();
        spectrum.setMzs(spectrumDO.getMzs());
        spectrum.setInts(spectrumDO.getInts());
        return Result.build(spectrum);
    }

    /**
     * 根据给定的spectraId, 返回SpectraDO的信息
     *
     * @param spectraId SpectraDO的数据库id
     * @return SpectraDO对象
     */
    @RequestMapping("/detail")
    Result detail(@RequestParam(value = "spectraId", required = true) String spectraId, String libraryId) throws XException {
        SpectrumDO spectra = spectrumService.tryGetById(spectraId, libraryId, ResultCode.SPECTRUM_NOT_EXISTED);
        return Result.build(spectra);
    }

}
