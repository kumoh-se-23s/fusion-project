package repository;

import model.domain.UserRoleRelation;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UserRoleRelationRepository extends BaseRepositoryImpl<UserRoleRelation, Long> {

    //사용자와 역할을 잇는 연결을 단건 조회
    public Optional<UserRoleRelation> findByUserAndRole(long userId, long roleId) {
        List<UserRoleRelation> result = find(Filter.builder()
                .conditions(Map.of("user", userId, "role", roleId))
                .limit(1)
                .build());

        return result.stream().findFirst();
    }

    //fk인 user_id를 사용해서 찾는 메서드 (Filter 키는 컬럼명 user_fk가 아니라 필드명 user)
    public List<UserRoleRelation> findByUserId(long userId) {

        return find(Filter.builder()
                .conditions(Map.of("user", userId))
                .build());
    }


}
