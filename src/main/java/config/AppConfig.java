package config;

import lombok.Getter;
import repository.CategoryRepository;
import repository.DenyFundingRepository;
import repository.DonateRepository;
import repository.FundingLikeRepository;
import repository.FundingRepository;
import repository.ReviewLikeRepository;
import repository.ReviewRepository;
import repository.RewardRepository;
import repository.UserRepository;
import repository.manager.schema.SchemaGenerator;
import service.internal.CategoryService;
import service.FundingCategoryService;
import service.external.FundingService;

/**
 * 앱 시작 시 리포지토리, 서비스를 한 번 생성하고 주입하는 조립 클래스
 * 새 서비스를 만들면 여기에 등록한다.
 */
@Getter
public class AppConfig {
    private final CategoryRepository categoryRepository = new CategoryRepository();
    private final DenyFundingRepository denyFundingRepository = new DenyFundingRepository();
    private final DonateRepository donateRepository = new DonateRepository();
    private final FundingLikeRepository fundingLikeRepository = new FundingLikeRepository();
    private final FundingRepository fundingRepository = new FundingRepository();
    private final ReviewLikeRepository reviewLikeRepository = new ReviewLikeRepository();
    private final ReviewRepository reviewRepository = new ReviewRepository();
    private final RewardRepository rewardRepository = new RewardRepository();
    private final UserRepository userRepository = new UserRepository();

    private final CategoryService categoryService = new CategoryService(categoryRepository);
    private final FundingCategoryService fundingCategoryService = new FundingCategoryService();
    private final FundingService fundingService =
            new FundingService(fundingRepository, donateRepository, rewardRepository);

    public AppConfig() {
        //db.ddl-auto 설정에 따라 도메인 기준으로 테이블 생성
        SchemaGenerator.Mode ddlMode = SchemaGenerator.Mode.from(PropertieData.get("db.ddl-auto", "none"));
        new SchemaGenerator().execute(ddlMode, "model.domain");
    }
}
