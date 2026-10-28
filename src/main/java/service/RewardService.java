package service;

import lombok.RequiredArgsConstructor;
import model.domain.Reward;
import model.dto.RewardData;
import repository.DonateRepository;
import repository.FundingRepository;
import repository.UserRepository;
import repository.RewardRepository;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class RewardService {
    private final FundingRepository fundingRepository;
    private final DonateRepository donateRepository;
    private final UserRepository userRepository;
    private final RewardRepository rewardRepository;

    public List<RewardData> getRewardByFunding(Long fundingPk){
        List<Reward> rewardList = rewardRepository.findByFundingPk(fundingPk);

        List<RewardData> dtoList = new ArrayList<>();
        for(int i = 0; i < rewardList.size(); i++ ){
            Reward reward = rewardList.get(i);
            //DTO Builder만들기
            RewardData.RewardDataBuilder builder =
                    RewardData.builder();

            builder.rewardPk(reward.getPk());
            builder.name(reward.getName());
            builder.text(reward.getText());
            builder.fundingPk(reward.getFunding().getPk());
            builder.imgFile(reward.getImgFile());
            builder.makerPk(reward.getFunding().getMaker().getPk());
            builder.providerDate(reward.getProvideDate());
            builder.createdAt(reward.getCreatedAt());
            builder.updatedAt(reward.getUpdatedAt());
            builder.requiredAmount(reward.getRequiredAmount());
            builder.maxQuantity(reward.getMaxQuantity());
            builder.remainQuantity(reward.getRemainQuantity());


            RewardData dto = builder.build();

            dtoList.add(dto);

        }
        return dtoList;//dto변환한거 반환;
    }

    public List<RewardData> getRewardByDonate(Long donatePk){

    }

}
