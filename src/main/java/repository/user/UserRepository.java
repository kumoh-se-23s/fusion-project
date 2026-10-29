package repository.user;

import model.domain.AppUser;
import repository.BaseRepositoryImpl;
import repository.Filter;

import java.util.List;
import java.util.Optional;

public class UserRepository extends BaseRepositoryImpl<AppUser,Long> {
    public Optional<AppUser> findUser(Long userPk) {
         List<AppUser> result = find(Filter.eq("pk", userPk).limit(1));

         if(result.isEmpty()) {
             return Optional.empty();
         } else {
             return Optional.of(result.getFirst());
         }
    }

    public void createUser() {

    }
}