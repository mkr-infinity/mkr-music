import React, { useState } from 'react';
import {
  Palette,
  Users,
  Smartphone,
  Disc3,
  Zap,
  HardDrive,
  Sliders,
  ShieldCheck,
  AudioLines,
  ArrowRight,
  Eye,
  Radio,
  Share2,
  Activity,
  Layers,
  Music2,
  Volume2,
  CheckCircle2
} from 'lucide-react';
import { getAssetUrl, handleScreenshotError, handleDeveloperAvatarError } from '@/lib/assets';
import {
  DiscordIcon,
  LastfmIcon,
  SpotifyIcon,
  YouTubeMusicIcon
} from './BrandIcons';
import { InteractiveEqualizer } from './InteractiveEqualizer';

interface FeaturesShowcaseProps {
  onOpenScreenshot: (index: number) => void;
}

export const FeaturesShowcase: React.FC<FeaturesShowcaseProps> = ({ onOpenScreenshot }) => {
  const [cacheDelay, setCacheDelay] = useState<number>(10);
  const [activeEngineTab, setActiveEngineTab] = useState<'spotify' | 'yt' | 'jiosaavn'>('spotify');

  const engineTabs = {
    spotify: {
      title: 'Import Spotify Playlists',
      service: 'Spotify Migration',
      tag: 'ZERO COST',
      shotIndex: 0,
      file: 'spotify.jpg',
      icon: SpotifyIcon,
      accent: 'green',
      headline: 'Bring All Your Playlists Over Instantly',
      description:
        'Migrate unlimited Spotify playlists, followed artists, and liked tracks in seconds with automated acoustic track matching. Zero subscription required.',
      metric: 'Instant Sync',
      badge: 'No API Key Needed',
    },
    yt: {
      title: 'YouTube Music Account Sync',
      service: 'YT Music Engine',
      tag: 'BI-DIRECTIONAL',
      shotIndex: 1,
      file: 'ytsync.jpg',
      icon: YouTubeMusicIcon,
      accent: 'yellow',
      headline: 'Synchronize Your Complete YouTube Music Library',
      description:
        'Seamlessly sync your personal likes, curated algorithmic mixes, custom playlists, and listening history with low-latency streaming.',
      metric: 'Live Cloud Sync',
      badge: 'Account Paired',
    },
    jiosaavn: {
      title: 'JioSaavn & YT Music All-in-One',
      service: 'Dual Engine Stream',
      tag: 'REGIONAL & GLOBAL',
      shotIndex: 2,
      file: 'iosavan.jpg',
      icon: Layers,
      accent: 'cream',
      headline: 'Stream Millions of Regional & Global Tracks',
      description:
        'Search across JioSaavn’s rich Indian regional catalogue and YouTube Music’s global library in a single search bar and unified audio queue.',
      metric: 'Dual Catalog',
      badge: '3-in-1 Architecture',
    },
  };

  const activeEngine = engineTabs[activeEngineTab];
  const ActiveEngineIcon = activeEngine.icon;

  return (
    <section id="features" className="py-24 md:py-32 relative overflow-hidden" aria-label="MKR Music Features">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
        
        {/* Section Header */}
        <div className="text-center max-w-3xl mx-auto mb-16 md:mb-20">
          <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-graphite-900/90 border border-white/10 text-xs font-mono text-anime-green mb-4">
            <Radio size={13} className="text-anime-green" />
            <span>Tri-Engine & Connected Ecosystem</span>
          </div>
          <h2 className="text-3xl sm:text-5xl lg:text-6xl font-extrabold text-anime-cream tracking-tight mb-6">
            3 Streaming Universes. <br />
            <span className="text-transparent bg-clip-text bg-gradient-to-r from-anime-green via-anime-yellow to-anime-cream">
              One Unified Audio Haven.
            </span>
          </h2>
          <p className="text-base sm:text-lg text-anime-cream/70 leading-relaxed">
            Import Spotify playlists, sync your YouTube Music library, and stream JioSaavn regional audio simultaneously in one app — completely free and without ads.
          </p>
        </div>

        {/* 1. ASYMMETRIC BENTO GRID */}
        <div className="grid grid-cols-1 md:grid-cols-12 gap-6 mb-16">
          
          {/* BENTO BLOCK 1: Hero Tri-Engine Interactive Card (Span 12 on mobile, 8 on desktop) */}
          <div className="md:col-span-12 lg:col-span-8 neo-glass-card rounded-3xl p-6 sm:p-8 flex flex-col justify-between border-white/10 relative overflow-hidden group">
            
            {/* Ambient Background Gradient */}
            <div className="absolute top-0 right-0 w-96 h-96 bg-gradient-to-bl from-anime-green/10 via-anime-yellow/5 to-transparent rounded-full blur-3xl pointer-events-none" />

            <div>
              {/* Tab Selector */}
              <div className="flex flex-wrap items-center justify-between gap-3 mb-6">
                <span className="text-[10px] font-mono uppercase tracking-widest text-anime-green font-bold">
                  TRI-ENGINE ARCHITECTURE
                </span>
                
                <div className="flex items-center gap-1.5 p-1 rounded-2xl bg-graphite-950/80 border border-white/10">
                  {(['spotify', 'yt', 'jiosaavn'] as const).map((key) => {
                    const tab = engineTabs[key];
                    const Icon = tab.icon;
                    const isActive = activeEngineTab === key;
                    return (
                      <button
                        key={key}
                        type="button"
                        onClick={() => setActiveEngineTab(key)}
                        className={`flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-semibold transition-all cursor-pointer ${
                          isActive
                            ? 'bg-anime-green text-graphite-950 font-bold shadow-neo-glow-green scale-102'
                            : 'text-anime-cream/70 hover:text-anime-cream hover:bg-white/5'
                        }`}
                      >
                        <Icon size={13} />
                        <span>{tab.service}</span>
                      </button>
                    );
                  })}
                </div>
              </div>

              {/* Active Tab Content */}
              <div className="grid grid-cols-1 md:grid-cols-12 gap-6 items-center">
                
                {/* Details */}
                <div className="md:col-span-7 flex flex-col justify-center">
                  <div className="inline-flex items-center gap-2 px-2.5 py-1 rounded-full bg-white/5 border border-white/10 text-[10px] font-mono text-anime-yellow mb-3 w-fit">
                    <ActiveEngineIcon size={12} />
                    <span>{activeEngine.tag}</span>
                  </div>

                  <h3 className="text-2xl sm:text-3xl font-extrabold text-anime-cream mb-3">
                    {activeEngine.headline}
                  </h3>

                  <p className="text-sm text-anime-cream/75 leading-relaxed mb-6">
                    {activeEngine.description}
                  </p>

                  <div className="flex items-center gap-4 text-xs font-mono text-anime-cream/70 mb-4">
                    <div className="flex items-center gap-1.5">
                      <CheckCircle2 size={14} className="text-anime-green" />
                      <span>{activeEngine.metric}</span>
                    </div>
                    <div className="flex items-center gap-1.5">
                      <span className="text-anime-yellow text-xs">◈</span>
                      <span>{activeEngine.badge}</span>
                    </div>
                  </div>
                </div>

                {/* Screenshot Interactive Preview */}
                <div className="md:col-span-5">
                  <div
                    onClick={() => onOpenScreenshot(activeEngine.shotIndex)}
                    className="relative aspect-[9/15] max-w-[220px] mx-auto rounded-2xl overflow-hidden bg-graphite-950 border border-white/15 cursor-pointer group/thumb shadow-2xl transition-transform duration-300 group-hover/thumb:scale-102"
                    title={`Click to preview ${activeEngine.title} in full screen`}
                  >
                    <img
                      src={getAssetUrl(`assets/screenshots/${activeEngine.file}`)}
                      onError={(e) => handleScreenshotError(e, activeEngine.file)}
                      alt={activeEngine.title}
                      loading="lazy"
                      className="w-full h-full object-cover transition-transform duration-500 group-hover/thumb:scale-105"
                    />
                    <div className="absolute inset-0 bg-graphite-950/40 opacity-0 group-hover/thumb:opacity-100 transition-opacity flex items-center justify-center backdrop-blur-xs">
                      <span className="px-3 py-1.5 rounded-xl bg-graphite-950/90 text-anime-cream text-xs font-mono flex items-center gap-1.5 border border-white/20 shadow-lg">
                        <Eye size={13} className="text-anime-green" />
                        <span>Inspect Full</span>
                      </span>
                    </div>
                  </div>
                </div>

              </div>
            </div>

            {/* Bottom Bar */}
            <div className="pt-6 mt-6 border-t border-white/10 flex flex-wrap items-center justify-between gap-3 text-xs font-mono text-anime-cream/70">
              <span>All 3 services unified in one single audio queue</span>
              <button
                type="button"
                onClick={() => onOpenScreenshot(activeEngine.shotIndex)}
                className="flex items-center gap-1 text-anime-green hover:underline cursor-pointer"
              >
                <span>View Full Screenshot</span>
                <ArrowRight size={13} />
              </button>
            </div>
          </div>

          {/* BENTO BLOCK 2: Discord Rich Presence Card (Span 12 on mobile, 4 on desktop) */}
          <div className="md:col-span-12 lg:col-span-4 neo-glass-card rounded-3xl p-6 sm:p-7 flex flex-col justify-between border-anime-green/30 hover:border-anime-green/50">
            <div>
              <div className="flex items-center justify-between mb-5">
                <div className="w-11 h-11 rounded-2xl bg-anime-green/15 text-anime-green flex items-center justify-center border border-anime-green/30">
                  <DiscordIcon size={22} />
                </div>
                <span className="text-[10px] font-mono text-anime-green uppercase tracking-wider font-semibold px-2.5 py-1 rounded-full bg-anime-green/10 border border-anime-green/20">
                  Live Status RPC
                </span>
              </div>

              <h3 className="text-xl font-bold text-anime-cream mb-2">
                Discord Rich Presence
              </h3>
              <p className="text-xs sm:text-sm text-anime-cream/70 leading-relaxed mb-5">
                Broadcast what you're vibing to in real time. Shows track title, artist, cover art, and playback progress on your Discord profile.
              </p>

              {/* Realistic Discord Mockup Card */}
              <div className="p-4 rounded-2xl bg-graphite-950 border border-white/10 font-mono text-xs mb-4">
                <div className="flex items-center gap-3 mb-3">
                  <div className="relative w-10 h-10 rounded-full bg-graphite-900 border border-white/15 overflow-hidden flex-shrink-0">
                    <img
                      src={getAssetUrl('assets/developer_avatar.png')}
                      onError={handleDeveloperAvatarError}
                      alt="Discord Avatar"
                      className="w-full h-full object-cover"
                    />
                    <span className="absolute bottom-0 right-0 w-2.5 h-2.5 rounded-full bg-anime-green border-2 border-graphite-950" />
                  </div>
                  <div className="min-w-0">
                    <div className="text-anime-cream font-bold text-xs truncate">Kaif Raja</div>
                    <div className="text-[10px] text-anime-green uppercase tracking-wide">Playing MKR Music</div>
                  </div>
                </div>

                <div className="p-2.5 rounded-xl bg-graphite-900/90 border border-white/5 space-y-1.5">
                  <div className="flex items-center justify-between">
                    <span className="text-anime-cream font-semibold text-xs truncate">Vinland Horizon</span>
                    {/* Mini Equalizer Wave */}
                    <div className="flex items-end gap-0.5 h-3">
                      <span className="w-0.5 h-3 bg-anime-green" />
                      <span className="w-0.5 h-1.5 bg-anime-green" />
                      <span className="w-0.5 h-2.5 bg-anime-green" />
                    </div>
                  </div>
                  <div className="text-[10px] text-anime-cream/50 truncate">MKR Acoustic Studio · Lossless</div>
                  
                  {/* Progress Bar */}
                  <div className="w-full bg-white/10 rounded-full h-1 mt-2 overflow-hidden">
                    <div className="bg-anime-green h-full w-2/3 rounded-full" />
                  </div>
                  <div className="flex justify-between text-[9px] text-anime-cream/40 pt-0.5">
                    <span>02:34</span>
                    <span>03:48</span>
                  </div>
                </div>
              </div>
            </div>

            <div className="pt-3 border-t border-white/5 text-[11px] font-mono text-anime-green flex items-center justify-between">
              <span>Native Discord Integration</span>
              <Activity size={13} />
            </div>
          </div>

          {/* BENTO BLOCK 3: Last.fm Real-Time Scrobbler (Span 12 on mobile, 4 on desktop) */}
          <div className="md:col-span-12 lg:col-span-4 neo-glass-card rounded-3xl p-6 sm:p-7 flex flex-col justify-between border-anime-yellow/30 hover:border-anime-yellow/50 neo-glass-yellow-card">
            <div>
              <div className="flex items-center justify-between mb-5">
                <div className="w-11 h-11 rounded-2xl bg-anime-yellow/15 text-anime-yellow flex items-center justify-center border border-anime-yellow/30">
                  <LastfmIcon size={22} />
                </div>
                <span className="text-[10px] font-mono text-anime-yellow uppercase tracking-wider font-semibold px-2.5 py-1 rounded-full bg-anime-yellow/10 border border-anime-yellow/20">
                  Background Scrobbler
                </span>
              </div>

              <h3 className="text-xl font-bold text-anime-cream mb-2">
                Last.fm Scrobbler
              </h3>
              <p className="text-xs sm:text-sm text-anime-cream/70 leading-relaxed mb-5">
                Automatically scrobbles every song in the background. Builds your listening history, artist milestones, and charts with zero companion apps.
              </p>

              {/* Realistic Last.fm Mockup Pill */}
              <div className="p-4 rounded-2xl bg-graphite-950 border border-white/10 font-mono text-xs mb-4">
                <div className="flex items-center justify-between mb-2">
                  <span className="text-[10px] uppercase text-anime-yellow font-bold">Scrobbled Just Now</span>
                  <span className="text-[10px] text-anime-cream/40">1,248 total scrobbles</span>
                </div>
                <div className="flex items-center gap-3 p-2 rounded-xl bg-graphite-900/90 border border-white/5">
                  <div className="w-8 h-8 rounded-lg bg-anime-yellow/15 text-anime-yellow flex items-center justify-center font-bold text-xs flex-shrink-0">
                    <Music2 size={15} />
                  </div>
                  <div className="min-w-0">
                    <div className="text-anime-cream font-semibold text-xs truncate">Acoustic Serenity</div>
                    <div className="text-[10px] text-anime-cream/50 truncate">MKR Studio Master</div>
                  </div>
                </div>
              </div>
            </div>

            <div className="pt-3 border-t border-white/5 text-[11px] font-mono text-anime-yellow flex items-center justify-between">
              <span>Automatic Background Tracking</span>
              <Share2 size={13} />
            </div>
          </div>

          {/* BENTO BLOCK 4: Automatic Artwork Monet Theming (Span 12 on mobile, 4 on desktop) */}
          <div
            onClick={() => onOpenScreenshot(12)}
            className="md:col-span-12 lg:col-span-4 neo-glass-card rounded-3xl p-6 sm:p-7 flex flex-col justify-between cursor-pointer group border-anime-green/30 hover:border-anime-green/60"
          >
            <div>
              <div className="flex items-center justify-between mb-5">
                <div className="w-11 h-11 rounded-2xl bg-anime-green/15 text-anime-green flex items-center justify-center border border-anime-green/30">
                  <Palette size={22} />
                </div>
                <span className="text-[10px] font-mono text-anime-green uppercase tracking-wider font-semibold px-2.5 py-1 rounded-full bg-anime-green/10 border border-anime-green/20">
                  Dynamic Aesthetics
                </span>
              </div>

              <h3 className="text-xl font-bold text-anime-cream mb-2 group-hover:text-anime-green transition-colors">
                Automatic Artwork Theming
              </h3>
              <p className="text-xs sm:text-sm text-anime-cream/70 leading-relaxed mb-5">
                The interface dynamically extracts dominant color harmonies directly from the album art, morphing UI buttons, glowing waves, and headers.
              </p>

              {/* Palette Visualizer */}
              <div className="p-3.5 rounded-2xl bg-graphite-950 border border-white/10 flex items-center justify-between gap-2 mb-4">
                <div className="flex items-center gap-2">
                  <span className="w-5 h-5 rounded-full bg-anime-green shadow-sm" />
                  <span className="w-5 h-5 rounded-full bg-anime-yellow shadow-sm" />
                  <span className="w-5 h-5 rounded-full bg-anime-cream shadow-sm" />
                  <span className="w-5 h-5 rounded-full bg-graphite-700 shadow-sm" />
                </div>
                <span className="text-[10px] font-mono text-anime-cream/50">Extracted Palette</span>
              </div>
            </div>

            <div className="pt-3 border-t border-white/5 text-[11px] font-mono text-anime-cream/70 group-hover:text-anime-green flex items-center justify-between transition-colors">
              <span>View Appearance Screen</span>
              <ArrowRight size={13} />
            </div>
          </div>

          {/* BENTO BLOCK 5: Synchronized Karaoke Lyrics (Span 12 on mobile, 4 on desktop) */}
          <div
            onClick={() => onOpenScreenshot(8)}
            className="md:col-span-12 lg:col-span-4 neo-glass-card neo-glass-yellow-card rounded-3xl p-6 sm:p-7 flex flex-col justify-between cursor-pointer group border-anime-yellow/30 hover:border-anime-yellow/60"
          >
            <div>
              <div className="flex items-center justify-between mb-5">
                <div className="w-11 h-11 rounded-2xl bg-anime-yellow/15 text-anime-yellow flex items-center justify-center border border-anime-yellow/30">
                  <Disc3 size={22} />
                </div>
                <span className="text-[10px] font-mono text-anime-yellow uppercase tracking-wider font-semibold px-2.5 py-1 rounded-full bg-anime-yellow/10 border border-anime-yellow/20">
                  Real-time Karaoke
                </span>
              </div>

              <h3 className="text-xl font-bold text-anime-cream mb-2 group-hover:text-anime-yellow transition-colors">
                Synchronized Lyrics
              </h3>
              <p className="text-xs sm:text-sm text-anime-cream/70 leading-relaxed mb-5">
                Sing along with time-aligned scrolling verses rendered over softened cover art. Tap any verse to instantly seek playback to that exact second.
              </p>

              {/* Karaoke Lyrics Mockup */}
              <div className="p-3.5 rounded-2xl bg-graphite-950 border border-white/10 space-y-1.5 font-mono text-xs mb-4">
                <div className="text-anime-cream/35 text-[11px]">In the quiet of the morning breeze...</div>
                <div className="text-anime-yellow font-bold text-xs flex items-center gap-2">
                  <span className="text-anime-yellow text-[10px]">▶</span>
                  <span>The song of freedom echoes clear</span>
                </div>
                <div className="text-anime-cream/35 text-[11px]">Beyond the horizons of the sea...</div>
              </div>
            </div>

            <div className="pt-3 border-t border-white/5 text-[11px] font-mono text-anime-cream/70 group-hover:text-anime-yellow flex items-center justify-between transition-colors">
              <span>Preview Lyrics Interface</span>
              <ArrowRight size={13} />
            </div>
          </div>

          {/* BENTO BLOCK 6: Apple-Inspired Fluid UI & Canvas Video (Span 12 on mobile, 6 on desktop) */}
          <div
            onClick={() => onOpenScreenshot(7)}
            className="md:col-span-12 lg:col-span-6 neo-glass-card rounded-3xl p-6 sm:p-8 flex flex-col justify-between cursor-pointer group border-white/12 hover:border-anime-green/50"
          >
            <div>
              <div className="flex items-center justify-between mb-5">
                <div className="w-11 h-11 rounded-2xl bg-white/10 text-anime-cream flex items-center justify-center border border-white/20">
                  <Smartphone size={22} />
                </div>
                <span className="text-[10px] font-mono text-anime-cream/70 uppercase tracking-wider font-semibold px-2.5 py-1 rounded-full bg-white/5 border border-white/10">
                  Fluid Dynamics
                </span>
              </div>

              <h3 className="text-2xl font-bold text-anime-cream mb-2 group-hover:text-anime-green transition-colors">
                Apple-Inspired Fluid Player UI
              </h3>
              <p className="text-sm text-anime-cream/75 leading-relaxed mb-6">
                Engineered with tactile spring physics, floating miniplayer, expandable now-playing modal, edge-to-edge canvas artwork loops, and intuitive swipe gestures.
              </p>

              <div className="grid grid-cols-3 gap-3 font-mono text-xs text-center mb-4">
                <div className="p-3 rounded-2xl bg-graphite-950 border border-white/10">
                  <span className="text-anime-green font-bold block text-sm">60 FPS</span>
                  <span className="text-[10px] text-anime-cream/50">Spring Physics</span>
                </div>
                <div className="p-3 rounded-2xl bg-graphite-950 border border-white/10">
                  <span className="text-anime-yellow font-bold block text-sm">Canvas</span>
                  <span className="text-[10px] text-anime-cream/50">Looping Video</span>
                </div>
                <div className="p-3 rounded-2xl bg-graphite-950 border border-white/10">
                  <span className="text-anime-cream font-bold block text-sm">Gestures</span>
                  <span className="text-[10px] text-anime-cream/50">Tactile Swipe</span>
                </div>
              </div>
            </div>

            <div className="pt-4 border-t border-white/5 text-xs font-mono text-anime-cream/70 group-hover:text-anime-green flex items-center justify-between transition-colors">
              <span>Preview Canvas Video Screen</span>
              <ArrowRight size={13} />
            </div>
          </div>

          {/* BENTO BLOCK 7: Smart Delayed Caching (Span 12 on mobile, 6 on desktop) */}
          <div className="md:col-span-12 lg:col-span-6 neo-glass-card neo-glass-yellow-card rounded-3xl p-6 sm:p-8 flex flex-col justify-between border-anime-yellow/30 hover:border-anime-yellow/60">
            <div>
              <div className="flex items-center justify-between mb-5">
                <div className="w-11 h-11 rounded-2xl bg-anime-yellow/15 text-anime-yellow flex items-center justify-center border border-anime-yellow/30">
                  <HardDrive size={22} />
                </div>
                <span className="text-[10px] font-mono text-anime-yellow uppercase tracking-wider font-semibold px-2.5 py-1 rounded-full bg-anime-yellow/10 border border-anime-yellow/20">
                  Storage Preservation
                </span>
              </div>

              <h3 className="text-2xl font-bold text-anime-cream mb-2">
                Smart Delayed Caching Engine
              </h3>
              <p className="text-sm text-anime-cream/75 leading-relaxed mb-6">
                Never waste precious device storage on songs you skip. Caching initiates strictly after you listen past your customized delay threshold (5s–90s).
              </p>

              {/* Interactive Slider */}
              <div className="p-4 rounded-2xl bg-graphite-950/90 border border-white/10 mb-4">
                <div className="flex justify-between items-center text-xs font-mono mb-2">
                  <span className="text-anime-cream/70">Custom Delay Threshold:</span>
                  <span className="text-anime-yellow font-bold text-sm">{cacheDelay} Seconds</span>
                </div>
                <input
                  type="range"
                  min="5"
                  max="90"
                  step="5"
                  value={cacheDelay}
                  onChange={(e) => setCacheDelay(parseInt(e.target.value, 10))}
                  className="w-full accent-anime-yellow cursor-pointer"
                  aria-label="Cache delay threshold in seconds"
                />
                <div className="flex justify-between text-[10px] text-anime-cream/40 font-mono mt-1">
                  <span>5s (Instant Cache)</span>
                  <span>45s (Standard)</span>
                  <span>90s (Strict Selective)</span>
                </div>
              </div>
            </div>

            <div className="pt-4 border-t border-white/5 text-xs font-mono text-anime-cream/70 flex items-center justify-between">
              <span>Saves up to 70% of device storage</span>
              <button
                type="button"
                onClick={() => onOpenScreenshot(13)}
                className="text-anime-yellow hover:underline cursor-pointer flex items-center gap-1"
              >
                <span>View Cache Screen</span>
                <ArrowRight size={13} />
              </button>
            </div>
          </div>

          {/* BENTO BLOCK 8: Audiophile Lossless & DAC (Span 12 on mobile, 4 on desktop) */}
          <div
            onClick={() => onOpenScreenshot(10)}
            className="md:col-span-12 lg:col-span-4 neo-glass-card rounded-3xl p-6 sm:p-7 flex flex-col justify-between cursor-pointer group border-anime-green/30 hover:border-anime-green/60"
          >
            <div>
              <div className="flex items-center justify-between mb-5">
                <div className="w-11 h-11 rounded-2xl bg-anime-green/15 text-anime-green flex items-center justify-center border border-anime-green/30">
                  <AudioLines size={22} />
                </div>
                <span className="text-[10px] font-mono text-anime-green uppercase tracking-wider font-semibold px-2.5 py-1 rounded-full bg-anime-green/10 border border-anime-green/20">
                  Hi-Res Audio
                </span>
              </div>

              <h3 className="text-xl font-bold text-anime-cream mb-2 group-hover:text-anime-green transition-colors">
                Lossless DAC & Audio Routing
              </h3>
              <p className="text-xs sm:text-sm text-anime-cream/70 leading-relaxed mb-5">
                Stream with internal 32-bit float audio routing, external USB-C DAC direct hardware pass-through, and Bluetooth LDAC output selection.
              </p>

              <div className="flex flex-wrap gap-1.5 font-mono text-[10px] text-anime-cream/80 mb-4">
                <span className="px-2.5 py-1 rounded-lg bg-graphite-950 border border-white/10">32-Bit Float</span>
                <span className="px-2.5 py-1 rounded-lg bg-graphite-950 border border-white/10">Direct USB DAC</span>
                <span className="px-2.5 py-1 rounded-lg bg-graphite-950 border border-white/10">LDAC 990kbps</span>
              </div>
            </div>

            <div className="pt-3 border-t border-white/5 text-[11px] font-mono text-anime-cream/70 group-hover:text-anime-green flex items-center justify-between transition-colors">
              <span>Inspect Audio Chooser</span>
              <ArrowRight size={13} />
            </div>
          </div>

          {/* BENTO BLOCK 9: Listen Together (Span 12 on mobile, 4 on desktop) */}
          <div
            onClick={() => onOpenScreenshot(3)}
            className="md:col-span-12 lg:col-span-4 neo-glass-card neo-glass-yellow-card rounded-3xl p-6 sm:p-7 flex flex-col justify-between cursor-pointer group border-anime-yellow/30 hover:border-anime-yellow/60"
          >
            <div>
              <div className="flex items-center justify-between mb-5">
                <div className="w-11 h-11 rounded-2xl bg-anime-yellow/15 text-anime-yellow flex items-center justify-center border border-anime-yellow/30">
                  <Users size={22} />
                </div>
                <span className="text-[10px] font-mono text-anime-yellow uppercase tracking-wider font-semibold px-2.5 py-1 rounded-full bg-anime-yellow/10 border border-anime-yellow/20">
                  Synchronized Rooms
                </span>
              </div>

              <h3 className="text-xl font-bold text-anime-cream mb-2 group-hover:text-anime-yellow transition-colors">
                Listen Together Rooms
              </h3>
              <p className="text-xs sm:text-sm text-anime-cream/70 leading-relaxed mb-5">
                Host synchronized listening sessions with your friends. Share a room link to stream the exact same track simultaneously across devices without delay.
              </p>

              <div className="p-3 rounded-2xl bg-graphite-950 border border-white/10 flex items-center justify-between font-mono text-xs mb-4">
                <div className="flex items-center gap-2">
                  <span className="w-2 h-2 rounded-full bg-anime-green/80" />
                  <span className="text-anime-cream font-bold">Room #MKR-892</span>
                </div>
                <span className="text-[10px] text-anime-yellow">3 Friends Synced</span>
              </div>
            </div>

            <div className="pt-3 border-t border-white/5 text-[11px] font-mono text-anime-cream/70 group-hover:text-anime-yellow flex items-center justify-between transition-colors">
              <span>Explore Community</span>
              <ArrowRight size={13} />
            </div>
          </div>

          {/* BENTO BLOCK 10: Zero Telemetry & Offline Vault (Span 12 on mobile, 4 on desktop) */}
          <div
            onClick={() => onOpenScreenshot(14)}
            className="md:col-span-12 lg:col-span-4 neo-glass-card rounded-3xl p-6 sm:p-7 flex flex-col justify-between cursor-pointer group border-white/12 hover:border-anime-cream/50"
          >
            <div>
              <div className="flex items-center justify-between mb-5">
                <div className="w-11 h-11 rounded-2xl bg-anime-cream/15 text-anime-cream flex items-center justify-center border border-white/20">
                  <ShieldCheck size={22} />
                </div>
                <span className="text-[10px] font-mono text-anime-cream/70 uppercase tracking-wider font-semibold px-2.5 py-1 rounded-full bg-white/5 border border-white/10">
                  Zero Telemetry
                </span>
              </div>

              <h3 className="text-xl font-bold text-anime-cream mb-2 group-hover:text-anime-green transition-colors">
                Offline Vault & Privacy First
              </h3>
              <p className="text-xs sm:text-sm text-anime-cream/70 leading-relaxed mb-5">
                Your downloads and cached tracks are strictly protected from automated eviction. Completely private with zero user tracking, analytics, or ads.
              </p>

              <div className="p-3 rounded-2xl bg-graphite-950 border border-white/10 flex items-center justify-between font-mono text-xs mb-4">
                <span className="text-anime-green font-bold">100% Offline Vault</span>
                <span className="text-[10px] text-anime-cream/50">GPL-3.0 Sovereign</span>
              </div>
            </div>

            <div className="pt-3 border-t border-white/5 text-[11px] font-mono text-anime-cream/70 group-hover:text-anime-green flex items-center justify-between transition-colors">
              <span>View Settings Screen</span>
              <ArrowRight size={13} />
            </div>
          </div>

        </div>

        {/* 2. STUDIO 10-BAND GRAPHIC EQUALIZER ANCHOR */}
        <div id="sound" className="mt-16 scroll-mt-28">
          <InteractiveEqualizer />
        </div>

      </div>
    </section>
  );
};
