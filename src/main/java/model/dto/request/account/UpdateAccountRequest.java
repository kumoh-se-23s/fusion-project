package model.dto.request.account;

import lombok.*;
import model.dto.BaseData;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateAccountRequest extends BaseData {
    private String currentPassword;

    private String newPassword;
}
