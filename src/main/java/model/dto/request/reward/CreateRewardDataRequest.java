package model.dto.request.reward;

import annotation.BindDomain;
import annotation.DataField;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import model.domain.Category;
import model.domain.Project;
import model.domain.Reward;
import model.dto.BaseData;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
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

    @DataField(name = "project.id")
    private Long projectFk;

    @DataField(name = "category.id")
    private Long categoryFk;

    private BigDecimal requiredAmount;

    @DataField(name = "maxQuantity")
    private Long rewardMaxQuantity;

    @DataField(name = "remainQuantity")
    private Long rewardRemainQuantity;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Service에서 조회한 도메인 객체
    private Project project;

    private Category category;

    // 리워드 생성 전 필요한 값 설정
    public void prepareForCreate(
            Project project,
            Category category
    ) {
        this.project = project;
        this.category = category;

        // 최초 남은 수량 = 최대 수량
        this.rewardRemainQuantity = rewardMaxQuantity;

        this.createdAt = LocalDateTime.now();
        this.updatedAt = createdAt;
    }
}