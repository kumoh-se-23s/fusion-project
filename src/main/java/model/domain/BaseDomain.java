package model.domain;

import annotation.Column;
import annotation.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public abstract class BaseDomain implements Domain{
    @Version
    @Column(name = "version")
    private Long version;
}
