package model.domain;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Review implements BaseDomain {

    @NonNull
    private Long pk;

    @NonNull
    private Funding funding;

    @NonNull
    private AppUser user;

    private String text;

    @NonNull
    private BigDecimal rating;

    @NonNull
    private Long likeCount;

    @NonNull
    private Long dislikeCount;

    @NonNull
    private LocalDateTime createdAt;

    @NonNull
    private LocalDateTime updatedAt;
}
