package repository;

import annotation.Id;
import model.domain.UserRole;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UserRoleRepository extends BaseRepositoryImpl<UserRole, Long> {

    public Optional<UserRole> findByName(String roleName) {
        List<UserRole> result = find(Filter.builder()
                .conditions(Map.of("roleName", roleName))
                .limit(1)
                .build());

        if(result.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(result.get(0));

    }
}
