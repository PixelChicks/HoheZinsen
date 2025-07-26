package com.InterestRatesAustria.InterestRatesAustria.repository;

import com.InterestRatesAustria.InterestRatesAustria.model.entity.TextSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TextSectionRepository extends JpaRepository<TextSection, Long> {

    @Query("SELECT t FROM TextSection t WHERE t.moreInfo.id = :moreInfoId AND t.deletedAt IS NULL")
    List<TextSection> findByMoreInfoId(@Param("moreInfoId") Long moreInfoId);

    @Query("SELECT t FROM TextSection t WHERE t.sectionIdentifier = :sectionIdentifier AND t.deletedAt IS NULL")
    Optional<TextSection> findBySectionIdentifier(@Param("sectionIdentifier") String sectionIdentifier);

    @Modifying
    @Query("DELETE FROM TextSection t WHERE t.moreInfo.id = :moreInfoId AND t.deletedAt IS NULL")
    void deleteByMoreInfoId(@Param("moreInfoId") Long moreInfoId);
}