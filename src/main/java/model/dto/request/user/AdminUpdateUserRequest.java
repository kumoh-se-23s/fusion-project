package model.dto.request.user;

import lombok.*;
import model.dto.request.UpdateRequest;

import java.util.Map;

/**
 * 유저 수정 (PATCH /internal/user/{user_id}) 요청 <br>
 * 관리자용이라 내 정보 수정(UpdateUserRequest)과 달리 email도 바꿀 수 있다. 보낸 값(null이 아닌 값)만 수정된다
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminUpdateUserRequest extends UpdateRequest {
    private String name;

    private String nickname;

    private String email;

    //내 정보 조회(MyInfoResponse)와 같은 이름. AppUser에서는 postal 필드다
    private String postalCode;

    private String address;

    private String phone;

    //AppUser의 필드명과 다른 postalCode를 postal로 바꿔서 넘긴다
    @Override
    public Map<String, Object> toUpdateMap() {
        Map<String, Object> updateData = super.toUpdateMap();
        Object postal = updateData.remove("postalCode");
        if (postal != null) {
            updateData.put("postal", postal);
        }
        return updateData;
    }
}
