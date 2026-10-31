package service;

import model.domain.Funding;
import repository.DonateRepository;
import repository.FundingRepository;
import repository.RewardRepository;

public class DonateService extends BaseService{
    private DonateRepository donateRepository;
    private RewardRepository rewardRepository;
    private FundingRepository fundingRepository;
    
    public void checkDuplicateDonate(Long fundingPk, Long userPk){
       
        Funding funding = fundingRepository.findById(fundingPk)
                .orElseThrow(() -> new RuntimeException("프로젝트가 존재하지 않습니다."));
            //에외처리하면 됨
        if(donateRepository.existsByUserPkAndFundingPk(userPk,fundingPk)){
            throw new IllegalStateException("이미 후원한 프로젝트 입니다.");

        }
    }

    //후원생성 만들기(여기서 동시성 처리 하기)
    //USER가 FUNDING을 만들면 CREATE로 하고 REWARD를 등록하면 FUNDING을 STATE로 변경
}
