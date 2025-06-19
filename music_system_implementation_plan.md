# Live Streaming Music System Implementation Plan

## Overview

This document outlines a comprehensive plan to implement a music system in the live streaming application. The system will allow hosts to play music from their local storage during live broadcasts, with high-quality audio transmission to the audience.

## Current Codebase Analysis

### Existing Music-Related Components

1. **LiveMusicPlayer** (`live/src/main/java/com/livestreaming/live/music/LiveMusicPlayer.java`)
   - Basic music player implementation
   - Support for lyrics display
   - Limited to internal music playback

2. **LiveMusicViewHolder** (`live/src/main/java/com/livestreaming/live/views/LiveMusicViewHolder.java`)
   - UI component for displaying music controls and lyrics
   - Basic play/stop functionality

3. **Audio Mixing APIs**
   - `startBgm()`, `pauseBgm()`, `resumeBgm()`, `stopBgm()` methods in:
     - `LivePushTxViewHolder` for Tencent SDK
     - `LiveChatRoomPushTxViewHolder` for chat rooms

4. **Music Selection**
   - `LiveMusicDialogFragment` for selecting music
   - Limited to predefined music sources

## Implementation Plan

### Phase 1: Local Music Access and Selection

#### Step 1: Create Music File Manager
- Create a new class `MusicFileManager` to scan and access local music files
- Implement MediaStore API integration to list all music files on the device
- Add metadata extraction (title, artist, album, duration)

#### Step 2: Enhance Music Selection UI
- Modify `LiveMusicDialogFragment` to browse local files
- Add sorting options (by name, date, artist)
- Implement search functionality
- Create playlist management capability

### Phase 2: Music Player Enhancement

#### Step 1: Improve Music Player Core
- Enhance `LiveMusicPlayer` to support:
  - Playlist management
  - Shuffle and repeat modes
  - Seeking within tracks
  - Audio focus management

#### Step 2: Create Modern Music Controls UI
- Design a floating/draggable music control panel
- Include:
  - Play/pause button
  - Next/previous buttons
  - Progress bar with seeking
  - Volume control
  - Playlist access

### Phase 3: Audio Streaming Integration

#### Step 1: Tencent SDK Integration (Primary)
- Utilize `TXAudioEffectManager` for high-quality music streaming
- Configure optimal audio parameters:
  - Set music bitrate to 128kbps minimum
  - Enable stereo playback if supported
  - Configure proper volume levels

#### Step 2: Agora SDK Integration (Alternative)
- Implement equivalent functionality using Agora's audio mixing APIs
- Configure dual-channel transmission for optimal quality

### Phase 4: UI/UX Refinement

#### Step 1: Host-Side UI
- Add "Now Playing" information display
- Implement music visualization (optional)
- Create volume balance controls between music and voice

#### Step 2: Audience-Side UI
- Add subtle "Music Playing" indicator
- Show current track information (optional)

## Technical Details

### Audio Configuration

For optimal music quality while maintaining voice clarity:

1. **Tencent SDK Configuration**:
```
audioEffectManager.setAllMusicVolume(70); // Music volume (0-100)
audioEffectManager.setVoiceVolume(100);   // Voice volume (0-100)

// Music playback parameters
AudioMusicParam audioMusicParam = new AudioMusicParam(1, musicPath);
audioMusicParam.publish = true;           // Publish to audience
audioMusicParam.loopCount = 0;            // No looping (managed by playlist)
```

2. **Audio Processing**:
- Apply minimal processing to music channel
- Maintain voice processing for microphone input
- Balance volumes to ensure voice is always intelligible

### File Access

1. **Permission Requirements**:
- `READ_EXTERNAL_STORAGE` for accessing local music files
- Handle runtime permissions for Android 6.0+
- Implement scoped storage access for Android 10+

2. **File Types Support**:
- MP3, AAC, WAV, FLAC formats
- Handle various bitrates and sampling rates

## Implementation Path

### 1. Core Music Player (Week 1)

Enhance the existing `LiveMusicPlayer` class:
- Add playlist management
- Implement playback controls
- Add event listeners for state changes

### 2. File Browser (Week 1-2)

Create or modify `LiveMusicDialogFragment`:
- Implement local file scanning
- Create UI for browsing music library
- Add search and filter capabilities

### 3. UI Components (Week 2)

Create new ViewHolders:
- `LiveMusicControlViewHolder` for host controls
- `LiveMusicInfoViewHolder` for audience display

### 4. SDK Integration (Week 3)

Modify existing SDK integration:
- Enhance `startBgm()` method implementation
- Configure optimal audio parameters
- Test with various network conditions

### 5. Testing and Refinement (Week 4)

- Test on various devices
- Optimize performance
- Refine UI based on feedback

## Files to Modify

1. **Create New Files**:
- `live/src/main/java/com/livestreaming/live/music/MusicFileManager.java`
- `live/src/main/java/com/livestreaming/live/music/PlaylistManager.java`
- `live/src/main/res/layout/view_live_music_control.xml`
- `live/src/main/res/layout/view_live_music_info.xml`

2. **Modify Existing Files**:
- `live/src/main/java/com/livestreaming/live/music/LiveMusicPlayer.java`
- `live/src/main/java/com/livestreaming/live/views/LiveMusicViewHolder.java`
- `live/src/main/java/com/livestreaming/live/music/LiveMusicDialogFragment.java`
- `live/src/main/java/com/livestreaming/live/activity/LiveAnchorActivity.java`
- `live/src/main/java/com/livestreaming/live/views/LivePushTxViewHolder.java`

## Additional Resources

1. **Icons and Assets**:
- Music control icons (play, pause, next, previous)
- Volume icons
- Music visualization assets

2. **String Resources**:
- Add music-related strings to resource files
- Support multilingual text

## Completion Criteria

The music system implementation will be considered complete when:

1. Hosts can browse and select music from their local storage
2. Music plays in the background during broadcasts with clear audio
3. Basic controls (play, pause, next, previous) function properly
4. Music is transmitted to the audience with high quality
5. Voice and music are properly balanced

## Future Enhancements (Post-MVP)

1. **Advanced Features**:
- Equalizer for music
- Music sharing between hosts
- Music recommendations

2. **Performance Optimizations**:
- Caching of music metadata
- Optimized file loading
- Reduced memory footprint

3. **UX Improvements**:
- Advanced visualizations
- Audience reactions to music
- Music request system 