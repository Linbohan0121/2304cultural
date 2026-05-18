package com.ruanjian.qasystem.graph;

import com.ruanjian.qasystem.common.QaIntent;
import com.ruanjian.qasystem.model.vo.QaFact;
import lombok.RequiredArgsConstructor;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 基于 Neo4j 的图谱查询服务。
 * 当前只先实现收藏地查询，其他类型后续逐步补充。
 */
@Service
@RequiredArgsConstructor
public class Neo4jGraphQueryService implements GraphQueryService {

    private final Driver driver;

    @Override
    public QaFact query(QaIntent intent, String keyword) {

        try {
            return switch (intent) {
                case ARTIFACT_MUSEUM -> queryArtifactMuseum(keyword);
                case ARTIFACT_DYNASTY -> queryArtifactRelation(keyword, "BELONGS_TO_DYNASTY");
                case ARTIFACT_MATERIAL -> queryArtifactRelation(keyword, "MADE_OF");
                case ARTIFACT_TYPE -> queryArtifactRelation(keyword, "HAS_TYPE");
                case ARTIFACT_DESCRIPTION -> queryArtifactProperty(keyword, "description");
                case ARTIFACT_SIZE -> queryArtifactProperty(keyword, "dimension");
                default -> null;
            };
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private QaFact queryArtifactMuseum(String keyword) {
        String cypher = """
                MATCH (a:Artifact)-[:COLLECTED_BY]->(m:Museum)
                WHERE a.name CONTAINS $keyword
                RETURN m.name AS value,
                       coalesce(m.name, '知识图谱') AS sourceName,
                       coalesce(a.sourceUrl, '') AS sourceUrl
                LIMIT 1
                """;

        try (var session = driver.session()) {
            return session.executeRead(tx -> {
                var result = tx.run(cypher, Map.of("keyword", keyword));

                if (!result.hasNext()) {
                    return null;
                }

                Record record = result.next();

                return new QaFact(
                        record.get("value").asString(""),
                        record.get("sourceName").asString("知识图谱"),
                        record.get("sourceUrl").asString("")
                );
            });
        }
    }
    private QaFact queryArtifactRelation(String keyword,String relationName){
        String cypher = """
            MATCH (a:Artifact)-[r:%s]->(n)
            WHERE a.name CONTAINS $keyword
            RETURN n.name AS value,
                   '知识图谱' AS sourceName,
                   coalesce(a.sourceUrl, '') AS sourceUrl
            LIMIT 1
            """.formatted(relationName);
        try (var session = driver.session()) {
            return session.executeRead(tx -> {
                var result = tx.run(cypher, Map.of("keyword", keyword));

                if (!result.hasNext()) {
                    return null;
                }

                Record record = result.next();

                return new QaFact(
                        record.get("value").asString(""),
                        record.get("sourceName").asString("知识图谱"),
                        record.get("sourceUrl").asString("")
                );
            });
        }
    }
    private QaFact queryArtifactProperty(String keyword, String propertyName) {
        String cypher = """
            MATCH (a:Artifact)
            WHERE a.name CONTAINS $keyword
            RETURN a.%s AS value,
                   '知识图谱' AS sourceName,
                   coalesce(a.sourceUrl, '') AS sourceUrl
            LIMIT 1
            """.formatted(propertyName);

        try (var session = driver.session()) {
            return session.executeRead(tx -> {
                var result = tx.run(cypher, Map.of("keyword", keyword));

                if (!result.hasNext()) {
                    return null;
                }

                Record record = result.next();

                return new QaFact(
                        record.get("value").asString(""),
                        record.get("sourceName").asString("知识图谱"),
                        record.get("sourceUrl").asString("")
                );
            });
        }
    }

}
