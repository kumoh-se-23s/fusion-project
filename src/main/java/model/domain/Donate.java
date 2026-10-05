package model.domain;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Donate implements BaseDomain {

    private Long pk;
    private AppUser user;
    private Reward reward;
    private BigDecimal amount;
    private Long quantity;
    private LocalDateTime createdAt;
    private Boolean state;
}
