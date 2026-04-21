package com.kynsof.identity.application.command.user.changeSelectedBusiness;


import com.kynsof.identity.domain.dto.UserSystemDto;
import com.kynsof.identity.domain.interfaces.service.IRedisService;
import com.kynsof.identity.domain.interfaces.service.IUserSystemService;
import com.kynsof.identity.infrastructure.config.IdentityCacheConfig;
import com.kynsof.share.core.domain.bus.command.ICommandHandler;
import com.kynsof.share.core.infrastructure.redis.CacheConfig;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Component
public class ChangeSelectedBusinessCommandHandler implements ICommandHandler<ChangeSelectedBusinessCommand> {
    private final IUserSystemService userSystemService;
    private final IRedisService redisService;
    private final CacheManager cacheManager;

    public ChangeSelectedBusinessCommandHandler(IUserSystemService userSystemService,
                                                 IRedisService redisService,
                                                 CacheManager cacheManager) {
        this.userSystemService = userSystemService;
        this.redisService = redisService;
        this.cacheManager = cacheManager;
    }

    @Override
    public void handle(ChangeSelectedBusinessCommand command) {
        UserSystemDto user = userSystemService.findById(command.getUserId());
        user.setSelectedBusiness(command.getBusinessId());
        userSystemService.update(user);

        // Invalidar caché del USER_CACHE (legacy)
        redisService.deleteKey(CacheConfig.USER_CACHE + "::" + command.getUserId());

        // Invalidar caché del UserMe (USER_INFO_CACHE)
        evictUserInfoCache(user.getKeyCloakId());

        command.setResul(true);
    }

    private void evictUserInfoCache(java.util.UUID keyCloakId) {
        var cache = cacheManager.getCache(IdentityCacheConfig.USER_INFO_CACHE);
        if (cache != null && keyCloakId != null) {
            cache.evict(keyCloakId);
        }
    }
}
