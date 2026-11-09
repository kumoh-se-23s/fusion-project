package service.external;

import constant.ProjectState;
import exception.NotFoundException;
import exception.LimitNumberOutException;
import exception.TransactionException;

import lombok.RequiredArgsConstructor;

import model.Serializer;

import model.domain.Category;
import model.domain.Donate;
import model.domain.Project;
import model.domain.Reward;

import model.dto.RewardData;
import model.dto.request.reward.CreateRewardDataRequest;

import model.dto.response.IdResponse;
import repository.CategoryRepository;
import repository.DonateRepository;
import repository.ProjectRepository;
import repository.RewardRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class RewardService {

    private final ProjectRepository projectRepository;
    private final DonateRepository donateRepository;
    private final RewardRepository rewardRepository;
    private final CategoryRepository categoryRepository;


    // 1. 프로젝트에 리워드 등록
    public IdResponse createReward(CreateRewardDataRequest request) {

        // 요청값 검사
        if (request == null) {
            throw new LimitNumberOutException(
                    "리워드 등록 요청 정보가 없습니다."
            );
        }

        if (request.getProjectFk() == null) {
            throw new LimitNumberOutException(
                    "프로젝트 또는 카테고리 ID가 없습니다."
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
        if (request.getRewardPerAmount() == null
                || request.getRewardPerAmount()
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

        // 프로젝트 조회
        Project project = projectRepository
                .findById(request.getProjectFk())
                .orElseThrow(
                        () -> new NotFoundException(
                                "리워드를 등록할 프로젝트를 찾을 수 없습니다."
                        )
                );

        // Service에서 초기값 설정
        request.prepareForCreate(project);

        // Builder 대신 Serializer 사용
        Reward reward = Serializer.toDomain(
                request,
                Reward.class
        );

        // 리워드 저장
        Reward result = rewardRepository.create(reward);
        if (result == null) {
             throw new TransactionException(
                    "리워드 저장에 실패했습니다."
            );
        }
        //첫 리워드 작성 시 프로젝트를 승인 대기로 변경
        if (project.getState() == ProjectState.JUST_USER_INPUT) {
            projectRepository.updateByPending(project.getId());
        }
        return new IdResponse(result.getId());
    }


    //  프로젝트 ID로 리워드 조회
    public List<RewardData> getRewardByProject(Long projectId) {

        if (projectId == null) {
            throw new LimitNumberOutException(
                    "조회할 프로젝트 ID가 없습니다."
            );
        }

        List<Reward> rewardList =
                rewardRepository.findByProjectId(projectId);

        return Serializer.toDataList(
                rewardList,
                RewardData.class
        );
    }


    //  본인이 후원한 리워드 조회
    public List<RewardData> getRewardByDonate(Long userId) {

        if (userId == null) {
            throw new LimitNumberOutException(
                    "조회할 사용자 ID가 없습니다."
            );
        }

        List<Donate> donateList =
                donateRepository.findByUserId(userId);

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


    //  메이커의 프로젝트별 리워드 조회
    public List<List<RewardData>> getRewardFindByMaker(
            Long makerId
    ) {

        if (makerId == null) {
            throw new LimitNumberOutException(
                    "조회할 메이커 ID가 없습니다."
            );
        }

        List<Project> projectList =
                projectRepository.findByUserId(makerId);

        List<List<RewardData>> dtoList = new ArrayList<>();

        for (Project project : projectList) {

            dtoList.add(
                    getRewardByProject(project.getId())
            );
        }

        return dtoList;
    }
}