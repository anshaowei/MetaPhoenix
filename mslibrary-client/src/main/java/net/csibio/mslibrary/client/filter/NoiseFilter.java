package net.csibio.mslibrary.client.filter;

import lombok.extern.slf4j.Slf4j;
import net.csibio.aird.enums.MsLevel;
import net.csibio.mslibrary.client.constants.Constants;
import net.csibio.mslibrary.client.constants.enums.IonMode;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import net.csibio.mslibrary.client.domain.query.SpectrumQuery;
import net.csibio.mslibrary.client.service.SpectrumService;
import net.csibio.mslibrary.client.utils.ArrayUtil;
import org.apache.commons.math3.stat.StatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component("noiseFilter")
@Slf4j
public class NoiseFilter {

    @Autowired
    SpectrumService spectrumService;

    public void filter(String libraryId) {
        log.info("start noise filter on library: {}", libraryId);
        List<SpectrumDO> spectrumDOS = spectrumService.getAll(new SpectrumQuery(), libraryId);
        int count = spectrumDOS.size();

        //1. remove spectra with empty key information
        spectrumDOS.removeIf(spectrumDO -> spectrumDO.getMzs() == null || spectrumDO.getMzs().length == 0 ||
                spectrumDO.getInts() == null || spectrumDO.getInts().length == 0 ||
                spectrumDO.getPrecursorMz() == null || spectrumDO.getPrecursorMz() == 0 ||
                spectrumDO.getIonMode() == null);
        spectrumDOS.removeIf(spectrumDO -> spectrumDO.getInChIKey() == null || spectrumDO.getInChIKey().equals("") || spectrumDO.getInChIKey().equals("N/A"));
        log.info("remove {} spectra with empty key information, {} spectra left", count - spectrumDOS.size(), spectrumDOS.size());
        count = spectrumDOS.size();

        //2. remove zero data points
        int dataPoint = 0;
        int totalDataPoint = 0;
        for (SpectrumDO spectrumDO : spectrumDOS) {
            List<Double> mzs = new ArrayList<>();
            List<Double> ints = new ArrayList<>();
            double basePeak = StatUtils.max(spectrumDO.getInts());
            for (int i = 0; i < spectrumDO.getMzs().length; i++) {
                if (spectrumDO.getInts()[i] < 0.01 * basePeak) {
                    continue;
                }
                mzs.add(spectrumDO.getMzs()[i]);
                ints.add(spectrumDO.getInts()[i]);
            }
            dataPoint += spectrumDO.getMzs().length - mzs.size();
            totalDataPoint += spectrumDO.getMzs().length;
            spectrumDO.setMzs(mzs.stream().mapToDouble(Double::doubleValue).toArray());
            spectrumDO.setInts(ints.stream().mapToDouble(Double::doubleValue).toArray());
        }
        log.info("remove {} zero data points, {} spectra left, {}% data points removed", dataPoint, spectrumDOS.size(), dataPoint * 100.0 / totalDataPoint);

        //3. remove low resolution data (the difference between the precursorMz and the nearest m/z is larger than 10ppm)
        spectrumDOS.removeIf(spectrumDO -> ArrayUtil.findNearestDiff(spectrumDO.getMzs(), spectrumDO.getPrecursorMz()) > 10 * Constants.PPM * spectrumDO.getPrecursorMz());
        log.info("remove {} spectra with low resolution, {} spectra left", count - spectrumDOS.size(), spectrumDOS.size());
        count = spectrumDOS.size();

        //4. remove spectra with <5 peaks with relative intensity above 2%
        spectrumDOS.removeIf(spectrumDO -> {
            int goodIons = 0;
            double baseIntensity = StatUtils.max(spectrumDO.getInts());
            for (double value : spectrumDO.getInts()) {
                if (value / baseIntensity > 0.02) {
                    goodIons++;
                }
            }
            return goodIons < 5;
        });
        log.info("remove {} spectra with <5 peaks with relative intensity above 2%, {} spectra left", count - spectrumDOS.size(), spectrumDOS.size());
        count = spectrumDOS.size();

        //5. remove spectra with precursorMz > 1000 or exactMass > 1000
        spectrumDOS.removeIf(spectrumDO -> spectrumDO.getPrecursorMz() > 1000 || (spectrumDO.getExactMass() != null && spectrumDO.getExactMass() > 1000));
        log.info("remove {} spectra with precursorMz or exactMass > 1000, {} spectra left", count - spectrumDOS.size(), spectrumDOS.size());
        count = spectrumDOS.size();

        //6. remove spectra except MS2
        spectrumDOS.removeIf(spectrumDO -> !spectrumDO.getMsLevel().equals(MsLevel.MS2.getCode()));
        log.info("remove {} spectra except MS2, {} spectra left", count - spectrumDOS.size(), spectrumDOS.size());
        count = spectrumDOS.size();

        //7. remove spectra except positive mode
        spectrumDOS.removeIf(spectrumDO -> !spectrumDO.getIonMode().equals(IonMode.Positive.getName()));
        log.info("remove {} spectra except positive mode, {} spectra left", count - spectrumDOS.size(), spectrumDOS.size());

        spectrumService.remove(new SpectrumQuery(), libraryId);
        spectrumService.insert(spectrumDOS, libraryId);
        log.info("finish noise filter on library: {}", libraryId);
    }

    public void basicFilter(String libraryId) {
        log.info("start basic noise filter on library: {}", libraryId);
        List<SpectrumDO> spectrumDOS = spectrumService.getAll(new SpectrumQuery(), libraryId);
        int count = spectrumDOS.size();

        //1. remove spectra with empty key information
        spectrumDOS.removeIf(spectrumDO -> spectrumDO.getMzs() == null || spectrumDO.getMzs().length == 0 ||
                spectrumDO.getInts() == null || spectrumDO.getInts().length == 0 ||
                spectrumDO.getPrecursorMz() == null || spectrumDO.getPrecursorMz() == 0);
        spectrumDOS.removeIf(spectrumDO -> spectrumDO.getInChIKey() == null || spectrumDO.getInChIKey().equals("") || spectrumDO.getInChIKey().equals("N/A"));
        log.info("remove {} spectra with empty key information, {} spectra left", count - spectrumDOS.size(), spectrumDOS.size());
        count = spectrumDOS.size();

        //2. remove data points with zero intensity
        AtomicInteger dataPoint = new AtomicInteger();
        AtomicInteger totalDataPoint = new AtomicInteger();
        spectrumDOS.parallelStream().forEach(spectrumDO -> {
            List<Double> mzs = new ArrayList<>();
            List<Double> ints = new ArrayList<>();
            for (int i = 0; i < spectrumDO.getMzs().length; i++) {
                if (spectrumDO.getInts()[i] != 0) {
                    mzs.add(spectrumDO.getMzs()[i]);
                    ints.add(spectrumDO.getInts()[i]);
                }
            }
            dataPoint.addAndGet(spectrumDO.getMzs().length - mzs.size());
            totalDataPoint.addAndGet(spectrumDO.getMzs().length);
            spectrumDO.setMzs(mzs.stream().mapToDouble(Double::doubleValue).toArray());
            spectrumDO.setInts(ints.stream().mapToDouble(Double::doubleValue).toArray());
        });
        log.info("remove {} zero data points, {} spectra left, {}% data points removed", dataPoint, spectrumDOS.size(), dataPoint.get() * 100.0 / totalDataPoint.get());
        spectrumDOS.removeIf(spectrumDO -> spectrumDO.getMzs() == null || spectrumDO.getInts() == null || spectrumDO.getMzs().length == 0 || spectrumDO.getInts().length == 0);
        log.info("remove {} spectra with zero data points, {} spectra left", count - spectrumDOS.size(), spectrumDOS.size());
        count = spectrumDOS.size();

        //3. remove spectra except MS2
        spectrumDOS.removeIf(spectrumDO -> !spectrumDO.getMsLevel().equals(MsLevel.MS2.getCode()));
        log.info("remove {} spectra not MS2, {} spectra left", count - spectrumDOS.size(), spectrumDOS.size());
        count = spectrumDOS.size();

        //4. remove low resolution data (the difference between the precursorMz and the nearest m/z is larger than 10ppm)
        spectrumDOS.removeIf(spectrumDO -> ArrayUtil.findNearestDiff(spectrumDO.getMzs(), spectrumDO.getPrecursorMz()) > 10 * Constants.PPM * spectrumDO.getPrecursorMz());
        log.info("remove {} spectra with low resolution, {} spectra left", count - spectrumDOS.size(), spectrumDOS.size());

        spectrumService.remove(new SpectrumQuery(), libraryId);
        spectrumService.insert(spectrumDOS, libraryId);
        log.info("finish noise filter on library: {}", libraryId);
    }

    public void filterZeroPoint(String libraryId) {
        log.info("start remove zero data points on library: {}", libraryId);
        List<SpectrumDO> spectrumDOS = spectrumService.getAll(new SpectrumQuery(), libraryId);

        spectrumDOS.removeIf(spectrumDO -> spectrumDO.getMzs() == null || spectrumDO.getMzs().length == 0 || spectrumDO.getInts() == null || spectrumDO.getInts().length == 0);
        int dataPoint = 0;
        int totalDataPoint = 0;
        for (SpectrumDO spectrumDO : spectrumDOS) {
            List<Double> mzs = new ArrayList<>();
            List<Double> ints = new ArrayList<>();
            for (int i = 0; i < spectrumDO.getMzs().length; i++) {
                if (spectrumDO.getInts()[i] == 0d) {
                    continue;
                }
                mzs.add(spectrumDO.getMzs()[i]);
                ints.add(spectrumDO.getInts()[i]);
            }
            dataPoint += spectrumDO.getMzs().length - mzs.size();
            totalDataPoint += spectrumDO.getMzs().length;
            spectrumDO.setMzs(mzs.stream().mapToDouble(Double::doubleValue).toArray());
            spectrumDO.setInts(ints.stream().mapToDouble(Double::doubleValue).toArray());
        }
        log.info("remove {} zero data points, {} spectra left, {}% data points removed", dataPoint, spectrumDOS.size(), dataPoint * 100.0 / totalDataPoint);
        spectrumService.remove(new SpectrumQuery(), libraryId);
        spectrumService.insert(spectrumDOS, libraryId);
        log.info("finish  on library: {}", libraryId);
    }

}
