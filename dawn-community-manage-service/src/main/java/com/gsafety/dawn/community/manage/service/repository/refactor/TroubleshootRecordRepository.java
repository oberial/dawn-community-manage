package com.gsafety.dawn.community.manage.service.repository.refactor;

import com.gsafety.dawn.community.manage.service.entity.refactor.BuildingUnitStaffEntity;
import com.gsafety.dawn.community.manage.service.entity.refactor.TroubleshootRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TroubleshootRecordRepository extends JpaRepository<TroubleshootRecordEntity, String> {

    /**
     * 通过填报事件(年月日)查询排查记录信息
     *
     * @param time 填报时间
     * @return
     */
    @Query("select c from TroubleshootRecordEntity c where to_char(c.createTime,'yyyy-MM-dd')  = :createTime")
    TroubleshootRecordEntity findByCreateTime(@Param("createTime") String time);

    @Query("select c from TroubleshootRecordEntity c where c.personBaseId  = :personBaseId")
    TroubleshootRecordEntity findByPersonBaseId(@Param("personBaseId") String personBaseId);

    @Query("select new com.gsafety.dawn.community.manage.service.entity.refactor.BuildingUnitStaffEntity(count(c),c.building,c.unitNumber,c.createDate,c.plot) " +
            "from TroubleshootRecordEntity c where c.plot  = :plotId " +
            "group by c.building,c.unitNumber,c.createDate,c.plot")
    List<BuildingUnitStaffEntity> findBuildingUnitStaff(@Param("plotId") String plotId);
}
