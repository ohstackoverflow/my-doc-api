package com.xxl.mydoc.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ResponseRecommend {
    List<LinkModel> carousel;
    List<LinkModel> list;
}
