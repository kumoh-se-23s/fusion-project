package service.external;

import exception.*;

import lombok.RequiredArgsConstructor;

import model.Serializer;

import model.domain.AppUser;
import model.domain.Donate;
import model.domain.Project;
import model.domain.Reward;

import model.dto.DonateData;
import model.dto.request.donate.CreateDonateRequest;

import model.dto.request.reward.CreateRewardDataRequest;
import model.dto.response.IdResponse;
import repository.DonateRepository;
import repository.ProjectRepository;
import repository.RewardRepository;
import repository.UserRepository;
import service.BaseService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class DonateService extends BaseService {

    private final DonateRepository donateRepository;
    private final RewardRepository rewardRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;


    // 중복 후원 검사
    public void checkDuplicateDonate(Long projectId, Long userId) {

        if (projectId == null || userId == null) {
            throw new InvalidRequestException(
                    "중복 후원 검사에 필요한 정보가 없습니다."
            );
        }

        Project project = projectRepository
                .findById(projectId)
                .orElseThrow(() -> new NotFoundException(
                        "후원하려는 프로젝트를 찾을 수 없습니다."
                ));

        if (donateRepository.existsByUserIdAndProjectId(
                userId,
                project.getId()
        )) {
            throw new AlreadyDonatedException(
                    "이미 후원한 프로젝트입니다."
            );
        }
    }


    // 후원 신청 생성
    public IdResponse createDonate(CreateDonateRequest request) {

        // 요청 정보 검사
        if (request == null) {
            throw new InvalidRequestException(
                    "후원 신청 정보가 없습니다."
            );
        }

        if (request.getUserFk() == null
                || request.getRewardFk() == null) {
            throw new InvalidRequestException(
                    "사용자 또는 리워드 ID가 없습니다."
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

        if (reward.getProject() == null
                || reward.getProject().getId() == null) {
            throw new NotFoundException(
                    "리워드에 연결된 프로젝트가 없습니다."
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
                reward.getProject().getId(),
                user.getId()
        );

        // 후원 생성 초기값 설정
        request.prepareForCreate(user, reward);

        // Builder 대신 Serializer 사용
        Donate donate = Serializer.toDomain(
                request,
                Donate.class
        );

        // 후원 내역 저장
        // 후원 내역 저장
        Donate result = donateRepository.create(donate);
        if (result == null) {
            throw new TransactionException(
                    "후원 신청 저장에 실패했습니다."
            );
        }

// 남은 재고 계산
        Long newRemainQuantity = remainQuantity - quantity;

// 리워드 재고 업데이트
        rewardRepository.update(
                reward.getId(),
                Map.of("remainQuantity", newRemainQuantity)
        ).orElseThrow(() -> new TransactionException(
                "리워드 재고 업데이트에 실패했습니다."
        ));

        BigDecimal resultAmount = reward.getProject().getDonateAmount().add(result.getAmount());
        projectRepository.updateByDonateAmount(reward.getProject().getId(), resultAmount);
        return new IdResponse(result.getId());
    }

    //유저id로 조회
        public List<DonateData> findDonateByUser(Long userId){
            if(userId == null){
                throw new NotFoundException("조회할 유저id가 없습니다");
            }
            //존재하지 않는경우 리스트가 나오긴하지만 비워있는 리스트가 나옴,굳이 null체크 필요??

            List<Donate>donateList = donateRepository.findByUserId(userId);
            return Serializer.toDataList(
                    donateList,
                    DonateData.class
            );
        }
    //리워드id로 조회
        public List<DonateData> findDonateByReward(Long rewardId){
            if(rewardId == null){
            throw new LimitNumberOutException("조회할 리워드id가 없습니다");
            }

         List<Donate>donateList = donateRepository.findByUserId(rewardId);
         return Serializer.toDataList(
                donateList,
                DonateData.class
        );
    }

    //펀딩id로 조회

    public List<DonateData> findDonateByFunding(Long fundingId){
        if(fundingId == null){
            throw new LimitNumberOutException("조회할 펀딩id가 없습니다");
        }

        List<Donate>donateList = donateRepository.findByUserId(fundingId);
        return Serializer.toDataList(
                donateList,
                DonateData.class
        );
    }

    //후원 환불
    public void refundDonate(Long donateId, Long userId){
        Donate donate = donateRepository.findById(donateId).get();//해당 후원id의 객체 찾기
        Reward reward = donate.getReward();
        DonateData thisDonate =  Serializer.toData(
                    donate,
                    DonateData.class
                );
        List<DonateData> donateList = findDonateByUser(userId);

        for(DonateData donateData : donateList){
            if(donateData.equals(thisDonate)){//userId가져와서 후원한 내역중에 해당 donateId가 있는지 조회

                if(!donate.getUser().getId().equals(userId)){//후원한 사람과 로그인 유저 동일인인지 확인
                    throw new InvalidException();//아닐 경우 예외처리
                }
                else{//맞는경우
                    Long donateQuantity = donate.getQuantity();
                    Long remainQuantity = reward.getRemainQuantity();
                    Long newQuantity = donateQuantity + remainQuantity;
                    rewardRepository.update(//리워드 수량 수정
                            reward.getId(),
                            Map.of("remainQuantity",newQuantity)
                    );
                }

            }

        }
        throw new InvalidException("해당 펀딩에 후원한 기록이 없습니다.");

    }

    //후원 총금액
}