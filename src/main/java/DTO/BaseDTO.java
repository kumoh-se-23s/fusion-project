package DTO;

import java.util.Collection;
import java.util.List;

public abstract class BaseDTO extends Serializer {

    // DTO 타입은 this.getClass()로 결정되므로 인자는 도메인 하나뿐이다.
    @SuppressWarnings("unchecked")
    public <T extends BaseDTO> T fromDomain(Object domain) {
        return (T) Serializer.fromDomain(domain, this.getClass());
    }

    @SuppressWarnings("unchecked")
    public <T extends BaseDTO> List<T> fromDomains(Collection<?> domains) {
        return (List<T>) (List<?>) Serializer.fromDomains(domains, this.getClass());
    }
}