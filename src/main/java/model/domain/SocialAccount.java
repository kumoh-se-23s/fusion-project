package model.domain;

import annotation.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "SOCIAL_ACCOUNT")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SocialAccount {

    @Id
    @Column(name = "social_account_id")
    private Long id;

    @JoinColumn(name = "user_fk", nullable = false, unique = true, onDelete = OnDelete.CASCADE)
    private AppUser appUser;

    @JoinColumn(name = "social_provider_fk", nullable = false, unique = true, onDelete = OnDelete.CASCADE)
    private SocialProvider socialProvider;
}
