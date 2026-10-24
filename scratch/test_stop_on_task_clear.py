#!/usr/bin/env python3
import sys
import re
from pathlib import Path

BASE_DIR = Path("/home/kaif/Downloads/git-clone/mkr-music")

def test_music_service():
    music_service_path = BASE_DIR / "app/src/main/kotlin/com/music/mkr/playback/MusicService.kt"
    content = music_service_path.read_text(encoding="utf-8")
    
    assert "override fun onTaskRemoved(rootIntent: Intent?)" in content, "Missing onTaskRemoved"
    assert "super.onTaskRemoved(rootIntent)" in content, "Missing super.onTaskRemoved"
    assert "StopMusicOnTaskClearKey" in content, "Missing StopMusicOnTaskClearKey"
    
    # Verify Process.killProcess is completely removed from onTaskRemoved
    on_task_removed_match = re.search(r"override fun onTaskRemoved\(rootIntent: Intent\?\)\s*\{(.*?)\n    \}", content, re.DOTALL)
    assert on_task_removed_match, "Could not find onTaskRemoved block"
    block = on_task_removed_match.group(1)
    
    assert "killProcess" not in block, "FATAL: killProcess should NOT be in onTaskRemoved (causes AMS restart loop and lag)"
    assert "stopSelf()" in block, "stopSelf() must be in onTaskRemoved"
    assert "releaseLoudnessEnhancer()" in block, "releaseLoudnessEnhancer() must be called to prevent audio HAL leaks"
    assert "player.pause()" in block, "player.pause() must be called"
    assert "player.stop()" in block, "player.stop() must be called"
    assert "stopForeground(STOP_FOREGROUND_REMOVE)" in block, "stopForeground must be called"
    
    # Check zombie restart protection in onStartCommand
    assert "intent == null && (!player.isPlaying || player.mediaItemCount == 0)" in content, "Missing headless zombie restart guard in onStartCommand"
    print("✓ MusicService onTaskRemoved & onStartCommand verification passed")

def test_main_activity():
    main_activity_path = BASE_DIR / "app/src/main/kotlin/com/music/mkr/MainActivity.kt"
    content = main_activity_path.read_text(encoding="utf-8")
    
    assert "isServiceBound" in content, "isServiceBound tracking missing in MainActivity"
    assert "StopMusicOnTaskClearKey" in content, "StopMusicOnTaskClearKey missing in MainActivity"
    
    # Check onDestroy does not double unbind unconditionally
    on_destroy_match = re.search(r"override fun onDestroy\(\)\s*\{(.*?)\n    \}", content, re.DOTALL)
    assert on_destroy_match, "Could not find onDestroy in MainActivity"
    destroy_block = on_destroy_match.group(1)
    assert "if (isServiceBound)" in destroy_block, "onDestroy must check isServiceBound before unbinding"
    print("✓ MainActivity service connection & onDestroy verification passed")

def test_player_settings():
    player_settings_path = BASE_DIR / "app/src/main/kotlin/com/music/mkr/ui/screens/settings/PlayerSettings.kt"
    content = player_settings_path.read_text(encoding="utf-8")
    
    assert "StopMusicOnTaskClearKey" in content, "Missing StopMusicOnTaskClearKey in PlayerSettings"
    assert "val (stopMusicOnTaskClear, onStopMusicOnTaskClearChange) = rememberPreference(" in content
    assert "stringResource(R.string.stop_music_on_task_clear)" in content
    assert "checked = stopMusicOnTaskClear" in content
    assert "onCheckedChange = onStopMusicOnTaskClearChange" in content
    assert "onClick = { onStopMusicOnTaskClearChange(!stopMusicOnTaskClear) }" in content
    print("✓ PlayerSettings toggle button verification passed")

if __name__ == "__main__":
    try:
        test_music_service()
        test_main_activity()
        test_player_settings()
        print("\nALL STOP ON TASK CLEAR VERIFICATIONS PASSED!")
    except AssertionError as e:
        print(f"FAILED: {e}", file=sys.stderr)
        sys.exit(1)
