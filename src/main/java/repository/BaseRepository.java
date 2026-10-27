package repository;

import model.domain.BaseDomain;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Filter;

public interface BaseRepository<T extends BaseDomain, PK> {
    //저장
    T create(T entity);

    //일괄 저장
    List<T> createAll(List<T> entities);

    //필터를 통해 모든 객체를 조회
    List<T> find(Filter filter);

    //Pk로 객체를 단건 조회
    Optional<T> findById(PK pk);

    //사용 조회하고 수정 후 수정한 객체를 그대로 파라미터
    Optional<T> update(PK pk, Map<String, Object> updateData);

    //객체를 삭제, 객체가 존재하지 않으면 False반환
    boolean delete(PK pk);
}