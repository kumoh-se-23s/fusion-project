package model.dto.response.account;

import constant.SocialProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.dto.response.Response;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SocialAccountResponse extends Response {
    private Long socialAccountId;

    private Long userId;

    private SocialProvider socialProvider;
}
