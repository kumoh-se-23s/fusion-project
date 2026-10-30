package service;

import lombok.RequiredArgsConstructor;
import model.domain.Reward;
import model.dto.response.funding.FundingRewardResponse;
import repository.FundingRepository;
import repository.RewardRepository;
import repository.UserRepository;

import java.util.List;
import java.util.logging.Filter;

@RequiredArgsConstructor
public class FundingService {
    private final FundingRepository fundingRepository;
    private final UserRepository userRepository;
    private final RewardRepository rewardRepository;

    public FundingRewardResponse getFundingByReward(Long fundingPK) {
        List<Reward> rewardList = rewardRepository.find(Filter());
    }


}
