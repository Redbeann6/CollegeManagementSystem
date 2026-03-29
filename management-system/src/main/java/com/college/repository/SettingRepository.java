package com.college.repository;

import com.college.model.Setting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SettingRepository extends JpaRepository<Setting, Long> {
    Optional<Setting> findBySettingKey(String settingKey);

    List<Setting> findByCategoryOrderBySortOrderAsc(String category);

    @Query("SELECT s FROM Setting s WHERE s.category = :category AND s.isEnabled = true ORDER BY s.sortOrder ASC")
    List<Setting> findEnabledByCategoryOrderBySortOrder(@Param("category") String category);

    boolean existsBySettingKey(String settingKey);
}