
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
import model.dto.response.DataCollectionResponse;
import model.dto.response.SingleDataResponse;
import model.dto.response.IdResponse;

import repository.DonateRepository;
import repository.ProjectRepository;
import repository.RewardRepository;
import repository.UserRepository;

import service.BaseService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class DonateService extends BaseService {

    private final DonateRepository donateRepository;
    private final RewardRepository rewardRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    /**
     * 중복 후원 검사
     */
    public void checkDuplicateDonate(
            Long projectId,
            Long userId) {

        if (projectId == null || userId == null) {
            throw new InvalidRequestException(
                    "중복 후원 검사 정보가 없습니다.");
        }

        Project project = projectRepository
                .findById(projectId)
                .orElseThrow(() -> new NotFoundException(
                        "프로젝트를 찾을 수 없습니다."));

        if (donateRepository.existsByUserIdAndProjectId(
                userId, project.getId())) {
            throw new AlreadyDonatedException(
                    "이미 후원한 프로젝트입니다.");
        }
    }

    /**
     * 로그인 사용자 정보로 후원 생성
     */
    public IdResponse createDonate(
            Long loginUserId,
            CreateDonateRequest request) {

        if (loginUserId == null || request == null) {
            throw new InvalidException();
        }

        if (request.getUserFk() != null
                && !loginUserId.equals(
                request.getUserFk())) {
            throw new InvalidException();
        }

        CreateDonateRequest safeRequest =
                new CreateDonateRequest(
                        loginUserId,
                        request.getRewardFk(),
                        request.getAmount(),
                        request.getQuantity()
                );

        return createDonateInternal(safeRequest);
    }

    /**
     * 후원 생성
     * amount는 후원 총금액
     */
    private IdResponse createDonateInternal(
            CreateDonateRequest request) {

        if (request.getRewardFk() == null) {
            throw new InvalidRequestException(
                    "리워드 ID가 없습니다.");
        }

        if (request.getAmount() == null
                || request.getAmount()
                .compareTo(BigDecimal.ZERO) <= 0) {
            throw new LimitNumberOutException(
                    "후원 금액은 0원보다 커야 합니다.");
        }

        if (request.getQuantity() == null
                || request.getQuantity() <= 0) {
            throw new LimitNumberOutException(
                    "신청 수량은 1개 이상이어야 합니다.");
        }

        AppUser user = userRepository
                .findById(request.getUserFk())
                .orElseThrow(() -> new NotFoundException(
                        "사용자를 찾을 수 없습니다."));

        Reward reward = rewardRepository
                .findById(request.getRewardFk())
                .orElseThrow(() -> new NotFoundException(
                        "리워드를 찾을 수 없습니다."));

        Project project = reward.getProject();

        if (project == null
                || project.getId() == null) {
            throw new NotFoundException(
                    "연결된 프로젝트가 없습니다.");
        }

        Long remain = reward.getRemainQuantity();
        Long quantity = request.getQuantity();

        if (remain == null || remain < quantity) {
            throw new LimitNumberOutException(
                    "리워드 재고가 부족합니다.");
        }

        checkDuplicateDonate(
                project.getId(), user.getId());

        request.prepareForCreate(user, reward);

        Donate donate = Serializer.toDomain(
                request, Donate.class);

        Donate result = donateRepository.create(donate);

        if (result == null) {
            throw new TransactionException(
                    "후원 저장에 실패했습니다.");
        }

        // 재고 감소
        rewardRepository.update(
                reward.getId(),
                Map.of("remainQuantity", remain - quantity)
        ).orElseThrow(() -> new TransactionException(
                "리워드 재고 수정에 실패했습니다."));

        BigDecimal currentAmount =
                project.getDonateAmount() == null
                        ? BigDecimal.ZERO
                        : project.getDonateAmount();

        // amount 자체가 총금액
        BigDecimal newAmount =
                currentAmount.add(result.getAmount());

        projectRepository.updateByDonateAmount(
                project.getId(), newAmount);

        return new IdResponse(result.getId());
    }

    /**
     * 사용자별 후원 조회
     */
    public DataCollectionResponse<DonateData> findDonateByUser(
            Long userId) {

        if (userId == null) {
            throw new InvalidRequestException(
                    "사용자 ID가 없습니다.");
        }

        List<Donate> donates =
                donateRepository.findByUserId(userId);

        return toCollection(donates);
    }

    /**
     * 리워드별 후원 조회
     */
    public DataCollectionResponse<DonateData> findDonateByReward(
            Long rewardId) {

        if (rewardId == null) {
            throw new InvalidRequestException(
                    "리워드 ID가 없습니다.");
        }

        List<Donate> result = new ArrayList<>();

        for (Donate donate : donateRepository.findAll()) {
            Reward reward = donate.getReward();

            if (reward != null
                    && rewardId.equals(reward.getId())) {
                result.add(donate);
            }
        }

        return toCollection(result);
    }

    /**
     * 프로젝트별 후원 조회
     */
    public DataCollectionResponse<DonateData> findDonateByFunding(
            Long projectId) {

        if (projectId == null) {
            throw new InvalidRequestException(
                    "프로젝트 ID가 없습니다.");
        }

        List<Donate> result = new ArrayList<>();

        for (Donate donate : donateRepository.findAll()) {
            Reward reward = donate.getReward();

            if (reward != null
                    && reward.getProject() != null
                    && projectId.equals(
                    reward.getProject().getId())) {
                result.add(donate);
            }
        }

        return toCollection(result);
    }

    /**
     * 외부용 조건별 후원 조회
     */
    public DataCollectionResponse<DonateData> getDonates(
            Long loginUserId,
            Optional<Long> userId,
            Optional<Long> rewardId,
            Optional<Long> projectId) {

        if (loginUserId == null) {
            throw new InvalidException();
        }

        int count = 0;

        if (userId.isPresent()) count++;
        if (rewardId.isPresent()) count++;
        if (projectId.isPresent()) count++;

        if (count != 1) {
            throw new InvalidRequestException(
                    "조회 조건을 하나만 입력해야 합니다.");
        }

        // 사용자별 조회: 본인만 허용
        if (userId.isPresent()) {
            if (!loginUserId.equals(userId.get())) {
                throw new InvalidException();
            }

            return findDonateByUser(userId.get());
        }

        // 리워드별 조회: 프로젝트 메이커만 허용
        if (rewardId.isPresent()) {
            Reward reward = rewardRepository
                    .findById(rewardId.get())
                    .orElseThrow(() -> new NotFoundException(
                            "리워드를 찾을 수 없습니다."));

            Project project = reward.getProject();

            if (project == null
                    || project.getMaker() == null
                    || !loginUserId.equals(
                    project.getMaker().getId())) {
                throw new InvalidException();
            }

            return findDonateByReward(rewardId.get());
        }

        // 프로젝트별 조회: 프로젝트 메이커만 허용
        Project project = projectRepository
                .findById(projectId.get())
                .orElseThrow(() -> new NotFoundException(
                        "프로젝트를 찾을 수 없습니다."));

        if (project.getMaker() == null
                || !loginUserId.equals(
                project.getMaker().getId())) {
            throw new InvalidException();
        }

        return findDonateByFunding(projectId.get());
    }

    /**
     * 관리자 후원 ID 조회
     */
    public SingleDataResponse<DonateData> getDonateById(
            Long donateId) {

        if (donateId == null) {
            throw new InvalidRequestException(
                    "후원 ID가 없습니다.");
        }

        Donate donate = donateRepository
                .findById(donateId)
                .orElseThrow(() -> new NotFoundException(
                        "후원 내역을 찾을 수 없습니다."));

        DonateData dto = Serializer.toData(
                donate, DonateData.class);

        return new SingleDataResponse<>(dto);
    }

    /**
     * 관리자 전체 후원 조회
     */
    public DataCollectionResponse<DonateData> getAllDonates() {
        return toCollection(donateRepository.findAll());
    }

    /**
     * 관리자 단건 또는 전체 조회
     */
    public DataCollectionResponse<DonateData> getAdminDonates(
            Optional<Long> donateId) {

        if (donateId.isEmpty()) {
            return getAllDonates();
        }

        Donate donate = donateRepository
                .findById(donateId.get())
                .orElseThrow(() -> new NotFoundException(
                        "후원 내역을 찾을 수 없습니다."));

        return toCollection(List.of(donate));
    }

    /**
     * 해당 후원이 속한 프로젝트의 누적 후원 금액
     */
    public BigDecimal getProjectDonationTotalByDonateId(
            Long donateId) {

        if (donateId == null) {
            throw new InvalidRequestException(
                    "후원 ID가 없습니다.");
        }

        Donate donate = donateRepository
                .findById(donateId)
                .orElseThrow(() -> new NotFoundException(
                        "후원 내역을 찾을 수 없습니다."));

        Reward reward = donate.getReward();

        if (reward == null
                || reward.getProject() == null) {
            throw new NotFoundException(
                    "프로젝트를 찾을 수 없습니다.");
        }

        BigDecimal amount =
                reward.getProject().getDonateAmount();

        return amount == null
                ? BigDecimal.ZERO : amount;
    }

    /**
     * 후원 환불
     */
    public void refundDonate(
            Long donateId,
            Long userId) {

        if (donateId == null || userId == null) {
            throw new InvalidException();
        }

        Donate donate = donateRepository
                .findById(donateId)
                .orElseThrow(() -> new NotFoundException(
                        "후원 내역을 찾을 수 없습니다."));

        if (donate.getUser() == null
                || !userId.equals(
                donate.getUser().getId())) {
            throw new InvalidException();
        }

        Reward reward = donate.getReward();

        if (reward == null
                || reward.getProject() == null) {
            throw new NotFoundException(
                    "리워드 또는 프로젝트가 없습니다.");
        }

        Project project = reward.getProject();

        if (donate.getQuantity() == null
                || donate.getAmount() == null
                || reward.getRemainQuantity() == null
                || project.getDonateAmount() == null) {
            throw new TransactionException(
                    "후원 정보가 올바르지 않습니다.");
        }

        Long newQuantity =
                reward.getRemainQuantity()
                        + donate.getQuantity();

        // amount는 총액이므로 그대로 차감
        BigDecimal newAmount =
                project.getDonateAmount()
                        .subtract(donate.getAmount());

        if (newAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new TransactionException(
                    "환불 금액이 누적 금액을 초과합니다.");
        }

        rewardRepository.update(
                reward.getId(),
                Map.of("remainQuantity", newQuantity)
        ).orElseThrow(() -> new TransactionException(
                "리워드 수량 복원에 실패했습니다."));

        projectRepository.updateByDonateAmount(
                project.getId(), newAmount);

        donateRepository.delete(donateId);
    }

    /**
     * 사용자의 해당 후원 총금액 조회
     */
    public BigDecimal donateAmountSum(
            Long donateId,
            Long userId) {

        Donate donate = donateRepository
                .findById(donateId)
                .orElseThrow(() -> new NotFoundException(
                        "후원 내역을 찾을 수 없습니다."));

        if (userId == null
                || donate.getUser() == null
                || !userId.equals(
                donate.getUser().getId())) {
            throw new InvalidException();
        }

        // amount는 이미 총액
        return donate.getAmount();
    }

    /**
     * 프로젝트 전체 누적 후원 금액
     */
    private BigDecimal donateAmountProject(
            Long projectId) {

        Project project = projectRepository
                .findById(projectId)
                .orElseThrow(() -> new NotFoundException(
                        "프로젝트를 찾을 수 없습니다."));

        return project.getDonateAmount();
    }

    /**
     * Donate 목록을 DataCollectionResponse로 변환
     */
    private DataCollectionResponse<DonateData> toCollection(
            List<Donate> donates) {

        List<DonateData> dtoList =
                Serializer.toDataList(
                        donates, DonateData.class);

        return DataCollectionResponse.<DonateData>builder()
                .amount((long) dtoList.size())
                .datas(dtoList)
                .build();
    }
}
