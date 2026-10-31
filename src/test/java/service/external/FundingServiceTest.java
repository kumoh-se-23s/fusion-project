package service.external;

import constant.FundingState;
import example.db.DatabaseTest;
import model.domain.Funding;
import model.domain.FundingCategory;
import model.dto.FundingData;
import model.dto.request.funding.CreateFundingByCategoryRequest;
import model.dto.request.funding.CreateFundingRequest;
import model.dto.request.funding.GetFundingByCategoryAndStateRequest;
import model.dto.response.DataCollectionResponse;
import model.dto.response.funding.CreateFundingByCategoryResponse;
import model.dto.response.funding.FundingRewardResponse;
import model.dto.response.reward.RewardInfoResponse;
import model.dto.response.reward.RewardUserResponse;
import model.dto.response.user.UserAddressResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.Filter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("FundingService")
public class FundingServiceTest extends DatabaseTest {
    private static final Logger LOG = LoggerFactory.getLogger(FundingServiceTest.class);

    private FundingService fundingService;

    @BeforeEach
    void setUp() {
        fundingService = db.config.getFundingService();
    }

    @Test
    @DisplayName("펀딩의 리워드와 리워드별 후원자를 조회한다")
    void getFundingByReward() {
        FundingRewardResponse response = fundingService.getFundingByReward(db.maker1CompleteFunding.getPk());
        LOG.info(response.toJsonString());

        assertEquals(db.maker1CompleteFunding.getPk(), response.getFundingInfo().getFundingPK());
        assertEquals(2, response.getRewards().size());
        Map<String, RewardInfoResponse> list = new HashMap<>();
        Map<String, UserAddressResponse> list2 = new HashMap<>();
        for (RewardUserResponse reward : response.getRewards()) {
            list.put(reward.getRewardInfoResponse().getName()
            , reward.getRewardInfoResponse());
            for (UserAddressResponse user : reward.getUserAddressInfo()) {
                list2.put(user.getAddress(),
                        user);
            }
        }
        assertEquals(
                db.maker1CompleteReward1.getPk(),
                list.get(db.maker1CompleteReward1.getName()).getRewardPk());

        assertEquals(
                db.maker1CompleteReward2.getPk(),
                list.get(db.maker1CompleteReward2.getName()).getRewardPk());

        assertEquals(
                db.donateUser1.getPk(),
                list2.get(db.donateUser1.getAddress()).getUserPK());
    }
    @Test
    @DisplayName("펀딩을 생성한다")
    void createFunding() {
        LocalDateTime endDate = LocalDateTime.now().plusDays(30).truncatedTo(ChronoUnit.MICROS);

        CreateFundingByCategoryResponse response = fundingService.createFunding(db.maker1.getPk(),
                CreateFundingByCategoryRequest.builder()
                        .createFundingRequest(CreateFundingRequest.builder()
                                .title("테스트용 새 프로젝트")
                                .text("프로젝트 소개")
                                .purposeAmount(new BigDecimal("123456"))
                                .endDate(endDate)
                                .build())
                        .categoryPKs(List.of(db.food.getPk(), db.tech.getPk()))
                        .build());

        Funding saved = db.config.getFundingRepository().findByPK(response.getPk()).orElseThrow();
        assertEquals(db.maker1.getPk(), saved.getMaker().getPk());
        assertEquals("테스트용 새 프로젝트", saved.getTitle());
        assertEquals("프로젝트 소개", saved.getText());
        assertAmount("123456", saved.getPurposeAmount());
        assertEquals(endDate, saved.getEndDate());
        assertEquals(FundingState.JUST_USER_INPUT, saved.getState());
        List<FundingCategory> list = db.config.getFundingCategoryRepository()
                        .find(Filter.builder()
                        .conditions(Map.of("funding", saved))
                        .build());

        assertEquals(saved.getPk(), list.getFirst().getFunding().getPk());
        assertEquals(Set.of(db.food.getPk(), db.tech.getPk()),
                list.stream()
                .map(fundingCategory -> fundingCategory.getCategory().getPk())
                .collect(Collectors.toSet()));

    }

    @Test
    @DisplayName("전체 펀딩을 조회한다")
    void getAllFunding() {
        DataCollectionResponse<FundingData> response = fundingService.getAllFunding();
        LOG.info(response.toJsonString());
        List<Long> pkList = response.getDatas().stream()
                        .map(FundingData::getPk).toList();
        assertTrue(pkList.contains(db.maker1ApproveFunding.getPk()));
        assertTrue(pkList.contains(db.maker2ApproveFunding.getPk()));
        assertTrue(pkList.contains(db.maker1PendingFunding.getPk()));
        assertTrue(pkList.contains(db.maker2PendingFunding.getPk()));
        assertTrue(pkList.contains(db.maker1CompleteFunding.getPk()));
    }

    @Test
    @DisplayName("카테고리와 상태로 필터링한다")
    void getByCategoryAndState() {
        DataCollectionResponse<FundingData> response = fundingService.getByCategoryAndState(
                GetFundingByCategoryAndStateRequest.builder()
                        .categoryList(List.of(db.tech.getName()))
                        .fundingStateList(List.of(FundingState.APPROVED))
                        .build());
        LOG.info(response.toJsonString());
        List<Long> pkList = response.getDatas().stream()
                        .map(FundingData::getPk).toList();
        assertTrue(pkList.contains(db.maker1ApproveFunding.getPk()));
        assertTrue(pkList.contains(db.maker2ApproveFunding.getPk()));


    }


}
