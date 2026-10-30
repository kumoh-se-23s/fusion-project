package service;

import model.domain.Funding;
import repository.DonateRepository;
import repository.FundingRepository;
import repository.RewardRepository;

import java.util.Optional;

public class DonateService extends BaseService{
    private DonateRepository donateRepository;
    private RewardRepository rewardRepository;
    private FundingRepository fundingRepository;
    
    public void checkDuplicateDonate(Long fundingPk, Long userPk){
       
        Funding funding = fundingRepository.findById(fundingPk)
                .orElseThrow(() -> new RuntimeException("프로젝트가 존재하지 않습니다."))
        if(donateRepository.existsByUserPkAndFundingPk(userPk,fundingPk)){
            throw new IllegalStateException("이미 후원한 프로젝트 입니다.");
        }
    }
}
