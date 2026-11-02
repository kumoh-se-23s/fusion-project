package model.dto.request.account;

import lombok.*;
import model.dto.BaseData;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeleteAccountRequest extends BaseData {
    private String checkPassword;
}
