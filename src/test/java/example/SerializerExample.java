package example;

import annotation.BindDomain;
import annotation.DataField;
import exception.SerializeException;
import model.Serializer;
import model.domain.AppUser;
import model.domain.Domain;
import model.domain.Project;
import model.dto.BaseData;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * {@link Serializer} 사용 예시.
 * <p>
 * 실제 DTO 에는 아직 {@link BindDomain} 이 붙어 있지 않으므로, 예시용 DataObject 를 이 파일 안에 정의해 사용한다.
 * main 을 실행하면 각 예시의 변환 결과가 출력된다.
 */
public class SerializerExample {

    // ===================================================================== 예시용 DataObject

    /** 1) 기본 매핑: 필드명이 같으면 자동으로 매핑된다. (AppUser.version 처럼 DTO 에 없는 필드는 소거) */
    @BindDomain(values = AppUser.class)
    public static class UserData extends BaseData {
        private Long id;
        private String name;
        private String email;
    }

    /** 2) 경로 매핑: @DataField(name = "maker.id") 로 연관 객체 내부 필드를 평탄화한다. */
    @BindDomain(values = Project.class)
    public static class ProjectSummaryData extends BaseData {
        private Long id;

        @DataField(name = "maker.id")
        private Long makerId;

        @DataField(name = "text")
        private String description;

        private BigDecimal purposeAmount;
        private LocalDateTime endDate;
    }

    /** 3) 재귀 변환: 필드 타입이 바인딩된 DataObject 이면 연관 객체도 함께 변환된다. */
    @BindDomain(values = Project.class)
    public static class ProjectDetailData extends BaseData {
        private Long id;
        private UserData maker;       // Project.maker(AppUser) <-> UserData
        private String text;
    }

    /** 4) 컬렉션 필드 예시용 도메인. (현재 도메인에는 컬렉션 필드가 없어서 예시로만 정의) */
    public static class UserGroup implements Domain {
        private String title;
        private List<AppUser> members;
    }

    @BindDomain(values = UserGroup.class)
    public static class UserGroupData extends BaseData {
        private String title;
        private List<UserData> members; // List<AppUser> <-> List<UserData>, 원소 단위로 재귀 변환
    }

    /** 바인딩이 없는 DataObject: 변환 시 SerializeException 이 발생한다. */
    public static class UnboundData extends BaseData {
        private Long id;
    }

    // ===================================================================== 예시 실행

    public static void main(String[] args) {
        AppUser maker = AppUser.builder()
                .id(1L)
                .name("홍길동")
                .email("hong@example.com")
                .build();

        Project project = Project.builder()
                .id(10L)
                .maker(maker)
                .text("프로젝트 설명")
                .purposeAmount(new BigDecimal("1000000"))
                .endDate(LocalDateTime.now().plusDays(30))
                .build();

        basicMapping(maker);
        pathMapping(project);
        nestedMapping(project);
        listMapping(maker);
        collectionFieldMapping(maker);
        errorCase(maker);
    }

    /** 1) Domain <-> DataObject 기본 변환 */
    private static void basicMapping(AppUser maker) {
        System.out.println("=== 1. 기본 매핑 ===");

        UserData data = Serializer.toData(maker, UserData.class);
        System.out.println(data.toJsonString());

        AppUser restored = Serializer.toDomain(data, AppUser.class);
        System.out.println("restored: id=" + restored.getId() + ", name=" + restored.getName());
    }

    /** 2) 경로 매핑: Domain -> DTO 는 maker.id 를 읽고, DTO -> Domain 은 id 만 채운 AppUser 를 만든다. */
    private static void pathMapping(Project project) {
        System.out.println("\n=== 2. 경로 매핑 (@DataField) ===");

        ProjectSummaryData data = Serializer.toData(project, ProjectSummaryData.class);
        System.out.println(data.toJsonString());

        Project restored = Serializer.toDomain(data, Project.class);
        AppUser restoredMaker = restored.getMaker();
        System.out.println("restored.maker: id=" + restoredMaker.getId() + ", name=" + restoredMaker.getName()); // name 은 null
        System.out.println("restored.text: " + restored.getText());
    }

    /** 3) 연관 객체 재귀 변환 */
    private static void nestedMapping(Project project) {
        System.out.println("\n=== 3. 재귀 변환 ===");

        ProjectDetailData data = Serializer.toData(project, ProjectDetailData.class);
        System.out.println(data.toJsonString());
        System.out.println("maker: " + data.maker.toJsonString());

        Project restored = Serializer.toDomain(data, Project.class);
        System.out.println("restored.maker.email: " + restored.getMaker().getEmail());
    }

    /** 4) 리스트 변환: 같은 maker 를 공유하는 프로젝트들은 변환 후에도 같은 UserData 인스턴스를 공유한다. */
    private static void listMapping(AppUser maker) {
        System.out.println("\n=== 4. 리스트 변환 ===");

        List<Project> projects = List.of(
                Project.builder().id(20L).maker(maker).endDate(LocalDateTime.now()).build(),
                Project.builder().id(21L).maker(maker).endDate(LocalDateTime.now()).build()
        );

        List<ProjectDetailData> datas = Serializer.toDataList(projects, ProjectDetailData.class);
        System.out.println("size: " + datas.size());
        System.out.println("maker 공유 여부: " + (datas.get(0).maker == datas.get(1).maker)); // true

        List<Project> restored = Serializer.toDomainList(datas, Project.class);
        System.out.println("restored ids: " + restored.stream().map(Project::getId).toList());
    }

    /** 5) 컬렉션 필드 변환 */
    private static void collectionFieldMapping(AppUser maker) {
        System.out.println("\n=== 5. 컬렉션 필드 변환 ===");

        UserGroup group = new UserGroup();
        group.title = "운영진";
        group.members = List.of(
                maker,
                AppUser.builder().id(2L).name("김철수").email("kim@example.com").build()
        );

        UserGroupData data = Serializer.toData(group, UserGroupData.class);
        data.members.forEach(member -> System.out.println(member.toJsonString()));

        UserGroup restored = Serializer.toDomain(data, UserGroup.class);
        System.out.println("restored members: " + restored.members.stream().map(AppUser::getName).toList());
    }

    /** 6) 잘못된 설정은 첫 변환 시점에 SerializeException 으로 바로 실패한다. */
    private static void errorCase(AppUser maker) {
        System.out.println("\n=== 6. 오류 예시 ===");

        try {
            Serializer.toData(maker, UnboundData.class);
        } catch (SerializeException exception) {
            System.out.println("SerializeException: " + exception.getMessage());
        }
    }
}
