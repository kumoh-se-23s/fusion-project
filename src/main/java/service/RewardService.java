package service;

import lombok.RequiredArgsConstructor;
import model.domain.Donate;
import model.domain.Reward;
import model.dto.RewardData;
import model.dto.request.reward.CreateRewardDataRequest;
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


    public void createReward(CreateRewardDataRequest request){
        //(리워드가 속할 펀딩의 Pk, 사용자가 입력한 리워드 정보)


    }



    public List<RewardData> getRewardByFunding(Long fundingPk){
        List<Reward> rewardList = rewardRepository.findByFundingPk(fundingPk);

        List<RewardData> dtoList = new ArrayList<>();
        for(int i = 0; i < rewardList.size(); i++ ){
            Reward reward = rewardList.get(i);
            //DTO Builder만들기
            RewardData.RewardDataBuilder builder = RewardData.builder();
            RewardData dto = toRewardData(reward);

            dtoList.add(dto);
        }
        return dtoList;//dto변환한거 반환;
    }

    public List<RewardData> getRewardByDonate(Long userPk){//본인이 후원한 리워드들 확인
        List<Donate> donateList = donateRepository.findByUserPk(userPk);

        List<RewardData> dtoList = new ArrayList<>();//donateDto에 있는 PkResponse사용,리워드 정보로 반영
        for(int i = 0; i < donateList.size(); i++){
            Donate donate = donateList.get(i);
            Reward reward = donate.getReward();
            RewardData dto = toRewardData(reward);

            dtoList.add(dto);
        }
        return dtoList;
    }

    private RewardData toRewardData(Reward reward){
        return RewardData.builder()
                .rewardPk(reward.getPk())
                .name(reward.getName())
                .text(reward.getText())
                .fundingPk(reward.getFunding().getPk())
                .imgFile(reward.getImgFile())
                .makerPk(reward.getFunding().getMaker().getPk())
                .providerDate(reward.getProvideDate())
                .createdAt(reward.getCreatedAt())
                .updatedAt(reward.getUpdatedAt())
                .requiredAmount(reward.getRequiredAmount())
                .maxQuantity(reward.getMaxQuantity())
                .remainQuantity(reward.getRemainQuantity())
                .build();
    }

}
