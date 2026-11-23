package model.dto.response.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.dto.response.Response;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LocalAccountResponse extends Response {
    private long localAccountId;

    private long userId;

    private String loginId;
}
