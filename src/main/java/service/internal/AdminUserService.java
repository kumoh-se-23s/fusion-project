package service.internal;

import constant.Role;
import constant.RedisKey;
import exception.AlreadyExistException;
import exception.InvalidRequestException;
import exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import model.domain.AppUser;
import model.domain.UserRole;
import model.domain.UserRoleRelation;
import model.dto.request.user.AdminUpdateUserRequest;
import model.dto.response.DeleteResponse;
import model.dto.response.IdResponse;
import model.dto.response.user.AdminUserListResponse;
import model.dto.response.user.AdminUserResponse;
import repository.Filter;
import repository.UserRepository;
import repository.UserRoleRelationRepository;
import repository.UserRoleRepository;
import service.BaseService;
import util.RedisUtil;

import java.util.List;

@RequiredArgsConstructor
public class AdminUserService extends BaseService {
    private static final int BAD_REQUEST = 400;

    private final UserRepository userRepository;
    private final RedisUtil redisUtil;

    /**
     * 유저 전체 조회 (GET /internal/user?limit={amount}) <br>
     * limit만큼 유저 목록을 돌려준다. limit을 안 보내면 전체를 돌려준다
     *
     * @param limit 조회 상한 (없으면 null, 1 이상이어야 함)
     * @return AdminUserListResponse { users: [...] }
     */
    public AdminUserListResponse getAllUsers(Integer limit) {
        if (limit != null && limit <= 0) {
            throw new InvalidRequestException("limit은 1 이상이어야 합니다");
        }

        return tx.execute(true, () -> {
            List<AppUser> users = userRepository.find(Filter.builder()
                    .limit(limit)
                    .build());

            return AdminUserListResponse.builder()
                    .users(users.stream().map(this::toUserResponse).toList())
                    .build();
        });
    }

    /**
     * 유저 단건 조회 (GET /internal/user/{user_id}) <br>
     * 유저 한 명의 정보를 명세서의 응답 형태로 돌려준다. 없으면 404
     *
     * @param userId 조회할 유저 id
     * @return AdminUserResponse
     */
    public AdminUserResponse getUser(long userId) {
        return tx.execute(true, () -> {
            AppUser user = userRepository.findById(userId).orElseThrow(NotFoundException::new);
            return toUserResponse(user);
        });
    }

    //AppUser -> 명세서 응답 형태. 전체 조회와 단건 조회가 같은 모양을 쓴다
    private AdminUserResponse toUserResponse(AppUser user) {
        return AdminUserResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .postalCode(user.getPostal())
                .address(user.getAddress())
                .phone(user.getPhone())
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * 유저 수정 (PATCH /internal/user/{user_id}) <br>
     * 관리자가 유저 정보를 수정한다. 보낸 값만 수정하고, 다른 유저가 쓰는 닉네임/이메일로 바꾸려 하면 409
     *
     * @param userId  수정할 유저 id
     * @param request 수정할 값 (보내지 않은 값은 null)
     * @return IdResponse 수정된 유저 id
     */
    public IdResponse editUser(long userId, AdminUpdateUserRequest request) {
        return tx.execute(() -> {
            AppUser user = userRepository.findById(userId).orElseThrow(NotFoundException::new);

            //값을 보냈고, 지금 값과 다를 때만 중복 검사 (안 보냈으면 null)
            String nickname = request.getNickname();
            if (nickname != null
                    && !nickname.equals(user.getNickname())
                    && userRepository.isExistNickname(nickname)) {
                throw new AlreadyExistException("이미 사용 중인 닉네임입니다");
            }

            String email = request.getEmail();
            if (email != null
                    && !email.equals(user.getEmail())
                    && userRepository.isExistEmail(email)) {
                throw new AlreadyExistException("이미 사용 중인 이메일입니다");
            }

            Long updateResult = userRepository.updateUser(userId, request.toUpdateMap()).orElseThrow(NotFoundException::new);

            return new IdResponse(updateResult);
        });
    }

    public DeleteResponse deleteUser(long userId) {
        DeleteResponse response = tx.execute(() -> {
            if (!userRepository.isExistById(userId)) {
                throw new NotFoundException();
            }

            boolean result = userRepository.delete(userId);
            return new DeleteResponse(result);
        });

        redisUtil.delete(RedisKey.ACCESS, userId);
        redisUtil.delete(RedisKey.REFRESH, userId);

        return response;
    }
}
