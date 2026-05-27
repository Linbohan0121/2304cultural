package com.ruanjian.qasystem.common;

/**
 * 知识问答支持的问题类型。
 * 先覆盖课程要求中的基础问答类型，后续可继续扩展。
 */
public enum QaIntent {

    ARTIFACT_MUSEUM,      // 文物收藏地
    ARTIFACT_DYNASTY,     // 文物年代/朝代
    ARTIFACT_MATERIAL,    // 文物材质
    ARTIFACT_TYPE,        // 文物类型
    ARTIFACT_DESCRIPTION, // 文物介绍
    ARTIFACT_ARTIST,      // 文物作者
    ARTIST_BIOGRAPHY,     // 作者生平
    ARTIST_WORKS,         // 同一作者作品
    DYNASTY_ARTIFACTS,    // 同一朝代文物
    ARTIFACT_SIZE,        // 文物尺寸规格
    RELATED_ARTIFACTS,    // 相关文物推荐
    MUSEUM_ARTIFACT_COUNT, // 博物馆藏品数量统计
    MUSEUM_ARTIFACTS,     // 博物馆藏品列表
    TYPE_ARTIFACTS,       // 某类型文物列表
    MATERIAL_ARTIFACTS,   // 某材质文物列表
    UNKNOWN               // 无法识别
}
