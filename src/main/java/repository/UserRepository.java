package repository;

import model.Serializer;
import model.domain.AppUser;
import model.domain.UserRole;
import model.dto.UserData;
import model.dto.request.user.UpdateUserRequest;
import repository.BaseRepositoryImpl;
import repository.Filter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UserRepository extends BaseRepositoryImpl<AppUser,Long> {

    //도메인 넘길것
    public Optional<AppUser> createUser(UserData userData) {
        AppUser appUser = Serializer.toDomain(userData, AppUser.class);

        return Optional.of(create(appUser));
    }

    public Optional<Long> updateUser(long userId, Map<String, Object> update) {
        Optional<AppUser> updateResult = update(userId, update);

        return updateResult.map(AppUser::getId);
    }

    public Optional<AppUser> findByEmail(String email) {
         List<AppUser> result = find(Filter.builder()
                .conditions(Map.of("email", email))
                .limit(1)
                .build());

         if(result.isEmpty()) {
             return Optional.empty();
         }

         return Optional.of(result.get(0));
    }

    public boolean isExistEmail(String email) {
        return isExist(Filter.builder()
                .conditions(Map.of("email", email))
                .build());
    }

    public List<AppUser> findByNickName(String nickName) {
        return find(Filter.builder()
                .conditions(Map.of("nickName", nickName))
                .build()
        );
    }
}