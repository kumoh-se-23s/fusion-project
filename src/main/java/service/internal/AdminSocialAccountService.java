package service.internal;

import exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import model.domain.SocialAccount;
import model.dto.response.DeleteResponse;
import model.dto.response.account.SocialAccountResponse;
import repository.SocialAccountRepository;
import service.BaseService;

@RequiredArgsConstructor
public class AdminSocialAccountService extends BaseService {
    private final SocialAccountRepository socialAccountRepository;

    /**
     * 소셜 계정 단건 조회
     *
     * @param socialAccountId
     * @return SocialAccountResponse
     */
    public SocialAccountResponse getSocialAccount(long socialAccountId) {
        return tx.execute(() -> {
            SocialAccount socialAccount = socialAccountRepository.findById(socialAccountId).orElseThrow(NotFoundException::new);
            return SocialAccountResponse.builder()
                    .socialAccountId(socialAccountId)
                    .userId(socialAccount.getAppUser().getId())
                    .socialProvider(socialAccount.getSocialProvider())
                    .build();
        });
    }

    /**
     * 소셜 계정 삭제
     *
     * @param socialAccountId
     * @return DeleteResponse
     */
    public DeleteResponse deleteSocialAccount(long socialAccountId) {
        return tx.execute(() -> {
            if (!socialAccountRepository.isExistById(socialAccountId)) {
                throw new NotFoundException();
            }

            boolean result = socialAccountRepository.delete(socialAccountId);
            return new DeleteResponse(result);
        });
    }
}
