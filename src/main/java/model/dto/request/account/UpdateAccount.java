package model.dto.request.account;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import model.dto.BaseData;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateAccount extends BaseData {
    private String currentPassword;

    private String newPassword;
}
