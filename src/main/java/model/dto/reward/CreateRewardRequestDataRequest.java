package model.dto.reward;
import java.time.LocalDateTime;

import lombok.*;

@Getter//값을 읽는 메서드 생성
@Builder//필드 이름을 지정하면서 객체 생성
@AllArgsConstructor//모든 필드 값을 받는 생성자 생성
//@NoArgsConstructor//빈 괄호로 객체를 만드느 생성자 생성>있으면 안됨

public class CreateRewardRequestDataRequest extends BaseDate {//BaseData로 바꿀거임

  //  @DomainField("rewardName")
    private String rewardName;


    //@DomainField("rewardImg")
    private String rewardImg;


    //DomainField("rewardCreateAt")
    // 서비스 시점에서 자동으로 넣어서 생성private LocalDateTime createdAt;


    //@DomainField("servedAt")
    private LocalDateTime provideDate;


    //@DomainField("rewardText")
    private String rewardText;


    //@DomainField("fundingFk")
    private Long fundingFk;


    //@DomainField("requiredAmount")
    private Long requiredAmount;//float는 소수계싼에서 오차가 생리 수 이쏙, 기본형이라 null도 저장가능


    //@DomainField("rewardMaxQuantity")
    private Long rewardMaxQuantity;//리워드 제공가능한 수량


    //@DomainField("rewardRemainQuantity")
    private Long rewardRemainQuantity;//리워드 현재 남은 수량


