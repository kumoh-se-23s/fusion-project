package model.dto.response.donate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter//값을 읽는 메서드 생성
@Builder//필드 이름을 지정하면서 객체 생성
@AllArgsConstructor//모든 필드 값을 받는 생성자 생성
@NoArgsConstructor//빈 괄호로 객체를 만드느 생성자 생성

public class TotalDonateRequest {
    //패스 파라미터 respose없음
    private Long userFk;
    private Long donate;
    private Long fundingFk;
}
