package repository;

import annotation.PK;
import model.domain.UserRoleRelation;

import java.util.Optional;

public class UserRoleRelationRepository extends BaseRepositoryImpl<UserRoleRelation, Long> {

    public Optional<UserRoleRelation> findUserRoleByUserPk(long userPk) {
        return findById(userPk);
    }
}
