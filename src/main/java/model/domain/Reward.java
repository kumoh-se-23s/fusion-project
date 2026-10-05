package model.domain;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Reward implements BaseDomain {

    @NonNull
    private Long pk;

    @NonNull
    private Funding funding;

    @NonNull
    private String name;

    private String text;
    private Byte[] imgFile;

    @NonNull
    private BigDecimal requiredAmount;

    @NonNull
    private Long maxQuantity;

    @NonNull
    private Long remainQuantity;

    @NonNull
    private LocalDateTime provideDate;

    @NonNull
    private LocalDateTime createdAt;

    @NonNull
    private LocalDateTime updatedAt;
}

