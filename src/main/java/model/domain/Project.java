package model.domain;

import annotation.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import constant.ProjectState;

@Table(name = "PROJECT")
@SuperBuilder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Project extends BaseDomain {
    @Id
    @Column(name = "project_id")
    private Long id;

    @JoinColumn(name = "maker_fk", nullable = false, onDelete = OnDelete.CASCADE)
    private AppUser maker;

    @JoinColumn(name = "category_fk", onDelete = OnDelete.SET_NULL)
    private Category category;

    @Builder.Default
    @Positive
    @Column(name = "like_count", nullable = false)
    private Long likeCount = 0L;

    @Builder.Default
    @Positive
    @Column(name = "dislike_count", nullable = false)
    private Long dislikeCount = 0L;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt  = LocalDateTime.now();

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate = LocalDateTime.now();

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Positive
    @Builder.Default
    @Column(name = "purpose_amount", nullable = false)
    private BigDecimal purposeAmount = BigDecimal.ZERO;

    @Positive
    @Builder.Default
    @Column(name = "donate_amount", nullable = false)
    private BigDecimal donateAmount = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "project_state", nullable = false, length = 30)
    private ProjectState state = ProjectState.JUST_USER_INPUT;

    @Column(name = "project_title")
    private String title;

    @Column(name = "project_text", columnDefinition = "TEXT")
    private String text;

    @Column(name = "project_img_file")
    private byte[] imgFile;

}
