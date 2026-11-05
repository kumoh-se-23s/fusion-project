package model.domain;

import annotation.Column;
import annotation.Id;
import annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "SOCIAL_PROVIDER")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SocialProvider {

    @Id
    @Column(name = "social_provider_id")
    private Long id;

    @Column(name = "social_provider_name")
    private String name;
}
