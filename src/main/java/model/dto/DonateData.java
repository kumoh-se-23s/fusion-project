package model.dto;

import annotation.BindDomain;

import model.domain.AppUser;
import model.domain.Donate;
import model.domain.Reward;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@BindDomain(values = {Donate.class})
public class DonateData extends BaseData {

    private AppUser user;

    private Reward reward;

    private BigDecimal amount;

    private Long quantity;

    private LocalDateTime createdAt;

    private Boolean state;

    public DonateData(
            AppUser user,
            Reward reward,
            BigDecimal amount,
            Long quantity,
            LocalDateTime createdAt,
            Boolean state
    ) {
        this.user = user;
        this.reward = reward;
        this.amount = amount;
        this.quantity = quantity;
        this.createdAt = createdAt;
        this.state = state;
    }
}