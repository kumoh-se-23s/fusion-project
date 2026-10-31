package service;

import exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import model.domain.Donate;
import model.domain.Funding;
import model.domain.Reward;
import model.dto.RewardData;
import model.dto.request.reward.CreateRewardDataRequest;
import repository.DonateRepository;
import repository.FundingRepository;
import repository.UserRepository;
import repository.RewardRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class RewardService {
    private final FundingRepository fundingRepository;
    private final DonateRepository donateRepository;
    private final UserRepository userRepository;
    private final RewardRepository rewardRepository;

 //펀딩에 리워드 등록
    public void createReward(CreateRewardDataRequest request){
        //(리워드가 속할 펀딩의 Pk, 사용자가 입력한 리워드 정보)
        Long fundingPk = request.getFundingFk();

        Funding funding =  fundingRepository.findById(fundingPk).
                orElseThrow(()-> new NotFoundException());//따로 만들어 놓은 예외처리 쓰기

        Reward reward = Reward.builder()//나중에 바뀜 Serializer
                .funding(funding)//펀딩에 유저pk가 있으니 여기서 안 받아도 되는 듯
                .name(request.getRewardName())
                .text(request.getRewardText())
                .imgFile(request.getRewardImg())
                .requiredAmount(BigDecimal.valueOf(request.getRequiredAmount()))
                .maxQuantity(request.getRewardMaxQuantity())
                .build();
                //남아있는 리워드 수량은 입력x
        rewardRepository.create(reward);
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
        List<Donate> donateList = donateRepository.findByUserPk(userPk);//후원한 펀딩들 전부 리스트에 넣음

        List<RewardData> dtoList = new ArrayList<>();//donateDto에 있는 PkResponse사용,리워드 정보로 반영
        for(int i = 0; i < donateList.size(); i++){
            Donate donate = donateList.get(i);//후원 하나씩 리워드 보기
            Reward reward = donate.getReward();//후원도메인에 있던거로 리워드 객체 생성
            RewardData dto = toRewardData(reward);//dto변환

            dtoList.add(dto);
        }
        return dtoList;
    }

    //유저>후원한 펀딩>리워드
    public List<List<RewardData>> getRewardfindByMaker(Long makerPk){
        List<Funding> fundingList = fundingRepository.findByUserPk(makerPk);//fundingRepository의 메서드 가져와서 사용
        //Map반환or Response만들기 혹은..Data이중으로 감싸기?
        List<List<RewardData>> dtoList = new ArrayList<>();
        for(int i = 0; i < fundingList.size(); i++){
            Funding funding = fundingList.get(i);
            dtoList.add(getRewardByFunding(funding.getPk()));
            //dtoList.addAll둘 중?
        }
        return dtoList;
    }

    private RewardData toRewardData(Reward reward){//Serializer로 변환
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
