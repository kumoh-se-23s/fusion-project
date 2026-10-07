package model.dto.reward;

import lombok.*;

@Getter//값을 읽는 메서드 생성
@Builder//필드 이름을 지정하면서 객체 생성
@AllArgsConstructor//모든 필드 값을 받는 생성자 생성
@NoArgsConstructor//빈 괄호로 객체를 만드느 생성자 생성



public class RewardListDataOneDataResponse extends BaseData{
//이건 전체 조회 관리자용 내부

    private Long rewardPk;

    private String name;

    private Byte[] imgFile;

    private Long fundingPk;

    private Long makerPk;

    private LocalDateTime providerDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String text;

    private BigDecimal requiredAmount;

    private Long maxQuantity; 제공가능한 수량

    private Long remainQuantity;//남아있는 리워드 수량


}
