package vn.com.routex.hub.analytics.processor.infrastructure.cache.redisson.impl;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import vn.com.go.routex.identity.security.log.SystemLog;
import vn.com.routex.hub.analytics.processor.infrastructure.cache.redisson.RedisDistributedLocker;
import vn.com.routex.hub.analytics.processor.infrastructure.cache.redisson.RedisDistributedService;

import java.util.List;
import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
public class RedisDistributedLockerImpl implements RedisDistributedService {

    private final RedissonClient redissonClient;
    private final SystemLog sLog = SystemLog.getLogger(this.getClass());

    @Override
    public RedisDistributedLocker getMultiLock(List<String> lockKeys) {
        RLock[] rlock = lockKeys.stream()
                .map(redissonClient::getLock)
                .toArray(RLock[]::new);


        RLock multiLock = redissonClient.getMultiLock(rlock);

        return new RedisDistributedLocker() {
            @Override
            public boolean tryLock(long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException {
                return multiLock.tryLock(waitTime, leaseTime, unit);
            }

            @Override
            public void lock(long leaseTime, TimeUnit unit) {
                multiLock.lock(leaseTime, unit);
            }

            @Override
            public void unlock() {
                multiLock.unlock();
            }

            @Override
            public boolean isLocked() {
                return multiLock.isLocked();
            }

            @Override
            public boolean isHeldByThread(long threadId) {
                return multiLock.isHeldByThread(threadId);
            }

            @Override
            public boolean isHeldByCurrentThread() {
                return multiLock.isHeldByCurrentThread();
            }
        };
    }

    @Override
    public RedisDistributedLocker getDistributedLock(String lockKey) {
        RLock rLock = redissonClient.getLock(lockKey);

        return new RedisDistributedLocker() {
            @Override
            public boolean tryLock(long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException {
                boolean isLockSuccess = rLock.tryLock(waitTime, leaseTime, unit);
                sLog.info("{} get lock result: {}", lockKey, isLockSuccess);
                return isLockSuccess;
            }

            @Override
            public void lock(long leaseTime, TimeUnit unit) {
                rLock.lock(leaseTime, unit);
            }

            @Override
            public void unlock() {
                if(isLocked() && isHeldByCurrentThread()) {
                    rLock.unlock();
                }
            }

            @Override
            public boolean isLocked() {
                return rLock.isLocked();
            }

            @Override
            public boolean isHeldByThread(long threadId) {
                return rLock.isHeldByThread(threadId);
            }

            @Override
            public boolean isHeldByCurrentThread() {
                return rLock.isHeldByCurrentThread();
            }
        };
    }
}
