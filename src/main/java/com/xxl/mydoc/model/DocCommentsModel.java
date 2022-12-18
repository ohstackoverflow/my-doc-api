package com.xxl.mydoc.model;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "doc_comments")
@Data
public class DocCommentsModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private long docId;
    private String comments;
    private String clientMac;
    private String clientIp;
    private String userId;
    private Date createdDate;
}
