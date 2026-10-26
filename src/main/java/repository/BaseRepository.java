package repository;

import model.domain.BaseDomain;

import java.util.List;
import java.util.Optional;

public interface BaseRepository<T extends BaseDomain, PK> {
	//저장
	// -> PK가 있는 객체(findBy 후 수정)가 들어오면 Update,
	// -> Pk가 없는 객체가 들어오면 Insert
    T save(T entity);

	//일괄 저장
    List<T> saveAll(List<T> entities);

	//필터를 통해 모든 객체를 조회
    List<T> find(Filter filter);

	//Pk로 객체를 단건 조회
    Optional<T> findById(PK pk);

	//객체를 삭제, 객체가 존재하지 않으면 False반환
    boolean delete(T entity);

}