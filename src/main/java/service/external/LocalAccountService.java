package service.external;

import exception.InvalidException;
import exception.InvalidRequestException;
import exception.PasswordMisMatchException;
import exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import model.domain.AppUser;
import model.domain.LocalAccount;
import model.dto.request.account.ChangePasswordRequest;
import model.dto.response.IdResponse;
import repository.LocalAccountRepository;
import repository.UserRepository;
import service.BaseService;
import util.AccountUtil;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

@RequiredArgsConstructor
public class LocalAccountService extends BaseService {
    private static final int BAD_REQUEST = 400;

    private final AccountUtil accountUtil;
    private final UserRepository userRepository;
    private final LocalAccountRepository localAccountRepository;

    /**
     * 로컬 유저 비밀번호 변경 (PATCH /account/change-password) <br>
     * 현재 비밀번호를 확인한 뒤, salt를 새로 만들어 새 비밀번호를 저장한다
     *
     * @param userId  토큰에서 꺼낸 로그인한 유저 id (믿을 수 있는 값)
     * @param request 현재 비밀번호, 새 비밀번호
     * @return 수정된 로컬 계정 id
     * @throws InvalidRequestException    입력 누락, 같은 비밀번호, 로컬 계정 없음 (400)
     * @throws PasswordMisMatchException  현재 비밀번호가 틀림 (400)
     * @throws UnauthorizedException      토큰은 있지만 유저가 이미 사라짐 (401)
     */
    public IdResponse changePassword(long userId, ChangePasswordRequest request) {
        String currentPassword = request.getCurrentPassword();
        String newPassword = request.getNewPassword();

        //DB를 보기 전에 입력부터 검사한다. null이면 hash()에서 터져 500이 나가므로 여기서 400으로 막는다
        if (isBlank(currentPassword) || isBlank(newPassword)) {
            throw new InvalidRequestException("현재 비밀번호와 새 비밀번호를 모두 입력해야 합니다.", BAD_REQUEST);
        }

        if (currentPassword.equals(newPassword)) {
            throw new InvalidRequestException("새 비밀번호가 현재 비밀번호와 같습니다.", BAD_REQUEST);
        }

        return tx.execute(() -> {
            AppUser appUser = userRepository.findById(userId).orElseThrow(UnauthorizedException::new);

            //소셜로만 가입한 유저는 로컬 계정이 없다
            LocalAccount localAccount = localAccountRepository.findByFk(appUser)
                    .orElseThrow(() -> new InvalidRequestException("로컬 계정이 없는 유저입니다.", BAD_REQUEST));

            //equals 대신 matches: 비교 시간이 항상 같아 타이밍 공격을 막는다
            if (!matches(currentPassword, localAccount)) {
                throw new PasswordMisMatchException(BAD_REQUEST);
            }

            String newSalt = accountUtil.generateSalt();
            String hashedPassword = hash(newPassword, newSalt);

            if (!localAccountRepository.updatePassword(localAccount.getId(), hashedPassword, newSalt)) {
                throw new InvalidException("비밀번호 변경에 실패했습니다.");
            }

            return new IdResponse(localAccount.getId());
        });
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    //해시 알고리즘이 없을 때만 나는 체크 예외를 여기서 한 번에 처리한다
    private boolean matches(String inputPassword, LocalAccount localAccount) {
        try {
            return accountUtil.matches(inputPassword, localAccount.getSalt(), localAccount.getPassword());
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new InvalidException(e.getMessage());
        }
    }

    private String hash(String password, String salt) {
        try {
            return accountUtil.hash(password, salt);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new InvalidException(e.getMessage());
        }
    }
}
