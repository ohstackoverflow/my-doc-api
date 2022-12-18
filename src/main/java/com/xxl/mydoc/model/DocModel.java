package com.xxl.mydoc.model;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "doc")
@Data
public class DocModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    private int area;
    private int grade;
    private int subject;
    private Date createdDate;
    private int initDownloaded;
    private int initLoved;
    private int downloaded;
    private int loved;
    private boolean isRecommend;
    private String shortDesc;
    private String replaceUrl;  //当视频不提供在线观看时，1）提供替换图。2）提供链接url，比如百度网盘链接，阿里云盘链接等。
    private String href;
    private int categoryId;
}
