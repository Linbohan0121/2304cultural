package com.ruanjian.qasystem.graph;

import com.ruanjian.qasystem.common.QaIntent;
import com.ruanjian.qasystem.model.vo.QaFact;
import lombok.RequiredArgsConstructor;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 基于 Neo4j 的图谱查询服务。
 * 当前只先实现收藏地查询，其他类型后续逐步补充。
 */
@Profile("!mock-graph")
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
                case ARTIFACT_ARTIST -> queryArtifactRelation(keyword, "CREATED_BY");
                case ARTIFACT_DESCRIPTION -> queryArtifactProperty(keyword, "description");
                case ARTIFACT_SIZE -> queryArtifactProperty(keyword, "dimensions");
                case ARTIST_WORKS -> queryArtistWorks(keyword);
                case ARTIST_BIOGRAPHY -> queryArtistBiography(keyword);
                case DYNASTY_ARTIFACTS -> queryDynastyArtifacts(keyword);
                case RELATED_ARTIFACTS -> queryRelatedArtifacts(keyword);
                case MUSEUM_ARTIFACT_COUNT -> queryMuseumArtifactCount(keyword);
                case MUSEUM_ARTIFACTS -> queryMuseumArtifacts(keyword);
                case TYPE_ARTIFACTS -> queryTypeArtifacts(keyword);
                case MATERIAL_ARTIFACTS -> queryMaterialArtifacts(keyword);
                default -> null;
            };
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private QaFact queryArtifactMuseum(String keyword) {
        String cypher = """
                MATCH (a:Artifact)-[:COLLECTED_BY]->(m:Museum)
                WHERE a.name CONTAINS $keyword OR a.name_zh CONTAINS $keyword
                RETURN coalesce(m.name_zh, m.name) AS value,
                                   coalesce(m.name_zh, m.name, '知识图谱') AS sourceName,
                                   coalesce(a.detail_url, '') AS sourceUrl
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
                WHERE a.name CONTAINS $keyword OR a.name_zh CONTAINS $keyword
                            RETURN coalesce(n.name_zh, n.name) AS value,
                                   '知识图谱' AS sourceName,
                                   coalesce(a.detail_url, '') AS sourceUrl
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
            WHERE a.name CONTAINS $keyword OR a.name_zh CONTAINS $keyword
            RETURN a.%s AS value,
                   '知识图谱' AS sourceName,
                   coalesce(a.detail_url, '') AS sourceUrl
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
    private QaFact queryArtistWorks(String keyword) {
        String cypher = """
            MATCH (artifact:Artifact)-[:CREATED_BY]->(artist:Artist)
            WHERE artist.name CONTAINS $keyword OR artist.name_zh CONTAINS $keyword
            RETURN collect(coalesce(artifact.name_zh, artifact.name))[0..10] AS value,
                   coalesce(artist.name_zh, artist.name, '知识图谱') AS sourceName,
                   coalesce(artist.source_url, '') AS sourceUrl
            """;

        try (var session = driver.session()) {
            return session.executeRead(tx -> {
                var result = tx.run(cypher, Map.of("keyword", keyword));

                if (!result.hasNext()) {
                    return null;
                }

                Record record = result.next();
                List<Object> values = record.get("value").asList();

                if (values.isEmpty()) {
                    return null;
                }

                String works = values.stream()
                        .map(String::valueOf)
                        .reduce((left, right) -> left + "、" + right)
                        .orElse("");

                return new QaFact(
                        works,
                        record.get("sourceName").asString("知识图谱"),
                        record.get("sourceUrl").asString("")
                );
            });
        }
    }
    private QaFact queryArtistBiography(String keyword) {
        String cypher = """
        MATCH (artist:Artist)
        WHERE artist.name CONTAINS $keyword OR artist.name_zh CONTAINS $keyword
        RETURN artist.biography AS value,
               coalesce(artist.name_zh, artist.name, '知识图谱') AS sourceName,
               coalesce(artist.source_url, '') AS sourceUrl
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
    private QaFact queryDynastyArtifacts(String keyword) {
        String cypher = """
            MATCH (artifact:Artifact)-[:BELONGS_TO_DYNASTY]->(dynasty:Dynasty)
            WHERE dynasty.name CONTAINS $keyword OR dynasty.name_zh CONTAINS $keyword
            RETURN collect(coalesce(artifact.name_zh, artifact.name))[0..10] AS value,
                   coalesce(dynasty.name_zh, dynasty.name, '知识图谱') AS sourceName,
                   '知识图谱' AS sourceUrl
            """;

        try (var session = driver.session()) {
            return session.executeRead(tx -> {
                var result = tx.run(cypher, Map.of("keyword", keyword));

                if (!result.hasNext()) {
                    return null;
                }

                Record record = result.next();
                List<Object> values = record.get("value").asList();

                if (values.isEmpty()) {
                    return null;
                }

                String artifacts = values.stream()
                        .map(String::valueOf)
                        .reduce((left, right) -> left + "、" + right)
                        .orElse("");

                return new QaFact(
                        artifacts,
                        record.get("sourceName").asString("知识图谱"),
                        record.get("sourceUrl").asString("")
                );
            });
        }
    }
    private QaFact queryRelatedArtifacts(String keyword) {
        String cypher = """
            MATCH (target:Artifact)
            WHERE target.name CONTAINS $keyword OR target.name_zh CONTAINS $keyword
            MATCH (target)-[:BELONGS_TO_DYNASTY|HAS_TYPE|MADE_OF]->(shared)<-[:BELONGS_TO_DYNASTY|HAS_TYPE|MADE_OF]-(related:Artifact)
            WHERE related <> target
            RETURN collect(DISTINCT coalesce(related.name_zh, related.name))[0..10] AS value,
                   coalesce(target.name_zh, target.name, '知识图谱') AS sourceName,
                   coalesce(target.detail_url, '') AS sourceUrl
            LIMIT 1
            """;

        try (var session = driver.session()) {
            return session.executeRead(tx -> {
                var result = tx.run(cypher, Map.of("keyword", keyword));

                if (!result.hasNext()) {
                    return null;
                }

                Record record = result.next();
                List<Object> values = record.get("value").asList();

                if (values.isEmpty()) {
                    return null;
                }

                String relatedArtifacts = values.stream()
                        .map(String::valueOf)
                        .reduce((left, right) -> left + "、" + right)
                        .orElse("");

                return new QaFact(
                        relatedArtifacts,
                        record.get("sourceName").asString("知识图谱"),
                        record.get("sourceUrl").asString("")
                );
            });
        }
    }
    private QaFact queryMuseumArtifactCount(String keyword) {
        String cypher = """
        MATCH (artifact:Artifact)-[:COLLECTED_BY]->(museum:Museum)
        WHERE museum.name CONTAINS $keyword OR museum.name_zh CONTAINS $keyword
        RETURN toString(count(artifact)) AS value,
               coalesce(museum.name_zh, museum.name, '知识图谱') AS sourceName,
               coalesce(museum.website, '') AS sourceUrl
        LIMIT 1
        """;

        try (var session = driver.session()) {
            return session.executeRead(tx -> {
                var result = tx.run(cypher, Map.of("keyword", keyword));

                if (!result.hasNext()) {
                    return null;
                }

                Record record = result.next();
                String value = record.get("value").asString("");

                if (value.isBlank() || "0".equals(value)) {
                    return null;
                }

                return new QaFact(
                        value,
                        record.get("sourceName").asString("知识图谱"),
                        record.get("sourceUrl").asString("")
                );
            });
        }
    }

    private QaFact queryMuseumArtifacts(String keyword) {
        String cypher = """
        MATCH (artifact:Artifact)-[:COLLECTED_BY]->(museum:Museum)
        WHERE museum.name CONTAINS $keyword OR museum.name_zh CONTAINS $keyword
        RETURN collect(coalesce(artifact.name_zh, artifact.name))[0..10] AS value,
               coalesce(museum.name_zh, museum.name, '知识图谱') AS sourceName,
               coalesce(museum.website, '') AS sourceUrl
        LIMIT 1
        """;

        try (var session = driver.session()) {
            return session.executeRead(tx -> {
                var result = tx.run(cypher, Map.of("keyword", keyword));

                if (!result.hasNext()) {
                    return null;
                }

                Record record = result.next();
                List<Object> values = record.get("value").asList();

                if (values.isEmpty()) {
                    return null;
                }

                String artifacts = values.stream()
                        .map(String::valueOf)
                        .reduce((left, right) -> left + "、" + right)
                        .orElse("");

                return new QaFact(
                        artifacts,
                        record.get("sourceName").asString("知识图谱"),
                        record.get("sourceUrl").asString("")
                );
            });
        }
    }

    private QaFact queryTypeArtifacts(String keyword) {
        String cypher = """
        MATCH (artifact:Artifact)-[:HAS_TYPE]->(type:Type)
        WHERE type.name CONTAINS $keyword OR type.name_zh CONTAINS $keyword
        RETURN collect(coalesce(artifact.name_zh, artifact.name))[0..10] AS value,
               coalesce(type.name_zh, type.name, '知识图谱') AS sourceName,
               '知识图谱' AS sourceUrl
        LIMIT 1
        """;

        try (var session = driver.session()) {
            return session.executeRead(tx -> {
                var result = tx.run(cypher, Map.of("keyword", keyword));

                if (!result.hasNext()) {
                    return null;
                }

                Record record = result.next();
                List<Object> values = record.get("value").asList();

                if (values.isEmpty()) {
                    return null;
                }

                String artifacts = values.stream()
                        .map(String::valueOf)
                        .reduce((left, right) -> left + "、" + right)
                        .orElse("");

                return new QaFact(
                        artifacts,
                        record.get("sourceName").asString("知识图谱"),
                        record.get("sourceUrl").asString("")
                );
            });
        }
    }

    private QaFact queryMaterialArtifacts(String keyword) {
        String cypher = """
        MATCH (artifact:Artifact)-[:MADE_OF]->(material:Material)
        WHERE material.name CONTAINS $keyword OR material.name_zh CONTAINS $keyword
        RETURN collect(coalesce(artifact.name_zh, artifact.name))[0..10] AS value,
               coalesce(material.name_zh, material.name, '知识图谱') AS sourceName,
               '知识图谱' AS sourceUrl
        LIMIT 1
        """;

        try (var session = driver.session()) {
            return session.executeRead(tx -> {
                var result = tx.run(cypher, Map.of("keyword", keyword));

                if (!result.hasNext()) {
                    return null;
                }

                Record record = result.next();
                List<Object> values = record.get("value").asList();

                if (values.isEmpty()) {
                    return null;
                }

                String artifacts = values.stream()
                        .map(String::valueOf)
                        .reduce((left, right) -> left + "、" + right)
                        .orElse("");

                return new QaFact(
                        artifacts,
                        record.get("sourceName").asString("知识图谱"),
                        record.get("sourceUrl").asString("")
                );
            });
        }
    }
}
