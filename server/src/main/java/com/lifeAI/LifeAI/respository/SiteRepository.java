package com.lifeAI.LifeAI.respository;

import com.lifeAI.LifeAI.model.Site;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SiteRepository extends JpaRepository<Site, Long> {
    Optional<Site> getSiteByUrlContaining(String url);
}
