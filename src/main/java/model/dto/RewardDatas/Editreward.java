package model.dto.RewardDatas;
import java.time.LocalDateTime;
import model.dto.BaseDTO;
import annotation.*;
import lombok.*;

@Getter//값을 읽는 메서드 생성
@Builder//필드 이름을 지정하면서 객체 생성
@AllArgsConstructor//모든 필드 값을 받는 생성자 생성
@NoArgsConstructor//빈 괄호로 객체를 만드느 생성자 생성
@BindDomain(Reward.class)//클래스가 연결할 도메인 지정

public class Editreward extends BaseDTO{
    @Annotation.NotNull
    @Annotation.DomainField("reward")
    private Reward rewardPk;
}//대체 뭐가 문제야........

