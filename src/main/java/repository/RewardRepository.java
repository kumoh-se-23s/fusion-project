package repository;

import java.util.List;
import java.util.Map;
import model.domain.Reward;





public class RewardRepository extends BaseRepositoryImpl<Reward, Long> {

    /*  create()리워드 저장 < 공통 기능 재사용
        find()모든 리워드 조회(외부/내부 조회 각각 존재) < //
        findById()리워드Pk로 조회 < //
        update()리워드 수정 < //
        delete()리워드 삭제 < //
    */

    public List<Reward> findByFundingPk(Long fundingPk) {
        return find(//해당 PK로 찾은 리워드 객체들의 목록 반환되는 것
                Filter.builder()//find가 Filter타입을 객체로 받아서,Filter클래스가 안 보여서 우선 형식 만든정도...
                        .conditions(Map.of("funding", fundingPk))//펀딩 pk로 필터링
                        .sort(Sort.by(Order.asc("created_at")))//생성일 기준 정렬
                        .build()//필터 객체 완성
        );


    }
/*
    public List<Reward> findByMakerPk(Long fundingPk) {

            유저(제작자)>제작자가 만든 펀딩> 해당 펀딩 리워드


        return find(//해당 PK로 찾은 리워드 객체들의 목록 반환되는 것
                Filter.builder()
                        .conditions(Map.of("funding", fundingPk))//펀딩 pk로 필터링
                        .sort(Sort.by(Sort.Order.asc("created_at")))//생성일 기준 정렬
                        .build()//필터 객체 완성
        );
    }

    public List<Reward> findByDonate(Long userPk) {//본인이 후원한 펀딩의 리워드

            본인후운내역>후원에 있는 리워드 외래키 사용

        return find(//해당 PK로 찾은 리워드 객체들의 목록 반환되는 것
                Filter.builder()
                        .conditions(Map.of("funding", userPk))//펀딩 pk로 필터링
                        .sort(Sort.by(Sort.Order.asc("created_at")))//생성일 기준 정렬
                        .build()//필터 객체 완성
        );


    }

*/


}

