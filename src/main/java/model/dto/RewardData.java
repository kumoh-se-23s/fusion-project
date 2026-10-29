package model.dto;

import annotation.BindDomain;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.domain.Reward;
import model.dto.BaseData;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter//값을 읽는 메서드 생성
@Builder//필드 이름을 지정하면서 객체 생성
@AllArgsConstructor//모든 필드 값을 받는 생성자 생성
@NoArgsConstructor//빈 괄호로 객체를 만드느 생성자 생성



public class RewardData extends BaseData {
//이건 리워드 조회respose용

    private Long rewardPk;

    private String name;

    private byte[] imgFile;

    private Long fundingPk;

    private Long makerPk;

    private LocalDateTime providerDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String text;

    private BigDecimal requiredAmount;//목표금액

    private Long maxQuantity; //제공가능한 수량

    private Long remainQuantity;//남아있는 리워드 수량


}
