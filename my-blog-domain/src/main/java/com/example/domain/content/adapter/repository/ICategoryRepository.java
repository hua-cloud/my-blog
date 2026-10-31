package com.example.domain.content.adapter.repository;

import com.example.domain.content.model.entity.Category;
import java.util.Optional;

public interface ICategoryRepository {
    Optional<Category> findById(Long id);
    /** 必须在事务内使用，未来文章建立分类关联时也须获取此锁。 */
    Optional<Category> findByIdForUpdate(Long id);
    Long create(Category category, Long operatorId);
    void update(Category category, Long operatorId);
    void delete(Long id);
    boolean existsByName(String name, Long excludingId);
}
