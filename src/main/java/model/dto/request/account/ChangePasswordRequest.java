package model.dto.request.account;

import lombok.*;
import model.dto.BaseData;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordRequest extends BaseData {
    private String currentPassword;

    private String newPassword;
}
