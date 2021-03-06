package com.gsafety.dawn.community.manage.service.serviceimpl.refactor;

import com.gsafety.dawn.community.common.util.CommonUtil;
import com.gsafety.dawn.community.common.util.StringUtil;
import com.gsafety.dawn.community.manage.contract.model.refactor.BuildingUnitStatistics;
import com.gsafety.dawn.community.manage.contract.model.refactor.ReportingStaffStatistics;
import com.gsafety.dawn.community.manage.contract.model.refactor.TroubleshootRecord;
import com.gsafety.dawn.community.manage.contract.service.refactor.TroubleshootRecordService;
import com.gsafety.dawn.community.manage.service.datamappers.refactor.PersonBaseMapper;
import com.gsafety.dawn.community.manage.service.datamappers.refactor.ReportingStaffMapper;
import com.gsafety.dawn.community.manage.service.datamappers.refactor.TroubleshootRecordMapper;
import com.gsafety.dawn.community.manage.service.entity.refactor.*;
import com.gsafety.dawn.community.manage.service.repository.refactor.TroubleshootHistoryRecordRepository;
import com.gsafety.dawn.community.manage.service.repository.refactor.PersonBaseRepository;
import com.gsafety.dawn.community.manage.service.repository.refactor.TroubleshootRecordRepository;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.transaction.Transactional;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class TroubleshootRecordServiceImpl implements TroubleshootRecordService {

    @Autowired
    private PersonBaseRepository personBaseRepository;

    @Autowired
    private TroubleshootRecordRepository troubleshootRecordRepository;

    @Autowired
    private TroubleshootHistoryRecordRepository troubleshootHistoryRecordRepository;

    @Autowired
    private PersonBaseMapper personBaseMapper;

    @Autowired
    private TroubleshootRecordMapper troubleshootRecordMapper;

    @Autowired
    private ReportingStaffMapper reportingStaffMapper;

    @Autowired
    private CommonUtil commonUtil;

    @Override
    public boolean add(TroubleshootRecord troubleshootRecord) {
        try {
            if (troubleshootRecord == null) {
                return false;
            }
            if (troubleshootRecord.getPersonBase() == null) {
                return false;
            }
            if (StringUtil.isEmpty(troubleshootRecord.getPersonBase().getName())) {
                return false;
            }
            if (StringUtil.isEmpty(troubleshootRecord.getPersonBase().getPhone())) {
                return false;
            }
            PersonBaseEntity personBaseEntity = personBaseRepository.findByNameAndPhone(troubleshootRecord.getPersonBase().getName(), troubleshootRecord.getPersonBase().getPhone());
            if (personBaseEntity == null) {
                personBaseEntity = personBaseMapper.modelToEntity(troubleshootRecord.getPersonBase());
                if (!troubleshootRecord.getIsByPhone()) {
                    personBaseEntity.setId(UUID.randomUUID().toString());
                }
                personBaseEntity.setTroubleshootRecord(null);
                personBaseEntity.setTroubleshootRecordId(null);
                personBaseRepository.save(personBaseEntity);
            }
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
            TroubleshootRecordEntity troubleshootRecordEntity = troubleshootRecordRepository.findByPersonBaseId(personBaseEntity.getId());
            troubleshootRecord.setPersonBaseId(personBaseEntity.getId());
            if (troubleshootRecordEntity == null) {
                troubleshootRecordEntity = troubleshootRecordMapper.modelToEntity(troubleshootRecord);
            } else {
                troubleshootRecordEntity = commonUtil.mapper(troubleshootRecordMapper.modelToEntity(troubleshootRecord), troubleshootRecordEntity);
            }
            troubleshootRecordEntity.setCreateDate(format.parse(format.format(troubleshootRecord.getCreateTime())));
            troubleshootRecordRepository.save(troubleshootRecordEntity);
            TroubleshootHistoryRecordEntity troubleshootHistoryRecordEntity = new TroubleshootHistoryRecordEntity();
            troubleshootHistoryRecordEntity = commonUtil.mapper(troubleshootRecordEntity, troubleshootHistoryRecordEntity);
            troubleshootHistoryRecordEntity.setId(UUID.randomUUID().toString());
            troubleshootHistoryRecordEntity.setPersonBase(null);
            troubleshootHistoryRecordEntity.setPersonBaseId(personBaseEntity.getId());
            troubleshootHistoryRecordRepository.save(troubleshootHistoryRecordEntity);
            personBaseEntity = personBaseRepository.getOne(personBaseEntity.getId());
            personBaseEntity.setTroubleshootRecordId(troubleshootRecordEntity.getId());
            personBaseRepository.save(personBaseEntity);
            return troubleshootRecordRepository.getOne(troubleshootRecord.getId()) != null;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean update(TroubleshootRecord troubleshootRecord) {
        try {
            if (troubleshootRecord == null) {
                return false;
            }
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
            TroubleshootRecordEntity troubleshootRecordEntity = troubleshootRecordRepository.findByPersonBaseId(troubleshootRecord.getPersonBaseId());
            if (troubleshootRecordEntity == null) {
                return false;
            } else {
                troubleshootRecordEntity = commonUtil.mapper(troubleshootRecordMapper.modelToEntity(troubleshootRecord), troubleshootRecordEntity);
            }
            troubleshootRecordEntity.setCreateDate(format.parse(format.format(troubleshootRecord.getCreateTime())));
            troubleshootRecordRepository.save(troubleshootRecordEntity);
            TroubleshootHistoryRecordEntity troubleshootHistoryRecordEntity = new TroubleshootHistoryRecordEntity();
            troubleshootHistoryRecordEntity = commonUtil.mapper(troubleshootRecordEntity, troubleshootHistoryRecordEntity);
            troubleshootHistoryRecordEntity.setId(UUID.randomUUID().toString());
            troubleshootHistoryRecordEntity.setPersonBase(null);
            troubleshootHistoryRecordEntity.setPersonBaseId(troubleshootRecordEntity.getPersonBaseId());
            troubleshootHistoryRecordRepository.save(troubleshootHistoryRecordEntity);
            return troubleshootRecordRepository.getOne(troubleshootRecord.getId()).getCreateTime() == troubleshootRecord.getCreateTime();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<ReportingStaffStatistics> getReportingStaffStatistics(String multiTenancy) {
        if (StringUtil.isEmpty(multiTenancy)) {
            return Collections.emptyList();
        }
        List<PlotReportingStaffEntity> plotReportingStaffEntities = personBaseRepository.findPlotReportingStaff(multiTenancy);
        if (CollectionUtils.isEmpty(plotReportingStaffEntities)) {
            return Collections.emptyList();
        }
        return reportingStaffMapper.entitiesToModels(plotReportingStaffEntities);
    }

    @Override
    public List<BuildingUnitStatistics> getBuildingUnitStatistics(String plotId) {
        try {
            if (StringUtil.isEmpty(plotId)) {
                return Collections.emptyList();
            }
            List<BuildingUnitStaffEntity> buildingUnitStaffEntities = troubleshootRecordRepository.findBuildingUnitStaff(plotId);
            if (CollectionUtils.isEmpty(buildingUnitStaffEntities)) {
                return Collections.emptyList();
            }
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
            Date date = format.parse(format.format(new Date()));
            List<BuildingUnitStatistics> result = new ArrayList<>();
            Map<String, List<BuildingUnitStaffEntity>> groupBuildingUnitNumbers = buildingUnitStaffEntities.stream().collect(Collectors.groupingBy(BuildingUnitStaffEntity::getBuildingUnitNumber));
            for (Map.Entry<String, List<BuildingUnitStaffEntity>> entry :
                    groupBuildingUnitNumbers.entrySet()) {

                if (!CollectionUtils.isEmpty(entry.getValue())) {
                    BuildingUnitStatistics buildingUnitStatistics = new BuildingUnitStatistics();
                    buildingUnitStatistics.setBuilding(entry.getValue().get(0).getBuilding());
                    buildingUnitStatistics.setPlotId(plotId);
                    buildingUnitStatistics.setUnitNumber(entry.getValue().get(0).getUnitNumber());
                    Long checkedCount = entry.getValue().stream().filter(f -> f.getCreateDate().getTime() == date.getTime()).mapToLong(m -> m.getCount()).sum();
                    buildingUnitStatistics.setCheckedCount(checkedCount.intValue());
                    Long unCheckedCount = entry.getValue().stream().filter(f -> f.getCreateDate().getTime() != date.getTime()).mapToLong(m -> m.getCount()).sum();
                    buildingUnitStatistics.setUnCheckedCount(unCheckedCount.intValue());
                    result.add(buildingUnitStatistics);
                }
            }
            return result;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
