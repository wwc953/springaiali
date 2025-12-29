CREATE TABLE public.spring_ai_chat_memory
(
    id BIGSERIAL PRIMARY KEY,
    conversation_id VARCHAR (256) NOT NULL,
    content TEXT NOT NULL,
    type VARCHAR(100) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    CONSTRAINT chk_message_type CHECK(type IN(
    'USER',
    'ASSISTANT',
    'SYSTEM',
    'TOOL'
)));