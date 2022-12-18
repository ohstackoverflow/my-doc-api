package com.xxl.mydoc.controller;

import com.xxl.mydoc.model.*;
import com.xxl.mydoc.repository.DocCommentsRepository;
import com.xxl.mydoc.repository.DocDownloadRepository;
import com.xxl.mydoc.repository.DocRepository;
import com.xxl.mydoc.repository.DocScoreRepository;
import com.xxl.mydoc.util.ClientInfoUtil;
import com.xxl.mydoc.util.HttpRequestFile;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Slf4j
@RestController
public class DoController {
    @Autowired
    private DocRepository docRepository;
    @Autowired
    private DocCommentsRepository docCommentsRepository;
    @Autowired
    private DocScoreRepository docScoreRepository;
    @Autowired
    private DocDownloadRepository docDownloadRepository;

    @GetMapping("/")
    public String index() {
        return "OK";
    }

    @GetMapping("/download")
    public void download(HttpServletRequest request, HttpServletResponse response, long docId, String name) {
        DocDownloadModel docDownloadModel = new DocDownloadModel();
        String ip = ClientInfoUtil.getClientIP(request);
        docDownloadModel.setClientIp(ip);
        docDownloadModel.setClientMac(ClientInfoUtil.getLocalMac(request));
        docDownloadModel.setCreatedDate(new Date());
        docDownloadModel.setDocId(docId);
        docDownloadRepository.save(docDownloadModel);

        Optional<DocModel> doc = docRepository.findById(docId);
        if(doc.isPresent()) {
            doc.get().setDownloaded(doc.get().getDownloaded() + 1);
            docRepository.save(doc.get());
        }

        HttpRequestFile.getFile(request, response, name, true);
    }

//    @GetMapping("/preview")
//    public void preview(HttpServletResponse response, String name) {
//        HttpRequestFile.getFile(response, name, false);
//    }

    @GetMapping("/findStatByIds")
    public List<StatModel> findStatByIds(String ids) {
        List<StatModel> models = new ArrayList<>();
        List<Long> docIds = new ArrayList<>();
        Stream.of(ids.split(",")).forEach(id -> {
            docIds.add( Long.valueOf(id) );
        });

        docRepository.findByIdIn(docIds).forEach(d -> {
            StatModel model = new StatModel();
            model.setId(d.getId());
            model.setDownload(d.getInitDownloaded() + d.getDownloaded());
            model.setLoved(d.getInitLoved() + d.getLoved());
            models.add(model);
        });
        return models;
    }

    @GetMapping("/getComments")
    public List<DocCommentsModel> getComments(int docId) {
        List<DocCommentsModel> list = docCommentsRepository.findByDocId(docId);
        list.sort(Comparator.comparing(DocCommentsModel::getCreatedDate).reversed());
        return list;
    }

    @GetMapping("/comments")
    public void comments(HttpServletRequest request, int docId, String comments) {
        DocCommentsModel docCommentsModel = new DocCommentsModel();
        String ip = ClientInfoUtil.getClientIP(request);
        docCommentsModel.setClientIp(ip);
        docCommentsModel.setClientMac(ClientInfoUtil.getLocalMac(request));
        docCommentsModel.setCreatedDate(new Date());
        docCommentsModel.setComments(comments);
        docCommentsModel.setDocId(docId);
        docCommentsRepository.save(docCommentsModel);
    }

    @GetMapping("/getScoresByDocId")
    public ResponseDocScores getScoresByDocId(long docId) {
        List<DocScoreModel> scores = docScoreRepository.findByDocId(docId);
        //Map<Integer, List<DocScoreModel>> map = scores.stream().collect(Collectors.groupingBy(DocScoreModel::getScore));
        Map<Integer, Long> scoresCount = scores.stream().collect(Collectors.groupingBy(DocScoreModel::getScore, Collectors.counting()));
        OptionalDouble optionalDouble = scores.stream().mapToInt( s -> s.getScore() ).average();
        ResponseDocScores docScores = new ResponseDocScores();
        if(optionalDouble.isPresent()) {
            double average = scores.stream().mapToInt( s -> s.getScore() ).average().getAsDouble();
            docScores.setTotal(scores.size());
            docScores.setAverage(average);
            docScores.setScoreCount(scoresCount);
        }

        return docScores;
    }

    @GetMapping("/getRecommend")
    public ResponseRecommend getRecommend() {
        ResponseRecommend responseRecommend = new ResponseRecommend();
        List<LinkModel> carousel = new ArrayList<>();

        LinkModel link = new LinkModel();
        link.setLinkName("Todd Parr: 你是特别的，你是最好的");
        link.setLinkHref("detail-0-4463.html");
        link.setTitle("Todd Parr通过简单的画面和简单的语言，向小宝们展示各类情绪、自我认知……");
        link.setImg("img1.jpg");
        carousel.add(link);

        link = new LinkModel();
        link.setLinkName("小学生学习习惯培养措施及方案");
        link.setLinkHref("detail-500-4379.html");
        link.setTitle("小学阶段什么最重要？在小学阶段,知识并不是最主要的,更为重要的是要让孩子养成良好的习惯,激发孩子的求知欲。从某种意义上说,让学生养成良好的习惯,比教会他们知识更重要。");
        link.setImg("img2.jpg");
        carousel.add(link);

        link = new LinkModel();
        link.setLinkName("如何学好高中数学？");
        link.setLinkHref("#");
        link.setTitle("");
        link.setImg("img3.png");
        carousel.add(link);

        responseRecommend.setCarousel(carousel);

        List<LinkModel> list = new ArrayList<>();
        link = new LinkModel();
        link.setImg("focus.png");
        link.setLinkHref("#");
        list.add(link);
        docRepository.findByIsRecommend(true).forEach(d -> {
            LinkModel linkModel = new LinkModel();
            linkModel.setLinkName(d.getName());
            linkModel.setLinkHref(d.getHref());
            list.add(linkModel);
        });
        responseRecommend.setList(list);

        return responseRecommend;
    }

    @GetMapping("/score")
    public void score(HttpServletRequest request, long docId, int score) {
        DocScoreModel docScoreModel = new DocScoreModel();
        String ip = ClientInfoUtil.getClientIP(request);
        docScoreModel.setClientIp(ip);
        docScoreModel.setClientMac(ClientInfoUtil.getLocalMac(request));
        docScoreModel.setCreatedDate(new Date());
        docScoreModel.setScore(score);
        docScoreModel.setDocId(docId);
        docScoreRepository.save(docScoreModel);

        Optional<DocModel> doc = docRepository.findById(docId);
        if(doc.isPresent() && score >= 4) {
            doc.get().setLoved(doc.get().getLoved() + 1);
            docRepository.save(doc.get());
        }
    }


    @GetMapping("/test")
    public Iterable<DocModel> getAll() {
        return docRepository.findAll();
    }

}
