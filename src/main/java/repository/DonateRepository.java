package repository;

import model.domain.Donate;
import model.domain.Reward;
import model.dto.RewardData;
import model.dto.Serializer;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Filter;

public class DonateRepository extends BaseRepositoryImpl<Donate, Long> {

        /*  create()리워드 저장 < 공통 기능 재사용
        find()모든 리워드 조회(외부/내부 조회 각각 존재) < //
        findById()리워드Pk로 조회 < //
        update()리워드 수정 < //
        delete()리워드 삭제 < //
    */

    public List<Donate> findByFundingPk(Long fundingPk) {
        return find(//해당 PK로 찾은 리워드 객체들의 목록 반환되는 것
                Filter.builder()
                        .conditions(Map.of("funding", fundingPk))//펀딩 pk로 필터링
                        .sort(Sort.by(Sort.Order.asc("created_at")))//생성일 기준 정렬
                        .build()//필터 객체 완성
        );
    }


    public List<Donate> findByUserPk(Long userPk) {
        return find(//해당 PK로 찾은 리워드 객체들의 목록 반환되는 것
                Filter.builder()
                        .conditions(Map.of("funding", userPk))//펀딩 pk로 필터링
                        .sort(Sort.by(Sort.Order.asc("created_at")))//생성일 기준 정렬
                        .build()//필터 객체 완성
        );
    }

    public List<Donate> findByMakerPk(Long userPk) {
        return find(//해당 PK로 찾은 리워드 객체들의 목록 반환되는 것
                Filter.builder()
                        .conditions(Map.of("funding", userPk))//펀딩 pk로 필터링
                        .sort(Sort.by(Sort.Order.asc("created_at")))//생성일 기준 정렬
                        .build()//필터 객체 완성
        );
    }

}
