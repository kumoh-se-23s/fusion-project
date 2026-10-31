package service.external;

import constant.FundingState;
import exception.NotFoundException;
import exception.NotNullException;
import exception.OutOfRangeException;
import lombok.RequiredArgsConstructor;
import model.Serializer;
import model.domain.*;
import model.dto.FundingData;
import model.dto.request.funding.CreateFundingByCategoryRequest;
import model.dto.request.funding.CreateFundingRequest;
import model.dto.request.funding.GetFundingByCategoryAndStateRequest;
import model.dto.response.DataCollectionResponse;
import model.dto.response.PKResponse;
import model.dto.response.funding.FundingInfoResponse;
import model.dto.response.reward.RewardInfoResponse;
import model.dto.response.reward.RewardUserResponse;
import model.dto.response.funding.CreateFundingByCategoryResponse;
import model.dto.response.funding.FundingRewardResponse;
import model.dto.response.fundingCategory.BindCategoryResponse;
import model.dto.response.user.UserAddressResponse;
import repository.*;
import service.BaseService;

import java.util.*;

@RequiredArgsConstructor
public class FundingService extends BaseService {
    private final FundingRepository fundingRepository;
    private final DonateRepository donateRepository;
    private final RewardRepository rewardRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final FundingCategoryRepository fundingCategoryRepository;

    //1.펀딩의 리워드 정보와 리워드별 후원자 배송지
    public FundingRewardResponse getFundingByReward(Long fundingPK) {
        return tx.execute(true, () -> {
            Funding funding = fundingRepository.findByPK(fundingPK)
                    .orElseThrow(NotFoundException::new);

            List<Reward> rewardList = rewardRepository.findByFunding(funding);
            List<Donate> donateList = donateRepository.findByRewards(rewardList);
            List<RewardUserResponse> rewardUserResponses = new ArrayList<>();
            Map<Long, List<UserAddressResponse>> rewardPKSet = new HashMap<>();

            for (Donate donate : donateList) {
                //탈퇴한 사용자의 후원은 배송지가 없으므로 제외
                if (donate.getUser() == null) {
                    continue;
                }
                UserAddressResponse UserAddressDTO =
                    Serializer.toData(donate.getUser(), UserAddressResponse.class);

                rewardPKSet.computeIfAbsent(donate.getReward().getPk(), k -> new ArrayList<>())
                    .add(UserAddressDTO);
            }
            for (Reward reward : rewardList) {
                rewardUserResponses.add(
                        RewardUserResponse.builder()
                            .rewardInfoResponse(Serializer.toData(reward, RewardInfoResponse.class))
                            .userAddressInfo(rewardPKSet.getOrDefault(reward.getPk(), List.of()))
                            .build()
                );
            }
            return FundingRewardResponse.builder()
                .fundingInfo(Serializer.toData(funding, FundingInfoResponse.class))
                .rewards(rewardUserResponses)
                .build();
        });
    }

    //2. 프로젝트 기본 정보 등록
    public CreateFundingByCategoryResponse createFunding(Long makerPK, CreateFundingByCategoryRequest request) {
        return tx.execute(false, () -> {
            AppUser maker = userRepository.findByPK(makerPK)
                    .orElseThrow(NotFoundException::new);

            CreateFundingRequest fundingRequest = request.getCreateFundingRequest();
            if (fundingRequest == null || fundingRequest.getEndDate() == null) {
                throw new NotNullException("Funding has Null Value");
            }
            //요청에서 비운 startDate, purposeAmount는 도메인 기본값을 유지하고 state는 JUST_USER_INPUT으로 시작
            Funding funding = Serializer.toDomain(fundingRequest, Funding.class)
                    .toBuilder()
                    .maker(maker)
                    .build();

            if (funding.getPurposeAmount().signum() <= 0) {
                throw new OutOfRangeException("purposeAmount must be greater than 0");
            }
            if (!funding.getEndDate().isAfter(funding.getStartDate())) {
                throw new OutOfRangeException("endDate must be after startDate");
            }

            Funding resultFunding = fundingRepository.create(funding);

            //카테고리를 지정하지 않으면 카테고리 없이 등록
            List<Category> categoryList = (request.getCategoryPKs() == null) ?
                    List.of() : categoryRepository.findByPKs(request.getCategoryPKs());

            List<PKResponse> fundingCategoryDTOs = new ArrayList<>();
            for (Category category : categoryList) {
                FundingCategory fundingCategory = FundingCategory.builder()
                    .category(category)
                    .funding(resultFunding)
                    .build();

                fundingCategoryDTOs.add(
                    new PKResponse(
                        fundingCategoryRepository.create(fundingCategory).getPk()
                    )
                );
            }
            return CreateFundingByCategoryResponse.builder()
                    .pk(resultFunding.getPk())
                    .fundingCategoryPKs(new BindCategoryResponse(fundingCategoryDTOs))
                    .build();
        });
    }

    //6. 전체 펀딩 조회
    public DataCollectionResponse<FundingData> getAllFunding() {
        return tx.execute(true, () -> {
            List<FundingData> dtos = new ArrayList<>();
            for (Funding funding : fundingRepository.findAll()) {
                dtos.add(Serializer.toData(funding, FundingData.class));
            }

            return DataCollectionResponse.<FundingData>builder()
                    .datas(dtos)
                    .amount((long) dtos.size())
                    .build();
        });
    }

    //7. 펀딩 필터링. 목록이 null이거나 비어 있으면 그 조건은 적용하지 않는다
    public DataCollectionResponse<FundingData> getByCategoryAndState(GetFundingByCategoryAndStateRequest dto)  {
        return tx.execute(true, () -> {
            List<FundingData> dtos = new ArrayList<>();

            List<FundingState> stateList = dto.getFundingStateList();
            List<String> categoryNames = dto.getCategoryList();
            boolean filterState = stateList != null && !stateList.isEmpty();

            List<Funding> fundingList = filterState ?
                    fundingRepository.findByState(stateList) : fundingRepository.findAll();

            //카테고리 조건이 없으면 카테고리가 없는 펀딩도 조회되도록 바로 반환한다
            if (categoryNames == null || categoryNames.isEmpty()) {
                return DataCollectionResponse.<FundingData>builder()
                        .datas(Serializer.toDataList(fundingList, FundingData.class))
                        .amount((long) fundingList.size())
                        .build();
            }

            List<Category> categoryList = categoryRepository.findByName(categoryNames);

            //한 펀딩이 여러 카테고리에 속하면 중복으로 조회되므로 한 번만 담는다
            Set<Long> fundingPKSet = new HashSet<>();

            for (FundingCategory fundingCategory :
                    fundingCategoryRepository.findByFundingAndCategoryPKs(fundingList, categoryList)) {
                if (!fundingPKSet.add(fundingCategory.getFunding().getPk())) {
                    continue;
                }
                dtos.add(Serializer.toData(fundingCategory.getFunding(), FundingData.class));
            }
            return DataCollectionResponse.<FundingData>builder()
                    .datas(dtos)
                    .amount((long) dtos.size())
                    .build();
        });
    }

}
