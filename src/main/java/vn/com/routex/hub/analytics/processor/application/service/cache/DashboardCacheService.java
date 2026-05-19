package vn.com.routex.hub.analytics.processor.application.service.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.stereotype.Service;
import vn.com.go.routex.identity.security.log.SystemLog;
import vn.com.routex.hub.analytics.processor.interfaces.model.dashboard.response.MerchantDashboardResponse;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DashboardCacheService {

    private final RedissonClient redissonClient;
    private final ObjectMapper objectMapper;
    private final SystemLog sLog = SystemLog.getLogger(this.getClass());

    private static final String DASHBOARD_CACHE_KEY = "dashboard:merchant:%s:filter:%s";
    private static final String DASHBOARD_PATTERN = "dashboard:merchant:%s:*";
    private static final Duration TTL = Duration.ofMinutes(15);

    public Optional<MerchantDashboardResponse> getDashboard(String merchantId, String filterType) {
        String key = String.format(DASHBOARD_CACHE_KEY, merchantId, filterType);
        RBucket<String> bucket = redissonClient.getBucket(key, StringCodec.INSTANCE);
        String json = bucket.get();

        if (json == null) return Optional.empty();

        try {
            return Optional.of(objectMapper.readValue(json, MerchantDashboardResponse.class));
        } catch (Exception e) {
            sLog.error("Error deserializing dashboard cache for merchant {}: {}", merchantId, e.getMessage());
            return Optional.empty();
        }
    }

    public void putDashboard(String merchantId, String filterType, MerchantDashboardResponse response) {
        String key = String.format(DASHBOARD_CACHE_KEY, merchantId, filterType);
        RBucket<String> bucket = redissonClient.getBucket(key, StringCodec.INSTANCE);
        try {
            String json = objectMapper.writeValueAsString(response);
            bucket.set(json, TTL);
        } catch (Exception e) {
            sLog.error("Error serializing dashboard cache for merchant {}: {}", merchantId, e.getMessage());
        }
    }

    public void evictMerchantDashboardCache(String merchantId) {
        String pattern = String.format(DASHBOARD_PATTERN, merchantId);
        Iterable<String> keys = redissonClient.getKeys().getKeysByPattern(pattern, 100);

        List<String> keyList = new ArrayList<>();
        keys.forEach(keyList::add);

        if (!keyList.isEmpty()) {
            redissonClient.getKeys().delete(keyList.toArray(new String[0]));
            sLog.info("Evicted {} dashboard cache keys for merchant: {}", keyList.size(), merchantId);
        }
    }
}
