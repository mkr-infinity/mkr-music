/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

@file:Suppress("DEPRECATION")

package com.music.mkr.playback

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.audiofx.AudioEffect
import android.media.audiofx.LoudnessEnhancer
import android.net.ConnectivityManager
import android.os.Binder
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.datastore.preferences.core.edit
import com.music.mkr.constants.StopMusicOnTaskClearKey
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Player.EVENT_POSITION_DISCONTINUITY
import androidx.media3.common.Player.EVENT_TIMELINE_CHANGED
import androidx.media3.common.Player.REPEAT_MODE_ALL
import androidx.media3.common.Player.REPEAT_MODE_OFF
import androidx.media3.common.Player.REPEAT_MODE_ONE
import androidx.media3.common.Player.STATE_IDLE
import androidx.media3.common.Timeline
import androidx.media3.common.audio.SonicAudioProcessor
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.datasource.ResolvingDataSource
import androidx.media3.datasource.TransferListener
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.analytics.PlaybackStats
import androidx.media3.exoplayer.analytics.PlaybackStatsListener
import androidx.media3.exoplayer.audio.DefaultAudioSink
import androidx.media3.exoplayer.audio.SilenceSkippingAudioProcessor
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.ShuffleOrder.DefaultShuffleOrder
import androidx.media3.extractor.ExtractorsFactory
import androidx.media3.extractor.mkv.MatroskaExtractor
import androidx.media3.extractor.mp4.FragmentedMp4Extractor
import androidx.media3.extractor.mp4.Mp4Extractor
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import com.music.innertube.YouTube
import com.music.innertube.models.SongItem
import com.music.innertube.models.WatchEndpoint
import com.music.innertube.pages.RadioChip
import com.music.lastfm.LastFM
import com.music.mkr.MainActivity
import com.music.mkr.R
import com.music.mkr.constants.AudioNormalizationKey
import com.music.mkr.constants.AudioOffload
import com.music.mkr.constants.AudioQualityKey
import com.music.mkr.constants.AutoLoadMoreKey
import com.music.mkr.constants.AutoSkipNextOnErrorKey
import com.music.mkr.constants.CrossfadeCurve
import com.music.mkr.constants.CrossfadeCurveKey
import com.music.mkr.constants.CrossfadeDurationKey
import com.music.mkr.constants.DefaultCrossfadeEnabled
import com.music.mkr.constants.CrossfadeEnabledKey
import com.music.mkr.constants.CrossfadeGaplessKey
import com.music.mkr.constants.CrossfadeManualSkipKey
import com.music.mkr.constants.DisableLoadMoreWhenRepeatAllKey
import com.music.mkr.constants.DisableSongCacheKey
import com.music.mkr.constants.MaxSongCacheCountKey
import com.music.mkr.constants.SongCacheDelaySecondsKey
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.cache.CacheWriter
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.music.mkr.constants.DiscordActivityNameKey
import com.music.mkr.constants.DiscordActivityTypeKey
import com.music.mkr.constants.DiscordAdvancedModeKey
import com.music.mkr.constants.DiscordButton1TextKey
import com.music.mkr.constants.DiscordButton1VisibleKey
import com.music.mkr.constants.DiscordButton2TextKey
import com.music.mkr.constants.DiscordButton2VisibleKey
import com.music.mkr.constants.DiscordStatusKey
import com.music.mkr.constants.DiscordTokenKey
import com.music.mkr.constants.DiscordUseDetailsKey
import com.music.mkr.constants.EnableDiscordRPCKey
import com.music.mkr.constants.EnableLastFMScrobblingKey
import com.music.mkr.constants.HideExplicitKey
import com.music.mkr.constants.HideVideoSongsKey
import com.music.mkr.constants.HistoryDuration
import com.music.mkr.constants.LastFMUseNowPlaying
import com.music.mkr.constants.MediaSessionConstants.CommandToggleLike
import com.music.mkr.constants.MediaSessionConstants.CommandToggleRepeatMode
import com.music.mkr.constants.MediaSessionConstants.CommandToggleShuffle
import com.music.mkr.constants.MediaSessionConstants.CommandToggleStartRadio
import com.music.mkr.constants.PauseListenHistoryKey
import com.music.mkr.constants.PauseOnMute
import com.music.mkr.constants.PersistentQueueKey
import com.music.mkr.constants.PersistentShuffleAcrossQueuesKey
import com.music.mkr.constants.PlayerVolumeKey
import com.music.mkr.constants.RememberShuffleAndRepeatKey
import com.music.mkr.constants.RepeatModeKey
import com.music.mkr.constants.ResumeOnBluetoothConnectKey
import com.music.mkr.constants.ScrobbleDelayPercentKey
import com.music.mkr.constants.ScrobbleDelaySecondsKey
import com.music.mkr.constants.ScrobbleMinSongDurationKey
import com.music.mkr.constants.ShowLyricsKey
import com.music.mkr.constants.ShuffleModeKey
import com.music.mkr.constants.ShufflePlaylistFirstKey
import com.music.mkr.constants.PreventDuplicateTracksInQueueKey
import com.music.mkr.constants.SimilarContent
import com.music.mkr.constants.SkipSilenceInstantKey
import com.music.mkr.constants.SkipSilenceKey
import com.music.mkr.constants.EnableSponsorBlockKey
import com.music.mkr.constants.SponsorBlockServerUrlKey
import com.music.mkr.constants.SponsorBlockSkipNonMusicKey
import com.music.mkr.constants.SponsorBlockSkipSponsorKey
import com.music.mkr.constants.SponsorBlockSkipSelfPromoKey
import com.music.mkr.constants.SponsorBlockSkipInteractionKey
import com.music.mkr.constants.SponsorBlockSkipIntroOutroKey
import com.music.mkr.constants.SponsorBlockSkipPreviewFillerKey
import com.music.mkr.constants.SponsorBlockShowToastKey
import com.music.mkr.sponsorblock.SponsorBlockApi
import com.music.mkr.sponsorblock.SponsorSegment
import com.music.mkr.constants.IpVersionKey
import com.music.innertube.models.IpVersion
import okhttp3.Dns
import java.net.InetAddress
import java.net.Inet4Address
import java.net.Inet6Address
import com.music.mkr.db.MusicDatabase
import com.music.mkr.db.entities.FormatEntity
import com.music.mkr.db.entities.LyricsEntity
import com.music.mkr.db.entities.RelatedSongMap
import com.music.mkr.db.entities.Song
import com.music.mkr.db.entities.SongEntity
import com.music.mkr.di.DownloadCache
import com.music.mkr.di.PlayerCache
import com.music.mkr.eq.EqualizerService
import com.music.mkr.eq.audio.CustomEqualizerAudioProcessor
import com.music.mkr.eq.data.EQProfileRepository
import com.music.mkr.playback.equalizer.EqualizerController
import com.music.mkr.extensions.SilentHandler
import com.music.mkr.extensions.collect
import com.music.mkr.extensions.collectLatest
import com.music.mkr.extensions.currentMetadata
import com.music.mkr.extensions.findMediaItemById
import com.music.mkr.extensions.findNextMediaItemById
import com.music.mkr.extensions.mediaItems
import com.music.mkr.extensions.metadata
import com.music.mkr.extensions.setOffloadEnabled
import com.music.mkr.extensions.toEnum
import com.music.mkr.extensions.toMediaItem
import com.music.mkr.extensions.toPersistQueue
import com.music.mkr.extensions.toQueue
import com.music.mkr.lyrics.LyricsHelper
import com.music.mkr.models.PersistPlayerState
import com.music.mkr.models.PersistQueue
import com.music.mkr.models.toMediaMetadata
import com.music.mkr.playback.audio.SilenceDetectorAudioProcessor
import com.music.mkr.playback.queues.EmptyQueue
import com.music.mkr.playback.queues.ListQueue
import com.music.mkr.playback.queues.Queue
import com.music.mkr.playback.queues.YouTubeQueue
import com.music.mkr.playback.queues.filterExplicit
import com.music.mkr.playback.queues.filterVideoSongs
import com.music.mkr.utils.CoilBitmapLoader
import com.music.mkr.utils.DiscordRPC
import com.music.mkr.utils.InnerTubeXPlayer
import com.music.mkr.utils.NetworkConnectivityObserver
import com.music.mkr.utils.ScrobbleManager
import com.music.mkr.utils.SyncUtils
import com.music.mkr.utils.YTPlayerUtils
import com.music.mkr.utils.dataStore
import com.music.mkr.utils.get
import com.music.mkr.utils.reportException
import com.music.mkr.widget.mkrMusicWidgetManager
import com.music.mkr.widget.MusicWidgetReceiver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.OkHttpClient
import timber.log.Timber
import java.time.LocalDateTime
import javax.inject.Inject
import kotlin.coroutines.coroutineContext
import kotlin.time.Duration.Companion.seconds

private const val INSTANT_SILENCE_SKIP_STEP_MS = 15_000L
private const val INSTANT_SILENCE_SKIP_SETTLE_MS = 350L

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@androidx.annotation.OptIn(UnstableApi::class)
@AndroidEntryPoint
class MusicService :
    MediaLibraryService(),
    Player.Listener,
    PlaybackStatsListener.Callback {
    @Inject
    lateinit var database: MusicDatabase

    @Inject
    lateinit var lyricsHelper: LyricsHelper

    @Inject
    lateinit var syncUtils: SyncUtils

    @Inject
    lateinit var mediaLibrarySessionCallback: MediaLibrarySessionCallback

    @Inject
    lateinit var queuePersistence: QueuePersistence

    @Inject
    lateinit var equalizerService: EqualizerService

    @Inject
    lateinit var eqProfileRepository: EQProfileRepository

    @Inject
    lateinit var equalizerController: EqualizerController

    @Inject
    lateinit var widgetManager: mkrMusicWidgetManager

    @Inject
    lateinit var listenTogetherManager: com.music.mkr.listentogether.ListenTogetherManager

    private lateinit var audioManager: AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null
    private var lastAudioFocusState = AudioManager.AUDIOFOCUS_NONE
    private var hasAudioFocus = false
    private var resumeAfterFocusLoss = false
    private var wasPlayingBeforeVolumeMute = false
    private var isPausedByVolumeMute = false
    var preferredDeviceId: Int? = null
        private set
    lateinit var audioOutputResolver: AudioOutputResolver
    val activeAudioDevice get() = audioOutputResolver.activeAudioDevice
    fun refreshActiveDevice() = audioOutputResolver.refresh()

    private var crossfadeEnabled = DefaultCrossfadeEnabled
    private var crossfadeDurationMs = 5_000L
    private var crossfadeGapless = false
    private var crossfadeManualSkipEnabled = false
    private var crossfadeCurve = CrossfadeCurve.EASE_OUT_QUAD
    private var crossfadeTriggerJob: Job? = null

    /** Holds the combined crossfade-related settings emitted from DataStore. */
    private data class CrossfadeSettings(
        val enabled: Boolean,
        val durationSeconds: Float,
        val gapless: Boolean,
        val manualSkip: Boolean,
        val curve: CrossfadeCurve,
    )

    private data class CrossfadeSource(
        val player: ExoPlayer,
        val windowUid: Any,
        val duration: Long,
        val speed: Float,
        val overlap: Long,
    )

    private data class CrossfadeTarget(val index: Int, val windowUid: Any, val item: MediaItem)
    private data class AutomaticCrossfade(val source: CrossfadeSource, val target: CrossfadeTarget, val repeat: Boolean)

    private class CrossfadePreparation(val player: ExoPlayer) {
        val ready = CompletableDeferred<Boolean>()
        val audio = CompletableDeferred<Pair<AnalyticsListener.EventTime, Long>?>()
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) ready.complete(true)
                if (state == Player.STATE_ENDED) {
                    ready.complete(false)
                    audio.complete(null)
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                ready.complete(false)
                audio.complete(null)
            }
        }
        val analytics = object : AnalyticsListener {
            override fun onAudioPositionAdvancing(eventTime: AnalyticsListener.EventTime, playoutStartSystemTimeMs: Long) {
                audio.complete(eventTime to playoutStartSystemTimeMs)
            }
        }
    }

    private val outgoingListener = object : Player.Listener {
        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
            if (reason == Player.PLAY_WHEN_READY_CHANGE_REASON_END_OF_MEDIA_ITEM) endOutgoingPlayback()
        }

        override fun onPlaybackStateChanged(state: Int) {
            if (state == Player.STATE_ENDED) endOutgoingPlayback()
        }

        override fun onPlayerError(error: PlaybackException) {
            endOutgoingPlayback()
        }
    }

    private var automaticFade: AutomaticCrossfade? = null
    private var crossfadePreparation: CrossfadePreparation? = null
    private val secondaryPlayer get() = crossfadePreparation?.player
    private var crossfadeProgress = 1f
    private var activeCrossfadeCurve = CrossfadeCurve.EASE_OUT_QUAD
    private val overlapPlaying = MutableStateFlow(false)
    private var crossfadeNeedsRestart = false

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var queueLoadJob: Job? = null
    private var restoringQueue = false
    private var queuePreviouslyAvailable = false
    private var pendingResumeShuffle: RestoredSessionQueue? = null
    private var pendingQueueSwitch = false

    private val binder = MusicBinder()

    inner class MusicBinder : Binder() {
        val service: MusicService
            get() = this@MusicService
    }


    private lateinit var connectivityManager: ConnectivityManager
    lateinit var connectivityObserver: NetworkConnectivityObserver
    val waitingForNetworkConnection = MutableStateFlow(false)
    private val isNetworkConnected = MutableStateFlow(false)

    private lateinit var audioQuality: com.music.mkr.constants.AudioQuality
    private lateinit var ipVersion: IpVersion

    var currentQueue: Queue = EmptyQueue
    var queueTitle: String? = null
    val radioChips = MutableStateFlow<List<RadioChip>>(emptyList())
    private var radioChipsJob: Job? = null

    val currentMediaMetadata = MutableStateFlow<com.music.mkr.models.MediaMetadata?>(null)
    private val currentSong =
        currentMediaMetadata
            .flatMapLatest { mediaMetadata ->
                database.song(mediaMetadata?.id)
            }.stateIn(scope, SharingStarted.Lazily, null)
    private val currentFormat =
        currentMediaMetadata.flatMapLatest { mediaMetadata ->
            database.format(mediaMetadata?.id)
        }

    lateinit var playerVolume: MutableStateFlow<Float>
    val isMuted = MutableStateFlow(false)

    fun toggleMute() {
        val newMutedState = !isMuted.value
        isMuted.value = newMutedState
        applyPlaybackVolume()
    }

    fun setMuted(muted: Boolean) {
        isMuted.value = muted
        applyPlaybackVolume()
    }

    fun setPreferredAudioDevice(deviceId: Int?) { // this helps us to change between devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            val deviceInfo = devices.find { it.id == deviceId }
            player.setPreferredAudioDevice(deviceInfo)
            secondaryPlayer?.setPreferredAudioDevice(deviceInfo)
            fadingPlayer?.setPreferredAudioDevice(deviceInfo)
            preferredDeviceId = deviceId
        }
    }
//

    lateinit var sleepTimer: SleepTimer

    @Inject
    @PlayerCache
    lateinit var playerCache: SimpleCache

    @Inject
    @DownloadCache
    lateinit var downloadCache: SimpleCache

    lateinit var player: ExoPlayer
        private set
    private var fadingPlayer: ExoPlayer? = null
    private var crossfadeJob: Job? = null

    private lateinit var mediaSession: MediaLibrarySession
    private var mediaButtonStartupJob: Job? = null
    private var mediaButtons = emptyList<CommandButton>()

    // Tracks if player has been properly initilized
    private val playerInitialized = MutableStateFlow(false)
    val isPlayerReady: kotlinx.coroutines.flow.StateFlow<Boolean> = playerInitialized.asStateFlow()

    // Expose active player flow for UI/Connection updates
    private val _playerFlow = MutableStateFlow<ExoPlayer?>(null)
    val playerFlow = _playerFlow.asStateFlow()

    private val playerSilenceProcessors = HashMap<Player, SilenceDetectorAudioProcessor>()
    private val playerEqualizerProcessors = HashMap<Player, CustomEqualizerAudioProcessor>()


    private val instantSilenceSkipEnabled = MutableStateFlow(false)

    private var isAudioEffectSessionOpened = false
    private var openedAudioSessionId: Int? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null

    private var discordRpc: DiscordRPC? = null
    private var lastPlaybackSpeed = 1.0f
    private var discordUpdateJob: kotlinx.coroutines.Job? = null

    private var scrobbleManager: ScrobbleManager? = null

    val automixItems = MutableStateFlow<List<MediaItem>>(emptyList())

    /**
     * Emits the video ID of a song immediately after [YouTube.registerPlayback] succeeds.
     * [HistoryViewModel] collects this to auto-refresh remote history without any
     * timing-based delays or tab-state checks.
     */
    val playbackRegistered = MutableSharedFlow<String>(extraBufferCapacity = 1)

    // Tracks the original queue size to distinguish original items from auto-added ones
    private var originalQueueSize: Int = 0

    private var consecutivePlaybackErr = 0
    private var retryJob: Job? = null
    private var retryCount = 0
    private var silenceSkipJob: Job? = null
    /** Background job that pre-resolves the next track's stream URL into [songUrlCache]. */
    private var prefetchJob: Job? = null

    // URL cache for stream URLs - class-level so it can be invalidated on errors
    private val songUrlCache = StreamUrlCache()

    // Flag to bypass cache when quality changes - forces fresh stream fetch
    private val bypassCacheForQualityChange = mutableSetOf<String>()

    // Enhanced error tracking for strict retry management
    private var currentMediaIdRetryCount = mutableMapOf<String, Int>()
    private val MAX_RETRY_PER_SONG = 3
    private val RETRY_DELAY_MS = 1000L

    // Track failed songs to prevent infinite retry loops
    private val recentlyFailedSongs = mutableSetOf<String>()
    private var failedSongsClearJob: Job? = null

    // Google Cast support
    var castConnectionHandler: CastConnectionHandler? = null
        private set

    private val screenStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    if (!player.isPlaying) {
                        scope.launch(Dispatchers.IO) {
                            discordRpc?.closeRPC()
                        }
                    }
                }
                Intent.ACTION_SCREEN_ON -> {
                    if (player.isPlaying) {
                        scope.launch {
                            currentSong.value?.let { song ->
                                updateDiscordRPC(song)
                            }
                        }
                    }
                }
            }
        }
    }

    private val audioDeviceCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) {
            super.onAudioDevicesAdded(addedDevices)
            if (::audioOutputResolver.isInitialized) {
                audioOutputResolver.refresh()
            }
            val hasBluetooth = addedDevices?.any {
                it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                it.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO
            } == true

            if (hasBluetooth) {
                if (dataStore.get(ResumeOnBluetoothConnectKey, false)) {
                    if (player.playbackState == Player.STATE_READY && !player.isPlaying) {
                        player.play()
                    }
                }
            }
        }

        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
            super.onAudioDevicesRemoved(removedDevices)
            if (removedDevices?.isNotEmpty() == true) releaseCrossfadePlayers()
            if (::audioOutputResolver.isInitialized) {
                audioOutputResolver.refresh()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true

        // Player rediness reset to false
        playerInitialized.value = false

        // 3. Connect the processor to the service
        // handled in createExoPlayer

        setMediaNotificationProvider(
            DefaultMediaNotificationProvider(
                this,
                { NOTIFICATION_ID },
                CHANNEL_ID,
                R.string.music_player
            ).apply { setSmallIcon(R.drawable.mkrmusicnotification) }
        )
        player = createExoPlayer()
        _playerFlow.value = player
        player.addListener(this@MusicService)
        sleepTimer = SleepTimer(scope, player)
        player.addListener(sleepTimer)

        // Mark player as initialized after successful creation
        playerInitialized.value = true
        Timber.tag(TAG).d("Player successfully initialized")

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioOutputResolver = AudioOutputResolver(audioManager)
        audioOutputResolver.refresh()
        setupAudioFocusRequest()

        mediaLibrarySessionCallback.apply {
            service = this@MusicService
            toggleLike = ::toggleLike
            toggleStartRadio = ::toggleStartRadio
            toggleLibrary = ::toggleLibrary
        }
        mediaSession =
            MediaLibrarySession
                .Builder(this, player, mediaLibrarySessionCallback)
                .setSessionActivity(
                    PendingIntent.getActivity(
                        this,
                        0,
                        Intent(this, MainActivity::class.java),
                        PendingIntent.FLAG_IMMUTABLE,
                    ),
                ).setBitmapLoader(CoilBitmapLoader(this, scope))
                .build()
        player.repeatMode = dataStore.get(RepeatModeKey, REPEAT_MODE_OFF)

        // Restore shuffle mode if remember option is enabled
        if (dataStore.get(RememberShuffleAndRepeatKey, true)) {
            player.shuffleModeEnabled = dataStore.get(ShuffleModeKey, false)
        }

        addSession(mediaSession)

        connectivityManager = getSystemService()!!
        connectivityObserver = NetworkConnectivityObserver(this)

        val screenStateFilter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        try {
            androidx.core.content.ContextCompat.registerReceiver(
                this,
                screenStateReceiver,
                screenStateFilter,
                androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED
            )
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to register screenStateReceiver")
        }

        audioManager.registerAudioDeviceCallback(audioDeviceCallback, null)

        audioQuality = dataStore.get(AudioQualityKey).toEnum(com.music.mkr.constants.AudioQuality.AUTO)
        ipVersion = dataStore.get(IpVersionKey).toEnum(IpVersion.AUTO)
        playerVolume = MutableStateFlow(dataStore.get(PlayerVolumeKey, 1f).coerceIn(0f, 1f))

        // Initialize Google Cast
        initializeCast()

        // 4. Watch for EQ profile changes
        scope.launch {
            eqProfileRepository.activeProfile.collect { profile ->
                if (profile != null) {
                    val result = equalizerService.applyProfile(profile)
                    if (result.isSuccess && player.playbackState == Player.STATE_READY && player.isPlaying) {
                        // Instant update: flush buffers and seek slightly to re-process audio
                        // Small seek to force re-buffer through the new EQ settings
                        // Seek to current position effectively resets the pipeline
                        player.seekTo(player.currentPosition)
                    }
                } else {
                    equalizerService.disable()
                    if (player.playbackState == Player.STATE_READY && player.isPlaying) {
                        player.seekTo(player.currentPosition)
                    }
                }
            }
        }

        scope.launch {
            connectivityObserver.networkStatus.collect { isConnected ->
                isNetworkConnected.value = isConnected
                if (isConnected && waitingForNetworkConnection.value) {
                    triggerRetry()
                }
                // Update Discord RPC when network becomes available
                if (isConnected && discordRpc != null && player.isPlaying) {
                    val mediaId = player.currentMetadata?.id
                    if (mediaId != null) {
                        database.song(mediaId).first()?.let { song ->
                            updateDiscordRPC(song)
                        }
                    }
                }
            }
        }

        // Watch for audio quality setting changes
        var isFirstQualityEmit = true
        scope.launch {
            dataStore.data
                .map { it[AudioQualityKey]?.let { value ->
                    com.music.mkr.constants.AudioQuality.entries.find { it.name == value }
                } ?: com.music.mkr.constants.AudioQuality.AUTO }
                .distinctUntilChanged()
                .collect { newQuality ->
                    val oldQuality = audioQuality
                    audioQuality = newQuality

                    // Skip reload on first emit (app startup)
                    if (isFirstQualityEmit) {
                        isFirstQualityEmit = false
                        Timber.tag("MusicService").i("QUALITY INIT: $newQuality")
                        return@collect
                    }

                    Timber.tag("MusicService").i("QUALITY CHANGED: $oldQuality -> $newQuality")

                    // Reload current song with new quality
                    val mediaId = player.currentMediaItem?.mediaId ?: return@collect
                    val downloaded = withContext(Dispatchers.IO) { downloadCache.completeAudioCachedAt(mediaId) != null }
                    if (downloaded || player.currentMediaItem?.mediaId != mediaId) return@collect
                    val currentPosition = player.currentPosition
                    val wasPlaying = player.isPlaying
                    val currentIndex = player.currentMediaItemIndex

                    Timber.tag("MusicService").i("RELOADING STREAM: $mediaId at position ${currentPosition}ms")

                    // Clear cached URL to force fresh fetch
                    songUrlCache.invalidate(mediaId)

                    // Discard the old automatic stream before opening the new format.
                    runBlocking(Dispatchers.IO) {
                        try {
                            playerCache.removeResource(mediaId)
                            Timber.tag("MusicService").d("Cleared player cache for $mediaId")
                        } catch (e: Exception) {
                            Timber.tag("MusicService").e(e, "Failed to clear cache for $mediaId")
                        }
                    }

                    // Set bypass flag so resolver skips cache checks
                    bypassCacheForQualityChange.add(mediaId)
                    Timber.tag("MusicService").d("Set bypass cache flag for $mediaId")

                    // Reload player at same position
                    player.stop()
                    player.seekTo(currentIndex, currentPosition)
                    player.prepare()
                    if (wasPlaying) {
                        player.play()
                    }
                }
        }

        // Watch for IP version changes
        scope.launch {
            dataStore.data
                .map { it[IpVersionKey]?.toEnum(IpVersion.AUTO) ?: IpVersion.AUTO }
                .distinctUntilChanged()
                .collect { newIpVersion ->
                    val oldIpVersion = ipVersion
                    ipVersion = newIpVersion

                    if (isFirstQualityEmit) return@collect

                    Timber.tag("MusicService").i("IP VERSION CHANGED: $oldIpVersion -> $newIpVersion")

                    // Reload player to apply new DNS filter
                    val mediaId = player.currentMediaItem?.mediaId ?: return@collect
                    val currentPosition = player.currentPosition
                    val currentIndex = player.currentMediaItemIndex
                    val wasPlaying = player.isPlaying

                    // Clear cached URL
                    songUrlCache.invalidate(mediaId)

                    // Reload player
                    player.stop()
                    player.seekTo(currentIndex, currentPosition)
                    player.prepare()
                    if (wasPlaying) {
                        player.play()
                    }
                }
        }

        combine(playerVolume, isMuted) { volume, muted ->
            if (muted) 0f else volume
        }.collectLatest(scope) {
            applyPlaybackVolume()
        }

        playerVolume.debounce(1000).collect(scope) { volume ->
            dataStore.edit { settings ->
                settings[PlayerVolumeKey] = volume
            }
        }

        currentSong.map { song -> song?.id to song?.song?.liked }.distinctUntilChanged().collect(scope) {
            updateNotification()
        }
        currentSong.debounce(1000).collect(scope) {
            updateWidgetUI(player.isPlaying)
        }

        currentMediaMetadata
            .distinctUntilChangedBy { it?.id }
            .collectLatest(scope) { mediaMetadata ->
                if (mediaMetadata != null) {
                    val existing = database.lyrics(mediaMetadata.id).firstOrNull()
                    if (existing == null || existing.lyrics.isBlank() || existing.lyrics == LyricsEntity.LYRICS_NOT_FOUND) {
                        try {
                            val lyricsWithProvider = lyricsHelper.getLyrics(mediaMetadata)
                            if (lyricsWithProvider.lyrics.isNotBlank() && lyricsWithProvider.lyrics != LyricsEntity.LYRICS_NOT_FOUND) {
                                database.query {
                                    upsert(
                                        LyricsEntity(
                                            id = mediaMetadata.id,
                                            lyrics = lyricsWithProvider.lyrics,
                                            provider = lyricsWithProvider.provider,
                                        ),
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            // Non-fatal background lyrics pre-cache
                        }
                    }
                }
            }

        dataStore.data
            .map { (it[SkipSilenceKey] ?: false) to (it[SkipSilenceInstantKey] ?: false) }
            .distinctUntilChanged()
            .collectLatest(scope) { (skipSilence, instantSkip) ->
                player.skipSilenceEnabled = skipSilence
                secondaryPlayer?.skipSilenceEnabled = skipSilence

                val enableInstant = skipSilence && instantSkip
                instantSilenceSkipEnabled.value = enableInstant

                playerSilenceProcessors.forEach { (owner, processor) ->
                    processor.instantModeEnabled = enableInstant && owner === player
                    if (!processor.instantModeEnabled) {
                        processor.resetTracking()
                    }
                }

                if (!enableInstant) {
                    silenceSkipJob?.cancel()
                }
            }

        // SponsorBlock automatic segment skipping flow
        var sponsorBlockJob: Job? = null
        combine(
            currentMediaMetadata,
            dataStore.data.map { prefs ->
                SponsorBlockConfig(
                    enabled = prefs[EnableSponsorBlockKey] ?: true,
                    serverUrl = prefs[SponsorBlockServerUrlKey] ?: "https://sponsor.ajay.app",
                    skipNonMusic = prefs[SponsorBlockSkipNonMusicKey] ?: true,
                    skipSponsor = prefs[SponsorBlockSkipSponsorKey] ?: true,
                    skipSelfPromo = prefs[SponsorBlockSkipSelfPromoKey] ?: true,
                    skipInteraction = prefs[SponsorBlockSkipInteractionKey] ?: true,
                    skipIntroOutro = prefs[SponsorBlockSkipIntroOutroKey] ?: true,
                    skipPreviewFiller = prefs[SponsorBlockSkipPreviewFillerKey] ?: false,
                    showToast = prefs[SponsorBlockShowToastKey] ?: true
                )
            }.distinctUntilChanged()
        ) { metadata, config ->
            metadata to config
        }.collectLatest(scope) { (metadata, config) ->
            sponsorBlockJob?.cancel()
            sponsorBlockJob = null

            if (!config.enabled || metadata == null) {
                return@collectLatest
            }

            val categories = mutableListOf<String>()
            if (config.skipNonMusic) categories.add("music_offtopic")
            if (config.skipSponsor) categories.add("sponsor")
            if (config.skipSelfPromo) categories.add("selfpromo")
            if (config.skipInteraction) categories.add("interaction")
            if (config.skipIntroOutro) {
                categories.add("intro")
                categories.add("outro")
            }
            if (config.skipPreviewFiller) {
                categories.add("preview")
                categories.add("filler")
            }

            if (categories.isEmpty()) {
                return@collectLatest
            }

            val result = SponsorBlockApi.getSkipSegments(metadata.id, categories, config.serverUrl)
            val segments = result.getOrDefault(emptyList())

            if (segments.isNotEmpty()) {
                sponsorBlockJob = scope.launch {
                    val skippedUuids = mutableSetOf<String>()
                    while (isActive) {
                        if (player.isPlaying) {
                            val currentPos = player.currentPosition
                            val segmentToSkip = segments.find { segment ->
                                currentPos >= segment.startMs && currentPos < (segment.endMs - 300) && !skippedUuids.contains(segment.uuid)
                            }

                            if (segmentToSkip != null) {
                                skippedUuids.add(segmentToSkip.uuid)
                                withContext(Dispatchers.Main) {
                                    player.seekTo(segmentToSkip.endMs)
                                    if (config.showToast) {
                                        val messageRes = when (segmentToSkip.category) {
                                            "music_offtopic" -> R.string.sponsorblock_skipped_non_music
                                            "sponsor" -> R.string.sponsorblock_skipped_sponsor
                                            "selfpromo" -> R.string.sponsorblock_skipped_selfpromo
                                            "interaction" -> R.string.sponsorblock_skipped_interaction
                                            "intro" -> R.string.sponsorblock_skipped_intro
                                            "outro" -> R.string.sponsorblock_skipped_outro
                                            else -> R.string.sponsorblock_skipped_segment
                                        }
                                        Toast.makeText(this@MusicService, getString(messageRes), Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                        val loopDelay = if (player.isPlaying) 250L else 1000L
                        delay(loopDelay)
                    }
                }
            }
        }
        // end of sponser block

        combine(
            currentFormat,
            dataStore.data
                .map { it[AudioNormalizationKey] ?: true }
                .distinctUntilChanged(),
        ) { format, normalizeAudio ->
            format to normalizeAudio
        }.collectLatest(scope) { (format, normalizeAudio) -> setupLoudnessEnhancer()}

        combine(
            dataStore.data.map { it[AudioOffload] ?: false },
            dataStore.data.map { it[CrossfadeEnabledKey] ?: DefaultCrossfadeEnabled }
        ) { offloadPref, crossfadeEnabled ->
             // Force disable offload if crossfade is enabled to prevent volume ramp issues
             if (crossfadeEnabled) false else offloadPref
        }.distinctUntilChanged()
        .collectLatest(scope) { useOffload ->
             player.setOffloadEnabled(useOffload)
        }

        dataStore.data
            .map { it[DiscordTokenKey] to (it[EnableDiscordRPCKey] ?: true) }
            .debounce(300)
            .distinctUntilChanged()
            .collect(scope) { (key, enabled) ->
                if (discordRpc?.isRpcRunning() == true) {
                    discordRpc?.closeRPC()
                }
                discordRpc = null
                if (key != null && enabled) {
                    discordRpc = DiscordRPC(this, key)
                    if (player.playbackState == Player.STATE_READY && player.playWhenReady) {
                        currentSong.value?.let {
                            updateDiscordRPC(it, true)
                        }
                    }
                }
            }

        // Watch all Discord customization preferences
        dataStore.data
            .map {
                listOf(
                    it[DiscordUseDetailsKey],
                    it[DiscordAdvancedModeKey],
                    it[DiscordStatusKey],
                    it[DiscordButton1TextKey],
                    it[DiscordButton1VisibleKey],
                    it[DiscordButton2TextKey],
                    it[DiscordButton2VisibleKey],
                    it[DiscordActivityTypeKey],
                    it[DiscordActivityNameKey]
                )
            }
            .debounce(300)
            .distinctUntilChanged()
            .collect(scope) {
                if (player.playbackState == Player.STATE_READY) {
                    currentSong.value?.let { song ->
                        updateDiscordRPC(song, true)
                    }
                }
            }

        dataStore.data
            .map { it[EnableLastFMScrobblingKey] ?: false }
            .debounce(300)
            .distinctUntilChanged()
            .collect(scope) { enabled ->
                if (enabled && scrobbleManager == null) {
                    val delayPercent = dataStore.get(ScrobbleDelayPercentKey, LastFM.DEFAULT_SCROBBLE_DELAY_PERCENT)
                    val minSongDuration = dataStore.get(ScrobbleMinSongDurationKey, LastFM.DEFAULT_SCROBBLE_MIN_SONG_DURATION)
                    val delaySeconds = dataStore.get(ScrobbleDelaySecondsKey, LastFM.DEFAULT_SCROBBLE_DELAY_SECONDS)
                    scrobbleManager = ScrobbleManager(
                        scope,
                        minSongDuration = minSongDuration,
                        scrobbleDelayPercent = delayPercent,
                        scrobbleDelaySeconds = delaySeconds
                    )
                    scrobbleManager?.useNowPlaying = dataStore.get(LastFMUseNowPlaying, false)
                } else if (!enabled && scrobbleManager != null) {
                    scrobbleManager?.destroy()
                    scrobbleManager = null
                }
            }

        dataStore.data
            .map { it[LastFMUseNowPlaying] ?: false }
            .distinctUntilChanged()
            .collectLatest(scope) {
                scrobbleManager?.useNowPlaying = it
            }

        dataStore.data
            .map { prefs ->
                Triple(
                    prefs[ScrobbleDelayPercentKey] ?: LastFM.DEFAULT_SCROBBLE_DELAY_PERCENT,
                    prefs[ScrobbleMinSongDurationKey] ?: LastFM.DEFAULT_SCROBBLE_MIN_SONG_DURATION,
                    prefs[ScrobbleDelaySecondsKey] ?: LastFM.DEFAULT_SCROBBLE_DELAY_SECONDS
                )
            }
            .distinctUntilChanged()
            .collect(scope) { (delayPercent, minSongDuration, delaySeconds) ->
                scrobbleManager?.let {
                    it.scrobbleDelayPercent = delayPercent
                    it.minSongDuration = minSongDuration
                    it.scrobbleDelaySeconds = delaySeconds
                }
            }

        combine(
            dataStore.data.map { prefs ->
                CrossfadeSettings(
                    enabled = prefs[CrossfadeEnabledKey] ?: DefaultCrossfadeEnabled,
                    durationSeconds = prefs[CrossfadeDurationKey] ?: 5f,
                    gapless = prefs[CrossfadeGaplessKey] ?: false,
                    manualSkip = prefs[CrossfadeManualSkipKey] ?: false,
                    curve = prefs[CrossfadeCurveKey].toEnum(CrossfadeCurve.EASE_OUT_QUAD),
                )
            },
            listenTogetherManager.roomState
        ) { settings, roomState ->
            // Disable crossfade if user is in a listen together room
            settings.copy(enabled = settings.enabled && roomState == null)
        }
            .distinctUntilChanged()
            .collect(scope) { settings ->
                val duration = settings.durationSeconds.takeIf { it.isFinite() }?.coerceIn(0f, 15f) ?: 0f
                cancelCrossfadePreparation()
                crossfadeEnabled = settings.enabled
                crossfadeDurationMs = (duration * 1000f).toLong()
                crossfadeGapless = settings.gapless
                crossfadeManualSkipEnabled = settings.manualSkip
                crossfadeCurve = settings.curve
                if (!crossfadeEnabled || crossfadeDurationMs == 0L) finishCrossfade()
                scheduleCrossfade()
            }

        if (dataStore.get(PersistentQueueKey, true)) {
            restoringQueue = true
            try {
                queuePersistence.readState()?.let { saved ->
                    playerVolume.value = saved.volume.takeIf { it.isFinite() }?.coerceIn(0f, 1f) ?: playerVolume.value
                }
                queuePersistence.readAutomix()?.let { saved ->
                    automixItems.value = saved.items.map { it.toMediaItem() }
                }
                queuePersistence.readQueue()?.let { saved ->
                    savedPlaybackPosition(saved)?.let { restored ->
                        adoptSavedQueue(saved.queue)
                        val media = restored.media
                        player.setMediaItems(media.mediaItems, media.startIndex, media.startPositionMs)
                        applyRestoredShuffle(restored.shuffle)
                        player.prepare()
                        refreshChipsForCurrentSong()
                    }
                }
            } finally {
                restoringQueue = false
            }
        }

        // Save queue periodically (30s) to preserve state without causing constant flash I/O churn
        scope.launch {
            while (isActive) {
                delay(30.seconds)
                if (dataStore.get(PersistentQueueKey, true) && (player.isPlaying || player.mediaItemCount > 0)) {
                    saveQueueToDisk()
                }
            }
        }
    }

    private data class RestoredSessionQueue(
        val media: MediaSession.MediaItemsWithStartPosition,
        val shuffle: IntArray?,
    )

    private fun savedPlaybackPosition(persisted: QueuePersistence.SavedQueue): RestoredSessionQueue? {
        val saved = persisted.queue
        val hideExplicit = dataStore.get(HideExplicitKey, false)
        val hideVideos = dataStore.get(HideVideoSongsKey, false)
        val deduplicate = dataStore.get(PreventDuplicateTracksInQueueKey, true)
        val seen = mutableSetOf<String>()
        val kept = saved.items.mapIndexedNotNull { index, metadata ->
            val item = metadata.toMediaItem()
            if (metadata.id.isBlank() ||
                listOf(item).filterExplicit(hideExplicit).filterVideoSongs(hideVideos).isEmpty() ||
                (deduplicate && !seen.add(item.mediaId))
            ) null else index to item
        }
        if (kept.isEmpty()) return null
        val savedId = saved.items.getOrNull(saved.mediaItemIndex)?.id
        val exact = kept.indexOfFirst { it.first == saved.mediaItemIndex }
        val sameSong = if (deduplicate) kept.indexOfFirst { it.second.mediaId == savedId } else -1
        val index = maxOf(exact, sameSong).takeIf { it >= 0 }
            ?: restoredQueueIndex(saved.mediaItemIndex, kept.map { it.first })
        val position = if (kept[index].second.mediaId == savedId) saved.position.coerceAtLeast(0) else 0L
        return RestoredSessionQueue(
            MediaSession.MediaItemsWithStartPosition(kept.map { it.second }, index, position),
            remapSavedShuffleOrder(persisted.shuffleOrder, kept.map { it.first }),
        )
    }

    private fun adoptSavedQueue(saved: PersistQueue) {
        currentQueue = saved.toQueue()
        queueTitle = saved.title
        originalQueueSize = saved.items.size
        queuePreviouslyAvailable = true
    }

    internal suspend fun resumeSavedPlayback(): MediaSession.MediaItemsWithStartPosition {
        check(dataStore.get(PersistentQueueKey, true)) { "Queue persistence is disabled" }
        val saved = withContext(Dispatchers.IO) { queuePersistence.readQueue() }
        check(player.mediaItemCount == 0 && queueLoadJob?.isActive != true) { "Playback queue already being restored" }
        val persisted = checkNotNull(saved) { "No saved playback queue" }
        val restored = savedPlaybackPosition(persisted) ?: error("The saved queue is empty")
        adoptSavedQueue(persisted.queue)
        pendingResumeShuffle = restored.takeIf { it.shuffle != null && player.shuffleModeEnabled }
        return restored.media
    }

    private fun applyRestoredShuffle(order: IntArray?) {
        if (order != null && player.shuffleModeEnabled) {
            player.setShuffleOrder(DefaultShuffleOrder(order, System.currentTimeMillis()))
        }
    }

    override fun onTimelineChanged(timeline: Timeline, reason: Int) {
        if (reason != Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED) return
        val pending = pendingResumeShuffle ?: return
        pendingResumeShuffle = null
        if (player.mediaItems.toList() == pending.media.mediaItems) applyRestoredShuffle(pending.shuffle)
    }

    private fun createExoPlayer(forCrossfade: Boolean = false): ExoPlayer {
        val eqProcessor = CustomEqualizerAudioProcessor()

        val silenceProcessor = SilenceDetectorAudioProcessor { handleLongSilenceDetected() }

        // Set initial state
        runBlocking {
            val skipSilence = dataStore.get(SkipSilenceKey, false)
            val instantSkip = dataStore.get(SkipSilenceInstantKey, false)
            silenceProcessor.instantModeEnabled = !forCrossfade && skipSilence && instantSkip
        }

        val player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(createMediaSourceFactory())
            .setRenderersFactory(createRenderersFactory(eqProcessor, silenceProcessor))
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                false,
            )
            .setSeekBackIncrementMs(5000)
            .setSeekForwardIncrementMs(5000)
            .setDeviceVolumeControlEnabled(true)
            .build()

        player.volume = 0f
        playerSilenceProcessors[player] = silenceProcessor
        playerEqualizerProcessors[player] = eqProcessor
        equalizerService.addAudioProcessor(eqProcessor)
        val offload = dataStore.get(AudioOffload, false)
        val crossfade = dataStore.get(CrossfadeEnabledKey, DefaultCrossfadeEnabled)
        player.setOffloadEnabled(!forCrossfade && !crossfade && offload)
        player.skipSilenceEnabled = dataStore.get(SkipSilenceKey, false)
        if (forCrossfade) {
            preferredDeviceId?.let { id ->
                audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS).firstOrNull { it.id == id }
                    ?.let(player::setPreferredAudioDevice)
            }
        } else {
            attachPlaybackAnalytics(player)
        }
        return player
    }

    private fun attachPlaybackAnalytics(player: ExoPlayer): PlaybackHistoryRecorder {
        lateinit var statsListener: PlaybackStatsListener
        val history = PlaybackHistoryRecorder(
            database,
            isEnabled = { !dataStore.get(PauseListenHistoryKey, false) },
            activePlayTime = { statsListener.playbackStats?.totalPlayTimeMs ?: 0L },
        )
        statsListener = PlaybackStatsListener(false) { eventTime, stats ->
            history.finish(eventTime, stats.totalPlayTimeMs)
            onPlaybackStatsReady(eventTime, stats)
        }
        player.addAnalyticsListener(statsListener)
        player.addAnalyticsListener(history)
        return history
    }

    private fun applyPlaybackVolume() {
        if (!::player.isInitialized || !::playerVolume.isInitialized) return
        val volume = playbackOutputVolume(
            playerVolume.value, isMuted.value, hasAudioFocus,
            ducking = lastAudioFocusState == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK,
        )
        val outgoing = fadingPlayer
        if (outgoing == null) {
            player.volume = volume
        } else {
            val (out, incoming) = crossfadeGains(activeCrossfadeCurve, crossfadeProgress)
            outgoing.volume = volume * out
            player.volume = volume * incoming
        }
    }

    private fun setupAudioFocusRequest() {
        audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(
                android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setOnAudioFocusChangeListener { focusChange ->
                handleAudioFocusChange(focusChange)
            }
            .build()
    }

    private fun handleAudioFocusChange(focusChange: Int) {
        when (focusChange) {

            AudioManager.AUDIOFOCUS_GAIN,
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT,
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK -> {
                hasAudioFocus = true

                val resume = resumeAfterFocusLoss
                resumeAfterFocusLoss = false
                lastAudioFocusState = focusChange
                applyPlaybackVolume()
                if (resume && castConnectionHandler?.isCasting?.value != true) player.play()
                updateOutgoingPlayback()
                scheduleCrossfade()
            }

            AudioManager.AUDIOFOCUS_LOSS -> {
                resumeAfterFocusLoss = false
                lastAudioFocusState = focusChange
                player.pause()
                fadingPlayer?.pause()
                releaseCrossfadePlayers()
                abandonAudioFocus()
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                hasAudioFocus = false
                resumeAfterFocusLoss = resumeAfterFocusLoss || player.playWhenReady
                lastAudioFocusState = focusChange
                applyPlaybackVolume()
                player.pause()
                fadingPlayer?.pause()
                overlapPlaying.value = false
                cancelCrossfadePreparation()
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                lastAudioFocusState = focusChange
                applyPlaybackVolume()
            }

        }
    }

    private fun requestAudioFocus(): Boolean {
        if (hasAudioFocus) return true
        if (lastAudioFocusState == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) return false

        audioFocusRequest?.let { request ->
            val result = audioManager.requestAudioFocus(request)
            hasAudioFocus = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            if (hasAudioFocus) lastAudioFocusState = AudioManager.AUDIOFOCUS_GAIN
            applyPlaybackVolume()
            return hasAudioFocus
        }
        return false
    }

    private fun abandonAudioFocus() {
        audioFocusRequest?.let(audioManager::abandonAudioFocusRequest)
        hasAudioFocus = false
        resumeAfterFocusLoss = false
        lastAudioFocusState = AudioManager.AUDIOFOCUS_NONE
        applyPlaybackVolume()
    }

    fun hasAudioFocusForPlayback(): Boolean {
        return hasAudioFocus
    }

    private fun waitOnNetworkError() {
        if (waitingForNetworkConnection.value) return

        // Check if we've exceeded max retry attempts
        if (retryCount >= MAX_RETRY_COUNT) {
            Timber.tag(TAG).w("Max retry count ($MAX_RETRY_COUNT) reached, stopping playback")
            stopOnError()
            retryCount = 0
            return
        }

        waitingForNetworkConnection.value = true

        // Start a retry timer with exponential backoff
        retryJob?.cancel()
        retryJob = scope.launch {
            // Exponential backoff: 3s, 6s, 12s, 24s... max 30s
            val delayMs = minOf(3000L * (1 shl retryCount), 30000L)
            Timber.tag(TAG).d("Waiting ${delayMs}ms before retry attempt ${retryCount + 1}/$MAX_RETRY_COUNT")
            delay(delayMs)

            if (isNetworkConnected.value && waitingForNetworkConnection.value) {
                retryCount++
                triggerRetry()
            }
        }
    }

    private fun triggerRetry() {
        waitingForNetworkConnection.value = false
        retryJob?.cancel()

        if (player.currentMediaItem != null) {
            // After 3+ failed retries, try to refresh the stream URL by seeking to current position
            // This forces ExoPlayer to re-resolve the data source and get a fresh URL
            if (retryCount > 3) {
                Timber.tag(TAG).d("Retry count > 3, attempting to refresh stream URL")
                val currentPosition = player.currentPosition
                player.seekTo(player.currentMediaItemIndex, currentPosition)
            }
            player.prepare()
            // Don't call play() here - let the player auto-resume via playWhenReady
            // This avoids stealing audio focus during retry attempts
        }
    }

    private fun skipOnError() {
        /**
         * Auto skip to the next media item on error.
         *
         * To prevent a "runaway diesel engine" scenario, force the user to take action after
         * too many errors come up too quickly. Pause to show player "stopped" state
         */
        consecutivePlaybackErr += 2
        val nextWindowIndex = player.nextMediaItemIndex

        if (consecutivePlaybackErr <= MAX_CONSECUTIVE_ERR && nextWindowIndex != C.INDEX_UNSET) {
            player.seekTo(nextWindowIndex, C.TIME_UNSET)
            player.prepare()
            // Don't start local playback if casting
            if (castConnectionHandler?.isCasting?.value != true) {
                player.play()
            }
            return
        }

        player.pause()
        consecutivePlaybackErr = 0
    }

    private fun stopOnError() {
        player.pause()
    }

    private fun updateNotification() {
        if (!::mediaSession.isInitialized) return
        val currentId = player.currentMediaItem?.mediaId
        val song = currentSong.value?.takeIf { it.id == currentId }
        val buttons = if (currentId == null) emptyList() else buildList {
            if (song != null) {
                val liked = song.song.liked
                add(
                    CommandButton.Builder()
                        .setDisplayName(getString(if (liked) R.string.action_remove_like else R.string.action_like))
                        .setIconResId(if (liked) R.drawable.ic_heart else R.drawable.ic_heart_outline)
                        .setSessionCommand(CommandToggleLike)
                        .setSlots(CommandButton.SLOT_OVERFLOW)
                        .build()
                )
            }

            val (repeatLabel, repeatIcon) = when (player.repeatMode) {
                REPEAT_MODE_ONE -> R.string.repeat_mode_one to R.drawable.repeat_one_on
                REPEAT_MODE_ALL -> R.string.repeat_mode_all to R.drawable.repeat_on
                else -> R.string.repeat_mode_off to R.drawable.repeat
            }
            add(
                CommandButton.Builder()
                    .setDisplayName(getString(repeatLabel))
                    .setIconResId(repeatIcon)
                    .setSessionCommand(CommandToggleRepeatMode)
                    .setSlots(CommandButton.SLOT_OVERFLOW)
                    .build()
            )
            val shuffled = player.shuffleModeEnabled
            add(
                CommandButton.Builder()
                    .setDisplayName(getString(if (shuffled) R.string.action_shuffle_off else R.string.action_shuffle_on))
                    .setIconResId(if (shuffled) R.drawable.shuffle_on else R.drawable.shuffle)
                    .setSessionCommand(CommandToggleShuffle)
                    .setSlots(CommandButton.SLOT_OVERFLOW)
                    .build()
            )

            if (song != null) {
                add(
                    CommandButton.Builder()
                        .setDisplayName(getString(R.string.start_radio))
                        .setIconResId(R.drawable.radio)
                        .setSessionCommand(CommandToggleStartRadio)
                        .setSlots(CommandButton.SLOT_OVERFLOW)
                        .build()
                )
            }
        }
        if (buttons != mediaButtons) {
            mediaButtons = buttons
            mediaSession.setMediaButtonPreferences(buttons)
        }
    }

    private suspend fun recoverSong(
        mediaId: String,
        playbackData: InnerTubeXPlayer.PlaybackData? = null
    ) {
        val song = database.song(mediaId).first()
        val mediaMetadata = withContext(Dispatchers.Main) {
            player.findMediaItemById(mediaId)?.metadata
        } ?: playbackData?.videoDetails?.let { details ->
            com.music.mkr.models.MediaMetadata(
                id = mediaId,
                title = details.title.orEmpty(),
                artists = listOf(com.music.mkr.models.MediaMetadata.Artist(id = details.channelId, name = details.author.orEmpty())),
                duration = details.lengthSeconds.toIntOrNull() ?: -1,
                thumbnailUrl = details.thumbnail.thumbnails.lastOrNull()?.url
            )
        } ?: return
        val duration = song?.song?.duration?.takeIf { it != -1 }
            ?: mediaMetadata.duration.takeIf { it != -1 }
            ?: (playbackData?.videoDetails ?: YTPlayerUtils.playerResponseForMetadata(mediaId)
                .getOrNull()?.videoDetails)?.lengthSeconds?.toInt()
            ?: -1
        database.query {
            val latestSong = getSongByIdBlocking(mediaId)
            if (latestSong == null) {
                insert(mediaMetadata.copy(duration = duration))
            } else {
                var updatedSong = latestSong.song
                if (latestSong.song.duration == -1) {
                    updatedSong = updatedSong.copy(duration = duration)
                }
                // Update isVideo flag if it's different from the current value
                if (latestSong.song.isVideo != mediaMetadata.isVideoSong) {
                    updatedSong = updatedSong.copy(isVideo = mediaMetadata.isVideoSong)
                }
                if (updatedSong != latestSong.song) {
                    update(updatedSong)
                }
            }
        }
        if (!database.hasRelatedSongs(mediaId)) {
            val relatedEndpoint =
                YouTube.next(WatchEndpoint(videoId = mediaId)).getOrNull()?.relatedEndpoint
                    ?: return
            val relatedPage = YouTube.related(relatedEndpoint).getOrNull() ?: return
            database.query {
                relatedPage.songs
                    .map(SongItem::toMediaMetadata)
                    .onEach(::insert)
                    .map {
                        RelatedSongMap(
                            songId = mediaId,
                            relatedSongId = it.id
                        )
                    }
                    .forEach(::insert)
            }
        }
    }

    fun playQueue(
        queue: Queue,
        playWhenReady: Boolean = true,
    ) {
        if (!scope.isActive) return
        queueLoadJob?.cancel()

        // Safety Check : Ensuring player is initilized
        if (!playerInitialized.value) {
            Timber.tag(TAG).w("playQueue called before player initialization, queuing request")
            queueLoadJob = scope.launch {
                playerInitialized.first { it }
                playQueue(queue, playWhenReady)
            }
            return
        }

        val interrupted = fadingPlayer != null || pendingQueueSwitch ||
            (crossfadePreparation != null && automaticFade == null)
        releaseCrossfadePlayers()
        val outgoing = if (playWhenReady && crossfadeManualSkipEnabled && !interrupted) currentCrossfadeSource() else null
        pendingQueueSwitch = outgoing != null
        val persistShuffleAcrossQueues = dataStore.get(PersistentShuffleAcrossQueuesKey, false)
        fun selectQueue() {
            pendingQueueSwitch = false
            currentQueue = queue
            queueTitle = null
            radioChipsJob?.cancel()
            radioChipsJob = null
            radioChips.value = emptyList()
            originalQueueSize = 0
        }
        fun applyQueueStatus(status: Queue.Status) {
            if (status.title != null) queueTitle = status.title
            originalQueueSize = status.items.size
            queue.radioChips?.let { chips ->
                radioChipsJob = scope.launch { chips.collect { radioChips.value = it } }
            }
        }

        val preload = queue.preloadItem?.toMediaItem()
        fun playPreload() {
            if (preload != null) {
                player.setMediaItem(preload)
                player.prepare()
                player.playWhenReady = playWhenReady
            }
        }
        if (outgoing == null) {
            selectQueue()
            if (!persistShuffleAcrossQueues) player.shuffleModeEnabled = false
            playPreload()
        }
        val loading = scope.launch(SilentHandler, start = CoroutineStart.LAZY) {
            if (outgoing != null && preload != null &&
                !crossfadeIntoQueue(outgoing, listOf(preload), 0, 0, persistShuffleAcrossQueues, ::selectQueue)
            ) {
                selectQueue()
                if (!persistShuffleAcrossQueues) player.shuffleModeEnabled = false
                playPreload()
            }
            val initialStatus =
                withContext(Dispatchers.IO) {
                    queue.getInitialStatus()
                        .filterExplicit(dataStore.get(HideExplicitKey, false))
                        .filterVideoSongs(dataStore.get(HideVideoSongsKey, false))
                        .let { status ->
                            if (dataStore.get(PreventDuplicateTracksInQueueKey, true)) {
                                status.withItems(status.items.distinctBy { it.mediaId })
                            } else status
                        }
                }
            ensureActive()
            if (queue.preloadItem != null && player.playbackState == STATE_IDLE) return@launch
            val queueItems = initialStatus.items
            if (queueItems.isEmpty()) {
                if (!pendingQueueSwitch) applyQueueStatus(initialStatus)
                return@launch
            }
            if (outgoing != null && preload == null) {
                if (crossfadeIntoQueue(
                        outgoing, queueItems, initialStatus.mediaItemIndex, initialStatus.position,
                        persistShuffleAcrossQueues,
                    ) {
                        selectQueue()
                        applyQueueStatus(initialStatus)
                    }
                ) return@launch
                selectQueue()
                if (!persistShuffleAcrossQueues) player.shuffleModeEnabled = false
            }
            applyQueueStatus(initialStatus)

            if (queue.preloadItem != null) {
                val actualIndex = queueItems.indexOfFirst { it.mediaId == queue.preloadItem!!.id }
                val targetIndex = if (actualIndex != -1) actualIndex else
                    initialStatus.mediaItemIndex.coerceIn(0, queueItems.size)

                if (actualIndex == -1) {
                    player.addMediaItems(0, queueItems.take(targetIndex))
                    player.addMediaItems(queueItems.drop(targetIndex))
                } else {
                    player.addMediaItems(
                        0,
                        queueItems.subList(0, targetIndex)
                    )
                    player.addMediaItems(
                        queueItems.subList(
                            targetIndex + 1,
                            queueItems.size
                        )
                    )
                    player.replaceMediaItem(targetIndex, queueItems[targetIndex])
                }
            } else {
                player.setMediaItems(
                    queueItems,
                    initialStatus.mediaItemIndex.coerceIn(0, queueItems.lastIndex),
                    initialStatus.position,
                )
                player.prepare()
                player.playWhenReady = playWhenReady
            }

            // Rebuild shuffle order if shuffle is enabled
            if (player.shuffleModeEnabled) {
                val shufflePlaylistFirst = dataStore.get(ShufflePlaylistFirstKey, false)
                applyShuffleOrder(player.currentMediaItemIndex, player.mediaItemCount, shufflePlaylistFirst)
            }
        }
        queueLoadJob = loading
        loading.invokeOnCompletion {
            scope.launch {
                if (queueLoadJob === loading) {
                    pendingQueueSwitch = false
                    scheduleCrossfade()
                }
            }
        }
        loading.start()
    }

    fun startRadioSeamlessly() {
        // Safety Check: Ensure Player is initilized
        if (!playerInitialized.value) {
            Timber.tag(TAG).w("startRadioSeamlessly called before player initialization")
            return
        }

        val currentMediaMetadata = player.currentMetadata ?: return

        val currentIndex = player.currentMediaItemIndex
        val currentMediaId = currentMediaMetadata.id

        scope.launch(SilentHandler) {
            // Use simple videoId to let YouTube personalize recommendations
            val radioQueue = YouTubeQueue(
                endpoint = WatchEndpoint(
                    videoId = currentMediaId,
                    playlistId = "RDAMVM$currentMediaId"
                )
            )

            try {
                val initialStatus = withContext(Dispatchers.IO) {
                    radioQueue.getInitialStatus()
                        .filterExplicit(dataStore.get(HideExplicitKey, false))
                        .filterVideoSongs(dataStore.get(HideVideoSongsKey, false))
                }

                if (initialStatus.title != null) {
                    queueTitle = initialStatus.title
                }

                if (radioQueue.radioChips != null) {
                    radioChipsJob?.cancel()
                    radioChipsJob = scope.launch {
                        radioQueue.radioChips!!.collect {
                            radioChips.value = it
                        }
                    }
                }

                val preventDuplicates = dataStore.get(PreventDuplicateTracksInQueueKey, true)
                val existingQueueIds = if (preventDuplicates) {
                    (0..currentIndex).map { player.getMediaItemAt(it).mediaId }.toSet() + currentMediaId
                } else {
                    setOf(currentMediaId)
                }

                // Filter radio items to exclude current media item and duplicates
                val radioItems = initialStatus.items
                    .filter { item -> item.mediaId !in existingQueueIds }
                    .let { if (preventDuplicates) it.distinctBy { item -> item.mediaId } else it }

                if (radioItems.isNotEmpty()) {
                    val itemCount = player.mediaItemCount

                    if (itemCount > currentIndex + 1) {
                        player.removeMediaItems(currentIndex + 1, itemCount)
                    }

                    player.addMediaItems(currentIndex + 1, radioItems)
                    if (player.shuffleModeEnabled) {
                        val shufflePlaylistFirst = dataStore.get(ShufflePlaylistFirstKey, false)
                        applyShuffleOrder(player.currentMediaItemIndex, player.mediaItemCount, shufflePlaylistFirst)
                    }
                }

                currentQueue = radioQueue
            } catch (e: Exception) {
                // Fallback: try with related endpoint
                try {
                    val nextResult = withContext(Dispatchers.IO) {
                        YouTube.next(WatchEndpoint(videoId = currentMediaId)).getOrNull()
                    }
                    nextResult?.relatedEndpoint?.let { relatedEndpoint ->
                        val relatedPage = withContext(Dispatchers.IO) {
                            YouTube.related(relatedEndpoint).getOrNull()
                        }
                        relatedPage?.songs?.let { songs ->
                            val preventDuplicates = dataStore.get(PreventDuplicateTracksInQueueKey, true)
                            val existingQueueIds = if (preventDuplicates) {
                                (0..currentIndex).map { player.getMediaItemAt(it).mediaId }.toSet() + currentMediaId
                            } else {
                                setOf(currentMediaId)
                            }

                            val radioItems = songs
                                .filter { it.id !in existingQueueIds }
                                .map { it.toMediaItem() }
                                .filterExplicit(dataStore.get(HideExplicitKey, false))
                                .filterVideoSongs(dataStore.get(HideVideoSongsKey, false))
                                .let { if (preventDuplicates) it.distinctBy { item -> item.mediaId } else it }

                            if (radioItems.isNotEmpty()) {
                                val itemCount = player.mediaItemCount
                                if (itemCount > currentIndex + 1) {
                                    player.removeMediaItems(currentIndex + 1, itemCount)
                                }
                                player.addMediaItems(currentIndex + 1, radioItems)
                                if (player.shuffleModeEnabled) {
                                    val shufflePlaylistFirst = dataStore.get(ShufflePlaylistFirstKey, false)
                                    applyShuffleOrder(player.currentMediaItemIndex, player.mediaItemCount, shufflePlaylistFirst)
                                }
                            }
                        }
                    }
                } catch (_: Exception) {
                    // Silent fail
                }
            }
        }
    }

    fun applyRadioChip(chip: RadioChip) {
        val currentMediaMetadata = player.currentMetadata ?: return
        val currentMediaId = currentMediaMetadata.id
        val currentIndex = player.currentMediaItemIndex

        val endpoint = (currentQueue as? YouTubeQueue)?.endpoint?.copy(params = chip.params)
            ?: com.music.innertube.models.WatchEndpoint(
                videoId = currentMediaId,
                playlistId = "RDAMVM$currentMediaId",
                params = chip.params
            )

        scope.launch(SilentHandler) {
            val radioQueue = YouTubeQueue(
                endpoint = endpoint
            )

            try {
                val initialStatus = withContext(Dispatchers.IO) {
                    radioQueue.getInitialStatus()
                        .filterExplicit(dataStore.get(HideExplicitKey, false))
                        .filterVideoSongs(dataStore.get(HideVideoSongsKey, false))
                }

                if (initialStatus.title != null) {
                    queueTitle = initialStatus.title
                }

                if (radioQueue.radioChips != null) {
                    radioChipsJob?.cancel()
                    radioChipsJob = scope.launch {
                        radioQueue.radioChips!!.collect {
                            radioChips.value = it.map { rc -> 
                                if (rc.title == chip.title) rc.copy(isSelected = true) else rc.copy(isSelected = false)
                            }
                        }
                    }
                }

                val preventDuplicates = dataStore.get(PreventDuplicateTracksInQueueKey, true)
                val existingQueueIds = if (preventDuplicates) {
                    (0..currentIndex).map { player.getMediaItemAt(it).mediaId }.toSet() + currentMediaId
                } else {
                    setOf(currentMediaId)
                }

                // Filter radio items to exclude current media item and duplicates
                val radioItems = initialStatus.items
                    .filter { item -> item.mediaId !in existingQueueIds }
                    .let { if (preventDuplicates) it.distinctBy { item -> item.mediaId } else it }

                if (radioItems.isNotEmpty()) {
                    val itemCount = player.mediaItemCount

                    if (itemCount > currentIndex + 1) {
                        player.removeMediaItems(currentIndex + 1, itemCount)
                    }

                    player.addMediaItems(currentIndex + 1, radioItems)
                    if (player.shuffleModeEnabled) {
                        val shufflePlaylistFirst = dataStore.get(ShufflePlaylistFirstKey, false)
                        applyShuffleOrder(player.currentMediaItemIndex, player.mediaItemCount, shufflePlaylistFirst)
                    }
                }

                this@MusicService.currentQueue = radioQueue
            } catch (e: Exception) {
                timber.log.Timber.tag(TAG).e(e, "Failed to apply radio chip")
            }
        }
    }

    fun refreshChipsForCurrentSong() {
        val currentQueue = currentQueue // Use smart cast via local variable
        if (currentQueue != null && currentQueue !is YouTubeQueue) {
            val isRestoredRadio = (currentQueue as? ListQueue)?.isRadio == true
            if (!isRestoredRadio) {
                radioChips.value = emptyList()
                return
            }
        }
        
        val currentMediaMetadata = player.currentMetadata ?: return
        val currentMediaId = currentMediaMetadata.id

        // Only auto-refresh when in a radio-style queue (RDAMVM or similar)
        // so we don't override chips for a chip-filtered queue the user selected
        scope.launch(SilentHandler) {
            val radioQueue = YouTubeQueue(
                endpoint = WatchEndpoint(
                    videoId = currentMediaId,
                    playlistId = "RDAMVM$currentMediaId"
                )
            )
            try {
                var nextResult = withContext(Dispatchers.IO) {
                    com.music.innertube.YouTube.next(
                        radioQueue.endpoint
                    ).getOrNull()
                }

                if (nextResult == null || nextResult.radioChips.isEmpty()) {
                    nextResult = withContext(Dispatchers.IO) {
                        com.music.innertube.YouTube.next(
                            WatchEndpoint(videoId = currentMediaId)
                        ).getOrNull()
                    }
                }

                if (nextResult != null && nextResult.radioChips.isNotEmpty()) {
                    radioChipsJob?.cancel()
                    // We directly update the chips' value from the nextResult
                    // no need to collect the dummy radioQueue stateflow
                    radioChips.value = nextResult.radioChips
                    timber.log.Timber.tag("Chippy").d("refreshChipsForCurrentSong: pushed ${nextResult.radioChips.size} chips for $currentMediaId")
                } else if (currentQueue == null) {
                    // No chips from API and no existing radio queue - clear chips
                    radioChips.value = emptyList()
                }
            } catch (_: Exception) {
                // Silently fail — chips are optional UX enhancement
            }
        }
    }

    fun getAutomixAlbum(albumId: String) {
        scope.launch(SilentHandler) {
            YouTube
                .album(albumId)
                .onSuccess {
                    getAutomix(it.album.playlistId)
                }
        }
    }

    fun getAutomix(playlistId: String) {
        if (dataStore.get(SimilarContent, true) &&
            !(dataStore.get(DisableLoadMoreWhenRepeatAllKey, false) && player.repeatMode == REPEAT_MODE_ALL)) {
            scope.launch(SilentHandler) {
                try {
                    // Try primary method
                    YouTube.next(WatchEndpoint(playlistId = playlistId))
                        .onSuccess { firstResult ->
                            YouTube.next(WatchEndpoint(playlistId = firstResult.endpoint.playlistId))
                                .onSuccess { secondResult ->
                                    automixItems.value = secondResult.items.map { song ->
                                        song.toMediaItem()
                                    }
                                }
                                .onFailure {
                                    // Fallback: use first result items
                                    if (firstResult.items.isNotEmpty()) {
                                        automixItems.value = firstResult.items.map { song ->
                                            song.toMediaItem()
                                        }
                                    }
                                }
                        }
                        .onFailure {
                            // Fallback: try with radio format
                            val currentSong = player.currentMetadata
                            if (currentSong != null) {
                                // Use simple videoId for better personalized recommendations
                                YouTube.next(WatchEndpoint(
                                    videoId = currentSong.id
                                )).onSuccess { radioResult ->
                                    val filteredItems = radioResult.items
                                        .filter { it.id != currentSong.id }
                                        .map { it.toMediaItem() }
                                    if (filteredItems.isNotEmpty()) {
                                        automixItems.value = filteredItems
                                    }
                                }.onFailure {
                                    // Final fallback: try related endpoint
                                    YouTube.next(WatchEndpoint(videoId = currentSong.id)).getOrNull()?.relatedEndpoint?.let { relatedEndpoint ->
                                        YouTube.related(relatedEndpoint).onSuccess { relatedPage ->
                                            val relatedItems = relatedPage.songs
                                                .filter { it.id != currentSong.id }
                                                .map { it.toMediaItem() }
                                            if (relatedItems.isNotEmpty()) {
                                                automixItems.value = relatedItems

                                            }
                                        }
                                    }
                                }
                            }
                        }
                } catch (_: Exception) {
                    // Silent fail
                }
            }
        }
    }

    private var moodRecommendationsJob: Job? = null

    /**
     * Dynamically loads recommendations strictly aligned with the mood, genre, tempo,
     * and artist sphere of the currently playing track.
     */
    fun loadMoodRecommendations(videoId: String) {
        if (!dataStore.get(SimilarContent, true) ||
            (dataStore.get(DisableLoadMoreWhenRepeatAllKey, false) && player.repeatMode == REPEAT_MODE_ALL)) {
            return
        }

        moodRecommendationsJob?.cancel()
        moodRecommendationsJob = scope.launch(Dispatchers.IO + SilentHandler) {
            try {
                val hideExplicit = dataStore.get(HideExplicitKey, false)
                val hideVideoSongs = dataStore.get(HideVideoSongsKey, false)

                // 1. YouTube Radio Endpoint for the exact song:
                // RDAMVM$videoId creates YouTube Music's mood-clustered radio stream
                val radioResult = YouTube.next(WatchEndpoint(
                    videoId = videoId,
                    playlistId = "RDAMVM$videoId"
                )).getOrNull()

                val radioItems = radioResult?.items
                    ?.filter { it.id != videoId }
                    ?.map { it.toMediaItem() }
                    ?.filterExplicit(hideExplicit)
                    ?.filterVideoSongs(hideVideoSongs)
                    ?: emptyList()

                // 2. Related songs endpoint for genre and stylistic counterparts
                val relatedItems = radioResult?.relatedEndpoint?.let { endpoint ->
                    YouTube.related(endpoint).getOrNull()?.songs
                        ?.filter { it.id != videoId }
                        ?.map { it.toMediaItem() }
                        ?.filterExplicit(hideExplicit)
                        ?.filterVideoSongs(hideVideoSongs)
                } ?: emptyList()

                // 3. Combine mood cluster and related items
                val combined = (radioItems + relatedItems).distinctBy { it.mediaId }

                // 4. Boost songs from artists the user listens to frequently in this genre/mood
                val lovedArtists = try {
                    database.mostPlayedArtists(System.currentTimeMillis() - 86400000L * 60, limit = 20).first()
                        .map { it.artist.name.lowercase().trim() }
                        .filter { it.isNotEmpty() }
                        .toSet()
                } catch (_: Exception) {
                    emptySet()
                }

                val resultList = if (lovedArtists.isNotEmpty() && combined.size > 4) {
                    val familiar = combined.filter { item ->
                        val artist = item.mediaMetadata.artist?.toString()?.lowercase() ?: ""
                        lovedArtists.any { it in artist || artist in it }
                    }
                    val discovery = combined.filter { it !in familiar }
                    // Weighted interleaving: 1 familiar artist in this mood : 2 fresh tracks in this mood
                    val interleaved = mutableListOf<MediaItem>()
                    var fIdx = 0
                    var dIdx = 0
                    while (interleaved.size < combined.size && (fIdx < familiar.size || dIdx < discovery.size)) {
                        if (fIdx < familiar.size) interleaved.add(familiar[fIdx++])
                        if (dIdx < discovery.size) interleaved.add(discovery[dIdx++])
                        if (dIdx < discovery.size) interleaved.add(discovery[dIdx++])
                    }
                    if (interleaved.isNotEmpty()) interleaved else combined
                } else {
                    combined
                }

                val preventDuplicates = dataStore.get(PreventDuplicateTracksInQueueKey, true)
                val finalRecommendations = if (preventDuplicates) {
                    val currentQueueIds = (0 until player.mediaItemCount).map { player.getMediaItemAt(it).mediaId }.toSet()
                    resultList.filter { it.mediaId !in currentQueueIds }.distinctBy { it.mediaId }
                } else {
                    resultList
                }

                if (finalRecommendations.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        automixItems.value = finalRecommendations
                    }
                }
            } catch (e: Exception) {
                Timber.tag(TAG).w(e, "Failed to load mood recommendations for videoId: $videoId")
            }
        }
    }

    fun addToQueueAutomix(
        item: MediaItem,
        position: Int,
    ) {
        automixItems.value =
            automixItems.value.toMutableList().apply {
                removeAt(position)
            }
        addToQueue(listOf(item))
    }

    fun playNextAutomix(
        item: MediaItem,
        position: Int,
    ) {
        automixItems.value =
            automixItems.value.toMutableList().apply {
                removeAt(position)
            }
        playNext(listOf(item))
    }

    fun dismissPlayback() {
        queueLoadJob?.cancel()
        retryJob?.cancel()
        waitingForNetworkConnection.value = false
        mediaButtonStartupJob?.cancel()
        castConnectionHandler?.pause()
        automixItems.value = emptyList()
        player.pause()
        player.stop()
        player.clearMediaItems()
        if (dataStore.get(PersistentQueueKey, true)) saveQueueToDisk()
        abandonAudioFocus()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    fun playNext(items: List<MediaItem>) {
        insertPlayNext(items, preserveIdleQueue = false)
    }

    internal fun playNextSearchMatch(item: MediaItem): SearchPlayNextResult {
        if (queueLoadJob?.isActive == true) return SearchPlayNextResult.QueueLoading
        if (dataStore.get(PreventDuplicateTracksInQueueKey, true)) {
            if (player.currentMediaItem?.mediaId == item.mediaId) return SearchPlayNextResult.AlreadyCurrent
            if (player.nextMediaItemIndex != C.INDEX_UNSET &&
                player.getMediaItemAt(player.nextMediaItemIndex).mediaId == item.mediaId
            ) return SearchPlayNextResult.AlreadyNext
        }
        return if (insertPlayNext(listOf(item), preserveIdleQueue = true)) {
            SearchPlayNextResult.Added
        } else SearchPlayNextResult.Failed
    }

    private fun insertPlayNext(items: List<MediaItem>, preserveIdleQueue: Boolean): Boolean {
        val preventDuplicates = dataStore.get(PreventDuplicateTracksInQueueKey, true)
        val distinctItems = if (preventDuplicates) {
            val currentId = player.currentMediaItem?.mediaId
            items.filter { it.mediaId != currentId }.distinctBy { it.mediaId }
        } else items
        if (distinctItems.isEmpty()) return false

        if (player.mediaItemCount == 0 || (!preserveIdleQueue && player.playbackState == STATE_IDLE)) {
            player.setMediaItems(distinctItems)
            player.prepare()
            if (castConnectionHandler?.isCasting?.value != true) player.play()
        } else {
            if (preventDuplicates) {
                val ids = distinctItems.mapTo(hashSetOf()) { it.mediaId }
                val currentIndex = player.currentMediaItemIndex
                for (index in player.mediaItemCount - 1 downTo 0) {
                    if (index != currentIndex && player.getMediaItemAt(index).mediaId in ids) {
                        player.removeMediaItem(index)
                    }
                }
            }

            val insertIndex = player.currentMediaItemIndex + 1
            val order = if (player.shuffleModeEnabled) getCurrentShuffleOrderIndices(player) else null
            player.addMediaItems(insertIndex, distinctItems)
            if (!preserveIdleQueue) player.prepare()
            if (order != null) {
                val nextOrder = shuffleOrderWithPlayNext(order, insertIndex, distinctItems.size)
                player.setShuffleOrder(DefaultShuffleOrder(nextOrder, System.currentTimeMillis()))
            }
        }

        if (dataStore.get(PersistentQueueKey, true)) saveQueueToDisk()
        return true
    }

    private fun isFullyCached(mediaId: String): Boolean =
        playerCache.completeAudioCachedAt(mediaId) != null ||
            downloadCache.completeAudioCachedAt(mediaId) != null

    private suspend fun markAutomaticallyCached(mediaId: String) {
        if (dataStore.get(DisableSongCacheKey, false)) {
            try {
                playerCache.removeResource(mediaId)
            } catch (_: Exception) {}
            return
        }
        if (playerCache.completeAudioCachedAt(mediaId) == null) return
        if (database.song(mediaId).first() == null) recoverSong(mediaId)
        database.query { markSongCached(mediaId, LocalDateTime.now()) }
    }

    fun addToQueue(items: List<MediaItem>) {
        if (player.mediaItemCount == 0 || player.playbackState == STATE_IDLE) {
            playNext(items)
            return
        }

        val preventDuplicates = dataStore.get(PreventDuplicateTracksInQueueKey, true)
        val itemsToAdd = if (preventDuplicates) {
            val existingIds = (0 until player.mediaItemCount).map { player.getMediaItemAt(it).mediaId }.toSet()
            items.filter { it.mediaId !in existingIds }.distinctBy { it.mediaId }
        } else {
            items
        }

        if (itemsToAdd.isEmpty()) return

        player.addMediaItems(itemsToAdd)
        if (player.shuffleModeEnabled) {
            val shufflePlaylistFirst = dataStore.get(ShufflePlaylistFirstKey, false)
            applyShuffleOrder(player.currentMediaItemIndex, player.mediaItemCount, shufflePlaylistFirst)
        }
        player.prepare()
    }

    fun toggleLibrary() {
        scope.launch {
            val songToToggle = currentSong.first()
            songToToggle?.let {
                val isInLibrary = it.song.inLibrary != null
                val token = if (isInLibrary) it.song.libraryRemoveToken else it.song.libraryAddToken

                // Call YouTube API with feedback token if available
                token?.let { feedbackToken ->
                    YouTube.feedback(listOf(feedbackToken))
                }

                // Update local database
                database.query {
                    update(it.song.toggleLibrary())
                }
                currentMediaMetadata.value = player.currentMetadata
            }
        }
    }

    fun toggleLike(targetMetadata: com.music.mkr.models.MediaMetadata? = null) {
        val currentItem = player.currentMediaItem
        val currentItemId = currentItem?.mediaId
        val initialMetadata = targetMetadata
            ?: (if (currentItemId != null && currentMediaMetadata.value?.id == currentItemId) currentMediaMetadata.value else null)
            ?: (if (currentItemId != null && player.currentMetadata?.id == currentItemId) player.currentMetadata else null)
            ?: (if (currentItemId != null) currentItem?.metadata else null)
            ?: currentMediaMetadata.value
            ?: player.currentMetadata

        val fallbackTitle = currentItem?.mediaMetadata?.title?.toString() ?: "Unknown"
        val fallbackArtist = (currentItem?.mediaMetadata?.artist ?: currentItem?.mediaMetadata?.subtitle)?.toString()
        val fallbackDuration = (player.duration.coerceAtLeast(0) / 1000).toInt()
        val fallbackThumb = currentItem?.mediaMetadata?.artworkUri?.toString()

        database.transaction {
            val mediaMetadata = initialMetadata
                ?: currentItemId?.takeIf { it.isNotBlank() }?.let { id ->
                    val fromDb = getSongByIdBlocking(id)
                    fromDb?.toMediaMetadata() ?: com.music.mkr.models.MediaMetadata(
                        id = id,
                        title = fallbackTitle,
                        artists = fallbackArtist?.let { listOf(com.music.mkr.models.MediaMetadata.Artist(id = null, name = it)) } ?: emptyList(),
                        duration = fallbackDuration,
                        thumbnailUrl = fallbackThumb
                    )
                }
                ?: return@transaction

            try {
                val dbSong = getSongByIdBlocking(mediaMetadata.id)
                val toggledSong: SongEntity
                if (dbSong == null) {
                    insert(mediaMetadata, SongEntity::toggleLike)
                    toggledSong = mediaMetadata.toSongEntity().toggleLike()
                } else {
                    toggledSong = dbSong.song.toggleLike()
                    update(toggledSong)
                }

                syncUtils.likeSong(toggledSong)

                val updatedMetadata = mediaMetadata.copy(
                    liked = toggledSong.liked,
                    likedDate = toggledSong.likedDate,
                    inLibrary = toggledSong.inLibrary
                )
                scope.launch(Dispatchers.Main) {
                    if (player.currentMediaItem?.mediaId == updatedMetadata.id) {
                        currentMediaMetadata.value = updatedMetadata
                    }
                }
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Error in toggleLike for ${mediaMetadata.id}")
            }
        }
    }

    fun toggleStartRadio() {
        startRadioSeamlessly()
    }

    private fun setupLoudnessEnhancer() {
        val audioSessionId = player.audioSessionId

        if (audioSessionId == C.AUDIO_SESSION_ID_UNSET || audioSessionId <= 0) {
            Timber.tag(TAG).w("setupLoudnessEnhancer: invalid audioSessionId ($audioSessionId), cannot create effect yet")
            return
        }

        // Create or recreate enhancer if needed
        if (loudnessEnhancer == null) {
            try {
                loudnessEnhancer = LoudnessEnhancer(audioSessionId)
                Timber.tag(TAG).d("LoudnessEnhancer created for sessionId=$audioSessionId")
            } catch (e: Exception) {
                reportException(e)
                loudnessEnhancer = null
                return
            }
        }

        scope.launch {
            try {
                val currentMediaId = withContext(Dispatchers.Main) {
                    player.currentMediaItem?.mediaId
                }

                val normalizeAudio = withContext(Dispatchers.IO) {
                    dataStore.data.map { it[AudioNormalizationKey] ?: true }.first()
                }

                if (normalizeAudio && currentMediaId != null) {
                    val format = withContext(Dispatchers.IO) {
                        database.format(currentMediaId).first()
                    }

                    Timber.tag(TAG).d("Audio normalization enabled: $normalizeAudio")
                    Timber.tag(TAG).d("Format loudnessDb: ${format?.loudnessDb}, perceptualLoudnessDb: ${format?.perceptualLoudnessDb}")

                    // Use loudnessDb if available, otherwise fall back to perceptualLoudnessDb
                    val loudness = format?.loudnessDb ?: format?.perceptualLoudnessDb

                    withContext(Dispatchers.Main) {
                        if (loudness != null) {
                            val loudnessDb = loudness.toFloat()
                            val targetGain = (-loudnessDb * 100).toInt()
                            val clampedGain = targetGain.coerceIn(MIN_GAIN_MB, MAX_GAIN_MB)

                            Timber.tag(TAG).d("Calculated raw normalization gain: $targetGain mB (from loudness: $loudnessDb)")

                            try {
                                if (clampedGain < 0) {
                                    // Attenuate overly loud tracks cleanly to prevent digital clipping
                                    loudnessEnhancer?.setTargetGain(clampedGain)
                                    loudnessEnhancer?.enabled = true
                                    Timber.tag(TAG).i("LoudnessEnhancer attenuation applied: $clampedGain mB")
                                } else {
                                    // Bypass LoudnessEnhancer for normal tracks to avoid Android DSP compressor bloating bass and muddying clarity
                                    loudnessEnhancer?.enabled = false
                                    Timber.tag(TAG).i("LoudnessEnhancer bypassed for clean, transparent audio output")
                                }
                            } catch (e: Exception) {
                                Timber.tag(TAG).e(e, "Failed to apply loudness enhancement")
                                reportException(e)
                                releaseLoudnessEnhancer()
                            }
                        } else {
                            loudnessEnhancer?.enabled = false
                            Timber.tag(TAG).w("Normalization enabled but no loudness data available - no normalization applied")
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        loudnessEnhancer?.enabled = false
                        Timber.tag(TAG).d("setupLoudnessEnhancer: normalization disabled or mediaId unavailable")
                    }
                }
            } catch (e: Exception) {
                reportException(e)
                releaseLoudnessEnhancer()
            }
        }
    }

    private fun releaseLoudnessEnhancer() {
        try {
            loudnessEnhancer?.release()
            Timber.tag(TAG).d("LoudnessEnhancer released")
        } catch (e: Exception) {
            reportException(e)
            Timber.tag(TAG).e(e, "Error releasing LoudnessEnhancer: ${e.message}")
        } finally {
            loudnessEnhancer = null
        }
    }

    private fun openAudioEffectSession() {
        val sessionId = player.audioSessionId
        if (isAudioEffectSessionOpened) {
            if (sessionId > 0 && sessionId != openedAudioSessionId) {
                openedAudioSessionId = sessionId
                setupLoudnessEnhancer()
                runCatching { equalizerController.attachSession(sessionId) }
            }
            return
        }
        if (sessionId <= 0) return
        isAudioEffectSessionOpened = true
        openedAudioSessionId = sessionId
        setupLoudnessEnhancer()
        runCatching { equalizerController.attachSession(sessionId) }
        sendBroadcast(
            Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION).apply {
                putExtra(AudioEffect.EXTRA_AUDIO_SESSION, sessionId)
                putExtra(AudioEffect.EXTRA_PACKAGE_NAME, packageName)
                putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
            },
        )
    }

    private fun closeAudioEffectSession() {
        if (!isAudioEffectSessionOpened) return
        isAudioEffectSessionOpened = false
        val sessionId = openedAudioSessionId ?: player.audioSessionId
        openedAudioSessionId = null
        releaseLoudnessEnhancer()
        runCatching { equalizerController.detachSession() }
        if (sessionId > 0) {
            sendBroadcast(
                Intent(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION).apply {
                    putExtra(AudioEffect.EXTRA_AUDIO_SESSION, sessionId)
                    putExtra(AudioEffect.EXTRA_PACKAGE_NAME, packageName)
                },
            )
        }
    }

    private var previousMediaItemIndex = C.INDEX_UNSET

    override fun onMediaItemTransition(
        mediaItem: MediaItem?,
        reason: Int,
    ) {
        if (crossfadeTriggerJob != null) cancelCrossfadePreparation()
        if (fadingPlayer != null) finishCrossfade()
        handleMediaItemTransition(mediaItem, reason)
    }

    private fun handleMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        val nextMeta = mediaItem?.metadata ?: player.currentMetadata
        currentMediaMetadata.value = nextMeta
        updateNotification()
        cancelPendingDelayedCacheTimer()
        if (player.isPlaying && mediaItem != null) {
            scheduleDelayedCache(mediaItem.mediaId)
        }
        if (mediaItem != null) {
            loadMoodRecommendations(mediaItem.mediaId)
        }
        // Force Repeat One if the player ignored it and auto-advanced
        if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO) {
            if (previousMediaItemIndex != C.INDEX_UNSET && previousMediaItemIndex < player.mediaItemCount) {
                val finishedMediaId = player.getMediaItemAt(previousMediaItemIndex).mediaId
                scope.launch(Dispatchers.IO) { markAutomaticallyCached(finishedMediaId) }
            }

            if (player.repeatMode == REPEAT_MODE_ONE &&
                previousMediaItemIndex != C.INDEX_UNSET &&
                previousMediaItemIndex != player.currentMediaItemIndex) {

                player.seekTo(previousMediaItemIndex, 0)
            }
        } else if (dataStore.get(DisableSongCacheKey, false)) {
            if (previousMediaItemIndex != C.INDEX_UNSET && previousMediaItemIndex < player.mediaItemCount) {
                val skippedMediaId = player.getMediaItemAt(previousMediaItemIndex).mediaId
                scope.launch(Dispatchers.IO) {
                    try { playerCache.removeResource(skippedMediaId) } catch (_: Exception) {}
                }
            }
        }
        previousMediaItemIndex = player.currentMediaItemIndex

        lastPlaybackSpeed = -1.0f // force update song

        setupLoudnessEnhancer()

        discordUpdateJob?.cancel()

        scrobbleManager?.onSongStop()
        if (player.playWhenReady && player.playbackState == Player.STATE_READY) {
            scrobbleManager?.onSongStart(player.currentMetadata, duration = player.duration)
        }

        // Sync Cast when media changes and Cast is connected
        // Skip if this change was triggered by Cast sync (to prevent loops)
        if (castConnectionHandler?.isCasting?.value == true &&
            castConnectionHandler?.isSyncingFromCast != true &&
            mediaItem != null) {
            val metadata = mediaItem.metadata
            if (metadata != null) {
                // Try to navigate to the item if it's already in Cast queue
                // This avoids a full reload which causes the widget to refresh
                val navigated = castConnectionHandler?.navigateToMediaIfInQueue(metadata.id) ?: false
                if (!navigated) {
                    // Item not in Cast queue, need to reload
                    castConnectionHandler?.loadMedia(metadata)
                }
            }
        }

        // Auto load more songs from queue
        if (dataStore.get(AutoLoadMoreKey, true) &&
            reason != Player.MEDIA_ITEM_TRANSITION_REASON_REPEAT &&
            player.mediaItemCount - player.currentMediaItemIndex <= 5 &&
            currentQueue.hasNextPage() &&
            !(dataStore.get(DisableLoadMoreWhenRepeatAllKey, false) && player.repeatMode == REPEAT_MODE_ALL)
        ) {
            val queue = currentQueue
            scope.launch(SilentHandler) {
                val mediaItems = withContext(Dispatchers.IO) {
                    queue.nextPage()
                        .filterExplicit(dataStore.get(HideExplicitKey, false))
                        .filterVideoSongs(dataStore.get(HideVideoSongsKey, false))
                }
                if (currentQueue === queue && player.playbackState != STATE_IDLE && mediaItems.isNotEmpty()) {
                    val preventDuplicates = dataStore.get(PreventDuplicateTracksInQueueKey, true)
                    val itemsToAdd = if (preventDuplicates) {
                        val existingIds = (0 until player.mediaItemCount).map { player.getMediaItemAt(it).mediaId }.toSet()
                        mediaItems.filter { it.mediaId !in existingIds }.distinctBy { it.mediaId }
                    } else {
                        mediaItems
                    }
                    if (itemsToAdd.isNotEmpty()) {
                        player.addMediaItems(itemsToAdd)
                        if (player.shuffleModeEnabled) {
                            val shufflePlaylistFirst = dataStore.get(ShufflePlaylistFirstKey, false)
                            applyShuffleOrder(player.currentMediaItemIndex, player.mediaItemCount, shufflePlaylistFirst)
                        }
                    }
                }
            }
        }

        // Pre-resolve the next track's stream URL so the transition feels instant
        prefetchNextTrack()

        // Save state when media item changes
        if (dataStore.get(PersistentQueueKey, true)) {
            saveQueueToDisk()
        }
    }

    /**
     * Look-ahead stream prefetcher.
     *
     * Resolves the next queued track's signed CDN stream URL in the background and stores it
     * in [songUrlCache].  When ExoPlayer later transitions to that track, [createDataSourceFactory]
     * reads the cached URL instantly — eliminating the 500 ms–2 s network round-trip that would
     * otherwise cause an audible gap between songs.
     *
     * The current playback order supplies up to two tracks. The job is cancelled on track changes.
     *
     * Note: this only resolves a small InnerTube JSON response (~10–30 KB).  No audio bytes are
     * downloaded here — that is handled by ExoPlayer's own CacheDataSource pipeline.
     */
    private fun prefetchNextTrack() {
        // Cancel any in-flight prefetch from the previous song
        prefetchJob?.cancel()

        val timeline = player.currentTimeline
        val nextIndex = player.nextMediaItemIndex
        if (nextIndex == C.INDEX_UNSET) return
        val repeatMode = if (player.repeatMode == REPEAT_MODE_ONE) REPEAT_MODE_OFF else player.repeatMode
        val lookaheadIndex = timeline.getNextWindowIndex(nextIndex, repeatMode, player.shuffleModeEnabled)
        val targetMediaIds = listOf(nextIndex, lookaheadIndex)
            .filter { it != C.INDEX_UNSET && it != player.currentMediaItemIndex }
            .map { player.getMediaItemAt(it).mediaId }
            .distinct()
        prefetchJob = scope.launch(Dispatchers.IO + SilentHandler) {
            for (targetMediaId in targetMediaIds) {
                if (!isActive) break
                if (songUrlCache[targetMediaId] != null || isFullyCached(targetMediaId)) continue

                Timber.tag(TAG).d("[Prefetch] Resolving stream URL for queued track: $targetMediaId")
                val cacheGeneration = songUrlCache.generation(targetMediaId)
                val result = runCatching {
                    YTPlayerUtils.playerResponseForPlayback(
                        videoId = targetMediaId,
                        audioQuality = audioQuality,
                        connectivityManager = connectivityManager,
                        context = this@MusicService,
                    )
                }
                result.getOrNull()?.getOrNull()?.let { playbackData ->
                    if (isActive) {
                        songUrlCache.put(
                            targetMediaId, playbackData.streamUrl, playbackData.streamHeaders,
                            playbackData.streamClient, playbackData.streamExpiresInSeconds,
                            expectedGeneration = cacheGeneration,
                        )
                        Timber.tag(TAG).d("[Prefetch] Cached stream URL for $targetMediaId (expires in ${playbackData.streamExpiresInSeconds}s)")

                        playbackData.format?.let { format ->
                            val loudnessDb = playbackData.audioConfig?.loudnessDb
                            val perceptualLoudnessDb = playbackData.audioConfig?.perceptualLoudnessDb
                            database.query {
                                upsert(
                                    FormatEntity(
                                        id = targetMediaId,
                                        itag = format.itag,
                                        mimeType = format.mimeType.split(";")[0],
                                        codecs = format.mimeType.split("codecs=").getOrNull(1)?.removeSurrounding("\"") ?: "mp3",
                                        bitrate = format.bitrate,
                                        sampleRate = format.audioSampleRate,
                                        contentLength = format.contentLength ?: 0L,
                                        loudnessDb = loudnessDb,
                                        perceptualLoudnessDb = perceptualLoudnessDb,
                                        playbackUrl = playbackData.playbackTracking?.videostatsPlaybackUrl?.baseUrl
                                    )
                                )
                            }
                        }
                    }
                } ?: Timber.tag(TAG).d("[Prefetch] Could not resolve stream URL for $targetMediaId — will resolve on demand")
            }
        }
    }

    override fun onPlaybackStateChanged(
        @Player.State playbackState: Int,
    ) {
        if (playbackState == STATE_IDLE || playbackState == Player.STATE_ENDED) releaseCrossfadePlayers()
        // Force Repeat All if the player ignored it and ended playback
        if (playbackState == Player.STATE_ENDED) {
            if (player.repeatMode == REPEAT_MODE_ALL && player.mediaItemCount > 0) {
                player.seekTo(0, 0)
                player.prepare()
                player.play()
            }
        }

        // Save state when playback state changes (but not during silence skipping)
        if (dataStore.get(PersistentQueueKey, true) && !isSilenceSkipping) {
            saveQueueToDisk()
        }

        if (playbackState == Player.STATE_READY) {
            consecutivePlaybackErr = 0
            retryCount = 0
            waitingForNetworkConnection.value = false
            retryJob?.cancel()

            // Reset retry count for current song on successful playback
            player.currentMediaItem?.mediaId?.let { mediaId ->
                resetRetryCount(mediaId)
                Timber.tag(TAG).d("Playback successful for $mediaId, reset retry count")
            }
            scheduleCrossfade()
        }

        if (playbackState == Player.STATE_IDLE || playbackState == Player.STATE_ENDED) {
            scrobbleManager?.onSongStop()
        }
    }

    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        if (!playWhenReady) {
            cancelCrossfadePreparation()
            updateOutgoingPlayback()
        }
        // Safety net: if local player tries to start while casting, immediately pause it
        if (playWhenReady && castConnectionHandler?.isCasting?.value == true) {
            player.pause()
            return
        }

        if (reason == Player.PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST) {
            if (playWhenReady) {
                isPausedByVolumeMute = false
            }

            if (!playWhenReady && !isPausedByVolumeMute) {
                wasPlayingBeforeVolumeMute = false
            }
        }

        if (playWhenReady) {
            setupLoudnessEnhancer()
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        updateOutgoingPlayback()
        if (isPlaying) {
            val mediaId = player.currentMediaItem?.mediaId
            if (mediaId != null) {
                scheduleDelayedCache(mediaId)
            }
        } else {
            cancelPendingDelayedCacheTimer()
        }
    }

    override fun onEvents(
        player: Player,
        events: Player.Events,
    ) {
        if (player !== this.player) return
        if (events.containsAny(
                Player.EVENT_PLAYBACK_STATE_CHANGED,
                Player.EVENT_PLAY_WHEN_READY_CHANGED,
                Player.EVENT_AUDIO_SESSION_ID
            )
        ) {
            val isBufferingOrReady =
                player.playbackState == Player.STATE_BUFFERING || player.playbackState == Player.STATE_READY
            if (isBufferingOrReady && player.playWhenReady) {
                val focusGranted = requestAudioFocus()
                if (focusGranted) {
                    openAudioEffectSession()
                    updateOutgoingPlayback()
                } else if (lastAudioFocusState == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                    resumeAfterFocusLoss = true
                    player.pause()
                } else {
                    player.pause()
                    releaseCrossfadePlayers()
                }
            } else {
                closeAudioEffectSession()
            }
        }
        if (events.containsAny(EVENT_TIMELINE_CHANGED, EVENT_POSITION_DISCONTINUITY)) {
            currentMediaMetadata.value = player.currentMetadata
        }

        if (events.containsAny(
                Player.EVENT_IS_PLAYING_CHANGED, Player.EVENT_MEDIA_ITEM_TRANSITION,
                Player.EVENT_PLAYBACK_STATE_CHANGED, Player.EVENT_REPEAT_MODE_CHANGED, Player.EVENT_SHUFFLE_MODE_ENABLED_CHANGED,
                Player.EVENT_PLAYBACK_PARAMETERS_CHANGED, EVENT_TIMELINE_CHANGED, EVENT_POSITION_DISCONTINUITY,
            )
        ) {
            if (events.contains(Player.EVENT_PLAYBACK_PARAMETERS_CHANGED)) releaseCrossfadePlayers()
            scheduleCrossfade()
        }

        // Widget and Discord RPC updates
        if (events.containsAny(Player.EVENT_IS_PLAYING_CHANGED)) {
            updateWidgetUI(player.isPlaying)
            if (player.isPlaying) {
                startWidgetUpdates()
            } else {
                stopWidgetUpdates()
            }
            if (!player.isPlaying && !events.containsAny(Player.EVENT_POSITION_DISCONTINUITY, Player.EVENT_MEDIA_ITEM_TRANSITION)) {
                scope.launch {
                    discordRpc?.close()
                }
            }
        }

        // Update Discord RPC when media item changes or playback starts
        if (events.containsAny(Player.EVENT_MEDIA_ITEM_TRANSITION, Player.EVENT_IS_PLAYING_CHANGED) && player.isPlaying) {
            val mediaId = player.currentMetadata?.id
            if (mediaId != null) {
                scope.launch {
                    // Fetch song from database to get full info
                    database.song(mediaId).first()?.let { song ->
                        updateDiscordRPC(song)
                    }
                }
            }
        }

        // Scrobbling
        if (events.containsAny(Player.EVENT_IS_PLAYING_CHANGED)) {
            scrobbleManager?.onPlayerStateChanged(player.isPlaying, player.currentMetadata, duration = player.duration)
        }

    }

    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
        updateNotification()
        if (shuffleModeEnabled) {
            // If queue is empty, don't shuffle
            if (player.mediaItemCount == 0) return

            val shufflePlaylistFirst = dataStore.get(ShufflePlaylistFirstKey, false)
            val currentIndex = player.currentMediaItemIndex
            val totalCount = player.mediaItemCount

            applyShuffleOrder(currentIndex, totalCount, shufflePlaylistFirst)
        }

        // Save shuffle mode to preferences
        if (dataStore.get(RememberShuffleAndRepeatKey, true)) {
            scope.launch {
                dataStore.edit { settings ->
                    settings[ShuffleModeKey] = shuffleModeEnabled
                }
            }
        }

        // Save state when shuffle mode changes
        if (dataStore.get(PersistentQueueKey, true)) {
            saveQueueToDisk()
        }
    }

    override fun onRepeatModeChanged(repeatMode: Int) {
        updateNotification()
        scope.launch {
            dataStore.edit { settings ->
                settings[RepeatModeKey] = repeatMode
            }
        }

        // Save state when repeat mode changes
        if (dataStore.get(PersistentQueueKey, true)) {
            saveQueueToDisk()
        }
    }

    /**
     * Reads the primary player's currently-active shuffle traversal order
     * straight from its [Timeline] (using getNext/getPreviousWindowIndex
     * with shuffle enabled, the same trick [playNext] uses) and returns it as an
     * explicit index array reproducing the full active traversal order, with
     * the current item at its real position (not necessarily first).
     *
     * ExoPlayer has no public getter for the live [ShuffleOrder], so this is how
     * we capture it just before a crossfade player swap in order to replay the
     * *same* order on the secondary player — instead of calling
     * [applyShuffleOrder], which regenerates a fresh random order and was the
     * cause of shuffle re-randomizing on every skip / song selection (and of
     * already-played tracks reappearing).
     */
    private fun getCurrentShuffleOrderIndices(sourcePlayer: Player): IntArray? {
        val timeline = sourcePlayer.currentTimeline
        if (timeline.isEmpty) return null
        val currentIndex = sourcePlayer.currentMediaItemIndex
        if (currentIndex == C.INDEX_UNSET) return null

        val before = mutableListOf<Int>()
        var prev = currentIndex
        while (true) {
            prev = timeline.getPreviousWindowIndex(prev, Player.REPEAT_MODE_OFF, true)
            if (prev == C.INDEX_UNSET) break
            before.add(prev)
        }
        before.reverse()

        val after = mutableListOf<Int>()
        var next = currentIndex
        while (true) {
            next = timeline.getNextWindowIndex(next, Player.REPEAT_MODE_OFF, true)
            if (next == C.INDEX_UNSET) break
            after.add(next)
        }

        return (before + currentIndex + after).toIntArray()
    }

    /**
     * Applies a new shuffle order to the player, maintaining the current item's position.
     * If `shufflePlaylistFirst` is true, it attempts to shuffle original items separately from added items.
     */
    private fun applyShuffleOrder(
        currentIndex: Int,
        totalCount: Int,
        shufflePlaylistFirst: Boolean
    ) {
        if (totalCount == 0) return

        if (shufflePlaylistFirst && originalQueueSize > 0 && originalQueueSize < totalCount) {
            // Shuffle original items and added items separately
            val originalIndices = (0 until originalQueueSize).filter { it != currentIndex }.toMutableList()
            val addedIndices = (originalQueueSize until totalCount).filter { it != currentIndex }.toMutableList()

            originalIndices.shuffle()
            addedIndices.shuffle()

            val shuffledIndices = IntArray(totalCount)
            var pos = 0
            shuffledIndices[pos++] = currentIndex

            if (currentIndex < originalQueueSize) {
                originalIndices.forEach { shuffledIndices[pos++] = it }
                addedIndices.forEach { shuffledIndices[pos++] = it }
            } else {
                (0 until originalQueueSize).shuffled().forEach { shuffledIndices[pos++] = it }
                addedIndices.forEach { shuffledIndices[pos++] = it }
            }
            player.setShuffleOrder(DefaultShuffleOrder(shuffledIndices, System.currentTimeMillis()))
        } else {
            val shuffledIndices = IntArray(totalCount) { it }
            shuffledIndices.shuffle()
            // Ensure current item is first in the shuffle order
            val currentItemIndexInShuffled = shuffledIndices.indexOf(currentIndex)
            if (currentItemIndexInShuffled != -1) { // Should always be true if totalCount > 0
                val temp = shuffledIndices[0]
                shuffledIndices[0] = shuffledIndices[currentItemIndexInShuffled]
                shuffledIndices[currentItemIndexInShuffled] = temp
            }
            player.setShuffleOrder(DefaultShuffleOrder(shuffledIndices, System.currentTimeMillis()))
        }
    }

    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
        super.onPlaybackParametersChanged(playbackParameters)
        if (playbackParameters.speed != lastPlaybackSpeed) {
            lastPlaybackSpeed = playbackParameters.speed
            discordUpdateJob?.cancel()

            // update scheduling thingy
            discordUpdateJob = scope.launch {
                delay(1000)
                if (player.playWhenReady && player.playbackState == Player.STATE_READY) {
                    currentSong.value?.let { song ->
                        updateDiscordRPC(song)
                    }
                }
            }
        }
    }

    /**
     * Extracts the HTTP response code from an error's cause chain.
     * Returns null if no HTTP response code is found.
     */
    private fun getHttpResponseCode(error: PlaybackException): Int? {
        var cause: Throwable? = error.cause
        while (cause != null) {
            if (cause is HttpDataSource.InvalidResponseCodeException) {
                return cause.responseCode
            }
            cause = cause.cause
        }
        return null
    }

    /**
     * Checks if the error is caused by an expired/forbidden URL (HTTP 403).
     * This typically happens when a YouTube stream URL expires.
     */
    private fun isExpiredUrlError(error: PlaybackException): Boolean {
        val responseCode = getHttpResponseCode(error)
        return responseCode == 403
    }

    /**
     * Checks if the error is a Range Not Satisfiable error (HTTP 416).
     * This happens when cached data doesn't match the actual stream size.
     */
    private fun isRangeNotSatisfiableError(error: PlaybackException): Boolean {
        val responseCode = getHttpResponseCode(error)
        return responseCode == 416
    }

    /**
     * Checks if the error is a "page needs to be reloaded" error.
     * This is a YouTube-specific error that requires refreshing the stream.
     */
    private fun isPageReloadError(error: PlaybackException): Boolean {
        val errorMessage = error.message?.lowercase() ?: ""
        val causeMessage = error.cause?.message?.lowercase() ?: ""
        val innerCauseMessage = error.cause?.cause?.message?.lowercase() ?: ""

        val reloadKeywords = listOf(
            "page needs to be reloaded",
            "pagina deve essere ricaricata",
            "la pagina deve essere ricaricata",
            "page must be reloaded",
            "reload",
            "ricaricata"
        )

        return reloadKeywords.any { keyword ->
            errorMessage.contains(keyword) ||
            causeMessage.contains(keyword) ||
            innerCauseMessage.contains(keyword)
        }
    }

    private fun isNetworkRelatedError(error: PlaybackException): Boolean {
        // Don't treat specific errors as network errors - they need special handling
        if (isExpiredUrlError(error) || isRangeNotSatisfiableError(error) || isPageReloadError(error)) {
            return false
        }
        return error.errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED ||
                error.errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT ||
                error.errorCode == PlaybackException.ERROR_CODE_IO_INVALID_HTTP_CONTENT_TYPE ||
                error.cause is java.net.ConnectException ||
                error.cause is java.net.UnknownHostException ||
                (error.cause as? PlaybackException)?.errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED
    }

    /**
     * Checks if the error is caused by AudioTrack write or initialization failures.
     * These errors indicate the audio renderer is in a corrupted/invalid state.
     */
    private fun isAudioRendererError(error: PlaybackException): Boolean {
        return error.errorCode == PlaybackException.ERROR_CODE_AUDIO_TRACK_WRITE_FAILED ||
                error.errorCode == PlaybackException.ERROR_CODE_AUDIO_TRACK_INIT_FAILED ||
                (error.cause as? PlaybackException)?.errorCode == PlaybackException.ERROR_CODE_AUDIO_TRACK_WRITE_FAILED ||
                (error.cause as? PlaybackException)?.errorCode == PlaybackException.ERROR_CODE_AUDIO_TRACK_INIT_FAILED
    }

    /**
     * Returns true if the root cause is a StreamResolveException — meaning InnerTubeX
     * exhausted every client and cannot provide a playable stream URL.
     * Retrying via the normal IO-error path is pointless in this case.
     */
    private fun isStreamResolveFailure(error: PlaybackException): Boolean {
        var cause: Throwable? = error.cause
        while (cause != null) {
            if (cause.javaClass.name.contains("StreamResolveException")) return true
            cause = cause.cause
        }
        return false
    }

    override fun onPlayerError(error: PlaybackException) {
        releaseCrossfadePlayers()
        super.onPlayerError(error)

        // Safety check : ensuring player is still initialized
        if (!playerInitialized.value) {
            Timber.tag(TAG).e(error, "Player error occurred but player not initialized")
            return
        }

        val mediaId = player.currentMediaItem?.mediaId
        Timber.tag(TAG).w(error, "Player error occurred for $mediaId: errorCode=${error.errorCode}, message=${error.message}")
        reportException(error)

        // Check if this song has failed too many times
        if (mediaId != null && hasExceededRetryLimit(mediaId)) {
            Timber.tag(TAG).w("Song $mediaId has exceeded retry limit, skipping")
            markSongAsFailed(mediaId)
            handleFinalFailure()
            return
        }

        // Aggressive cache clearing for all playback errors
        if (mediaId != null) {
            performAggressiveCacheClear(mediaId)
        }

        // Handle specific error types with strict strategies
        when {
            isStreamResolveFailure(error) -> {
                // All InnerTubeX clients were exhausted — no stream is available.
                // Retrying would just repeat the same full client sweep with the same result.
                Timber.tag(TAG).e(
                    "Stream resolution failed for $mediaId — all clients exhausted. " +
                    "Stopping immediately (skipping retry loop)."
                )
                if (mediaId != null) markSongAsFailed(mediaId)
                handleFinalFailure()
                return
            }
            isAudioRendererError(error) -> {
                Timber.tag(TAG).d("AudioTrack error detected (${error.errorCode}), performing safe recovery")
                handleAudioRendererError(mediaId)
                return
            }
            isRangeNotSatisfiableError(error) -> {
                Timber.tag(TAG).d("Range Not Satisfiable (416) detected, performing strict recovery")
                handleRangeNotSatisfiableError(mediaId)
                return
            }
            isPageReloadError(error) -> {
                Timber.tag(TAG).d("Page reload error detected, performing strict recovery")
                handlePageReloadError(mediaId)
                return
            }
            isExpiredUrlError(error) -> {
                Timber.tag(TAG).d("Expired URL (403) detected, refreshing stream URL")
                handleExpiredUrlError(mediaId)
                return
            }

            !isNetworkConnected.value || isNetworkRelatedError(error) -> {
                Timber.tag(TAG).d("Network-related error detected, waiting for connection")
                waitOnNetworkError()
                return
            }
        }

        // For IO_UNSPECIFIED and IO_BAD_HTTP_STATUS, try recovery first
        if (error.errorCode == PlaybackException.ERROR_CODE_IO_UNSPECIFIED ||
            error.errorCode == PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS) {
            Timber.tag(TAG).d("IO error detected (${error.errorCode}), attempting recovery")
            handleGenericIOError(mediaId)
            return
        }

        // Final fallback
        if (dataStore.get(AutoSkipNextOnErrorKey, false)) {
            Timber.tag(TAG).d("Auto-skipping to next track due to unrecoverable error")
            skipOnError()
        } else {
            Timber.tag(TAG).d("Stopping playback due to unrecoverable error")
            stopOnError()
        }
    }

    /**
     * Performs aggressive cache clearing for a media item.
     * Clears both player cache and download cache, plus URL cache.
     */
    private fun performAggressiveCacheClear(mediaId: String) {
        Timber.tag(TAG).d("Performing aggressive cache clear for $mediaId")

        // Clear URL cache
        songUrlCache.invalidate(mediaId)

        // Clear player cache
        try {
            playerCache.removeResource(mediaId)
            Timber.tag(TAG).d("Cleared player cache for $mediaId")
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to clear player cache for $mediaId")
        }


    }

    /**
     * Stops the player, clears all caches for the current song on the IO thread,
     * then restarts playback — ensuring no stale cache data causes parse errors.
     * Safe to call from the main thread (UI).
     */
    fun retryCurrentStream() {
        val mediaId = player.currentMediaItem?.mediaId ?: return

        // Stop player before touching the cache so ExoPlayer isn't mid-read
        player.stop()

        // Clear all caches synchronously on IO — must finish before prepare()
        runBlocking(Dispatchers.IO) {
            performAggressiveCacheClear(mediaId)
        }

        // Now safe to restart — ExoPlayer will fetch a fresh stream
        player.seekTo(0)
        player.prepare()
        player.play()
    }

    /**
     * Checks if a song has exceeded the retry limit.
     */
    private fun hasExceededRetryLimit(mediaId: String): Boolean {
        val currentRetries = currentMediaIdRetryCount[mediaId] ?: 0
        return currentRetries >= MAX_RETRY_PER_SONG
    }

    /**
     * Increments the retry count for a song.
     */
    private fun incrementRetryCount(mediaId: String) {
        val currentRetries = currentMediaIdRetryCount[mediaId] ?: 0
        currentMediaIdRetryCount[mediaId] = currentRetries + 1
        Timber.tag(TAG).d("Retry count for $mediaId: ${currentRetries + 1}/$MAX_RETRY_PER_SONG")
    }

    /**
     * Resets the retry count for a song (called on successful playback).
     */
    private fun resetRetryCount(mediaId: String) {
        currentMediaIdRetryCount.remove(mediaId)
        recentlyFailedSongs.remove(mediaId)
    }

    /**
     * Marks a song as failed to prevent further retry attempts.
     */
    private fun markSongAsFailed(mediaId: String) {
        recentlyFailedSongs.add(mediaId)
        currentMediaIdRetryCount.remove(mediaId)

        // Schedule cleanup of failed songs list after 5 minutes
        failedSongsClearJob?.cancel()
        failedSongsClearJob = scope.launch {
            delay(5 * 60 * 1000L) // 5 minutes
            recentlyFailedSongs.clear()
            Timber.tag(TAG).d("Cleared recently failed songs list")
        }
    }

    /**
     * Handles AudioTrack errors (write failed, init failed) with safe recovery.
     * These errors indicate the audio renderer is corrupted and needs careful reset.
     */
    private fun handleAudioRendererError(mediaId: String?) {
        if (mediaId == null) {
            handleFinalFailure()
            return
        }

        incrementRetryCount(mediaId)

        retryJob?.cancel()
        retryJob = scope.launch {
            try {
                val resumeAfterRecovery = player.playWhenReady
                // Pause playback immediately to stop the renderer
                player.pause()
                Timber.tag(TAG).d("Paused playback due to AudioTrack error")

                // Wait longer for audio renderer to settle before retry
                // This prevents the renderer from continuing to fail in a loop
                delay(RETRY_DELAY_MS * 3) // 3 seconds instead of 1 second

                // Check if player is still initialized before attempting recovery
                if (!playerInitialized.value) {
                    Timber.tag(TAG).w("Player no longer initialized, aborting AudioTrack recovery")
                    return@launch
                }

                val currentIndex = player.currentMediaItemIndex
                if (currentIndex != C.INDEX_UNSET) {
                    // Seek to current position to force a clean audio renderer reinit
                    val currentPosition = player.currentPosition
                    player.seekTo(currentIndex, currentPosition)
                    player.prepare()

                    Timber.tag(TAG).d("Retrying playback for $mediaId after AudioTrack error")

                    // Resume playback if it wasn't paused by user
                    if (resumeAfterRecovery) {
                        delay(500) // Brief delay to allow renderer to be ready
                        if (hasAudioFocus && playerInitialized.value) {
                            if (castConnectionHandler?.isCasting?.value != true) {
                                player.play()
                            }
                        }
                    }
                } else {
                    Timber.tag(TAG).w("Invalid media item index during AudioTrack recovery")
                    handleFinalFailure()
                }
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Error during AudioTrack error recovery")
                handleFinalFailure()
            }
        }
    }

    /**
     * Handles Range Not Satisfiable (416) errors with strict recovery.
     * This error occurs when cached data doesn't match the actual stream size.
     */
    private fun handleRangeNotSatisfiableError(mediaId: String?) {
        if (mediaId == null) {
            handleFinalFailure()
            return
        }

        incrementRetryCount(mediaId)

        retryJob?.cancel()
        retryJob = scope.launch {
            // Clear all caches aggressively
            performAggressiveCacheClear(mediaId)

            // Wait before retry
            delay(RETRY_DELAY_MS)

            // Force re-prepare from position 0 to avoid range issues
            val currentIndex = player.currentMediaItemIndex
            player.seekTo(currentIndex, 0)
            player.prepare()

            Timber.tag(TAG).d("Retrying playback for $mediaId after 416 error (from position 0)")
        }
    }

    /**
     * Handles "page needs to be reloaded" errors with strict recovery.
     * This requires clearing decryption caches and getting fresh stream URLs.
     */
    private fun handlePageReloadError(mediaId: String?) {
        if (mediaId == null) {
            handleFinalFailure()
            return
        }

        incrementRetryCount(mediaId)

        retryJob?.cancel()
        retryJob = scope.launch {
            Timber.tag(TAG).d("Handling page reload error for $mediaId")

            // Clear all caches including decryption caches
            performAggressiveCacheClear(mediaId)

            // Short delay - the cipher JS is cached after the first attempt, 
            // so the second attempt resolves quickly.
            delay(RETRY_DELAY_MS)

            // Re-prepare the player
            val currentPosition = player.currentPosition
            val currentIndex = player.currentMediaItemIndex
            player.seekTo(currentIndex, currentPosition)
            player.prepare()

            Timber.tag(TAG).d("Retrying playback for $mediaId after page reload error")
        }
    }

    /**
     * Handles expired URL (403) errors by clearing caches and retrying.
     */
    private fun handleExpiredUrlError(mediaId: String?) {
        if (mediaId == null) {
            handleFinalFailure()
            return
        }

        incrementRetryCount(mediaId)

        // Clear the cached URL
        songUrlCache.invalidate(mediaId)
        Timber.tag(TAG).d("Cleared cached URL for $mediaId")



        retryJob?.cancel()
        retryJob = scope.launch {
            delay(RETRY_DELAY_MS)

            // Seek to current position to force URL re-resolution
            val currentPosition = player.currentPosition
            val currentIndex = player.currentMediaItemIndex
            player.seekTo(currentIndex, currentPosition)
            player.prepare()

            Timber.tag(TAG).d("Retrying playback for $mediaId after 403 error")
        }
    }

    /**
     * Handles generic IO errors with recovery attempt.
     */
    private fun handleGenericIOError(mediaId: String?) {
        if (mediaId == null) {
            handleFinalFailure()
            return
        }

        incrementRetryCount(mediaId)

        retryJob?.cancel()
        retryJob = scope.launch {
            performAggressiveCacheClear(mediaId)
            delay(RETRY_DELAY_MS)

            val currentPosition = player.currentPosition
            val currentIndex = player.currentMediaItemIndex
            player.seekTo(currentIndex, currentPosition)
            player.prepare()

            Timber.tag(TAG).d("Retrying playback for $mediaId after generic IO error")
        }
    }

    /**
     * Handles final failure when all recovery attempts have been exhausted.
     */
    private fun handleFinalFailure() {
        if (dataStore.get(AutoSkipNextOnErrorKey, false)) {
            Timber.tag(TAG).d("All recovery attempts exhausted, auto-skipping to next track")
            skipOnError()
        } else {
            Timber.tag(TAG).d("All recovery attempts exhausted, stopping playback")
            stopOnError()
        }
    }

    override fun onDeviceVolumeChanged(volume: Int, muted: Boolean) {
        super.onDeviceVolumeChanged(volume, muted)
        val pauseOnMute = dataStore.get(PauseOnMute, false)

        if ((volume == 0 || muted) && pauseOnMute) {
            if (player.isPlaying) {
                wasPlayingBeforeVolumeMute = true
                isPausedByVolumeMute = true
                player.pause()
            }
        } else if (volume > 0 && !muted && pauseOnMute) {
            if (wasPlayingBeforeVolumeMute && !player.isPlaying && castConnectionHandler?.isCasting?.value != true) {
                wasPlayingBeforeVolumeMute = false
                isPausedByVolumeMute = false
                player.play()
            }
        }
    }

    private fun createCacheDataSource(): DataSource.Factory {
        val disableSongCache = dataStore.get(DisableSongCacheKey, false)
        val upstreamFactory = DefaultDataSource.Factory(
            this,
            OkHttpDataSource.Factory(
                OkHttpClient
                    .Builder()
                    .dns(object : Dns {
                        override fun lookup(hostname: String): List<InetAddress> {
                            val addresses = Dns.SYSTEM.lookup(hostname)
                            return when (this@MusicService.ipVersion) {
                                IpVersion.IPV4 -> addresses.filter { it is Inet4Address }.ifEmpty { addresses }
                                IpVersion.IPV6 -> addresses.filter { it is Inet6Address }.ifEmpty { addresses }
                                IpVersion.AUTO -> addresses
                            }
                        }
                    })
                    .proxy(YouTube.proxy)
                    .proxyAuthenticator { _, response ->
                        YouTube.proxyAuth?.let { auth ->
                            response.request.newBuilder()
                                .header("Proxy-Authorization", auth)
                                .build()
                        } ?: response.request
                    }
                    .build(),
            ),
        )

        val automatic = CacheDataSource.Factory()
            .setCache(playerCache)
            .setUpstreamDataSourceFactory(upstreamFactory)
            .setCacheWriteDataSinkFactory(null)
        val streaming: DataSource.Factory = if (disableSongCache) upstreamFactory else automatic
        val downloads = CacheDataSource.Factory()
            .setCache(downloadCache)
            .setUpstreamDataSourceFactory(streaming)
            .setCacheWriteDataSinkFactory(null)
            .setFlags(FLAG_IGNORE_CACHE_ON_ERROR)
        return DataSource.Factory {
            val downloadSource = downloads.createDataSource()
            val streamingSource = streaming.createDataSource()
            object : DataSource {
                private var active: DataSource = downloadSource

                override fun addTransferListener(transferListener: TransferListener) {
                    downloadSource.addTransferListener(transferListener)
                    streamingSource.addTransferListener(transferListener)
                }

                override fun open(dataSpec: DataSpec): Long {
                    // A partial download can use a different encoding from the automatic cache.
                    val key = dataSpec.key
                    active = if (key != null &&
                        downloadCache.getCachedSpans(key).isNotEmpty() &&
                        downloadCache.completeAudioCachedAt(key) == null
                    ) streamingSource else downloadSource
                    return active.open(dataSpec)
                }

                override fun read(buffer: ByteArray, offset: Int, length: Int) = active.read(buffer, offset, length)
                override fun getUri() = active.uri
                override fun getResponseHeaders() = active.responseHeaders
                override fun close() = active.close()
            }
        }
    }

    private var pendingCacheTimerJob: Job? = null
    private val activeCacheJobs = java.util.concurrent.ConcurrentHashMap<String, Job>()

    private fun cancelPendingDelayedCacheTimer() {
        pendingCacheTimerJob?.cancel()
        pendingCacheTimerJob = null
    }

    private fun scheduleDelayedCache(mediaId: String) {
        cancelPendingDelayedCacheTimer()
        if (dataStore.get(DisableSongCacheKey, false)) return

        val delaySeconds = dataStore.get(SongCacheDelaySecondsKey, 5).coerceIn(5, 90)
        pendingCacheTimerJob = scope.launch(Dispatchers.IO) {
            delay(delaySeconds * 1000L)

            val isStillPlaying = withContext(Dispatchers.Main) {
                player.isPlaying && player.currentMediaItem?.mediaId == mediaId
            }
            if (!isStillPlaying) return@launch
            cacheSongInBackground(mediaId)
        }
    }

    private fun cacheSongInBackground(mediaId: String) {
        if (dataStore.get(DisableSongCacheKey, false)) return
        val job = scope.launch(Dispatchers.IO, start = CoroutineStart.LAZY) {
            try {
                if (isFullyCached(mediaId)) {
                    markAutomaticallyCached(mediaId)
                    return@launch
                }

                val cachedStream = songUrlCache[mediaId]
                val playbackData = if (cachedStream == null) {
                    try {
                        YTPlayerUtils.playerResponseForPlayback(
                            videoId = mediaId,
                            audioQuality = audioQuality,
                            connectivityManager = connectivityManager,
                            context = this@MusicService,
                        ).getOrNull()
                    } catch (_: Exception) {
                        null
                    }
                } else null

                if (playbackData != null) {
                    val format = playbackData.format
                    database.query {
                        upsert(
                            FormatEntity(
                                id = mediaId,
                                itag = format.itag,
                                mimeType = format.mimeType.split(";")[0],
                                codecs = format.mimeType.split("codecs=").getOrNull(1)?.removeSurrounding("\"") ?: "mp3",
                                bitrate = format.bitrate,
                                sampleRate = format.audioSampleRate,
                                contentLength = format.contentLength ?: 0L,
                                loudnessDb = playbackData.audioConfig?.loudnessDb,
                                perceptualLoudnessDb = playbackData.audioConfig?.perceptualLoudnessDb,
                                playbackUrl = playbackData.playbackTracking?.videostatsPlaybackUrl?.baseUrl
                            )
                        )
                    }
                    recoverSong(mediaId, playbackData)
                }

                val streamUrl = cachedStream?.url ?: playbackData?.streamUrl ?: return@launch
                val headers = cachedStream?.requestHeaders ?: playbackData?.streamHeaders.orEmpty()

                val upstreamFactory = OkHttpDataSource.Factory(
                    OkHttpClient.Builder()
                        .proxy(YouTube.proxy)
                        .build()
                )
                val cacheDataSource = CacheDataSource.Factory()
                    .setCache(playerCache)
                    .setUpstreamDataSourceFactory(upstreamFactory)
                    .setFlags(FLAG_IGNORE_CACHE_ON_ERROR)
                    .createDataSource()

                val dataSpec = DataSpec.Builder()
                    .setUri(streamUrl.toUri())
                    .setKey(mediaId)
                    .setHttpRequestHeaders(headers)
                    .build()

                val buffer = ByteArray(64 * 1024)
                val writer = CacheWriter(cacheDataSource, dataSpec, buffer, null)
                writer.cache()

                if (isFullyCached(mediaId)) {
                    markAutomaticallyCached(mediaId)
                    Timber.tag("MusicService").d("Successfully background-cached and marked $mediaId")
                }
            } catch (e: Exception) {
                Timber.tag("MusicService").w(e, "Background cacheWriter failed for $mediaId")
            } finally {
                activeCacheJobs.remove(mediaId, checkNotNull(coroutineContext[Job]))
            }
        }
        if (activeCacheJobs.putIfAbsent(mediaId, job) == null) job.start() else job.cancel()
    }

    // Flag to prevent queue saving during silence skip operations
    private var isSilenceSkipping = false

    private fun handleLongSilenceDetected() {
        if (!instantSilenceSkipEnabled.value) return
        if (silenceSkipJob?.isActive == true) return

        silenceSkipJob = scope.launch {
            // Debounce so short fades or transitions do not trigger a jump.
            delay(200)
            performInstantSilenceSkip()
        }
    }

    private suspend fun performInstantSilenceSkip() {
        if (player.playbackState == Player.STATE_BUFFERING || !player.isPlaying) return
        val duration = player.duration.takeIf { it != C.TIME_UNSET && it > 0 } ?: return
        if (duration <= INSTANT_SILENCE_SKIP_STEP_MS * 2) return

        isSilenceSkipping = true
        try {
            var hops = 0
            val silenceProcessor = playerSilenceProcessors[player] ?: return
            while (coroutineContext.isActive && instantSilenceSkipEnabled.value && silenceProcessor.isCurrentlySilent()) {
                if (player.playbackState == Player.STATE_BUFFERING || !player.isPlaying) break
                val current = player.currentPosition
                // Never skip into the final 15 seconds of the song to avoid premature track transitions
                val maxTarget = duration - 15_000L
                if (current >= maxTarget) break

                val target = (current + INSTANT_SILENCE_SKIP_STEP_MS).coerceAtMost(maxTarget)
                if (target <= current) break

                // Reset silence tracking before seeking to prevent immediate re-trigger
                silenceProcessor.resetTracking()
                player.seekTo(target)
                hops++

                if (hops >= 20 || target >= maxTarget) break

                delay(INSTANT_SILENCE_SKIP_SETTLE_MS)
            }
            if (hops > 0) {
                Timber.tag(TAG).d("Silence skip: jumped $hops times")
            }
        } finally {
            isSilenceSkipping = false
        }
    }

    private fun updateDiscordRPC(song: Song, showFeedback: Boolean = false) {
        val useDetails = dataStore.get(DiscordUseDetailsKey, false)
        val advancedMode = dataStore.get(DiscordAdvancedModeKey, false)

        val status = if (advancedMode) dataStore.get(DiscordStatusKey, "online") else "online"
        val b1Text = if (advancedMode) dataStore.get(DiscordButton1TextKey, "") else ""
        val b1Visible = if (advancedMode) dataStore.get(DiscordButton1VisibleKey, true) else true
        val b2Text = if (advancedMode) dataStore.get(DiscordButton2TextKey, "") else ""
        val b2Visible = if (advancedMode) dataStore.get(DiscordButton2VisibleKey, true) else true
        val activityType = if (advancedMode) dataStore.get(DiscordActivityTypeKey, "listening") else "listening"
        val activityName = if (advancedMode) dataStore.get(DiscordActivityNameKey, "") else ""

        discordUpdateJob?.cancel()
        discordUpdateJob = scope.launch {
            discordRpc?.updateSong(
                song,
                player.currentPosition,
                player.playbackParameters.speed,
                useDetails,
                status,
                b1Text,
                b1Visible,
                b2Text,
                b2Visible,
                activityType,
                activityName
            )?.onFailure {
                // Rate limited or error
                if (showFeedback) {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(this@MusicService, "Discord RPC update failed: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun createDataSourceFactory(): DataSource.Factory {
        return ResolvingDataSource.Factory(createCacheDataSource()) { dataSpec ->
            val mediaId = dataSpec.key ?: error("No media id")

            // Check if we need to bypass cache for quality change
            val shouldBypassCache = bypassCacheForQualityChange.contains(mediaId)

            if (!shouldBypassCache) {
                val isCachingDisabled = dataStore.get(DisableSongCacheKey, false)
                if (downloadCache.completeAudioCachedAt(mediaId) != null ||
                    (!isCachingDisabled && playerCache.completeAudioCachedAt(mediaId) != null)
                ) {
                    return@Factory dataSpec
                }

                songUrlCache[mediaId]?.let { stream ->
                    scope.launch(Dispatchers.IO) { recoverSong(mediaId) }
                    return@Factory dataSpec.withUri(stream.url.toUri())
                        .withRequestHeaders(dataSpec.httpRequestHeaders + stream.requestHeaders)
                }
            } else {
                Timber.tag("MusicService").i("BYPASSING CACHE for $mediaId due to quality change")
            }

            Timber.tag("MusicService").i("FETCHING STREAM: $mediaId | quality=$audioQuality")
            val cacheGeneration = songUrlCache.generation(mediaId)
            val playbackData = runBlocking(Dispatchers.IO) {
                YTPlayerUtils.playerResponseForPlayback(
                    mediaId,
                    audioQuality = audioQuality,
                    connectivityManager = connectivityManager,
                    context = this@MusicService,
                )
            }.getOrElse { throwable ->
                when (throwable) {
                    is PlaybackException -> throw throwable

                    is java.net.ConnectException, is java.net.UnknownHostException -> {
                        throw PlaybackException(
                            getString(R.string.error_no_internet),
                            throwable,
                            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED
                        )
                    }

                    is java.net.SocketTimeoutException -> {
                        throw PlaybackException(
                            getString(R.string.error_timeout),
                            throwable,
                            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT
                        )
                    }

                    else -> throw PlaybackException(
                        getString(R.string.error_unknown),
                        throwable,
                        PlaybackException.ERROR_CODE_REMOTE_ERROR
                    )
                }
            }

            val nonNullPlayback = requireNotNull(playbackData) {
                getString(R.string.error_unknown)
            }
            run {
                val format = nonNullPlayback.format
                val loudnessDb = nonNullPlayback.audioConfig?.loudnessDb
                val perceptualLoudnessDb = nonNullPlayback.audioConfig?.perceptualLoudnessDb

                Timber.tag(TAG).d("Storing format for $mediaId with loudnessDb: $loudnessDb, perceptualLoudnessDb: $perceptualLoudnessDb")
                if (loudnessDb == null && perceptualLoudnessDb == null) {
                    Timber.tag(TAG).w("No loudness data available from YouTube for video: $mediaId")
                }

                database.query {
                    upsert(
                        FormatEntity(
                            id = mediaId,
                            itag = format.itag,
                            mimeType = format.mimeType.split(";")[0],
                            codecs = format.mimeType.split("codecs=").getOrNull(1)?.removeSurrounding("\"") ?: "mp3",
                            bitrate = format.bitrate,
                            sampleRate = format.audioSampleRate,
                            contentLength = format.contentLength ?: 0L,
                            loudnessDb = loudnessDb,
                            perceptualLoudnessDb = perceptualLoudnessDb,
                            playbackUrl = nonNullPlayback.playbackTracking?.videostatsPlaybackUrl?.baseUrl
                        )
                    )
                }
                scope.launch(Dispatchers.IO) { recoverSong(mediaId, nonNullPlayback) }

                // Clear bypass flag now that we've fetched fresh stream
                if (bypassCacheForQualityChange.remove(mediaId)) {
                    Timber.tag("MusicService").d("Cleared bypass cache flag for $mediaId after fresh fetch")
                }

                val streamUrl = nonNullPlayback.streamUrl

                songUrlCache.put(
                    mediaId, streamUrl, nonNullPlayback.streamHeaders,
                    nonNullPlayback.streamClient, nonNullPlayback.streamExpiresInSeconds,
                    expectedGeneration = cacheGeneration,
                )
                return@Factory dataSpec.withUri(streamUrl.toUri())
                    .withRequestHeaders(dataSpec.httpRequestHeaders + nonNullPlayback.streamHeaders)
                    .subrange(dataSpec.uriPositionOffset, CHUNK_LENGTH)
            }
        }
    }

    private fun createMediaSourceFactory() =
        DefaultMediaSourceFactory(
            createDataSourceFactory(),
            ExtractorsFactory {
                arrayOf(
                    MatroskaExtractor(),        // .webm / Opus
                    FragmentedMp4Extractor(),   // fragmented .mp4 / AAC (YouTube)
                    Mp4Extractor(),             // regular .mp4 / AAC
                )
            },
        )

    private fun createRenderersFactory(
        eqProcessor: CustomEqualizerAudioProcessor,
        silenceProcessor: SilenceDetectorAudioProcessor
    ) =
        object : DefaultRenderersFactory(this) {
            override fun buildAudioSink(
                context: Context,
                enableFloatOutput: Boolean,
                enableAudioTrackPlaybackParams: Boolean,
            ) = DefaultAudioSink
                .Builder(this@MusicService)
                .setEnableFloatOutput(enableFloatOutput)
                .setEnableAudioTrackPlaybackParams(enableAudioTrackPlaybackParams)
                .setAudioProcessorChain(
                    DefaultAudioSink.DefaultAudioProcessorChain(
                        // 2. Inject processor into audio pipeline
                        arrayOf(
                            eqProcessor,
                            silenceProcessor,
                        ),
                        SilenceSkippingAudioProcessor(2_000_000, 20_000, 256),
                        SonicAudioProcessor(),
                    ),
                ).build()
        }

    override fun onPlaybackStatsReady(
        eventTime: AnalyticsListener.EventTime,
        playbackStats: PlaybackStats,
    ) {
        if (eventTime.timeline.isEmpty) return
        val mediaItem = eventTime.timeline.getWindow(eventTime.windowIndex, Timeline.Window()).mediaItem
        val remoteHistoryMinimumMs = (dataStore[HistoryDuration]?.times(1000f) ?: 5000f).toLong()

        if (playbackStats.totalPlayTimeMs >= remoteHistoryMinimumMs &&
            !dataStore.get(PauseListenHistoryKey, false)
        ) {
            // Transient playback caching: ensure played song is cached in playerCache without invoking explicit download workflow
            if (!dataStore.get(DisableSongCacheKey, false)) {
                scope.launch(Dispatchers.IO) {
                    try {
                        if (!isFullyCached(mediaItem.mediaId)) {
                            cacheSongInBackground(mediaItem.mediaId)
                            Timber.tag(TAG).d("Scheduled background playerCache caching for ${mediaItem.mediaId}")
                        } else {
                            markAutomaticallyCached(mediaItem.mediaId)
                        }
                    } catch (e: Exception) {
                        Timber.tag(TAG).w(e, "Could not background cache playback song ${mediaItem.mediaId}")
                    }
                }
            }
        }

        if (playbackStats.totalPlayTimeMs >= remoteHistoryMinimumMs) {
            CoroutineScope(Dispatchers.IO).launch {
                // Must fetch a FRESH playback tracking object. Cached `playbackUrl`s from the database
                // have expired security nonces/signatures and will result in YouTube silently ignoring the
                // history registration despite returning an HTTP 200/204 response.
                val freshPlayerResponse = YTPlayerUtils.playerResponseForMetadata(mediaItem.mediaId, null).getOrNull()
                
                val playbackUrl = freshPlayerResponse?.playbackTracking?.videostatsPlaybackUrl?.baseUrl
                val watchtimeUrl = freshPlayerResponse?.playbackTracking?.videostatsWatchtimeUrl?.baseUrl
                
                playbackUrl?.let { baseUrl ->
                    YouTube.registerPlayback(null, baseUrl)
                        .onSuccess {
                            // Also optionally ping watchtime to be absolutely sure the view registers
                            watchtimeUrl?.let { wtUrl -> 
                                YouTube.registerPlayback(null, wtUrl)
                            }
                            playbackRegistered.tryEmit(mediaItem.mediaId)
                        }
                        .onFailure {
                            reportException(it)
                        }
                }
            }
        }
    }

    private fun saveQueueToDisk() {
        if (restoringQueue || !::playerVolume.isInitialized) return
        if (player.mediaItemCount == 0 && !queuePreviouslyAvailable) return
        queuePreviouslyAvailable = player.mediaItemCount > 0

        val retained = player.mediaItems.mapIndexedNotNull { index, item -> item.metadata?.let { index to it } }
        val indices = retained.map { it.first }
        val index = if (retained.isEmpty()) 0 else restoredQueueIndex(player.currentMediaItemIndex, indices)
        val position = if (indices.getOrNull(index) == player.currentMediaItemIndex) player.currentPosition else 0L
        val queue = currentQueue.toPersistQueue(
            title = queueTitle,
            items = retained.map { it.second },
            mediaItemIndex = index,
            position = position,
        )
        val shuffle = if (player.shuffleModeEnabled) {
            remapSavedShuffleOrder(getCurrentShuffleOrderIndices(player), indices)
        } else null
        val automix = PersistQueue(
            title = "automix",
            items = automixItems.value.mapNotNull { it.metadata },
            mediaItemIndex = 0,
            position = 0,
        )
        val state = PersistPlayerState(
            playWhenReady = player.playWhenReady,
            repeatMode = player.repeatMode,
            shuffleModeEnabled = player.shuffleModeEnabled,
            volume = playerVolume.value,
            currentPosition = position,
            currentMediaItemIndex = index,
            playbackState = player.playbackState,
        )
        queuePersistence.save(QueuePersistence.Snapshot(QueuePersistence.SavedQueue(queue, shuffle), automix, state))
    }

    override fun onDestroy() {
        isRunning = false
        scope.cancel()

        try {
            unregisterReceiver(screenStateReceiver)
        } catch (e: Exception) {
            // Ignore
        }
        audioManager.unregisterAudioDeviceCallback(audioDeviceCallback)
        castConnectionHandler?.release()
        if (dataStore.get(PersistentQueueKey, true)) {
            saveQueueToDisk()
        }
        if (discordRpc?.isRpcRunning() == true) {
            discordRpc?.closeRPC()
        }
        discordRpc = null
        connectivityObserver.unregister()
        abandonAudioFocus()
        releaseLoudnessEnhancer()
        runCatching { equalizerController.release() }
        removeSession(mediaSession)
        mediaSession.release()
        stopForeground(STOP_FOREGROUND_REMOVE)
        getSystemService(NotificationManager::class.java)?.cancel(NOTIFICATION_ID)
        player.removeListener(this)
        player.removeListener(sleepTimer)
        releaseCrossfadePlayers()
        releasePlaybackPlayer(player)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = super.onBind(intent) ?: binder

    override fun onTaskRemoved(rootIntent: Intent?) {
        if (!shouldStopPlaybackOnTaskRemoval(
                stopOnTaskClear = dataStore.get(StopMusicOnTaskClearKey, false),
                hasQueue = player.mediaItemCount > 0,
                isLoadingQueue = queueLoadJob?.isActive == true,
                isCasting = castConnectionHandler?.isCasting?.value == true,
            )
        ) return

        queueLoadJob?.cancel()
        mediaButtonStartupJob?.cancel()
        castConnectionHandler?.pause()
        player.pause()
        releaseCrossfadePlayers()
        pauseAllPlayersAndStopSelf()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun cancelCrossfadePreparation() {
        if (pendingQueueSwitch) {
            pendingQueueSwitch = false
            queueLoadJob?.cancel()
        }
        val job = crossfadeTriggerJob
        crossfadeTriggerJob = null
        automaticFade = null
        job?.cancel()
        discardCrossfadePreparation()
    }

    private fun discardCrossfadePreparation() {
        val pending = crossfadePreparation ?: return
        crossfadePreparation = null
        pending.ready.cancel()
        pending.audio.cancel()
        pending.player.removeListener(pending.listener)
        pending.player.removeAnalyticsListener(pending.analytics)
        releasePlaybackPlayer(pending.player)
    }

    private fun finishCrossfade() {
        val outgoing = fadingPlayer ?: return
        fadingPlayer = null
        val job = crossfadeJob
        crossfadeJob = null
        job?.cancel()
        outgoing.removeListener(outgoingListener)
        outgoing.volume = 0f
        applyPlaybackVolume()
        releasePlaybackPlayer(outgoing)
    }

    private fun endOutgoingPlayback() {
        val outgoing = fadingPlayer ?: return
        val mediaId = outgoing.currentMediaItem?.mediaId
        finishCrossfade()
        if (mediaId != null && mediaId != player.currentMediaItem?.mediaId) {
            scope.launch(Dispatchers.IO) { markAutomaticallyCached(mediaId) }
        }
        sleepTimer.notifySongTransition()
        scheduleCrossfade()
    }

    private fun releaseCrossfadePlayers() {
        cancelCrossfadePreparation()
        finishCrossfade()
    }

    private fun releasePlaybackPlayer(player: ExoPlayer) {
        playerSilenceProcessors.remove(player)
        playerEqualizerProcessors.remove(player)?.let(equalizerService::removeAudioProcessor)
        player.release()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo) = mediaSession

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == Intent.ACTION_MEDIA_BUTTON && intent.hasExtra(Intent.EXTRA_KEY_EVENT) && !isPlaybackOngoing()) {
            startMediaButtonForeground(startId)
        }
        when (intent?.action) {
            MusicWidgetReceiver.ACTION_PLAY_PAUSE -> {
                if (player.isPlaying) player.pause() else player.play()
                updateWidgetUI(player.isPlaying)
            }
            MusicWidgetReceiver.ACTION_LIKE -> {
                toggleLike()
            }
            MusicWidgetReceiver.ACTION_NEXT -> {
                if (!manualSkipToNextWithCrossfade()) {
                    player.seekToNext()
                }
                updateWidgetUI(player.isPlaying)
            }
            MusicWidgetReceiver.ACTION_PREVIOUS -> {
                if (!manualSkipToPreviousWithCrossfade()) {
                    player.seekToPrevious()
                }
                updateWidgetUI(player.isPlaying)
            }
            MusicWidgetReceiver.ACTION_UPDATE_WIDGET -> {
                updateWidgetUI(player.isPlaying)
            }
        }

        super.onStartCommand(intent, flags, startId)
        if (intent?.action != Intent.ACTION_MEDIA_BUTTON &&
            player.mediaItemCount == 0 && queueLoadJob?.isActive != true &&
            castConnectionHandler?.isCasting?.value != true
        ) {
            stopSelf(startId)
        }
        return START_NOT_STICKY
    }

    private fun startMediaButtonForeground(startId: Int) {
        // Media3's hardware receiver calls startForegroundService even before playback can resume.
        getSystemService(NotificationManager::class.java)?.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, getString(R.string.music_player), NotificationManager.IMPORTANCE_LOW),
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setSmallIcon(R.drawable.mkrmusicnotification)
            .setContentIntent(mediaSession.sessionActivity)
            .setOngoing(true)
            .build()
        try {
            startForeground(NOTIFICATION_ID, notification)
        } catch (error: IllegalStateException) {
            Timber.w(error, "Foreground startup unavailable for media button")
            stopSelf(startId)
            return
        } catch (error: SecurityException) {
            Timber.w(error, "Foreground startup denied for media button")
            stopSelf(startId)
            return
        }
        mediaButtonStartupJob?.cancel()
        mediaButtonStartupJob = scope.launch {
            delay(5_000L)
            if (!isPlaybackOngoing()) {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf(startId)
            }
        }
    }

    /**
     * Updates all app widgets with current playback state
     */
    private fun updateWidgetUI(isPlaying: Boolean) {
        scope.launch {
            try {
                val songData = currentSong.value
                val song = songData?.song
                val songTitle = song?.title ?: getString(R.string.no_song_playing)
                val artistName = songData?.artists?.joinToString(", ") { it.name } ?: getString(R.string.tap_to_open)
                val isLiked = songData?.song?.liked == true

                widgetManager.updateWidgets(
                    title = songTitle,
                    artist = artistName,
                    artworkUri = song?.thumbnailUrl,
                    isPlaying = isPlaying,
                    isLiked = isLiked,
                    duration = if (player.duration != C.TIME_UNSET) player.duration else 0,
                    currentPosition = player.currentPosition
                )
            } catch (e: Exception) {
                // Widget not added to home screen or other error
            }
        }
    }

    private var widgetUpdateJob: Job? = null

    private fun startWidgetUpdates() {
        widgetUpdateJob?.cancel()
        widgetUpdateJob = scope.launch {
            while (isActive) {
                if (player.isPlaying) {
                    updateWidgetUI(true)
                }
                delay(1000)
            }
        }
    }

    private fun stopWidgetUpdates() {
        widgetUpdateJob?.cancel()
        widgetUpdateJob = null
    }

    private fun shareSong() {
        val songData = currentSong.value
        val songId = songData?.song?.id ?: return

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "https://music.youtube.com/watch?v=$songId")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(Intent.createChooser(shareIntent, null).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    /**
     * Get the stream URL for a given media ID.
     * This is used for Google Cast to send the audio URL to Chromecast.
     */
    suspend fun getStreamUrl(mediaId: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val playbackData = YTPlayerUtils.playerResponseForPlayback(
                    videoId = mediaId,
                    audioQuality = audioQuality,
                    connectivityManager = connectivityManager,
                    context = this@MusicService,
                ).getOrNull()
                playbackData?.streamUrl
            } catch (e: Exception) {
                timber.log.Timber.e(e, "Failed to get stream URL for Cast")
                null
            }
        }
    }

    /**
     * Initialize Google Cast support
     */
    private fun initializeCast() {
        if (dataStore.get(com.music.mkr.constants.EnableGoogleCastKey, true)) {
            try {
                castConnectionHandler = CastConnectionHandler(this, scope, this)
                castConnectionHandler?.initialize()
                timber.log.Timber.d("Google Cast initialized")
            } catch (e: Exception) {
                timber.log.Timber.e(e, "Failed to initialize Google Cast")
            }
        }
    }


    override fun onPositionDiscontinuity(
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        reason: Int,
    ) {
        if (reason == Player.DISCONTINUITY_REASON_SEEK) {
            releaseCrossfadePlayers()
            scheduleCrossfade()
            if (dataStore.get(PersistentQueueKey, true)) saveQueueToDisk()
        }
    }

    private fun currentCrossfadeSource(): CrossfadeSource? {
        val source = player
        val index = source.currentMediaItemIndex
        val timeline = source.currentTimeline
        if (!crossfadeEnabled || crossfadeDurationMs <= 0 || !source.isPlaying || !hasAudioFocus ||
            castConnectionHandler?.isCasting?.value == true || timeline.isEmpty ||
            index !in 0 until timeline.windowCount || source.isCurrentMediaItemLive || source.duration <= 0
        ) return null
        val speed = source.playbackParameters.speed
        val overlap = crossfadeLengthMs(crossfadeDurationMs, source.duration, speed)
        if (overlap < CROSSFADE_MIN_OVERLAP_MS) return null
        return CrossfadeSource(source, timeline.getWindow(index, Timeline.Window()).uid, source.duration, speed, overlap)
    }

    private fun isCurrent(source: CrossfadeSource): Boolean {
        val active = player
        val index = active.currentMediaItemIndex
        val timeline = active.currentTimeline
        return active === source.player && active.isPlaying && hasAudioFocus && crossfadeEnabled &&
            castConnectionHandler?.isCasting?.value != true && index in 0 until timeline.windowCount &&
            timeline.getWindow(index, Timeline.Window()).uid == source.windowUid &&
            active.playbackParameters.speed == source.speed
    }

    private fun queuedTarget(index: Int): CrossfadeTarget? {
        val timeline = player.currentTimeline
        if (index !in 0 until timeline.windowCount) return null
        return CrossfadeTarget(index, timeline.getWindow(index, Timeline.Window()).uid, player.getMediaItemAt(index))
    }

    private fun automaticCrossfade(): AutomaticCrossfade? {
        if (sleepTimer.pauseWhenSongEnd) return null
        val source = currentCrossfadeSource() ?: return null
        val repeatOne = source.player.repeatMode == REPEAT_MODE_ONE
        val index = if (repeatOne) source.player.currentMediaItemIndex else source.player.nextMediaItemIndex
        val target = queuedTarget(index) ?: return null
        if (index != source.player.currentMediaItemIndex && shouldKeepGapless(target.item)) return null
        return AutomaticCrossfade(source, target, index == source.player.currentMediaItemIndex)
    }

    private fun shouldKeepGapless(item: MediaItem): Boolean {
        val current = player.currentMediaItem?.mediaMetadata ?: return false
        val next = item.mediaMetadata
        return crossfadeGapless && current.albumTitle != null && current.albumTitle == next.albumTitle &&
            (current.albumArtist == null || next.albumArtist == null || current.albumArtist == next.albumArtist)
    }

    private fun scheduleCrossfade() {
        if (fadingPlayer != null || queueLoadJob?.isActive == true ||
            (crossfadeTriggerJob != null && automaticFade == null) ||
            (crossfadePreparation != null && automaticFade == null)
        ) return
        val plan = automaticCrossfade()
        if (plan == automaticFade) return
        cancelCrossfadePreparation()
        if (plan == null) return
        automaticFade = plan
        launchCrossfadeRequest {
            val source = plan.source
            delay(crossfadePreparationDelayMs(source.remainingMs(), source.overlap))
            if (automaticCrossfade() != plan) return@launchCrossfadeRequest
            prepareCrossfade(
                source = source,
                item = plan.target.item,
                valid = { automaticCrossfade() == plan },
                automaticReason = if (plan.repeat) Player.MEDIA_ITEM_TRANSITION_REASON_REPEAT else Player.MEDIA_ITEM_TRANSITION_REASON_AUTO,
            ) { next ->
                copyCrossfadeQueue(next, plan.target.index)
            }
        }
    }

    fun manualSkipToNextWithCrossfade(): Boolean = startManualCrossfade(player.nextMediaItemIndex, honorGapless = true)

    fun manualSkipToPreviousWithCrossfade(): Boolean = startManualCrossfade(player.previousMediaItemIndex)

    fun manualSeekToIndexWithCrossfade(targetIndex: Int): Boolean = startManualCrossfade(targetIndex)

    private fun startManualCrossfade(targetIndex: Int, honorGapless: Boolean = false): Boolean {
        val interrupted = fadingPlayer != null || pendingQueueSwitch ||
            (crossfadeTriggerJob != null && automaticFade == null) ||
            (crossfadePreparation != null && automaticFade == null)
        releaseCrossfadePlayers()
        if (interrupted || !crossfadeManualSkipEnabled || targetIndex == player.currentMediaItemIndex) return false
        val source = currentCrossfadeSource() ?: return false
        val target = queuedTarget(targetIndex) ?: return false
        if (honorGapless && shouldKeepGapless(target.item)) return false

        launchCrossfadeRequest {
            val valid = { isCurrent(source) && queuedTarget(targetIndex) == target }
            val swapped = prepareCrossfade(source, target.item, valid) { next ->
                copyCrossfadeQueue(next, targetIndex)
            }
            if (!swapped && valid()) {
                player.seekToDefaultPosition(targetIndex)
                if (player.playbackState == STATE_IDLE || player.playbackState == Player.STATE_ENDED) player.prepare()
                player.play()
            }
        }
        return true
    }

    private fun launchCrossfadeRequest(action: suspend () -> Unit) {
        val job = scope.launch(start = CoroutineStart.LAZY) {
            try {
                action()
            } finally {
                if (crossfadeTriggerJob === coroutineContext[Job]) {
                    crossfadeTriggerJob = null
                    automaticFade = null
                }
            }
        }
        crossfadeTriggerJob = job
        job.start()
    }

    private fun copyCrossfadeQueue(next: ExoPlayer, targetIndex: Int) {
        val source = player
        val items = source.mediaItems.toList()
        val order = if (source.shuffleModeEnabled) getCurrentShuffleOrderIndices(source) else null
        next.addMediaItems(0, items.take(targetIndex))
        next.addMediaItems(items.drop(targetIndex + 1))
        next.repeatMode = source.repeatMode
        next.shuffleModeEnabled = source.shuffleModeEnabled
        if (order != null) next.setShuffleOrder(DefaultShuffleOrder(order, System.currentTimeMillis()))
    }

    private suspend fun crossfadeIntoQueue(
        source: CrossfadeSource,
        items: List<MediaItem>,
        targetIndex: Int,
        position: Long,
        persistShuffle: Boolean,
        selectQueue: () -> Unit,
    ): Boolean {
        if (items.isEmpty()) return false
        val index = targetIndex.coerceIn(items.indices)
        return prepareCrossfade(
            source = source,
            item = items[index],
            valid = { isCurrent(source) },
            position = position,
            beforeHandoff = selectQueue,
        ) { next ->
            if (items.size > 1) {
                next.addMediaItems(0, items.take(index))
                next.addMediaItems(items.drop(index + 1))
            }
            next.repeatMode = source.player.repeatMode
            next.shuffleModeEnabled = persistShuffle && source.player.shuffleModeEnabled
            val order = if (next.shuffleModeEnabled && items == source.player.mediaItems.toList()) {
                getCurrentShuffleOrderIndices(source.player)
            } else null
            if (order != null) {
                next.setShuffleOrder(DefaultShuffleOrder(order, System.currentTimeMillis()))
            } else if (next.shuffleModeEnabled && items.size > 1) {
                val shuffled = intArrayOf(index) + items.indices.filter { it != index }.shuffled().toIntArray()
                next.setShuffleOrder(DefaultShuffleOrder(shuffled, System.currentTimeMillis()))
            }
        }
    }

    private suspend fun prepareCrossfade(
        source: CrossfadeSource,
        item: MediaItem,
        valid: () -> Boolean,
        position: Long = 0L,
        automaticReason: Int? = null,
        beforeHandoff: () -> Unit = {},
        installQueue: (ExoPlayer) -> Unit,
    ): Boolean {
        if (!valid() || crossfadePreparation != null || fadingPlayer != null || source.remainingMs() < CROSSFADE_MIN_OVERLAP_MS) return false
        val next = try {
            createExoPlayer(forCrossfade = true)
        } catch (error: Exception) {
            Timber.w(error, "Could not create crossfade player")
            return false
        }
        val pending = CrossfadePreparation(next)
        crossfadePreparation = pending
        try {
            next.addListener(pending.listener)
            next.addAnalyticsListener(pending.analytics)
            next.playbackParameters = source.player.playbackParameters
            next.volume = 0f
            next.setMediaItem(item, position)
            next.prepare()

            val readyTimeout = if (automaticReason != null) crossfadeStartupBudgetMs(source.remainingMs())
                else minOf(crossfadeStartupBudgetMs(source.remainingMs()), CROSSFADE_PREPARE_LEAD_MS)
            if (withTimeoutOrNull(readyTimeout) { pending.ready.await() } != true ||
                !valid() || next.isCurrentMediaItemLive || next.duration <= 0
            ) return false

            val incomingLength = (next.duration - next.currentPosition).coerceAtLeast(0)
            val overlap = crossfadeLengthMs(source.overlap, incomingLength, source.speed)
            if (overlap < CROSSFADE_MIN_OVERLAP_MS) return false
            if (automaticReason != null) delay((source.remainingMs() - overlap).coerceAtLeast(0))
            if (!valid() || next.playbackState != Player.STATE_READY) return false

            val budget = crossfadeStartupBudgetMs(source.remainingMs())
            if (budget == 0L) return false
            // A prepared decoder may still need time to start rendering on some routes.
            next.play()
            val startupTimeout = if (automaticReason != null) budget else minOf(budget, 1_000L)
            val audio = withTimeoutOrNull(startupTimeout) { pending.audio.await() }
            if (audio == null || !valid() || !next.isPlaying || next.currentMediaItem != item) return false

            installQueue(next)
            if (!valid() || !next.isPlaying || next.currentMediaItem != item) return false
            val duration = minOf(overlap, source.remainingMs(), remainingPlaybackMs(next.duration, next.currentPosition, source.speed))
            if (duration < CROSSFADE_MIN_OVERLAP_MS) return false

            next.removeListener(pending.listener)
            next.removeAnalyticsListener(pending.analytics)
            performCrossfadeSwap(next, duration, audio, automaticReason ?: Player.MEDIA_ITEM_TRANSITION_REASON_SEEK, beforeHandoff)
            return true
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Timber.w(error, "Crossfade preparation failed")
            if (player === next) {
                finishCrossfade()
                return true
            }
            return false
        } finally {
            if (crossfadePreparation === pending) discardCrossfadePreparation()
        }
    }

    private fun performCrossfadeSwap(
        next: ExoPlayer,
        duration: Long,
        audio: Pair<AnalyticsListener.EventTime, Long>,
        reason: Int,
        beforeHandoff: () -> Unit,
    ) {
        val outgoing = player
        mediaSession.player = next
        player = next
        crossfadePreparation = null
        fadingPlayer = outgoing
        outgoing.removeListener(this)
        outgoing.removeListener(sleepTimer)
        silenceSkipJob?.cancel()
        playerSilenceProcessors[outgoing]?.instantModeEnabled = false
        playerSilenceProcessors[next]?.instantModeEnabled = instantSilenceSkipEnabled.value
        // Prevent the outgoing player from starting a second queue item.
        outgoing.addListener(outgoingListener)
        outgoing.setPauseAtEndOfMediaItems(true)
        crossfadeProgress = 0f
        crossfadeNeedsRestart = false
        val curve = crossfadeCurve
        activeCrossfadeCurve = curve
        next.addListener(this)
        next.addListener(sleepTimer)
        sleepTimer.player = next
        _playerFlow.value = next
        val history = attachPlaybackAnalytics(next)
        history.onAudioPositionAdvancing(audio.first, audio.second)
        beforeHandoff()
        openAudioEffectSession()
        previousMediaItemIndex = C.INDEX_UNSET
        handleMediaItemTransition(next.currentMediaItem, reason)
        updateWidgetUI(next.isPlaying)
        updateOutgoingPlayback()
        applyPlaybackVolume()

        var startPosition = next.currentPosition
        var overlapDuration = duration
        val speed = next.playbackParameters.speed
        crossfadeJob = scope.launch {
            try {
                while (isActive && fadingPlayer === outgoing && player === next) {
                    if (withTimeoutOrNull(5_000L) { overlapPlaying.first { it } } != true) break
                    if (crossfadeNeedsRestart) {
                        crossfadeNeedsRestart = false
                        startPosition = next.currentPosition
                        overlapDuration = minOf(duration, remainingPlaybackMs(outgoing.duration, outgoing.currentPosition, speed))
                        if (overlapDuration < CROSSFADE_MIN_OVERLAP_MS) break
                    }
                    val elapsed = (next.currentPosition - startPosition).coerceAtLeast(0) / speed
                    crossfadeProgress = (elapsed / overlapDuration).coerceIn(0f, 1f)
                    applyPlaybackVolume()
                    if (crossfadeProgress >= 1f || outgoing.playbackState != Player.STATE_READY ||
                        !outgoing.playWhenReady
                    ) break
                    delay(20)
                }
            } finally {
                if (fadingPlayer === outgoing) endOutgoingPlayback()
            }
        }
    }

    private fun updateOutgoingPlayback() {
        val outgoing = fadingPlayer ?: return
        val transition = outgoingPlayback(
            focusGranted = hasAudioFocus,
            playRequested = player.playWhenReady,
            suppressed = player.playbackSuppressionReason != Player.PLAYBACK_SUPPRESSION_REASON_NONE,
            incomingPlaying = player.isPlaying,
            incomingBuffering = player.playbackState == Player.STATE_BUFFERING,
        )
        overlapPlaying.value = transition == OutgoingPlayback.Fade
        when (transition) {
            OutgoingPlayback.Pause -> outgoing.pause()
            OutgoingPlayback.Fade -> outgoing.play()
            OutgoingPlayback.Carry -> {
                crossfadeNeedsRestart = true
                crossfadeProgress = 0f
                applyPlaybackVolume()
                outgoing.play()
            }
        }
    }

    private fun CrossfadeSource.remainingMs() = remainingPlaybackMs(duration, player.currentPosition, speed)

    companion object {
        const val ROOT = "root"
        const val SONG = "song"
        const val ARTIST = "artist"
        const val ALBUM = "album"
        const val PLAYLIST = "playlist"
        const val YOUTUBE_PLAYLIST = "youtube_playlist"
        const val SEARCH = "search"
        const val SHUFFLE_ACTION = "__shuffle__"

        const val CHANNEL_ID = "music_channel_01"
        const val NOTIFICATION_ID = 888
        const val ERROR_CODE_NO_STREAM = 1000001
        const val CHUNK_LENGTH = 512 * 1024L
        const val PERSISTENT_QUEUE_FILE = "persistent_queue.data"
        const val PERSISTENT_AUTOMIX_FILE = "persistent_automix.data"
        const val PERSISTENT_PLAYER_STATE_FILE = "persistent_player_state.data"
        const val MAX_CONSECUTIVE_ERR = 5
        const val MAX_RETRY_COUNT = 10
        // Constants for audio normalization
        // Cap positive gain at 0 mB so LoudnessEnhancer never acts as a boomy bass compressor
        private const val MAX_GAIN_MB = 0
        private const val MIN_GAIN_MB = -1500 // Minimum gain in millibels (-15 dB)

        private const val TAG = "MusicService"

        @Volatile
        var isRunning = false
            private set
    }
}

private data class SponsorBlockConfig(
    val enabled: Boolean,
    val serverUrl: String,
    val skipNonMusic: Boolean,
    val skipSponsor: Boolean,
    val skipSelfPromo: Boolean,
    val skipInteraction: Boolean,
    val skipIntroOutro: Boolean,
    val skipPreviewFiller: Boolean,
    val showToast: Boolean,
)
