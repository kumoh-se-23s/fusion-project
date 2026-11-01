import example.db.InitDB;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;

import java.io.PrintWriter;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

/**
 * 검사 항목 실행 클래스. 테스트 본문은 {@link MainTest}에 있다.
 * main에서 항목의 주석을 해제해 실행하면 콘솔에 JUnit 요약이 출력된다.
 */
public class Main {
    public static void main(String[] args) {
         initDB();                       // 검사에 필요한 사전 데이터를 insert하는 메소드 (모든 테이블을 삭제 후 다시 생성)
//         검사할 항목 하나만 주석 해제하여 실행

         test01ViewFundingStatus();      // MAKER     - 프로젝트별 펀딩 현황 보기
         test02RegisterProject();        // MAKER     - 프로젝트 기본 정보 등록
         test03RegisterReward();         // MAKER     - 리워드 등록 및 프로젝트 신청
         test04ApproveProject();         // ADMIN     - 프로젝트 등록 승인
         test05UpdateProfile();          // SUPPORTER - 개인정보 수정
         test06ViewProjects();           // SUPPORTER - 프로젝트 보기
         test07FilterProjects();         // SUPPORTER - 프로젝트 필터링
         test08ApplyReward();            // SUPPORTER - 리워드 신청
         test09RegisterReview();         // SUPPORTER - 리뷰 및 별점 등록
         test10ConcurrencyControl();     // SUPPORTER - 제공 가능 수량 동시성 제어
    }

    private static void initDB() {
        InitDB.main(new String[0]);
    }

    private static void test01ViewFundingStatus() {
        run(MainTest.Test01ViewFundingStatus.class);
    }

    private static void test02RegisterProject() {
        run(MainTest.Test02RegisterProject.class);
    }

    private static void test03RegisterReward() {
        run(MainTest.Test03RegisterReward.class);
    }

    private static void test04ApproveProject() {
        run(MainTest.Test04ApproveProject.class);
    }

    private static void test05UpdateProfile() {
        run(MainTest.Test05UpdateProfile.class);
    }

    private static void test06ViewProjects() {
        run(MainTest.Test06ViewProjects.class);
    }

    private static void test07FilterProjects() {
        run(MainTest.Test07FilterProjects.class);
    }

    private static void test08ApplyReward() {
        run(MainTest.Test08ApplyReward.class);
    }

    private static void test09RegisterReview() {
        run(MainTest.Test09RegisterReview.class);
    }

    private static void test10ConcurrencyControl() {
        run(MainTest.Test10ConcurrencyControl.class);
    }

    private static void run(Class<?> testClass) {
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        LauncherFactory.create().execute(
                LauncherDiscoveryRequestBuilder.request().selectors(selectClass(testClass)).build(), listener);

        PrintWriter out = new PrintWriter(System.out, true);
        listener.getSummary().printTo(out);
        listener.getSummary().printFailuresTo(out);
    }
}
