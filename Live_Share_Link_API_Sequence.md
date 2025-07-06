# Live Share Link API Call Sequence

## Overview

This document details the complete sequence of API calls that occur when a user clicks on a shared live stream link in the MyLive application. The system performs real-time validation and loads all necessary data to ensure a seamless user experience.

## Deep Link Processing Flow

### 1. Initial Link Processing
When a user clicks on a shared link, the following sequence occurs:

```
User Clicks Shared Link
         ↓
Android System Intent Filter Matching
         ↓
LauncherActivity.getBranchIoDeeplink()
         ↓
BranchHelper.processBranchIoDeepLink()
         ↓
Extract Stream Data from Link
         ↓
Navigate to MainActivity with Bundle
         ↓
MainActivity.checkIfComingFromShareLive()
         ↓
CheckLivePresenter.watchLive()
         ↓
🔴 API CALL SEQUENCE BEGINS
```

## API Call Sequence

### 1. Live.checkLive
**Purpose**: Validate live stream status and get room information  
**Method**: `GET`  
**Endpoint**: `Live.checkLive`  
**Priority**: **CRITICAL** - Always called first

#### Parameters:
```json
{
    "uid": "current_user_id",
    "token": "authentication_token", 
    "liveuid": "stream_owner_uid",
    "stream": "stream_identifier"
}
```

#### Response:
```json
{
    "live_type": 0,           // 0=normal, 1=voice room
    "voice_type": 1,          // Voice room sub-type
    "type": 0,                // 0=normal, 1=password, 2=paid
    "type_val": "0",          // Price or password
    "type_msg": "",           // Room message
    "live_sdk": 1,            // 1=TX, 2=Agora
    "cam_status": 1,          // Camera status
    "guests": [],             // Guest list
    "voice_wheats": 9         // Voice room wheat count
}
```

#### Code Reference:
```java
LiveHttpUtil.checkLive(bean.getUid(), bean.getStream(), new HttpCallback() {
    @Override
    public void onSuccess(int code, String msg, String[] info) {
        if (code == 0) {
            JSONObject obj = JSON.parseObject(info[0]);
            mIsChatRoom = obj.getIntValue("live_type") == 1;
            mLiveType = obj.getIntValue("type");
            // Process room data...
        }
    }
});
```

---

### 2. Live.enterRoom
**Purpose**: Join the live room and get room data  
**Method**: `GET`  
**Endpoint**: `Live.enterRoom`  
**Priority**: **HIGH** - Called if access granted

#### Parameters:
```json
{
    "uid": "current_user_id",
    "token": "authentication_token",
    "city": "user_city",
    "liveuid": "stream_owner_uid", 
    "mobileid": "device_id",
    "stream": "stream_identifier"
}
```

#### Response:
```json
{
    "room_info": {
        "title": "Live Stream Title",
        "pull_url": "streaming_url",
        "push_url": "broadcast_url",
        "online_count": 150,
        "like_count": 1250
    },
    "user_info": {
        "level": 5,
        "coins": 1000,
        "vip_level": 2
    }
}
```

#### Code Reference:
```java
LiveHttpUtil.enterRoom(mLiveUid, mStream, new HttpCallback() {
    @Override
    public void onSuccess(int code, String msg, String[] info) {
        // Initialize live room UI and streaming
        // Setup chat, gifts, user lists, etc.
    }
});
```

---

### 3. Live.getGiftList
**Purpose**: Load available gifts for the room  
**Method**: `GET`  
**Endpoint**: `Live.getGiftList`  
**Priority**: **HIGH** - Always called

#### Parameters:
```json
{
    "uid": "current_user_id",
    "token": "authentication_token",
    "live_type": 0  // 0=normal, 1=voice room
}
```

#### Response:
```json
{
    "gifts": [
        {
            "id": 1,
            "name": "Rose",
            "price": 10,
            "icon": "gift_icon_url",
            "animation": "gift_animation_url"
        }
    ],
    "user_coins": 1000,
    "user_level": 5
}
```

#### Code Reference:
```java
LiveHttpUtil.getGiftList(live_type, new HttpCallback() {
    @Override
    public void onSuccess(int code, String msg, String[] info) {
        // Load gift list and user's remaining coins
    }
});
```

---

### 4. Live.getUserList
**Purpose**: Get current room participants  
**Method**: `GET`  
**Endpoint**: `Live.getUserList`  
**Priority**: **HIGH** - Always called

#### Parameters:
```json
{
    "uid": "current_user_id",
    "token": "authentication_token",
    "liveuid": "stream_owner_uid",
    "stream": "stream_identifier"
}
```

#### Response:
```json
{
    "users": [
        {
            "uid": "user_id",
            "nickname": "User Name",
            "avatar": "avatar_url",
            "level": 3,
            "is_admin": false,
            "is_guard": false
        }
    ],
    "online_count": 150,
    "admin_list": ["admin_uid1", "admin_uid2"]
}
```

#### Code Reference:
```java
LiveHttpUtil.getUserList(liveuid, stream, new HttpCallback() {
    @Override
    public void onSuccess(int code, String msg, String[] info) {
        // Update user list and online count
    }
});
```

---

### 5. Live.getDailyTask
**Purpose**: Check daily tasks and rewards  
**Method**: `GET`  
**Endpoint**: `Live.getDailyTask`  
**Priority**: **MEDIUM** - Called if user is logged in

#### Parameters:
```json
{
    "uid": "current_user_id",
    "token": "authentication_token",
    "liveuid": "stream_owner_uid",
    "islive": 1  // 1=watching live, 0=not watching
}
```

#### Response:
```json
{
    "tasks": [
        {
            "id": 1,
            "name": "Watch Live Stream",
            "description": "Watch live stream for 5 minutes",
            "reward": 50,
            "completed": false,
            "progress": 0
        }
    ],
    "total_rewards": 200
}
```

#### Code Reference:
```java
LiveHttpUtil.getDailyTask(liveUid, isLive, new HttpCallback() {
    @Override
    public void onSuccess(int code, String msg, String[] info) {
        // Show daily tasks and rewards
    }
});
```

---

### 6. Live.getLiveSdk
**Purpose**: Get streaming SDK configuration  
**Method**: `GET`  
**Endpoint**: `Live.getLiveSdk`  
**Priority**: **MEDIUM** - Called if needed

#### Parameters:
```json
{
    "uid": "current_user_id",
    "token": "authentication_token"
}
```

#### Response:
```json
{
    "sdk_config": {
        "tx_appid": "tencent_app_id",
        "agora_appid": "agora_app_id",
        "default_sdk": 1,  // 1=TX, 2=Agora
        "streaming_params": {
            "bitrate": 2000,
            "fps": 30,
            "resolution": "720p"
        }
    }
}
```

#### Code Reference:
```java
LiveHttpUtil.getLiveSdk(new HttpCallback() {
    @Override
    public void onSuccess(int code, String msg, String[] info) {
        // Configure streaming SDK
    }
});
```

---

### 7. Live.getLiveInfo
**Purpose**: Get additional live stream information  
**Method**: `GET`  
**Endpoint**: `Live.getLiveInfo`  
**Priority**: **MEDIUM** - Always called

#### Parameters:
```json
{
    "uid": "current_user_id",
    "token": "authentication_token",
    "liveuid": "stream_owner_uid"
}
```

#### Response:
```json
{
    "stream_info": {
        "title": "Live Stream Title",
        "description": "Stream description",
        "category": "Entertainment",
        "tags": ["fun", "live"],
        "start_time": "2024-01-01 10:00:00",
        "duration": 3600
    },
    "statistics": {
        "viewers": 150,
        "likes": 1250,
        "shares": 45,
        "gifts": 89
    }
}
```

#### Code Reference:
```java
LiveHttpUtil.getLiveInfo(liveUid, new HttpCallback() {
    @Override
    public void onSuccess(int code, String msg, String[] info) {
        // Update stream information and statistics
    }
});
```

---

### 8. Live.getTurntable
**Purpose**: Load game configurations  
**Method**: `GET`  
**Endpoint**: `Live.getTurntable`  
**Priority**: **LOW** - Called if games are enabled

#### Parameters:
```json
{
    "uid": "current_user_id",
    "token": "authentication_token",
    "live_type": 0  // 0=normal, 1=voice room
}
```

#### Response:
```json
{
    "games": [
        {
            "id": 1,
            "name": "Lucky Wheel",
            "enabled": true,
            "prizes": [
                {
                    "id": 1,
                    "name": "100 Coins",
                    "probability": 0.3
                }
            ]
        }
    ]
}
```

#### Code Reference:
```java
LiveHttpUtil.getTurntable(live_type, new HttpCallback() {
    @Override
    public void onSuccess(int code, String msg, String[] info) {
        // Load game configurations and prizes
    }
});
```

---

### 9. Live.getBackpack
**Purpose**: Load user's inventory items  
**Method**: `GET`  
**Endpoint**: `Live.getBackpack`  
**Priority**: **MEDIUM** - Always called

#### Parameters:
```json
{
    "uid": "current_user_id",
    "token": "authentication_token",
    "live_type": 0  // 0=normal, 1=voice room
}
```

#### Response:
```json
{
    "items": [
        {
            "id": 1,
            "name": "Rose Gift",
            "count": 5,
            "icon": "item_icon_url",
            "type": "gift"
        }
    ],
    "effects": [
        {
            "id": 1,
            "name": "Fireworks",
            "duration": 30,
            "icon": "effect_icon_url"
        }
    ]
}
```

#### Code Reference:
```java
LiveHttpUtil.getBackpack(live_type, new HttpCallback() {
    @Override
    public void onSuccess(int code, String msg, String[] info) {
        // Load user's inventory and effects
    }
});
```

---

### 10. Live.getGuardList
**Purpose**: Get room moderators and guards  
**Method**: `GET`  
**Endpoint**: `Live.getGuardList`  
**Priority**: **LOW** - Called if user is admin

#### Parameters:
```json
{
    "uid": "current_user_id",
    "token": "authentication_token",
    "liveuid": "stream_owner_uid",
    "p": 1  // Page number
}
```

#### Response:
```json
{
    "guards": [
        {
            "uid": "guard_uid",
            "nickname": "Guard Name",
            "avatar": "avatar_url",
            "level": 3,
            "guard_type": 1  // 1=moderator, 2=guard
        }
    ],
    "total_count": 10
}
```

#### Code Reference:
```java
LiveHttpUtil.getGuardList(liveUid, page, new HttpCallback() {
    @Override
    public void onSuccess(int code, String msg, String[] info) {
        // Load guard list and moderator permissions
    }
});
```

---

## Conditional API Calls

### Voice Room Specific APIs

#### Live.getVoiceMicApplyList
**Purpose**: Get microphone applicants  
**Trigger**: Voice room type  
**Parameters**: uid, token, stream  
**Response**: Mic applicants list

#### Live.getVoiceControlList
**Purpose**: Get voice control settings  
**Trigger**: Voice room type  
**Parameters**: uid, token, stream  
**Response**: Voice control permissions

#### Live.getVoiceMicStream
**Purpose**: Get voice stream URLs  
**Trigger**: Voice room type  
**Parameters**: uid, token, stream  
**Response**: Voice streaming URLs

### PK Room Specific APIs

#### Livepk.GetLiveList
**Purpose**: Get PK room list  
**Trigger**: PK room type  
**Parameters**: uid, token, p  
**Response**: PK room list

#### Livepk.Search
**Purpose**: Search for PK opponents  
**Trigger**: PK room type  
**Parameters**: uid, key, p  
**Response**: PK search results

### Paid Room Specific APIs

#### Live.roomCharge
**Purpose**: Process payment  
**Trigger**: Paid room type  
**Parameters**: uid, token, liveuid, stream  
**Response**: Payment result

#### Live.timeCharge
**Purpose**: Handle time-based charging  
**Trigger**: Time-based room type  
**Parameters**: uid, token, liveuid, stream  
**Response**: Time charge result

---

## API Call Summary Table

| Order | API Name | Purpose | Priority | Trigger | Parameters |
|-------|----------|---------|----------|---------|------------|
| 1 | **Live.checkLive** | Validate stream status | CRITICAL | Always | uid, token, liveuid, stream |
| 2 | **Live.enterRoom** | Join room | HIGH | If access granted | uid, token, city, liveuid, mobileid, stream |
| 3 | **Live.getGiftList** | Load gifts | HIGH | Always | uid, token, live_type |
| 4 | **Live.getUserList** | Get participants | HIGH | Always | uid, token, liveuid, stream |
| 5 | **Live.getDailyTask** | Check tasks | MEDIUM | If logged in | uid, token, liveuid, islive |
| 6 | **Live.getLiveSdk** | Get SDK config | MEDIUM | If needed | uid, token |
| 7 | **Live.getLiveInfo** | Get stream info | MEDIUM | Always | uid, token, liveuid |
| 8 | **Live.getTurntable** | Load games | LOW | If enabled | uid, token, live_type |
| 9 | **Live.getBackpack** | Load inventory | MEDIUM | Always | uid, token, live_type |
| 10 | **Live.getGuardList** | Get moderators | LOW | If admin | uid, token, liveuid, p |

---

## Error Handling

### Common Error Scenarios

#### 1. Stream Offline
- **Error Code**: 1001
- **Message**: "Live stream has ended"
- **Action**: Show offline message, redirect to home

#### 2. Invalid Stream
- **Error Code**: 1002
- **Message**: "Stream not found"
- **Action**: Show error dialog, return to previous screen

#### 3. Access Denied
- **Error Code**: 1003
- **Message**: "You don't have permission to access this room"
- **Action**: Show access denied dialog

#### 4. Network Error
- **Error Code**: 1004
- **Message**: "Connection failed"
- **Action**: Retry mechanism, show retry button

#### 5. Authentication Failed
- **Error Code**: 1005
- **Message**: "Please login first"
- **Action**: Redirect to login screen

### Retry Logic

```java
// Example retry mechanism
private void retryApiCall() {
    if (retryCount < MAX_RETRY_COUNT) {
        retryCount++;
        // Retry the failed API call
        makeApiCall();
    } else {
        // Show error message
        showErrorMessage();
    }
}
```

---

## Performance Optimization

### Parallel API Calls
Some APIs are called in parallel for better performance:

```java
// Parallel execution example
CompletableFuture.allOf(
    CompletableFuture.runAsync(() -> getGiftList()),
    CompletableFuture.runAsync(() -> getUserList()),
    CompletableFuture.runAsync(() -> getLiveInfo())
).join();
```

### Caching Strategy
- **Gift Lists**: Cached for 5 minutes
- **SDK Config**: Cached for 1 hour
- **User Info**: Cached for 2 minutes
- **Room Info**: No caching (real-time)

### Loading States
- **Initial Loading**: Show loading spinner
- **Progressive Loading**: Load critical data first
- **Background Loading**: Load non-critical data in background

---

## Security Considerations

### Authentication
- All API calls require valid `uid` and `token`
- Tokens expire after 24 hours
- Automatic token refresh mechanism

### Rate Limiting
- API calls are rate-limited per user
- Maximum 10 requests per minute per endpoint
- Rate limit exceeded returns 429 status code

### Data Validation
- All input parameters are validated
- Stream IDs are sanitized
- User permissions are verified

---

## Monitoring and Analytics

### API Metrics
- **Response Time**: Track average response time per API
- **Success Rate**: Monitor API success/failure rates
- **Error Tracking**: Log and analyze error patterns

### User Experience Metrics
- **Time to Load**: Measure total time from link click to room ready
- **Drop-off Rate**: Track users who leave during loading
- **Error Recovery**: Monitor successful error recovery rates

---

## Conclusion

The API call sequence ensures that users have a complete and up-to-date live room experience when clicking on shared links. The system prioritizes critical data loading while providing fallback mechanisms for error scenarios.

### Key Takeaways:
1. **Real-time Validation**: Always validates live stream status
2. **Progressive Loading**: Loads critical data first
3. **Error Handling**: Comprehensive error handling and recovery
4. **Performance**: Optimized for speed and reliability
5. **Security**: Proper authentication and validation

This sequence guarantees that users can seamlessly join live streams from shared links with all necessary functionality available. 