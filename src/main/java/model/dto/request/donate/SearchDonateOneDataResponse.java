package model.dto.request.donate;


import lombok.*;

import java.math.BigDecimal;

@Getter//값을 읽는 메서드 생성
@Builder//필드 이름을 지정하면서 객체 생성
@AllArgsConstructor//모든 필드 값을 받는 생성자 생성
@NoArgsConstructor//빈 괄호로 객체를 만드느 생성자 생성


public class SearchDonateOneDataResponse {

    private Long donatePk;

    private Long userPk;

    private Long rewardPk;

    private BigDecimal amount;//후원금액

    private Long quantity;//후원 물품 수량
}
