package com.xxl.mydoc.model;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "doc_download")
@Data
public class DocDownloadModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private long docId;
    private String clientMac;
    private String clientIp;
    private String userId;
    private Date createdDate;
}
