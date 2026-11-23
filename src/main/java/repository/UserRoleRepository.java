package repository;

import constant.Role;
import model.domain.UserRole;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UserRoleRepository extends BaseRepositoryImpl<UserRole, Long> {

    //권한 enum으로 권한 조회 (role_name 컬럼에는 enum 이름이 저장됨)
    public Optional<UserRole> findByRole(Role role) {
        List<UserRole> result = find(Filter.builder()
                .conditions(Map.of("role", role))
                .limit(1)
                .build());

        if(result.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(result.getFirst());

    }
}
