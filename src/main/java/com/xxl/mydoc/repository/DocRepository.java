package com.xxl.mydoc.repository;

import com.xxl.mydoc.model.DocModel;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.List;

public interface DocRepository extends CrudRepository<DocModel,Long> {
    List<DocModel> findByName(String name);
    List<DocModel> findByGrade(int grade);
    List<DocModel> findBySubject(int subject);
    List<DocModel> findByGradeAndSubject(int grade, int subject);

    List<DocModel> findByIsRecommend(boolean isRecommend);

    List<DocModel> findByIdIn(Collection ids);

}
