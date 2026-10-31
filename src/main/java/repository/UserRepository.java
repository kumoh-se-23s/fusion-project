package repository;

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

    private void putIfNotNull(Map<String, Object> resultMap, String key, Object value) {
        if(value != null) {
            resultMap.put(key, value);
        }
    }

    public Optional<Long> updateUser(long userPk, UserData userData) {
        Map<String, Object> updateData = new HashMap<>();

        putIfNotNull(updateData, "name" , userData.getName());
        putIfNotNull(updateData, "email" , userData.getEmail());
        putIfNotNull(updateData, "address" , userData.getAddress());
        putIfNotNull(updateData, "postal" , userData.getPostal());
        putIfNotNull(updateData, "phone" , userData.getPhone());

        Optional<AppUser> updateResult = update(userPk, updateData);

        return updateResult.map(AppUser::getPk);
    }
}