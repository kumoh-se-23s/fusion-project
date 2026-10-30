package repository;

import model.domain.Donate;
import model.domain.Reward;
import model.dto.RewardData;


import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Filter;

public class DonateRepository extends BaseRepositoryImpl<Donate, Long> {


    public List<Donate> findByUserPk(Long userPk) {
        return find(
                Filter.builder()
                        .conditions(Map.of("user", userPk))//펀딩 pk로 필터링
                        .sort(Sort.by(Sort.Order.asc("donated_at")))//후원일 기준 정렬
                        .build()//필터 객체 완성
        );
    }



}
