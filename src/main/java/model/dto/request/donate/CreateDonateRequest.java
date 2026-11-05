package model.dto.request.donate;

import annotation.BindDomain;
import annotation.DataField;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import model.domain.AppUser;
import model.domain.Donate;
import model.domain.Reward;
import model.dto.BaseData;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@BindDomain(values = {Donate.class})
public class CreateDonateRequest extends BaseData {

    @DataField(name = "user.id")
    private Long userFk;

    @DataField(name = "reward.id")
    private Long rewardFk;

    private BigDecimal amount;

    private Long quantity;

    private LocalDateTime createdAt;

    private Boolean state;

    // Service에서 조회한 객체
    private AppUser user;

    private Reward reward;

    // 사용자가 입력한 정보
    public CreateDonateRequest(
            Long userFk,
            Long rewardFk,
            BigDecimal amount,
            Long quantity
    ) {
        this.userFk = userFk;
        this.rewardFk = rewardFk;
        this.amount = amount;
        this.quantity = quantity;
    }

    // 후원 생성 전 초기값 설정
    public void prepareForCreate(
            AppUser user,
            Reward reward
    ) {
        this.user = user;
        this.reward = reward;

        this.createdAt = LocalDateTime.now();
        this.state = false;
    }
}