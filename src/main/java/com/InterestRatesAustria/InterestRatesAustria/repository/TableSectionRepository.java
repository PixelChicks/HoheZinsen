package com.InterestRatesAustria.InterestRatesAustria.repository;

import com.InterestRatesAustria.InterestRatesAustria.model.entity.TableSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TableSectionRepository extends JpaRepository<TableSection, Long> {

    @Query("SELECT t FROM TableSection t WHERE t.moreInfo.id = :moreInfoId AND t.deletedAt IS NULL")
    List<TableSection> findByMoreInfoId(@Param("moreInfoId") Long moreInfoId);

    @Query("SELECT t FROM TableSection t WHERE t.sectionIdentifier = :sectionIdentifier AND t.deletedAt IS NULL")
    Optional<TableSection> findBySectionIdentifier(@Param("sectionIdentifier") String sectionIdentifier);

    @Modifying
    @Query("DELETE FROM TableSection t WHERE t.moreInfo.id = :moreInfoId AND t.deletedAt IS NULL")
    void deleteByMoreInfoId(@Param("moreInfoId") Long moreInfoId);
}