package service.internal;

import exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import model.domain.LocalAccount;
import model.dto.response.account.LocalAccountResponse;
import repository.LocalAccountRepository;
import service.BaseService;

@RequiredArgsConstructor
public class AdminLocalAccountService extends BaseService {
    private final LocalAccountRepository localAccountRepository;

    public LocalAccountResponse getLocalAccount(long localAccountId) {
        return tx.execute(() -> {
            LocalAccount localAccount = localAccountRepository.findById(localAccountId).orElseThrow(NotFoundException::new);

            return LocalAccountResponse.builder()
                    .localAccountId(localAccountId)
                    .userId(localAccount.getUser().getId())
                    .loginId(localAccount.getLoginId())
                    .build();
        });
    }
}
