package vn.com.routex.hub.analytics.processor.infrastructure.cache.redisson;

import java.util.List;

public interface RedisDistributedService {

    RedisDistributedLocker getMultiLock(List<String> lockKeys);
    RedisDistributedLocker getDistributedLock(String lockKey);
}
