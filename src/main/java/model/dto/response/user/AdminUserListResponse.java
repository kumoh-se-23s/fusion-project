package model.dto.response.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.dto.response.Response;

import java.util.List;

/**
 * 유저 전체 조회 (GET /internal/user?limit={amount}) 응답 <br>
 * API 명세서처럼 { "users" : [ 유저 정보 목록 ] } 모양으로 내보낸다
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminUserListResponse extends Response {
    private List<AdminUserResponse> users;
}
