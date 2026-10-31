import example.db.InitDB;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import service.external.DonateServiceTest;
import service.external.FundingServiceTest;
import service.external.ReviewServiceTest;
import service.external.RewardServiceTest;
import service.external.UserServiceTest;

import java.io.PrintWriter;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

/**
 * 검사 항목별로 단위 테스트 메서드를 실행하는 클래스
 */
public class MainTest {

    public static void main(String[] args) {
        initDB();                          // 검사에 필요한 사전 데이터를 insert하는 메소드 (모든 테이블을 삭제 후 다시 생성)
        testAll();                         // service.external, service.internal의 테스트 전체 실행

        // 검사할 항목 하나만 실행하려면 위 testAll을 주석 처리하고 아래에서 주석 해제

        // test01ViewFundingStatus();      // MAKER     - 프로젝트별 펀딩 현황 보기
        // test02RegisterProject();        // MAKER     - 프로젝트 기본 정보 등록
        // test03RegisterReward();         // MAKER     - 리워드 등록 및 프로젝트 신청
        // test04ApproveProject();         // ADMIN     - 프로젝트 등록 승인
        // test05UpdateProfile();          // SUPPORTER - 개인정보 수정
        // test06ViewProjects();           // SUPPORTER - 프로젝트 보기
        // test07FilterProjects();         // SUPPORTER - 프로젝트 필터링
        // test08ApplyReward();            // SUPPORTER - 리워드 신청
        // test09RegisterReview();         // SUPPORTER - 리뷰 및 별점 등록
        // test10ConcurrencyControl();     // SUPPORTER - 제공 가능 수량 동시성 제어
    }

    private static void initDB() {
        InitDB.main(new String[0]);
    }

    private static void testAll() {
        run(selectPackage("service.external"), selectPackage("service.internal"));
    }

    private static void test01ViewFundingStatus() {
        run(selectMethod(FundingServiceTest.class, "getFundingByReward"));
    }

    private static void test02RegisterProject() {
        run(selectMethod(FundingServiceTest.class, "createFunding"));
    }

    private static void test03RegisterReward() {
        run(selectMethod(RewardServiceTest.CreateReward.class, "success"));
    }

    private static void test04ApproveProject() {
        //이름이 같은 external.FundingServiceTest와 구분하기 위해 전체 경로 사용
        run(selectMethod(FundingServiceTest.class, "updateFundingApprove"));
    }

    private static void test05UpdateProfile() {
        run(selectMethod(UserServiceTest.UpdateUser.class, "success"));
    }

    private static void test06ViewProjects() {
        run(selectMethod(FundingServiceTest.class, "getAllFunding"));
    }

    private static void test07FilterProjects() {
        run(selectMethod(FundingServiceTest.class, "getByCategoryAndState"));
    }

    private static void test08ApplyReward() {
        run(selectMethod(DonateServiceTest.CreateDonate.class, "success"));
    }

    private static void test09RegisterReview() {
        run(selectMethod(ReviewServiceTest.class, "createReview"));
    }

    //TODO 동시성 테스트를 작성하면 selectMethod로 넘기도록 변경
    private static void test10ConcurrencyControl() {
        run();
    }

    private static void run(DiscoverySelector... selectors) {
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        LauncherFactory.create().execute(
                LauncherDiscoveryRequestBuilder.request().selectors(selectors).build(), listener);

        PrintWriter out = new PrintWriter(System.out, true);
        listener.getSummary().printTo(out);
        listener.getSummary().printFailuresTo(out);
    }
}
