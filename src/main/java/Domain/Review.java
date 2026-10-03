package Domain;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Review implements BaseDomain {

    private Long pk;
    private Funding funding;
    private AppUser user;
    private String text;
    private BigDecimal rating;
    private Long likeCount;
    private Long dislikeCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
