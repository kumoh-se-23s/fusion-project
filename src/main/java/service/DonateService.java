package service;

import exception.AlreadyDonatedException;
import exception.InvalidRequestException;
import exception.LimitNumberOutException;
import exception.NotFoundException;
import exception.TransactionException;

import lombok.RequiredArgsConstructor;

import model.Serializer;

import model.domain.AppUser;
import model.domain.Donate;
import model.domain.Funding;
import model.domain.Reward;

import model.dto.request.donate.CreateDonateRequest;

import repository.DonateRepository;
import repository.FundingRepository;
import repository.RewardRepository;
import repository.UserRepository;

import java.math.BigDecimal;

@RequiredArgsConstructor
public class DonateService extends BaseService {

    private final DonateRepository donateRepository;
    private final RewardRepository rewardRepository;
    private final FundingRepository fundingRepository;
    private final UserRepository userRepository;


    // 중복 후원 검사
    public void checkDuplicateDonate(Long fundingPk, Long userPk) {

        if (fundingPk == null || userPk == null) {
            throw new InvalidRequestException(
                    "중복 후원 검사에 필요한 정보가 없습니다."
            );
        }

        Funding funding = fundingRepository
                .findById(fundingPk)
                .orElseThrow(() -> new NotFoundException(
                        "후원하려는 펀딩을 찾을 수 없습니다."
                ));

        if (donateRepository.existsByUserPkAndFundingPk(
                userPk,
                funding.getPk()
        )) {
            throw new AlreadyDonatedException(
                    "이미 후원한 프로젝트입니다."
            );
        }
    }


    // 후원 신청 생성
    public void createDonate(CreateDonateRequest request) {

        // 요청 정보 검사
        if (request == null) {
            throw new InvalidRequestException(
                    "후원 신청 정보가 없습니다."
            );
        }

        if (request.getUserFk() == null
                || request.getRewardFk() == null) {
            throw new InvalidRequestException(
                    "사용자 또는 리워드 PK가 없습니다."
            );
        }

        // 후원 금액 검사
        if (request.getAmount() == null
                || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new LimitNumberOutException(
                    "후원 금액은 0원보다 커야 합니다."
            );
        }

        // 신청 수량 검사
        if (request.getQuantity() == null
                || request.getQuantity() <= 0) {
            throw new LimitNumberOutException(
                    "리워드 신청 수량은 1개 이상이어야 합니다."
            );
        }

        // 사용자 조회
        AppUser user = userRepository
                .findById(request.getUserFk())
                .orElseThrow(() -> new NotFoundException(
                        "후원자를 찾을 수 없습니다."
                ));

        // 리워드 조회
        Reward reward = rewardRepository
                .findById(request.getRewardFk())
                .orElseThrow(() -> new NotFoundException(
                        "신청할 리워드를 찾을 수 없습니다."
                ));

        if (reward.getFunding() == null
                || reward.getFunding().getPk() == null) {
            throw new NotFoundException(
                    "리워드에 연결된 펀딩이 없습니다."
            );
        }
        // 리워드 남은 수량 확인
        Long remainQuantity = reward.getRemainQuantity();
        Long quantity = request.getQuantity();

        // 리워드 재고가 없을 경우
        if (remainQuantity == null || remainQuantity <= 0) {
            throw new LimitNumberOutException(
                    "리워드 재고가 소진되어 후원할 수 없습니다."
            );
        }

        // 신청 수량이 남은 재고보다 많은 경우
        if (quantity > remainQuantity) {
            throw new LimitNumberOutException(
                    "신청 수량이 남은 리워드 수량을 초과했습니다."
            );
        }

        // 중복 후원 확인
        checkDuplicateDonate(
                reward.getFunding().getPk(),
                user.getPk()
        );

        // 후원 생성 초기값 설정
        request.prepareForCreate(user, reward);

        // Builder 대신 Serializer 사용
        Donate donate = Serializer.toDomain(
                request,
                Donate.class
        );

        // 후원 내역 저장
        if (donateRepository.create(donate) == null) {
            throw new TransactionException(
                    "후원 신청 저장에 실패했습니다."
            );
        }
    }
}