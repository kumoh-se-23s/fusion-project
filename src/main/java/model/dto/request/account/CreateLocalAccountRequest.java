package model.dto.request.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.dto.BaseData;


@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateLocalAccountRequest extends BaseData {
    private String id;

    private String password;

}
