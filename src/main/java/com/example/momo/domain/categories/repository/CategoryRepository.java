package com.example.momo.domain.categories.repository;

import com.example.momo.domain.categories.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
	List<Category> findAllByIdIn(Collection<Integer> ids);
}