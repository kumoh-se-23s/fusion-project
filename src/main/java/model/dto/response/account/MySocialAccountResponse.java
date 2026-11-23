package model.dto.response.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.dto.response.Response;

import java.util.List;

/**
 * 내 소셜 계정 목록 (GET /account/social)
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MySocialAccountResponse extends Response {
    //연동된 소셜 계정이 없으면 빈 목록
    private List<SocialAccountResponse> socialAccounts;
}
