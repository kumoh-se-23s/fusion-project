package repository.reward;

import model.domain.Reward;
import repository.BaseRepositoryImpl;
import repository.Filter;

import java.util.List;
import java.util.Map;
import java.util.Optional;


public class RewardRepository extends BaseRepositoryImpl<Reward, Long> {
    @Override
    public List<Reward> createAll(List<Reward> entities) {
        return List.of();
    }

    @Override
    public List<Reward> find(Filter filter) {
        return List.of();
    }

    @Override
    public Optional<Reward> findById(Long pk) {
        return Optional.empty();
    }

    @Override
    public Optional<Reward> update(Long pk, Map<String, Object> updateData) {
        return Optional.empty();
    }

    @Override
    public boolean delete(Long pk) {
        return false;
    }

}
