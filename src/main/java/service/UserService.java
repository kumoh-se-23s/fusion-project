package service;

import model.domain.LocalAccount;
import model.dto.AccountData;

public class UserService {

    public void login(AccountData accountData) {
        String id = accountData.getId();
        String password = accountData.getPassword();
    }
}
