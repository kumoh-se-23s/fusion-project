package repository;

import java.util.List;
import java.util.Map;

import annotation.BindDomain;
import model.domain.Reward;
import model.dto.RewardData;
import model.dto.Serializer;
import repository.Filter;


public class RewardRepository extends BaseRepositoryImpl<Reward, Long> {

    /*  create()리워드 저장 < 공통 기능 재사용
        find()모든 리워드 조회(외부/내부 조회 각각 존재) < //
        findById()리워드Pk로 조회 < //
        update()리워드 수정 < //
        delete()리워드 삭제 < //
    */

    public List<Reward> findByFundingPk(Long fundingPk) {
        // Filter filter = new Filter("funding_fk", fundingPk);// 인자로 받은 펀딩pk를 필터로 추가
        return find(//해당 PK로 찾은 리워드 객체들의 목록 반환되는 것
                Filter.builder()
                        .conditions(Map.of("funding", fundingPk))//펀딩 pk로 필터링
                        .sort(Sort.by(Sort.Order.asc("created_at")))//생성일 기준 정렬
                        .build()//필터 객체 완성
        );


    }


    public List<Reward> findByUserPk(Long userPk) {
        return find(//해당 PK로 찾은 리워드 객체들의 목록 반환되는 것
                Filter.builder()
                        .conditions(Map.of("funding", fundingPk))//펀딩 pk로 필터링
                        .sort(Sort.by(Sort.Order.asc("created_at")))//생성일 기준 정렬
                        .build()//필터 객체 완성
        );
    }

    public List<Reward> findMaker(Long userPk) {
        return find(//해당 PK로 찾은 리워드 객체들의 목록 반환되는 것
                Filter.builder()
                        .conditions(Map.of("funding", fundingPk))//펀딩 pk로 필터링
                        .sort(Sort.by(Sort.Order.asc("created_at")))//생성일 기준 정렬
                        .build()//필터 객체 완성
        );


    }
}

