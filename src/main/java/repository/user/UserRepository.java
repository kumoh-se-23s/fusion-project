package repository.user;

import model.domain.AppUser;
import repository.BaseRepositoryImpl;
import repository.Filter;

public class UserRepository extends BaseRepositoryImpl<AppUser,Long> {
    public AppUser findUser(Long pk) {
        return find(Filter.eq("pk", pk).limit(1)).getFirst();
    }

    public void createUser() {

    }
}