package vn.com.routex.hub.analytics.processor.infrastructure.persistence.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.com.routex.hub.analytics.processor.infrastructure.persistence.jpa.entity.MerchantAnalyticsEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MerchantAnalyticsRepository extends JpaRepository<MerchantAnalyticsEntity, String> {
    Optional<MerchantAnalyticsEntity> findByMerchantIdAndAnalyticsDate(String merchantId, LocalDate analyticsDate);
    List<MerchantAnalyticsEntity> findAllByMerchantIdAndAnalyticsDateBetween(String merchantId, LocalDate startDate, LocalDate endDate);
}
