package service.external;

import constant.SocialProvider;
import exception.AlreadyExistException;
import exception.NotFoundException;
import exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import model.domain.AppUser;
import model.domain.SocialAccount;
import model.dto.SocialUserInfo;
import model.dto.response.DeleteResponse;
import model.dto.response.account.MySocialAccountResponse;
import model.dto.response.account.SocialAccountResponse;
import repository.SocialAccountRepository;
import repository.UserRepository;
import service.BaseService;
import service.OAuthClient;

import java.util.List;

@RequiredArgsConstructor
public class SocialAccountService extends BaseService {
    private final SocialAccountRepository socialAccountRepository;
    private final UserRepository userRepository;
    private final OAuthClient oAuthClient;

    /**
     * 내 소셜 계정 조회 (GET /account/social) <br>
     * 로그인한 유저에게 연동된 소셜 계정 목록을 돌려준다
     *
     * @param userId 토큰에서 꺼낸 로그인한 유저 id (믿을 수 있는 값)
     * @return 소셜 계정 목록 (없으면 빈 목록)
     * @throws UnauthorizedException 토큰은 있지만 유저가 이미 사라짐 (401)
     */
    public MySocialAccountResponse getMySocialAccount(long userId) {
        return tx.execute(true, () -> {
            if (!userRepository.isExistById(userId)) {
                throw new UnauthorizedException();
            }

            List<SocialAccountResponse> socialAccounts = socialAccountRepository.findByUserId(userId).stream()
                    .map(socialAccount -> SocialAccountResponse.builder()
                            .socialAccountId(socialAccount.getId())
                            .userId(userId)
                            .socialProvider(socialAccount.getSocialProvider())
                            .build())
                    .toList();

            return MySocialAccountResponse.builder()
                    .socialAccounts(socialAccounts)
                    .build();
        });
    }

    /**
     * 소셜 계정 연동 (POST /account/social/{provider}) <br>
     * 로그인한 유저 본인에게 소셜 계정을 연결한다
     *
     * @param userId   토큰에서 꺼낸 로그인한 유저 id (믿을 수 있는 값)
     * @param code     소셜 로그인 후 받은 인가 코드
     * @param provider 소셜 제공자 (GOOGLE / KAKAO / NAVER)
     * @return 생성된 소셜 계정 정보
     * @throws AlreadyExistException 이미 연동된 소셜 계정 (409)
     * @throws NotFoundException     유저가 없음
     */
    public SocialAccountResponse linkSocialAccount(long userId, String code, SocialProvider provider) {
        //외부 HTTP 호출은 느릴 수 있으므로 DB 트랜잭션을 열기 전에 끝낸다
        String tokenJson = oAuthClient.exchangeCode(provider, code);
        SocialUserInfo socialUserInfo = oAuthClient.fetchUserInfo(provider, tokenJson);
        String providerUserId = socialUserInfo.getProviderUserId();

        return tx.execute(() -> {
            AppUser appUser = userRepository.findById(userId).orElseThrow(NotFoundException::new);

            //같은 소셜 계정이 이미 (나 또는 다른 유저에게) 연동되어 있음
            if (socialAccountRepository.isExistSocialIdentity(provider, providerUserId)) {
                throw new AlreadyExistException("이미 연동된 소셜 계정입니다");
            }

            //이 유저는 같은 제공자 계정을 이미 하나 연동했음
            if (socialAccountRepository.isExistByUserAndProvider(userId, provider)) {
                throw new AlreadyExistException("이미 연동된 소셜 계정입니다");
            }

            SocialAccount socialAccount = socialAccountRepository.createSocialAccount(providerUserId, appUser, provider);

            return SocialAccountResponse.builder()
                    .socialAccountId(socialAccount.getId())
                    .userId(userId)
                    .socialProvider(provider)
                    .build();
        });
    }



    /**
     * 소셜 계정 연동 해제 <br>
     * 로그인한 유저 본인의 소셜 계정만 해제할 수 있다
     *
     * @param userId          토큰에서 꺼낸 로그인한 유저 id (믿을 수 있는 값)
     * @param socialAccountId 요청으로 들어온 소셜 계정 id (조작될 수 있는 값)
     * @param provider        소셜 제공자
     * @return DeleteResponse
     */
    public DeleteResponse unlinkedSocialAccount(long userId, long socialAccountId, SocialProvider provider) {
        return tx.execute(() -> {
            SocialAccount socialAccount = socialAccountRepository.findById(socialAccountId).orElseThrow(NotFoundException::new);

            //남의 계정이면 존재 여부를 알려 주지 않도록 403 대신 404
            if (!socialAccount.getAppUser().getId().equals(userId)) {
                throw new NotFoundException();
            }

            if (socialAccount.getSocialProvider() != provider) {
                throw new NotFoundException();
            }

            boolean result = socialAccountRepository.delete(socialAccountId);
            return new DeleteResponse(result);
        });
    }
}
