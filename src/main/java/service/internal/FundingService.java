package service.internal;

import constant.FundingState;
import exception.NotFoundException;
import exception.NotPendingFundingException;
import lombok.RequiredArgsConstructor;
import model.domain.Funding;
import model.dto.response.PKResponse;
import repository.FundingRepository;
import service.BaseService;

@RequiredArgsConstructor
public class FundingService extends BaseService {
    private final FundingRepository fundingRepository;

    //4. 메이커의 펀딩 프로젝트 승인
    public PKResponse updateFundingApprove(Long fundingPK) {
        return tx.execute(false, () -> {
            Funding funding = fundingRepository.findById(fundingPK)
                    .orElseThrow(NotFoundException::new);
            if (funding.getState() != FundingState.PENDING_APPROVAL) {
                throw new NotPendingFundingException();
            }
            Funding approved = fundingRepository.updateByApprove(funding.getPk())
                    .orElseThrow(NotFoundException::new);
            return new PKResponse(approved.getPk());
        });
    }
}
