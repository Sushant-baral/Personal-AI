from pydantic import BaseModel


class SettingsResponse(BaseModel):
    assistant_name: str


class SettingsUpdate(BaseModel):
    assistant_name: str
