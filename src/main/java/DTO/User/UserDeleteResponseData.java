package DTO.User;

import DTO.BaseDTO;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class UserDeleteResponseData extends BaseDTO {

    @NonNull
    private boolean result;

}
