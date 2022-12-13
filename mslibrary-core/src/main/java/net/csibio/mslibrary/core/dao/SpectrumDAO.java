package net.csibio.mslibrary.core.dao;

import com.mongodb.client.result.DeleteResult;
import net.csibio.aird.constant.SymbolConst;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import net.csibio.mslibrary.client.domain.query.SpectrumQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
public class SpectrumDAO extends BaseMultiDAO<SpectrumDO, SpectrumQuery> {

    public static String CollectionName = "spectrum";

    @Override
    protected String getCollectionName(String routerId) {
        if (StringUtils.isNotEmpty(routerId)) {
            return CollectionName + SymbolConst.DELIMITER + routerId;
        } else {
            return CollectionName;
        }
    }


    @Override
    protected Class getDomainClass() {
        return SpectrumDO.class;
    }

    @Override
    protected boolean allowSort() {
        return true;
    }

    @Override
    protected Query buildQueryWithoutPage(SpectrumQuery spectrumQuery) {
        Query query = new Query();
        if (StringUtils.isNotEmpty(spectrumQuery.getId())) {
            query.addCriteria(where("id").is(spectrumQuery.getId()));
        }
        if (StringUtils.isNotEmpty(spectrumQuery.getCompoundId())) {
            query.addCriteria(where("compoundId").is(spectrumQuery.getCompoundId()));
        }
        if (StringUtils.isNotEmpty(spectrumQuery.getLibraryId())) {
            query.addCriteria(where("libraryId").is(spectrumQuery.getLibraryId()));
        }
        if (StringUtils.isNotEmpty(spectrumQuery.getSpectrumId())) {
            query.addCriteria(where("spectrumId").is(spectrumQuery.getSpectrumId()));
        }
        if (spectrumQuery.getMsLevel() != null) {
            query.addCriteria(where("msLevel").is(spectrumQuery.getMsLevel()));
        }
        if (StringUtils.isNotEmpty(spectrumQuery.getCompoundName())) {
            query.addCriteria(where("compoundName").is(spectrumQuery.getCompoundName()));
        }
        //ionSource
        if (StringUtils.isNotEmpty(spectrumQuery.getIonSource())) {
            query.addCriteria(where("ionSource").is(spectrumQuery.getIonSource()));
        }
        //instrument
        if (StringUtils.isNotEmpty(spectrumQuery.getInstrument())) {
            query.addCriteria(where("instrument").is(spectrumQuery.getInstrument()));
        }
        //instrumentType
        if (StringUtils.isNotEmpty(spectrumQuery.getInstrumentType())) {
            query.addCriteria(where("instrumentType").is(spectrumQuery.getInstrumentType()));
        }
        //adduct
        if (StringUtils.isNotEmpty(spectrumQuery.getPrecursorAdduct())) {
            query.addCriteria(where("precursorAdduct").is(spectrumQuery.getPrecursorAdduct()));
        }
        //precursorMz
        if (spectrumQuery.getPrecursorMz() != null) {
            query.addCriteria(where("precursorMz").gte(spectrumQuery.getPrecursorMz() - spectrumQuery.getMzTolerance()).lte(spectrumQuery.getPrecursorMz() + spectrumQuery.getMzTolerance()));
        }
        //exactMass
        if (spectrumQuery.getExactMass() != null) {
            query.addCriteria(where("exactMass").is(spectrumQuery.getExactMass()));
        }
        //inchI
        if (StringUtils.isNotEmpty(spectrumQuery.getInchI())) {
            query.addCriteria(where("inchI").is(spectrumQuery.getInchI()));
        }
        //collisionEnergy
        if (spectrumQuery.getCollisionEnergy() != null) {
            query.addCriteria(where("collisionEnergy").is(spectrumQuery.getCollisionEnergy()));
        }
        //formula
        if (StringUtils.isNotEmpty(spectrumQuery.getFormula())) {
            query.addCriteria(where("formula").is(spectrumQuery.getFormula()));
        }
        //ionMode
        if (StringUtils.isNotEmpty(spectrumQuery.getIonMode())) {
            query.addCriteria(where("ionMode").is(spectrumQuery.getIonMode()));
        }
        if (spectrumQuery.getIds() != null && spectrumQuery.getIds().size() > 0) {
            query.addCriteria(where("id").in(spectrumQuery.getIds()));
        }
        if (spectrumQuery.getCompoundIds() != null && spectrumQuery.getCompoundIds().size() > 0) {
            query.addCriteria(where("compoundId").in(spectrumQuery.getCompoundIds()));
        }
        return query;
    }

    @Override
    public SpectrumDO insert(SpectrumDO spectrumDO, String routerId) {
        if (!mongoTemplate.collectionExists(getCollectionName(routerId))) {
            mongoTemplate.createCollection(getCollectionName(routerId));
            buildIndex(SpectrumDO.class, routerId);
        }
        mongoTemplate.insert(spectrumDO, getCollectionName(routerId));
        return spectrumDO;
    }

    @Override
    public List<SpectrumDO> insert(List<SpectrumDO> spectrumDOS, String routerId) {
        if (!mongoTemplate.collectionExists(getCollectionName(routerId))) {
            mongoTemplate.createCollection(getCollectionName(routerId));
            buildIndex(SpectrumDO.class, routerId);
        }
        mongoTemplate.insert(spectrumDOS, getCollectionName(routerId));
        return spectrumDOS;
    }

    public List<SpectrumDO> getAllByCompoundId(String compoundId, String libraryId) {
        SpectrumQuery query = new SpectrumQuery();
        query.setCompoundId(compoundId);
        return mongoTemplate.find(buildQueryWithoutPage(query), SpectrumDO.class, getCollectionName(libraryId));
    }

    public List<SpectrumDO> getAllByLibraryId(String libraryId) {
        return mongoTemplate.find(buildQueryWithoutPage(new SpectrumQuery()), SpectrumDO.class, getCollectionName(libraryId));
    }

    public List<SpectrumDO> getByIds(List<String> spectraIds, String libraryId) {
        SpectrumQuery query = new SpectrumQuery();
        query.setIds(spectraIds);
        return mongoTemplate.find(buildQueryWithoutPage(query), SpectrumDO.class, getCollectionName(libraryId));
    }

    public long removeAllByLibraryId(String libraryId) {
        Query query = new Query(where("libraryId").is(libraryId));
        DeleteResult result = mongoTemplate.remove(query, SpectrumDO.class, getCollectionName(libraryId));
        return result.getDeletedCount();
    }

    public long removeAllByCompoundId(String compoundId, String libraryId) {
        Query query = new Query(where("compoundId").is(compoundId));
        DeleteResult result = mongoTemplate.remove(query, SpectrumDO.class, getCollectionName(libraryId));
        return result.getDeletedCount();
    }

    public List<SpectrumDO> getByPrecursorMz(Double precursorMz, String libraryId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("precursorMz").gte(precursorMz - 0.01).lte(precursorMz + 0.01));
        return mongoTemplate.find(query, SpectrumDO.class, getCollectionName(libraryId));
    }

}
