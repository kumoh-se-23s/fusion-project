package model.dto.request.user_role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.dto.request.Request;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AssignRoleRequest extends Request {
    private Long userId;

    private String role;

}
