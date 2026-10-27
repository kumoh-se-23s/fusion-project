package repository;

import java.util.ArrayList;
import java.util.List;
import model.domain.Reward;
import model.dto.RewardData;
import repository.BaseRepositoryImpl;
import repository.Filter;

import java.util.List;
import java.util.Map;
import java.util.Optional;


public class RewardRepository extends BaseRepositoryImpl<Reward, Long> {

    /*  create()리워드 저장 < 공통 기능 재사용
        find()모든 리워드 조회(외부/내부 조회 각각 존재) < //
        findById()리워드Pk로 조회 < //
        update()리워드 수정 < //
        delete()리워드 삭제 < //
    */

    public List<RewardData> findByFundingPk(Long fundingPk){
        Filter filter = new Filter("funding_fk", fundingPk);
        List<Reward> rewards = find(filter);

    }


    public abstract List<RewardData> findByUserPk(Long userPk){

    }

    public abstract List<RewardData> findMaker(Long  userPk){

    }


}

