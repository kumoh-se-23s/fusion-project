package service.external;

import exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import model.domain.*;
import model.dto.request.PKQueryRequest;
import model.dto.request.funding.CreateFundingRequest;
import model.dto.response.PKResponse;
import model.dto.response.donate.DonateUserAddressResponse;
import model.dto.response.funding.FundingRewardResponse;
import model.dto.response.reward.RewardResponse;
import repository.*;
import service.BaseService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class FundingService extends BaseService {
    private final FundingRepository fundingRepository;
    private final DonateRepository donateRepository;
    private final RewardRepository rewardRepository;
    private final UserRepository userRepository;
    private final FundingCategoryRepository fundingCategoryRepository;

    //펀딩의 리워드 정보와 리워드별 후원자 배송지
    public FundingRewardResponse getFundingByReward(PKQueryRequest request) {
        return tx.execute(true, () -> {
            Funding funding = fundingRepository.findById(request.getPk())
                    .orElseThrow(NotFoundException::new);

            List<Reward> rewardList = rewardRepository.findByFunding(funding.getPk());
            List<Long> rewardPKs = new ArrayList<>();
            for (Reward reward : rewardList) {
                rewardPKs.add(reward.getPk());
            }
            List<Donate> donateList = donateRepository.findByRewards(rewardPKs);
            List<RewardResponse> rewardResponses = new ArrayList<>();

            for (Reward reward : rewardList) {
                List<DonateUserAddressResponse> donateUserAddressResponses = new ArrayList<>();
                for (Donate donate : donateList) {
                    if (donate.getReward().getPk().equals(reward.getPk())) {
                        donateUserAddressResponses.add(
                            DonateUserAddressResponse.builder()
                                .donatePK(donate.getPk())
                                .userPK(donate.getUser().getPk())
                                .address(donate.getUser().getAddress())
                                .build()
                        );
                    }
                }
                rewardResponses.add(
                        RewardResponse.builder()
                            .rewardPk(reward.getPk())
                            .name(reward.getName())
                            .maxQuantity(reward.getMaxQuantity())
                            .remainQuantity(reward.getRemainQuantity())
                            .requiredAmount(reward.getRequiredAmount())
                            .provideDate(reward.getProvideDate())
                            .donateUserInfo(donateUserAddressResponses)
                            .build()
                );
            }
            return FundingRewardResponse.builder()
                .fundingPk(funding.getPk())
                .purposeAmount(funding.getPurposeAmount())
                .donateAmount(funding.getDonateAmount())
                .rewards(rewardResponses)
                .build();
        });
    }

    public PKResponse createFunding(CreateFundingRequest request) {
        AppUser maker = userRepository.findById(request.getMakerPK())
                .orElseThrow(NotFoundException::new);

        Funding funding = Funding.builder()
                .maker(maker)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .purposeAmount(request.getPurposeAmount())
                .donateAmount(request.getDonateAmount())
                .text(request.getText())
                .imgFile(request.getImgFile())
                .build();

        Funding resultFunding = fundingRepository.create(funding);
        List<Category> categoryList = CategoryRepository.findByPKs(request.getCategoryPKs());
        for (Category category : categoryList) {
            FundingCategory fundingCategory = FundingCategory.builder()
                    .category(category)
                    .funding(resultFunding)
                    .build();

        }


    }

}
