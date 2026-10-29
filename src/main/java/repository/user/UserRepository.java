package repository.user;

import model.domain.AppUser;
import model.dto.UserData;
import model.dto.request.user.UpdateUserRequest;
import repository.BaseRepositoryImpl;
import repository.Filter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UserRepository extends BaseRepositoryImpl<AppUser,Long> {

    private void putIfNotNull(Map<String, Object> resultMap, String key, Object value) {
        if(value != null) {
            resultMap.put(key, value);
        }
    }

    public Optional<AppUser> findUser(Long userPk) {
         List<AppUser> result = find(Filter.eq("pk", userPk).limit(1));

         if(result.isEmpty()) {
             return Optional.empty();
         } else {
             return Optional.of(result.getFirst());
         }
    }

    public Optional<AppUser> findByUserPk(long userPk) {
        return findById(userPk);
    }

    public Optional<Long> updateUser(long userPk, UserData userData) {
        Map<String, Object> updateData = new HashMap<>();

        putIfNotNull(updateData, "name" , userData.getName());
        putIfNotNull(updateData, "email" , userData.getEmail());
        putIfNotNull(updateData, "address" , userData.getAddress());
        putIfNotNull(updateData, "postalCode" , userData.getPostalCode());
        putIfNotNull(updateData, "phone" , userData.getPhone());

        Optional<AppUser> updateResult = update(userPk, updateData);

        if(updateResult.isPresent()) {
            return Optional.of(updateResult.get().getPk());
        }

        return Optional.empty();
    }
}