package model.dto;

import annotation.BindDomain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.domain.AppUser;
import model.domain.Donate;
import model.domain.Reward;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Getter
@NoArgsConstructor
@AllArgsConstructor
@BindDomain(values = {Donate.class})
public class DonateData extends BaseData {

    private AppUser user;

    private Reward reward;

    private BigDecimal amount;

    private Long quantity;

    private LocalDateTime createdAt;

    private Boolean state;


}