package service.external;

import constant.FundingState;
import exception.NotFoundException;
import exception.NotNullException;
import exception.OutOfRangeException;
import lombok.RequiredArgsConstructor;
import model.domain.*;
import model.dto.FundingData;
import model.dto.request.funding.CreateFundingRequest;
import model.dto.request.funding.GetFundingByCategoryAndStateRequest;
import model.dto.response.DataCollectionResponse;
import model.dto.response.PKResponse;
import model.dto.response.donate.DonateUserAddressResponse;
import model.dto.response.funding.CreateFundingByCategoryResponse;
import model.dto.response.funding.FundingRewardResponse;
import model.dto.response.fundingCategory.BindCategoryResponse;
import model.dto.response.reward.RewardResponse;
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
            List<RewardResponse> rewardResponses = new ArrayList<>();
            Map<Long, List<DonateUserAddressResponse>> rewardPKSet = new HashMap<>();
            for (Donate donate : donateList) {
                DonateUserAddressResponse donateUserAddressResponse =
                    DonateUserAddressResponse.builder()
                        .donatePK(donate.getPk())
                        .userPK(donate.getUser().getPk())
                        .address(donate.getUser().getAddress())
                        .build();
                rewardPKSet.computeIfAbsent(donate.getReward().getPk(), k -> new ArrayList<>())
                        .add(donateUserAddressResponse);
            }
            for (Reward reward : rewardList) {
                rewardResponses.add(
                        RewardResponse.builder()
                            .rewardPk(reward.getPk())
                            .name(reward.getName())
                            .maxQuantity(reward.getMaxQuantity())
                            .remainQuantity(reward.getRemainQuantity())
                            .requiredAmount(reward.getRequiredAmount())
                            .provideDate(reward.getProvideDate())
                            .donateUserInfo(rewardPKSet.getOrDefault(reward.getPk(), List.of()))
                            .build()
                );
            }
            return FundingRewardResponse.builder()
                .pk(funding.getPk())
                .title(funding.getTitle())
                .purposeAmount(funding.getPurposeAmount())
                .donateAmount(funding.getDonateAmount())
                .rewards(rewardResponses)
                .build();
        });
    }

    //2. 프로젝트 기본 정보 등록
    public CreateFundingByCategoryResponse createFunding(Long makerPK, CreateFundingRequest request) {
        return tx.execute(false, () -> {
            AppUser maker = userRepository.findByPK(makerPK)
                    .orElseThrow(NotFoundException::new);



            Funding funding;
            try {
                funding = Funding.builder()
                        .maker(maker)
                        .startDate(request.getStartDate())
                        .endDate(request.getEndDate())
                        .purposeAmount(request.getPurposeAmount())
                        .title(request.getTitle())
                        .text(request.getText())
                        .imgFile(request.getImgFile())
                        .state(FundingState.JUST_USER_INPUT)
                        .build();
            } catch (NullPointerException e) {
                throw new NotNullException("Funding has Null Value");
            }
            //두 값이 null이 아닌지 먼저 검사하고 판단
            if (funding.getPurposeAmount().signum() <= 0) {
                throw new OutOfRangeException("purposeAmount must be greater than 0");
            }
            if (funding.getEndDate().isAfter(funding.getStartDate())) {
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
                FundingData dto = FundingData.builder()
                        .pk(funding.getPk())
                        .makerPK(funding.getMaker().getPk())
                        .title(funding.getTitle())
                        .text(funding.getText())
                        .createdAt(funding.getCreatedAt())
                        .updatedAt(funding.getUpdatedAt())
                        .purposeAmount(funding.getPurposeAmount())
                        .donateAmount(funding.getDonateAmount())
                        .startDate(funding.getStartDate())
                        .endDate(funding.getEndDate())
                        .state(funding.getState())
                        .likeCount(funding.getLikeCount())
                        .dislikeCount(funding.getDislikeCount())
                        .imgFile(funding.getImgFile())
                        .build();
                dtos.add(dto);
            }

            return DataCollectionResponse.<FundingData>builder()
                    .datas(dtos)
                    .amount((long) dtos.size())
                    .build();
        });
    }

    //7. 펀딩 필터링
    public DataCollectionResponse<FundingData> getByCategoryAndState(GetFundingByCategoryAndStateRequest dto)  {
        return tx.execute(true, () -> {
            List<FundingData> dtos = new ArrayList<>();

            List<Funding> fundingList = dto.getFundingStateList().isEmpty() ?
                    List.of() : fundingRepository.findByState(dto.getFundingStateList());

            List<Category> categoryList = dto.getCategoryList().isEmpty() ?
                    List.of() : categoryRepository.findByName(dto.getCategoryList());


            //한 펀딩이 여러 카테고리에 속하면 중복으로 조회되므로 한 번만 담는다
            Set<Long> fundingPKSet = new HashSet<>();

            for (FundingCategory fundingCategory :
                    fundingCategoryRepository.findByFundingAndCategoryPKs(
                            fundingList, categoryList
                    )
            ) {
                if (!fundingPKSet.add(fundingCategory.getFunding().getPk())) {
                    continue;
                }

                FundingData fundingDTO = FundingData.builder()
                        .pk(fundingCategory.getFunding().getPk())
                        .makerPK(fundingCategory.getFunding().getMaker().getPk())
                        .title(fundingCategory.getFunding().getTitle())
                        .text(fundingCategory.getFunding().getText())
                        .createdAt(fundingCategory.getFunding().getCreatedAt())
                        .updatedAt(fundingCategory.getFunding().getUpdatedAt())
                        .purposeAmount(fundingCategory.getFunding().getPurposeAmount())
                        .donateAmount(fundingCategory.getFunding().getDonateAmount())
                        .startDate(fundingCategory.getFunding().getStartDate())
                        .endDate(fundingCategory.getFunding().getEndDate())
                        .state(fundingCategory.getFunding().getState())
                        .likeCount(fundingCategory.getFunding().getLikeCount())
                        .dislikeCount(fundingCategory.getFunding().getDislikeCount())
                        .imgFile(fundingCategory.getFunding().getImgFile())
                        .build();

                dtos.add(fundingDTO);
            }
            return DataCollectionResponse.<FundingData>builder()
                    .datas(dtos)
                    .amount((long) dtos.size())
                    .build();
        });
    }

}
