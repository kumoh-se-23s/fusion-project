package model.domain;

import annotation.*;
import constant.SocialProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "SOCIAL_ACCOUNT")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SocialAccount extends BaseDomain {

    @Id
    @Column(name = "social_account_id")
    private String id;

    @JoinColumn(name = "user_fk", nullable = false, unique = true, onDelete = OnDelete.CASCADE)
    private AppUser appUser;

    private SocialProvider socialProvider;
}
