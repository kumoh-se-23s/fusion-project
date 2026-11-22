package service.internal;

import lombok.RequiredArgsConstructor;
import repository.UserRepository;
import repository.UserRoleRelationRepository;
import repository.UserRoleRepository;
import service.BaseService;
import util.RedisUtil;

@RequiredArgsConstructor
public class UserRoleService extends BaseService {
    //Redis에 저장하는 사용자별 역할 명단의 키 접두사 (예: roles:7)
    private static final String ROLE_KEY_PREFIX = "roles:";

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserRoleRelationRepository userRoleRelationRepository;
    private final RedisUtil redisUtil;
}
