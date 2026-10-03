package Domain;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reward implements BaseDomain {
    private Long pk;
    private Funding funding;
    private String name;
    private String text;
    private Byte[] imgFile;
    private BigDecimal requiredAmount;
    private Long maxQuantity;
    private Long remainQuantity;
    private LocalDateTime provideDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

