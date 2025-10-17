-- 启用 pgvector 插件
CREATE EXTENSION IF NOT EXISTS vector;
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