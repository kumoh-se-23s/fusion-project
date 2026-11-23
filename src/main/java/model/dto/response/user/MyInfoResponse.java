package model.dto.response.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.dto.response.Response;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MyInfoResponse extends Response {
    private Long userId;

    private String name;

    private String nickname;

    private String email;

    private String postalCode;

    private String address;

    private String phone;

    private LocalDateTime createdAt;

}
