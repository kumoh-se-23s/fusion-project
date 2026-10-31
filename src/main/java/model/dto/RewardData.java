package model.dto;

import annotation.BindDomain;
import annotation.DataField;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import model.domain.Reward;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@BindDomain(values = {Reward.class})
public class RewardData extends BaseData {

    @DataField(name = "pk")
    private Long rewardPk;

    private String name;

    private byte[] imgFile;

    @DataField(name = "funding.pk")
    private Long fundingPk;

    @DataField(name = "funding.maker.pk")
    private Long makerPk;

    @DataField(name = "provideDate")
    private LocalDateTime providerDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String text;

    private BigDecimal requiredAmount;

    private Long maxQuantity;

    private Long remainQuantity;
}