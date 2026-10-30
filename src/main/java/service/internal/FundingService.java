package service.internal;

import constant.FundingState;
import exception.NotFoundException;
import exception.NotPendingFundingException;
import lombok.RequiredArgsConstructor;
import model.domain.Funding;
import model.dto.response.PKResponse;
import repository.FundingRepository;
import service.BaseService;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public class FundingService extends BaseService {
    private final FundingRepository fundingRepository;

    //4. 관리자가 메이커의 펀딩 프로젝트 승인
    public PKResponse updateFundingApprove(Long fundingPK) {
        return tx.execute(false, () -> {
            Funding funding = fundingRepository.findByPK(fundingPK)
                    .orElseThrow(NotFoundException::new);
            if (funding.getState() != FundingState.PENDING_APPROVAL) {
                throw new NotPendingFundingException();
            }
            funding.toBuilder()
                    .updatedAt(LocalDateTime.now())
                    .build();
            Funding approved = fundingRepository.updateByApprove(funding.getPk())
                    .orElseThrow(NotFoundException::new);
            return new PKResponse(approved.getPk());
        });
    }
}
