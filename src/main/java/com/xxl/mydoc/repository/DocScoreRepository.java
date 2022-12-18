package com.xxl.mydoc.repository;

import com.xxl.mydoc.model.DocScoreModel;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface DocScoreRepository extends CrudRepository<DocScoreModel,Long> {
    List<DocScoreModel> findByDocId(long docId);
}
