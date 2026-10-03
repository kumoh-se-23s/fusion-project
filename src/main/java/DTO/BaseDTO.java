package DTO;

import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

public abstract class BaseDTO extends Serializer {

    private static final Logger LOGGER = Logger.getLogger(BaseDTO.class.getName());

    @SuppressWarnings("unchecked")
    public <T extends BaseDTO> T fromDomain(Object domain) {
        return (T) Serializer.fromDomain(domain, this.getClass());
    }

    @SuppressWarnings("unchecked")
    public <T extends BaseDTO> List<T> fromDomains(Collection<?> domains) {
        return (List<T>) (List<?>) Serializer.fromDomains(domains, this.getClass());
    }
}