package service.external;

import example.db.DatabaseTest;
import exception.LimitNumberOutException;
import exception.NotFoundException;
import model.domain.AppUser;
import model.domain.Category;
import model.domain.Donate;
import model.domain.Project;
import model.domain.Reward;
import model.dto.RewardData;
import model.dto.request.reward.CreateRewardDataRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import repository.CategoryRepository;
import repository.DonateRepository;
import repository.ProjectRepository;
import repository.RewardRepository;
import repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RewardService")
public class RewardServiceTest extends DatabaseTest {

    private RewardService rewardService;
    private UserRepository userRepository;
    private ProjectRepository projectRepository;
    private DonateRepository donateRepository;
    private RewardRepository rewardRepository;
    private CategoryRepository categoryRepository;

    private AppUser maker;
    private AppUser supporter;
    private Project project;
    private Project secondProject;
    private Category category;

    @BeforeEach
    void setUp() {

        userRepository = db.config.getUserRepository();
        projectRepository = db.config.getProjectRepository();
        donateRepository = db.config.getDonateRepository();
        rewardRepository = db.config.getRewardRepository();
        categoryRepository = db.config.getCategoryRepository();

        rewardService = new RewardService(
                projectRepository,
                donateRepository,
                rewardRepository,
                categoryRepository
        );

        // 테스트 데이터가 중복되지 않도록 고유값 생성
        String unique = UUID.randomUUID().toString().replace("-", "");

        maker = userRepository.create(
                AppUser.builder()
                        .name("테스트 메이커")
                        .email("maker-" + unique + "@example.com")
                        .build()
        );

        supporter = userRepository.create(
                AppUser.builder()
                        .name("테스트 서포터")
                        .email("supporter-" + unique + "@example.com")
                        .build()
        );

        assertNotNull(maker);
        assertNotNull(maker.getId());
        assertNotNull(supporter);
        assertNotNull(supporter.getId());

        category = categoryRepository.create(
                Category.builder()
                        .name("테스트 카테고리 " + unique)
                        .build()
        );

        assertNotNull(category);
        assertNotNull(category.getId());

        project = createProject();
        secondProject = createProject();
    }

    // 테스트용 프로젝트 생성
    private Project createProject() {

        Project saved = projectRepository.create(
                Project.builder()
                        .maker(maker)
                        .endDate(LocalDateTime.now().plusDays(30))
                        .build()
        );

        assertNotNull(saved);
        assertNotNull(saved.getId());

        return saved;
    }

    // 정상적인 리워드 등록 요청 생성
    private CreateRewardDataRequest validRequest(Long projectId, String name) {

        return new CreateRewardDataRequest(
                name,
                null,
                LocalDateTime.now().plusDays(7),
                "테스트 리워드 설명",
                projectId,
                BigDecimal.valueOf(10000),
                10L,
                null,
                null,
                null,
                null
        );
    }

    // DB에서 저장된 리워드 조회
    private Reward findSavedReward(Long projectId, String name) {

        return rewardRepository.findByProjectId(projectId)
                .stream()
                .filter(reward -> name.equals(reward.getName()))
                .findFirst()
                .orElseThrow(() ->
                        new AssertionError("리워드 조회 실패: " + name)
                );
    }

    // 테스트 종료 후 생성 데이터 삭제
    @AfterEach
    void tearDown() {

        if (supporter != null && supporter.getId() != null) {

            for (Donate donate :
                    donateRepository.findByUserId(supporter.getId())) {

                if (donate.getReward() == null ||
                        donate.getReward().getProject() == null) {
                    continue;
                }

                Long projectId = donate.getReward().getProject().getId();

                if ((project != null && projectId.equals(project.getId()))
                        || (secondProject != null &&
                        projectId.equals(secondProject.getId()))) {

                    donateRepository.delete(donate.getId());
                }
            }
        }

        for (Project f : new Project[]{project, secondProject}) {

            if (f == null || f.getId() == null) {
                continue;
            }

            for (Reward reward : rewardRepository.findByProjectId(f.getId())) {
                rewardRepository.delete(reward.getId());
            }

            projectRepository.delete(f.getId());
        }

        if (category != null && category.getId() != null) {
            categoryRepository.delete(category.getId());
        }

        if (supporter != null && supporter.getId() != null) {
            userRepository.delete(supporter.getId());
        }

        if (maker != null && maker.getId() != null) {
            userRepository.delete(maker.getId());
        }
    }

    // =========================================================
    // 리워드 등록 테스트
    // =========================================================

    @Nested
    @DisplayName("createReward")
    public class CreateReward {

        @Test
        @DisplayName("남은 수량을 최대 수량과 같게 생성한다")
        void success() {

            // Given
            CreateRewardDataRequest request =
                    validRequest(project.getId(), "정상 리워드");

            // When
            rewardService.createReward(request);

            // Then
            Reward saved =
                    findSavedReward(project.getId(), "정상 리워드");

            assertEquals(10L, saved.getMaxQuantity());
            assertEquals(10L, saved.getRemainQuantity());

            assertEquals(
                    0,
                    BigDecimal.valueOf(10000)
                            .compareTo(saved.getRewardPerAmount())
            );

            assertEquals(project.getId(), saved.getProject().getId());
        }

        @ParameterizedTest(name = "이름 \"{0}\"")
        @ValueSource(strings = {"", " "})
        @DisplayName("이름이 비어 있으면 LimitNumberOutException")
        void blankName(String name) {

            CreateRewardDataRequest request =
                    validRequest(project.getId(), name);

            int before =
                    rewardRepository.findByProjectId(project.getId()).size();

            assertThrows(
                    LimitNumberOutException.class,
                    () -> rewardService.createReward(request)
            );

            assertEquals(
                    before,
                    rewardRepository.findByProjectId(project.getId()).size()
            );
        }

        @ParameterizedTest(name = "금액 {0}")
        @ValueSource(strings = {"0", "-1000"})
        @DisplayName("금액이 0 이하면 LimitNumberOutException")
        void invalidAmount(String amount) {

            CreateRewardDataRequest request =
                    new CreateRewardDataRequest(
                            "금액 테스트",
                            null,
                            LocalDateTime.now().plusDays(7),
                            "설명",
                            project.getId(),
                            new BigDecimal(amount),
                            10L,
                            null,
                            null,
                            null,
                            null
                    );

            int before =
                    rewardRepository.findByProjectId(project.getId()).size();

            assertThrows(
                    LimitNumberOutException.class,
                    () -> rewardService.createReward(request)
            );

            assertEquals(
                    before,
                    rewardRepository.findByProjectId(project.getId()).size()
            );
        }

        @ParameterizedTest(name = "수량 {0}")
        @ValueSource(longs = {0L, -1L})
        @DisplayName("수량이 0 이하면 LimitNumberOutException")
        void invalidQuantity(Long quantity) {

            CreateRewardDataRequest request =
                    new CreateRewardDataRequest(
                            "수량 테스트",
                            null,
                            LocalDateTime.now().plusDays(7),
                            "설명",
                            project.getId(),
                            BigDecimal.valueOf(10000),
                            quantity,
                            null,
                            null,
                            null,
                            null
                    );

            int before =
                    rewardRepository.findByProjectId(project.getId()).size();

            assertThrows(
                    LimitNumberOutException.class,
                    () -> rewardService.createReward(request)
            );

            assertEquals(
                    before,
                    rewardRepository.findByProjectId(project.getId()).size()
            );
        }

        @Test
        @DisplayName("없는 프로젝트면 NotFoundException")
        void projectNotFound() {

            Long nonexistentId = Long.MAX_VALUE;

            CreateRewardDataRequest request =
                    validRequest(nonexistentId, "없는 프로젝트 리워드");

            assertThrows(
                    NotFoundException.class,
                    () -> rewardService.createReward(request)
            );
        }
    }

    // =========================================================
    // 리워드 조회 테스트
    // =========================================================

    @Nested
    @DisplayName("리워드 조회")
    class GetReward {

        @Test
        @DisplayName("프로젝트별 리워드를 조회한다")
        void byProject() {

            // Given
            rewardService.createReward(
                    validRequest(project.getId(), "프로젝트A 상품1")
            );

            rewardService.createReward(
                    validRequest(project.getId(), "프로젝트A 상품2")
            );

            rewardService.createReward(
                    validRequest(secondProject.getId(), "프로젝트B 상품")
            );

            // When
            List<RewardData> result =
                    rewardService.getRewardByProject(project.getId());

            // Then
            assertEquals(2, result.size());

            assertTrue(
                    result.stream().allMatch(
                            reward -> project.getId().equals(reward.getProjectId())
                    )
            );
        }

        @Test
        @DisplayName("후원한 리워드를 조회한다")
        void byDonate() {

            // Given
            rewardService.createReward(
                    validRequest(project.getId(), "후원한 리워드")
            );

            rewardService.createReward(
                    validRequest(secondProject.getId(), "후원 안 한 리워드")
            );

            Reward donatedReward =
                    findSavedReward(project.getId(), "후원한 리워드");

            Donate savedDonate = donateRepository.create(
                    Donate.builder()
                            .user(supporter)
                            .reward(donatedReward)
                            .amount(BigDecimal.valueOf(10000))
                            .quantity(1L)
                            .build()
            );

            assertNotNull(savedDonate);

            // When
            List<RewardData> result =
                    rewardService.getRewardByDonate(supporter.getId());

            // Then
            assertEquals(1, result.size());

            assertEquals(
                    donatedReward.getId(),
                    result.get(0).getRewardId()
            );
        }

        @Test
        @DisplayName("메이커의 프로젝트별 리워드를 조회한다")
        void byMaker() {

            // Given
            rewardService.createReward(
                    validRequest(project.getId(), "첫 번째 프로젝트 리워드")
            );

            rewardService.createReward(
                    validRequest(secondProject.getId(), "두 번째 프로젝트 리워드")
            );

            // When
            List<List<RewardData>> result =
                    rewardService.getRewardFindByMaker(maker.getId());

            // Then
            assertEquals(2, result.size());

            List<Long> allProjectIds = new ArrayList<>();

            for (List<RewardData> perProject : result) {
                for (RewardData reward : perProject) {
                    allProjectIds.add(reward.getProjectId());
                }
            }

            assertTrue(allProjectIds.contains(project.getId()));
            assertTrue(allProjectIds.contains(secondProject.getId()));
        }
    }
}