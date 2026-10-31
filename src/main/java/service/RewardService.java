package service;

import exception.NotFoundException;
import exception.LimitNumberOutException;
import exception.TransactionException;

import lombok.RequiredArgsConstructor;

import model.Serializer;

import model.domain.Category;
import model.domain.Donate;
import model.domain.Funding;
import model.domain.Reward;

import model.dto.RewardData;
import model.dto.request.reward.CreateRewardDataRequest;

import repository.CategoryRepository;
import repository.DonateRepository;
import repository.FundingRepository;
import repository.UserRepository;
import repository.RewardRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class RewardService {

    private final FundingRepository fundingRepository;
    private final DonateRepository donateRepository;
    private final UserRepository userRepository;
    private final RewardRepository rewardRepository;
    private final CategoryRepository categoryRepository;


    // 1. 펀딩에 리워드 등록
    public void createReward(CreateRewardDataRequest request) {

        // 요청값 검사
        if (request == null) {
            throw new LimitNumberOutException(
                    "리워드 등록 요청 정보가 없습니다."
            );
        }

        if (request.getFundingFk() == null
                || request.getCategoryFk() == null) {
            throw new LimitNumberOutException(
                    "펀딩 또는 카테고리 PK가 없습니다."
            );
        }

        if (request.getRewardName() == null
                || request.getRewardName().isBlank()) {
            throw new LimitNumberOutException(
                    "리워드 이름을 입력해야 합니다."
            );
        }

        if (request.getProvideDate() == null) {
            throw new LimitNumberOutException(
                    "리워드 제공 예정일이 없습니다."
            );
        }

        // 금액 검사
        if (request.getRequiredAmount() == null
                || request.getRequiredAmount()
                .compareTo(BigDecimal.ZERO) <= 0) {
            throw new LimitNumberOutException(
                    "리워드 필요 금액은 0원보다 커야 합니다."
            );
        }

        // 수량 검사
        if (request.getRewardMaxQuantity() == null
                || request.getRewardMaxQuantity() <= 0) {
            throw new LimitNumberOutException(
                    "제공 가능 수량은 1개 이상이어야 합니다."
            );
        }

        // 펀딩 조회
        Funding funding = fundingRepository
                .findById(request.getFundingFk())
                .orElseThrow(
                        () -> new NotFoundException(
                                "리워드를 등록할 펀딩을 찾을 수 없습니다."
                        )
                );

        // 카테고리 조회
        Category category = categoryRepository
                .findById(request.getCategoryFk())
                .orElseThrow(
                        () -> new NotFoundException(
                                "리워드 카테고리를 찾을 수 없습니다."
                        )
                );

        // Service에서 초기값 설정
        request.prepareForCreate(funding, category);

        // Builder 대신 Serializer 사용
        Reward reward = Serializer.toDomain(
                request,
                Reward.class
        );

        // 리워드 저장
        if (rewardRepository.create(reward) == null) {
            throw new TransactionException(
                    "리워드 저장에 실패했습니다."
            );
        }
    }


    // 2. 펀딩 PK로 리워드 조회
    public List<RewardData> getRewardByFunding(Long fundingPk) {

        if (fundingPk == null) {
            throw new LimitNumberOutException(
                    "조회할 펀딩 PK가 없습니다."
            );
        }

        List<Reward> rewardList =
                rewardRepository.findByFundingPk(fundingPk);

        return Serializer.toDataList(
                rewardList,
                RewardData.class
        );
    }


    // 3. 본인이 후원한 리워드 조회
    public List<RewardData> getRewardByDonate(Long userPk) {

        if (userPk == null) {
            throw new LimitNumberOutException(
                    "조회할 사용자 PK가 없습니다."
            );
        }

        List<Donate> donateList =
                donateRepository.findByUserPk(userPk);

        List<Reward> rewardList = new ArrayList<>();

        for (Donate donate : donateList) {

            Reward reward = donate.getReward();

            if (reward != null) {
                rewardList.add(reward);
            }
        }

        return Serializer.toDataList(
                rewardList,
                RewardData.class
        );
    }


    // 4. 메이커의 프로젝트별 리워드 조회
    public List<List<RewardData>> getRewardfindByMaker(
            Long makerPk
    ) {

        if (makerPk == null) {
            throw new LimitNumberOutException(
                    "조회할 메이커 PK가 없습니다."
            );
        }

        List<Funding> fundingList =
                fundingRepository.findByUserPk(makerPk);

        List<List<RewardData>> dtoList = new ArrayList<>();

        for (Funding funding : fundingList) {

            dtoList.add(
                    getRewardByFunding(funding.getPk())
            );
        }

        return dtoList;
    }
}