package example;

import annotation.BindDomain;
import annotation.DataField;
import exception.SerializeException;
import model.Serializer;
import model.domain.AppUser;
import model.domain.Domain;
import model.domain.Funding;
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
        private Long pk;
        private String name;
        private String email;
    }

    /** 2) 경로 매핑: @DataField(name = "maker.pk") 로 연관 객체 내부 필드를 평탄화한다. */
    @BindDomain(values = Funding.class)
    public static class FundingSummaryData extends BaseData {
        private Long pk;

        @DataField(name = "maker.pk")
        private Long makerPK;

        @DataField(name = "text")
        private String description;

        private BigDecimal purposeAmount;
        private LocalDateTime endDate;
    }

    /** 3) 재귀 변환: 필드 타입이 바인딩된 DataObject 이면 연관 객체도 함께 변환된다. */
    @BindDomain(values = Funding.class)
    public static class FundingDetailData extends BaseData {
        private Long pk;
        private UserData maker;       // Funding.maker(AppUser) <-> UserData
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
        private Long pk;
    }

    // ===================================================================== 예시 실행

    public static void main(String[] args) {
        AppUser maker = AppUser.builder()
                .pk(1L)
                .name("홍길동")
                .email("hong@example.com")
                .build();

        Funding funding = Funding.builder()
                .pk(10L)
                .maker(maker)
                .text("펀딩 설명")
                .purposeAmount(new BigDecimal("1000000"))
                .endDate(LocalDateTime.now().plusDays(30))
                .build();

        basicMapping(maker);
        pathMapping(funding);
        nestedMapping(funding);
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
        System.out.println("restored: pk=" + restored.getPk() + ", name=" + restored.getName());
    }

    /** 2) 경로 매핑: Domain -> DTO 는 maker.pk 를 읽고, DTO -> Domain 은 pk 만 채운 AppUser 를 만든다. */
    private static void pathMapping(Funding funding) {
        System.out.println("\n=== 2. 경로 매핑 (@DataField) ===");

        FundingSummaryData data = Serializer.toData(funding, FundingSummaryData.class);
        System.out.println(data.toJsonString());

        Funding restored = Serializer.toDomain(data, Funding.class);
        AppUser restoredMaker = restored.getMaker();
        System.out.println("restored.maker: pk=" + restoredMaker.getPk() + ", name=" + restoredMaker.getName()); // name 은 null
        System.out.println("restored.text: " + restored.getText());
    }

    /** 3) 연관 객체 재귀 변환 */
    private static void nestedMapping(Funding funding) {
        System.out.println("\n=== 3. 재귀 변환 ===");

        FundingDetailData data = Serializer.toData(funding, FundingDetailData.class);
        System.out.println(data.toJsonString());
        System.out.println("maker: " + data.maker.toJsonString());

        Funding restored = Serializer.toDomain(data, Funding.class);
        System.out.println("restored.maker.email: " + restored.getMaker().getEmail());
    }

    /** 4) 리스트 변환: 같은 maker 를 공유하는 펀딩들은 변환 후에도 같은 UserData 인스턴스를 공유한다. */
    private static void listMapping(AppUser maker) {
        System.out.println("\n=== 4. 리스트 변환 ===");

        List<Funding> fundings = List.of(
                Funding.builder().pk(20L).maker(maker).endDate(LocalDateTime.now()).build(),
                Funding.builder().pk(21L).maker(maker).endDate(LocalDateTime.now()).build()
        );

        List<FundingDetailData> datas = Serializer.toDataList(fundings, FundingDetailData.class);
        System.out.println("size: " + datas.size());
        System.out.println("maker 공유 여부: " + (datas.get(0).maker == datas.get(1).maker)); // true

        List<Funding> restored = Serializer.toDomainList(datas, Funding.class);
        System.out.println("restored pks: " + restored.stream().map(Funding::getPk).toList());
    }

    /** 5) 컬렉션 필드 변환 */
    private static void collectionFieldMapping(AppUser maker) {
        System.out.println("\n=== 5. 컬렉션 필드 변환 ===");

        UserGroup group = new UserGroup();
        group.title = "운영진";
        group.members = List.of(
                maker,
                AppUser.builder().pk(2L).name("김철수").email("kim@example.com").build()
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
