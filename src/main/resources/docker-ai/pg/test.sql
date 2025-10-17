-- 启用 pgvector 插件
CREATE EXTENSION IF NOT EXISTS vector;
-- hstore扩展, 支持键值对存储，适用于动态属性场景（如用户配置）‌
CREATE EXTENSION IF NOT EXISTS hstore;
-- 提供生成通用唯一标识符（UUID）的函数
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 创建表，包含一个向量字段（维度为3）
CREATE TABLE items (
                       id SERIAL PRIMARY KEY,
                       embedding vector(3)
);

-- 插入向量数据
INSERT INTO items (embedding) VALUES
                                  ('[1,1,1]'),
                                  ('[2,2,2]'),
                                  ('[1,0,0]');

-- 查询与 [1,1,1] 最接近的向量（基于欧几里得距离）
SELECT id, embedding
FROM items
ORDER BY embedding <-> '[1,1,1]'
    LIMIT 3;