package repository;

import model.domain.Donate;
import model.domain.Reward;
import model.dto.RewardData;


import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Filter;

public class DonateRepository extends BaseRepositoryImpl<Donate, Long> {


    public List<Donate> findByUserPk(Long userPk) {//rewardService에서 사용할
        return find(
                Filter.builder()
                        .conditions(Map.of("user", userPk))//펀딩 pk로 필터링
                        .sort(Sort.by(Order.asc("donated_at")))//후원일 기준 정렬
                        .build()//필터 객체 완성
        );
    }

    public boolean existsByUserPkAndFundingPk(Long userPk, Long fundingPk) {//하나의 펀딩에는 후원 한 번만 가능
        List<Donate> donateList = findByUserPk(userPk);

        for (Donate donate : donateList) {

            Reward reward = donate.getReward();

            if (reward != null && reward.getFunding().getPk().equals(fundingPk)) {
                return true;
            }
        }
        return false;

    }

}
