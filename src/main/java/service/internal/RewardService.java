package service.internal;

import lombok.RequiredArgsConstructor;

import model.Serializer;
import model.domain.Reward;
import model.dto.RewardData;

import repository.RewardRepository;

import java.util.List;

@RequiredArgsConstructor
public class RewardService {

    private final RewardRepository rewardRepository;

    // 관리자용 전체 리워드 조회
    public List<RewardData> getAllRewards() {//response반환으로 수정하기

        //  전체 리워드 조회
        List<Reward> rewardList = rewardRepository.findAllRewards();//이걸 위해 findAllRewards를 rewardRepository에 만듦

        //  Domain DTO 변환 후 반환
        return Serializer.toDataList(
                rewardList,
                RewardData.class
        );
    }
}