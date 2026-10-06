package model.dto.RewardDatas;
import java.time.LocalDateTime;
import model.dto.BaseDTO;
import lombok.*;

@Getter//값을 읽는 메서드 생성
@Builder//필드 이름을 지정하면서 객체 생성
@AllArgsConstructor//모든 필드 값을 받는 생성자 생성
@NoArgsConstructor//빈 괄호로 객체를 만드느 생성자 생성
@BindDomain(Reward.class)//클래스가 연결할 도메인 지정
public class CreateRewardRequestData extends BaseDTO {
    @NotNull
    @DomainField("rewardName")
    private String rewardName;

    @NotNull
    @DomainField("rewardImg")
    private String rewardImg;

    @NotNull
    @DomainField("rewardCreateAt")
    private LocalDateTime createdAt;

    @NotNull
    @DomainField("servedAt")
    private LocalDateTime servedAt;

    @NotNull
    @DomainField("rewardText")
    private String rewardText;

    @NotNull
    @DomainField("fundingFk")
    private Long fundingFk;

    @NotNull
    @DomainField("requiredAmount")
    private Long requiredAmount;//float는 소수계싼에서 오차가 생리 수 이쏙, 기본형이라 null도 저장가능

    @NotNull
    @DomainField("rewardMaxQuantity")
    private Integer rewardMaxQuantity;//리워드 제공가능한 수량

    @NotNull
    @DomainField("rewardRemainQuantity")
    private Integer rewardReminQuantity;//리워드 현재 남은 수량

    @Getter//값을 읽는 메서드 생성
    @Builder//필드 이름을 지정하면서 객체 생성
    @AllArgsConstructor//모든 필드 값을 받는 생성자 생성
    @NoArgsConstructor//빈 괄호로 객체를 만드느 생성자 생성
    @BindDomain(Reward.class)//클래스가 연결할 도메인 지정

    public static class RewardList extends BaseDTO{
        //음 전체 조회는 파라미텅가 없는데 DTO를 어떻게 작성?
    }
}
