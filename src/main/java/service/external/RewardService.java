
package service.external;

import constant.ProjectState;
import exception.*;
import lombok.RequiredArgsConstructor;

import model.Serializer;
import model.domain.Donate;
import model.domain.Project;
import model.domain.Reward;
import model.dto.RewardData;
import model.dto.request.reward.CreateRewardDataRequest;
import model.dto.response.DataCollectionResponse;
import model.dto.response.ImageData;
import model.dto.response.SingleDataResponse;
import model.dto.response.IdResponse;

import repository.CategoryRepository;
import repository.DonateRepository;
import repository.ProjectRepository;
import repository.RewardRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class RewardService {

    private final ProjectRepository projectRepository;
    private final DonateRepository donateRepository;
    private final RewardRepository rewardRepository;
    private final CategoryRepository categoryRepository;

    /**
     * 리워드 생성
     */
    public IdResponse createReward(
            Long loginUserId,
            CreateRewardDataRequest request) {

        if (loginUserId == null || request == null) {
            throw new InvalidRequestException("필수값이 누락됐습니다.");
        }

        if (request.getProjectFk() == null) {
            throw new LimitNumberOutException(
                    "프로젝트 ID가 없습니다.");
        }

        if (request.getRewardName() == null
                || request.getRewardName().isBlank()) {
            throw new LimitNumberOutException(
                    "리워드 이름을 입력해야 합니다.");
        }

        if (request.getProvideDate() == null) {
            throw new LimitNumberOutException(
                    "리워드 제공 예정일이 없습니다.");
        }

        if (request.getRewardPerAmount() == null
                || request.getRewardPerAmount()
                .compareTo(BigDecimal.ZERO) <= 0) {
            throw new LimitNumberOutException(
                    "리워드 금액은 0원보다 커야 합니다.");
        }

        if (request.getRewardMaxQuantity() == null
                || request.getRewardMaxQuantity() <= 0) {
            throw new LimitNumberOutException(
                    "리워드 수량은 1개 이상이어야 합니다.");
        }

        Project project = projectRepository
                .findById(request.getProjectFk())
                .orElseThrow(() -> new NotFoundException(
                        "프로젝트를 찾을 수 없습니다."));

        if (project.getMaker() == null
                || !loginUserId.equals(
                project.getMaker().getId())) {
            throw new InvalidRequestException("필수값이 누락됐습니다.");
        }

        request.prepareForCreate(project);

        Reward reward = Serializer.toDomain(
                request, Reward.class);

        Reward result = rewardRepository.create(reward);

        if (result == null) {
            throw new TransactionException(
                    "리워드 저장에 실패했습니다.");
        }

        if (project.getState()
                == ProjectState.JUST_USER_INPUT) {
            projectRepository.updateByPending(
                    project.getId());
        }

        return new IdResponse(result.getId());
    }

    /**
     * 리워드 ID로 조회
     */
    public SingleDataResponse<RewardData> getRewardById(
            Long rewardId) {

        if (rewardId == null) {
            throw new LimitNumberOutException(
                    "리워드 ID가 없습니다.");
        }

        Reward reward = rewardRepository
                .findById(rewardId)
                .orElseThrow(() -> new NotFoundException(
                        "리워드를 찾을 수 없습니다."));

        RewardData dto = Serializer.toData(
                reward, RewardData.class);

        return new SingleDataResponse<>(dto);
    }

    /**
     * 프로젝트별 리워드 조회
     */
    public DataCollectionResponse<RewardData> findRewardByProject(
            Long projectId) {

        if (projectId == null) {
            throw new LimitNumberOutException(
                    "프로젝트 ID가 없습니다.");
        }

        List<Reward> rewards =
                rewardRepository.findByProjectId(projectId);

        return toCollection(rewards);
    }

    /**
     * 본인이 후원한 리워드 조회
     */
    public DataCollectionResponse<RewardData> getRewardByDonate(
            Long loginUserId,
            Long userId) {

        if (loginUserId == null
                || !loginUserId.equals(userId)) {
            throw new InvalidRequestException("필수값이 누락됐습니다.");
        }

        List<Donate> donates =
                donateRepository.findByUserId(userId);

        List<Reward> rewards = new ArrayList<>();

        for (Donate donate : donates) {
            if (donate.getReward() != null) {
                rewards.add(donate.getReward());
            }
        }

        return toCollection(rewards);
    }

    /**
     * 메이커별 리워드 조회
     */
    public DataCollectionResponse<RewardData> getRewardFindByMaker(
            Long makerId) {

        if (makerId == null) {
            throw new LimitNumberOutException(
                    "메이커 ID가 없습니다.");
        }

        List<Project> projects =
                projectRepository.findByUserId(makerId);

        List<Reward> rewards = new ArrayList<>();

        for (Project project : projects) {
            List<Reward> projectRewards =
                    rewardRepository.findByProjectId(
                            project.getId());

            if (projectRewards != null) {
                rewards.addAll(projectRewards);
            }
        }

        return toCollection(rewards);
    }

    /**
     * 리워드 수정
     */
    public void updateReward(
            Long loginUserId,
            Long rewardId,
            CreateRewardDataRequest request) {

        if (loginUserId == null
                || rewardId == null
                || request == null) {
            throw new InvalidRequestException("필수값이 누락됐습니다.");
        }

        Reward reward = rewardRepository
                .findById(rewardId)
                .orElseThrow(() -> new NotFoundException(
                        "리워드를 찾을 수 없습니다."));

        Project project = reward.getProject();

        if (project == null
                || project.getMaker() == null
                || !loginUserId.equals(
                project.getMaker().getId())) {
            throw new InvalidRequestException("필수값이 누락됐습니다.");
        }

        if (request.getProjectFk() != null
                && !request.getProjectFk().equals(
                project.getId())) {
            throw new InvalidRequestException("필수값이 누락됐습니다.");
        }

        Map<String, Object> updates = new HashMap<>();

        if (request.getRewardName() != null) {
            if (request.getRewardName().isBlank()) {
                throw new LimitNumberOutException(
                        "리워드 이름이 비어 있습니다.");
            }

            updates.put("name",
                    request.getRewardName());
        }

        if (request.getRewardImg() != null) {
            updates.put("imgFile",
                    request.getRewardImg());
        }

        if (request.getRewardText() != null) {
            updates.put("text",
                    request.getRewardText());
        }

        if (request.getProvideDate() != null) {
            updates.put("provideDate",
                    request.getProvideDate());
        }

        if (request.getRewardPerAmount() != null) {
            if (request.getRewardPerAmount()
                    .compareTo(BigDecimal.ZERO) <= 0) {
                throw new LimitNumberOutException(
                        "금액은 0원보다 커야 합니다.");
            }

            updates.put("rewardPerAmount",
                    request.getRewardPerAmount());
        }

        if (request.getRewardMaxQuantity() != null) {

            Long oldMax = reward.getMaxQuantity();
            Long oldRemain = reward.getRemainQuantity();
            Long newMax = request.getRewardMaxQuantity();

            if (oldMax == null || oldRemain == null
                    || newMax <= 0) {
                throw new LimitNumberOutException(
                        "수량을 확인할 수 없습니다.");
            }

            long alreadySelected = oldMax - oldRemain;

            if (newMax < alreadySelected) {
                throw new LimitNumberOutException(
                        "이미 신청된 수량보다 적게 수정할 수 없습니다.");
            }

            updates.put("maxQuantity", newMax);
            updates.put("remainQuantity",
                    newMax - alreadySelected);
        }

        if (!updates.isEmpty()) {
            updates.put("updatedAt",
                    LocalDateTime.now());

            rewardRepository.update(rewardId, updates)
                    .orElseThrow(() -> new TransactionException(
                            "리워드 수정에 실패했습니다."));
        }
    }

    /**
     * 리워드 삭제
     */
    public void deleteReward(
            Long loginUserId,
            Long rewardId) {

        if (loginUserId == null || rewardId == null) {
            throw new InvalidRequestException("필수값이 누락됐습니다.");
        }

        Reward reward = rewardRepository
                .findById(rewardId)
                .orElseThrow(() -> new NotFoundException(
                        "리워드를 찾을 수 없습니다."));

        Project project = reward.getProject();

        if (project == null
                || project.getMaker() == null
                || !loginUserId.equals(
                project.getMaker().getId())) {
            throw new InvalidRequestException("필수값이 누락됐습니다.");
        }

        // 후원 내역이 존재하면 삭제 제한
        for (Donate donate : donateRepository.findAll()) {
            if (donate.getReward() != null
                    && rewardId.equals(
                    donate.getReward().getId())) {
                throw new InvalidRequestException("필수값이 누락됐습니다.");
            }
        }

        rewardRepository.delete(rewardId);
    }

    /**
     * 관리자 전체 리워드 조회
     */
    public DataCollectionResponse<RewardData> getAllRewards() {

        List<Reward> rewards =
                rewardRepository.findAll();

        return toCollection(rewards);
    }

    /**
     * 리워드 목록을 응답 DTO로 변환
     */
    private DataCollectionResponse<RewardData> toCollection(
            List<Reward> rewards) {

        List<RewardData> dtoList =
                Serializer.toDataList(
                        rewards, RewardData.class);

        return DataCollectionResponse.<RewardData>builder()
                .amount((long) dtoList.size())
                .datas(dtoList)
                .build();
    }

    public ImageData getRewardImage(Long rewardId) {
        Reward reward = rewardRepository.findById(rewardId)
                .orElseThrow(NotFoundException::new);
        if (reward.getImgFile() == null || reward.getImgFile().length == 0) {
            throw new EmptyImageException();
        }
        return new ImageData(reward.getImgFile());
    }
}
