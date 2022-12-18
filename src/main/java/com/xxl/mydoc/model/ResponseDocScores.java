package com.xxl.mydoc.model;

import lombok.Data;

import java.util.Map;

@Data
public class ResponseDocScores {
    int total;
    double average;
    Map<Integer, Long> scoreCount;
}
