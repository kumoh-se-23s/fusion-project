package model.dto.request.reward;

import annotation.BindDomain;
import annotation.DataField;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import model.domain.Category;
import model.domain.Funding;
import model.domain.Reward;
import model.dto.BaseData;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@BindDomain(values = {Reward.class})
public class CreateRewardDataRequest extends BaseData {

    @DataField(name = "name")
    private String rewardName;

    @DataField(name = "imgFile")
    private byte[] rewardImg;

    private LocalDateTime provideDate;

    @DataField(name = "text")
    private String rewardText;

    @DataField(name = "funding.pk")
    private Long fundingFk;

    @DataField(name = "category.pk")
    private Long categoryFk;

    private BigDecimal requiredAmount;

    @DataField(name = "maxQuantity")
    private Long rewardMaxQuantity;

    @DataField(name = "remainQuantity")
    private Long rewardRemainQuantity;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Service에서 조회한 도메인 객체
    private Funding funding;

    private Category category;

    // 리워드 생성 전 필요한 값 설정
    public void prepareForCreate(
            Funding funding,
            Category category
    ) {
        this.funding = funding;
        this.category = category;

        // 최초 남은 수량 = 최대 수량
        this.rewardRemainQuantity = rewardMaxQuantity;

        this.createdAt = LocalDateTime.now();
        this.updatedAt = createdAt;
    }
}