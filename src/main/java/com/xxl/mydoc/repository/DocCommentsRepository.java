package com.xxl.mydoc.repository;

import com.xxl.mydoc.model.DocCommentsModel;
import com.xxl.mydoc.model.DocModel;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface DocCommentsRepository extends CrudRepository<DocCommentsModel,Long> {
    List<DocCommentsModel> findByDocId(long docId);
}
