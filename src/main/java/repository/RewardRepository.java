package repository;

import java.util.List;
import java.util.Map;

import model.domain.AppUser;
import model.domain.Project;
import model.domain.Reward;

public class RewardRepository extends BaseRepositoryImpl<Reward, Long> {

    // 프로젝트로 리워드찾기
    public List<Reward> findByProjectId(Long projectId) {
        return find(//해당 ID로 찾은 리워드 객체들의 목록 반환되는 것
                Filter.builder()//find가 Filter타입을 객체로 받아서,Filter클래스가 안 보여서 우선 형식 만든정도...
                        .conditions(Map.of("project", projectId))//프로젝트 id로 필터링
                        .sort(Sort.by(Order.asc("createdAt")))//생성일 기준 정렬
                        .build()//필터 객체 완성
        );


    }

    public boolean decreaseRemainQuantity(Long rewardId, Long quantity){//재고를 안전하게 차감을 했는가?
        if( quantity <= 0){//재고가 0이하인지 확인하기
            return false;
        }
        return true;
    }
/*
    public List<Reward> findByMakerId(Long makerId) {

          //  유저(제작자)>제작자가 만든 프로젝트> 해당 프로젝트 리워드

        AppUser maker =
        return find(//해당 ID로 찾은 리워드 객체들의 목록 반환되는 것
                Filter.builder()
                        .conditions(Map.of("maker", maker.getReward()))//프로젝트 id로 필터링
                        .sort(Sort.by(Order.asc("created_at")))//생성일 기준 정렬
                        .build()//필터 객체 완성
        );
    }

    public List<Reward> findByDonate(Long userId) {//본인이 후원한 프로젝트의 리워드

        //    본인후원내역>후원에 있는 리워드 외래키 사용

        return find(//해당 ID로 찾은 리워드 객체들의 목록 반환되는 것
                Filter.builder()
                        .conditions(Map.of("project", userId))//프로젝트 id로 필터링
                        .sort(Sort.by(Order.asc("created_at")))//생성일 기준 정렬
                        .build()//필터 객체 완성
        );


    }

 */

}

