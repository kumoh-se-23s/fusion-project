package model.domain;

import annotation.Column;
import annotation.Version;

public abstract class BaseDomain implements Domain{
    @Version
    @Column(name = "version")
    private Long version;
}
