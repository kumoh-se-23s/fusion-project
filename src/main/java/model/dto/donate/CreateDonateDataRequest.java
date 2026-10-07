package model.dto.donate;
import lombok.*;

import java.math.BigDecimal;

@Getter//값을 읽는 메서드 생성
@Builder//필드 이름을 지정하면서 객체 생성
@AllArgsConstructor//모든 필드 값을 받는 생성자 생성
@NoArgsConstructor//빈 괄호로 객체를 만드느 생성자 생성

public class CreateDonateDataRequest extends BaseData{
    private Long userFk;

    private Long rewardFk;

    private BigDecimal amount;

    private Long quantity;

    private Boolean state;
}
