package com.example.astaroswiki.repository;

import com.example.astaroswiki.entity.PageVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PageVersionRepository extends JpaRepository<PageVersion, Long> {
    List<PageVersion> findByPageIdOrderByVersionNumberDesc(Long pageId); // 根据页面ID查询所有版本信息
    Optional<PageVersion> findByPageIdAndVersionNumber(Long pageId, Integer versionNumber); // 根据页面ID和版本号查询版本信息
    Optional<PageVersion> findTopByPageIdOrderByVersionNumberDesc(Long pageId); // 根据页面ID查询最新的版本信息
}
