package vn.com.routex.hub.analytics.processor.infrastructure.persistence.jpa.finance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.com.routex.hub.analytics.processor.infrastructure.persistence.jpa.finance.entity.MerchantDailyStatsEntity;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MerchantDailyStatsRepository extends JpaRepository<MerchantDailyStatsEntity, String> {
    List<MerchantDailyStatsEntity> findAllByMerchantIdAndStatsDateBetween(String merchantId, LocalDate startDate, LocalDate endDate);
}
