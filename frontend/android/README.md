# Jarvis — Android App (Phase 2)

Phase 2 of the personal assistant project: an Android UI (Kotlin + Jetpack
Compose) that talks to the Phase 1 FastAPI backend. Built to feel like a
calm, everyday personal app — not an "AI chatbot" screen.

## What's here

```
app/src/main/java/com/jarvis/app/
├── MainActivity.kt              Entry point, wires theme + nav
├── JarvisApp.kt                 Tiny manual DI container
├── ui/theme/                    Color.kt, Type.kt, Theme.kt — light & dark
├── ui/home/HomeScreen.kt        Greeting, input pill, Recent list
├── ui/chat/ChatScreen.kt        Message thread + composer
├── ui/chat/ChatViewModel.kt     Shared state for Home + Chat
├── ui/common/                   ChatInputBar, MessageRow, TypingIndicator
├── navigation/JarvisNavHost.kt  Home → Chat, with a subtle slide transition
└── data/
    ├── network/                 Retrofit client for POST /chat
    ├── model/                   Wire + local UI models
    ├── repository/              ChatRepository — one seam between UI and backend
    └── local/ConversationStore.kt   Local cache backing the "Recent" list
```

## Why there's a local conversation store

The Phase 1 backend only exposes `POST /chat` (send a message, get a reply)
and the `/memory` endpoints — there's no endpoint yet to list past
conversations or fetch an old one's messages. So the app keeps its own local
copy of each conversation's messages (plain `SharedPreferences` + Gson) purely
to power the Home screen's "Recent" list and to let you reopen a thread. Every
new message you send still goes through `POST /chat` with the right
`conversation_id`, so the assistant's own context stays exactly what the
backend expects. If you add a "list conversations" / "get conversation"
endpoint later, swap `ConversationStore` for that and the UI won't need to
change.

## Design decisions, mapped to your brief

- **No AI-dashboard look**: no gradients, glow, glass, or robot/circuit
  imagery anywhere — just typography, warm color, and generous whitespace.
- **Palette**: one accent (muted terracotta), warm cream background in light
  mode, soft near-black warm charcoal in dark mode — each theme tuned on its
  own, not an inversion of the other. See `ui/theme/Color.kt`.
- **Typography carries hierarchy**: big semi-bold greeting, medium regular
  prompt, small label for "Recent" — see `ui/theme/Type.kt`.
- **Chat feels like text, not a demo**: user messages get a small rounded
  chip; assistant replies are plain paragraph text with no bubble/card.
- **Motion is subtle**: messages fade + slide in gently, screen transitions
  fade/slide instead of snapping, and the "thinking" state is a soft
  three-dot pulse rather than a spinner or glow.
- **Navigation**: just Home → Chat for now, per the brief — no sidebar, no
  bottom dashboard nav yet. Chats / Memory / Settings are natural next
  screens once you want them.

## Running it

1. **Start the Phase 1 backend** (from `backend/`):
   ```bash
   source .venv/bin/activate
   uvicorn app.main:app --reload --host 127.0.0.1 --port 8000
   ```
   Make sure `ollama serve` is running too, or `/chat` will 503.

2. **Open this folder in Android Studio** (File → Open). Let it sync —
   Android Studio will download Gradle, the Android Gradle Plugin, and the
   Compose libraries the first time, so that initial sync needs internet.

3. **Run on an emulator.** The app is pre-configured to reach
   `http://10.0.2.2:8000/`, which is the emulator's alias for your host
   machine's `localhost`, matching the backend's default `127.0.0.1:8000`.
   No setup needed — just press Run.

4. **Running on a physical device instead?** Your phone can't resolve
   `10.0.2.2`. In `app/build.gradle.kts`, change:
   ```kotlin
   buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:8000/\"")
   ```
   to your computer's LAN IP (find it with `ip addr` / `ifconfig`), e.g.
   `"http://192.168.1.23:8000/"`, and make sure the backend is bound to
   `0.0.0.0` instead of `127.0.0.1` (`uvicorn app.main:app --host 0.0.0.0 --port 8000`)
   and your phone is on the same Wi-Fi network.

## Known Phase 2 limits (by design, matching your brief)

- Only text input — no mic button or attachments yet.
- Only Home and Chat exist — Chats/Memory/Settings are not implemented.
- "Recent" titles are just the first ~60 characters of your opening message;
  no summarization yet.
- No auth/multi-user handling — this mirrors the backend, which is
  single-user and local-only for now.
