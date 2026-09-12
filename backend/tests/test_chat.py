from unittest.mock import patch

from fastapi.testclient import TestClient

from app.main import app

client = TestClient(app)

FAKE_EMBEDDING = [0.0] * 768


@patch("app.memory.rag.get_embedding", return_value=FAKE_EMBEDDING)
@patch("app.memory.rag.search", return_value=[])
@patch("app.memory.rag.chat", return_value="Sure, noted.")
def test_new_conversation_creates_id(mock_chat, mock_search, mock_embed):
    response = client.post("/chat", json={"message": "Hello"})
    assert response.status_code == 200
    data = response.json()
    assert "conversation_id" in data
    assert isinstance(data["conversation_id"], int)
    assert data["answer"] == "Sure, noted."


@patch("app.memory.rag.get_embedding", return_value=FAKE_EMBEDDING)
@patch("app.memory.rag.search", return_value=[])
@patch("app.memory.rag.chat", return_value="Got it.")
def test_continuing_conversation_reuses_id(mock_chat, mock_search, mock_embed):
    first = client.post("/chat", json={"message": "I just ordered a keyboard."})
    conversation_id = first.json()["conversation_id"]

    second = client.post(
        "/chat", json={"message": "Where from?", "conversation_id": conversation_id}
    )
    assert second.status_code == 200
    assert second.json()["conversation_id"] == conversation_id


@patch("app.memory.rag.get_embedding", return_value=FAKE_EMBEDDING)
@patch("app.memory.rag.search", return_value=[])
@patch("app.memory.rag.chat")
def test_history_passed_to_llm(mock_chat, mock_search, mock_embed):
    mock_chat.return_value = "Noted."
    first = client.post("/chat", json={"message": "I just ordered a keyboard."})
    conversation_id = first.json()["conversation_id"]

    mock_chat.return_value = "Probably from an online store."
    client.post(
        "/chat", json={"message": "Where from?", "conversation_id": conversation_id}
    )

    second_call_prompt = mock_chat.call_args[0][0]
    assert "keyboard" in second_call_prompt.lower()


def test_no_hardcoded_jarvis_in_prompt():
    import app.memory.rag as rag_module

    assert "jarvis" not in rag_module.PROMPT_TEMPLATE.lower()
