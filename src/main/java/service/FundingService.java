package service;

import annotation.Transaction;
import exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import model.domain.AppUser;
import model.domain.Donate;
import model.domain.Funding;
import model.domain.Reward;
import model.dto.request.PKQueryRequest;
import model.dto.response.funding.FundingRewardResponse;
import model.dto.response.funding.FundingRewardResponse.DonateUserAddressResponse;
import model.dto.response.funding.FundingRewardResponse.RewardResponse;
import repository.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class FundingService extends BaseService {
    private final FundingRepository fundingRepository;
    private final DonateRepository donateRepository;
    private final RewardRepository rewardRepository;

    //펀딩의 리워드 정보와 리워드별 후원자 배송지
    @Transaction(name = "funding.reward")
    public FundingRewardResponse getFundingByReward(PKQueryRequest request) {
        Funding funding = fundingRepository.findById(request.getPk())
                .orElseThrow(NotFoundException::new);

        List<Reward> rewardList = rewardRepository.find(
                Filter.builder()
                        .conditions(Map.of("funding", funding.getPk()))
                        .sort(Sort.by(Sort.Order.asc("name")))
                        .build()
        );

        List<FundingRewardResponse.RewardResponse> rewardResponses = new ArrayList<>();
        for (Reward reward : rewardList) {
            List<Donate> donateList = donateRepository.find(
                    Filter.builder()
                            .conditions(Map.of("reward", reward.getPk()))
                            .sort(Sort.by(Sort.Order.asc("pk")))
                            .build()
            );

            List<DonateUserAddressResponse> donateUserList = new ArrayList<>();
            for (Donate donate : donateList) {
                //유저는 펀딩당 한 번만 후원하므로 후원자마다 한 번씩만 조회된다
                AppUser user = donate.getUser().get();
                donateUserList.add(new DonateUserAddressResponse(
                        donate.getPk(),
                        user.getPk(),
                        user.getName(),
                        user.getPhone(),
                        user.getPostal(),
                        user.getAddress(),
                        donate.getQuantity()));
            }

            rewardResponses.add(new RewardResponse(
                    reward.getPk(),
                    reward.getName(),
                    reward.getRequiredAmount(),
                    reward.getMaxQuantity(),
                    reward.getRemainQuantity(),
                    reward.getProvideDate(),
                    donateUserList));
        }

        return FundingRewardResponse.builder()
                .fundingPk(funding.getPk())
                .purposeAmount(funding.getPurposeAmount())
                .donateAmount(funding.getDonateAmount())
                .rewards(rewardResponses)
                .build();
    }
}
