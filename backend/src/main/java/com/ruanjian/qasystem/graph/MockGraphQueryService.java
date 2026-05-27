package com.ruanjian.qasystem.graph;

import com.ruanjian.qasystem.common.QaIntent;
import com.ruanjian.qasystem.model.vo.QaFact;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("mock-graph")
@Service
public class MockGraphQueryService implements GraphQueryService {

    @Override
    public QaFact query(QaIntent intent, String keyword) {
        return switch (intent) {
            case ARTIFACT_MUSEUM -> new QaFact("大英博物馆", "Mock 知识图谱", "https://example.com/artifact");
            case ARTIFACT_DYNASTY -> new QaFact("明代", "Mock 知识图谱", "https://example.com/artifact");
            case ARTIFACT_MATERIAL -> new QaFact("陶瓷", "Mock 知识图谱", "https://example.com/artifact");
            case ARTIFACT_TYPE -> new QaFact("瓷器", "Mock 知识图谱", "https://example.com/artifact");
            case ARTIFACT_DESCRIPTION -> new QaFact("这是一件用于开发联调的模拟文物介绍。", "Mock 知识图谱", "https://example.com/artifact");
            case ARTIFACT_ARTIST -> new QaFact("佚名", "Mock 知识图谱", "https://example.com/artifact");
            case ARTIST_BIOGRAPHY -> new QaFact(keyword + "的生平信息暂使用模拟数据。", "Mock 知识图谱", "https://example.com/artist");
            case ARTIST_WORKS -> new QaFact("作品一、作品二、作品三", "Mock 知识图谱", "https://example.com/artist");
            case DYNASTY_ARTIFACTS -> new QaFact("文物一、文物二、文物三", "Mock 知识图谱", "");
            case ARTIFACT_SIZE -> new QaFact("高 20cm，宽 12cm", "Mock 知识图谱", "https://example.com/artifact");
            case RELATED_ARTIFACTS -> new QaFact("相关文物一、相关文物二", "Mock 知识图谱", "https://example.com/artifact");
            case MUSEUM_ARTIFACT_COUNT -> new QaFact("2", "Mock 知识图谱", "https://example.com/museum");
            case MUSEUM_ARTIFACTS -> new QaFact("青花瓷瓶、青铜鼎", "Mock 知识图谱", "https://example.com/museum");
            case TYPE_ARTIFACTS -> new QaFact("青花瓷瓶、青花瓷盘", "Mock 知识图谱", "https://example.com/type");
            case MATERIAL_ARTIFACTS -> new QaFact("青花瓷瓶、青花瓷盘", "Mock 知识图谱", "https://example.com/material");
            default -> null;
        };
    }
}
