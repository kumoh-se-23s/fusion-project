package service;

import exception.AlreadyDonatedException;
import exception.BusinessException;
import exception.NotFoundException;
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
                .orElseThrow(() -> new NotFoundException());
            //있던걸로 에외처리하면 됨
        if(donateRepository.existsByUserPkAndFundingPk(userPk,fundingPk)){
            throw new AlreadyDonatedException();
        }
    }

    public void createDonate(){


    }
    //후원생성 만들기(여기서 동시성 처리 하기)
    //USER가 FUNDING을 만들면 CREATE로 하고 REWARD를 등록하면 FUNDING을 STATE로 변경
}
