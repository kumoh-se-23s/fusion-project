package config;

import lombok.Getter;
import repository.*;
import repository.manager.schema.SchemaGenerator;
import service.external.FundingService;
import service.external.ReviewService;
import service.internal.CategoryService;

/**
 * 앱 시작 시 리포지토리, 서비스를 한 번 생성하고 주입하는 조립 클래스
 * 새 서비스를 만들면 여기에 등록한다.
 */
@Getter
public class AppConfig {
    private final CategoryRepository categoryRepository = new CategoryRepository();
    private final DenyFundingRepository denyFundingRepository = new DenyFundingRepository();
    private final DonateRepository donateRepository = new DonateRepository();
    private final FundingCategoryRepository fundingCategoryRepository = new FundingCategoryRepository();
    private final FundingLikeRepository fundingLikeRepository = new FundingLikeRepository();
    private final FundingRepository fundingRepository = new FundingRepository();
    private final ReviewLikeRepository reviewLikeRepository = new ReviewLikeRepository();
    private final ReviewRepository reviewRepository = new ReviewRepository();
    private final RewardRepository rewardRepository = new RewardRepository();
    private final UserRepository userRepository = new UserRepository();
    private final UserRoleRepository userRoleRepository = new UserRoleRepository();
    private final UserRoleRelationRepository userRoleRelationRepository = new UserRoleRelationRepository();

    private final CategoryService categoryService = new CategoryService(categoryRepository);
    private final FundingService fundingService = new FundingService(
            fundingRepository, donateRepository, rewardRepository,
            userRepository, categoryRepository, fundingCategoryRepository);
    //이름이 같은 external.FundingService와 구분하기 위해 전체 경로 사용
    private final service.internal.FundingService adminFundingService =
            new service.internal.FundingService(fundingRepository);
    private final ReviewService reviewService = new ReviewService(
            reviewRepository, userRepository, donateRepository, rewardRepository);

    public AppConfig() {
        //db.ddl-auto 설정에 따라 도메인 기준으로 테이블 생성
        SchemaGenerator.Mode ddlMode = SchemaGenerator.Mode.from(PropertieData.get("db.ddl-auto", "none"));
        new SchemaGenerator().execute(ddlMode, "model.domain");
    }
}
