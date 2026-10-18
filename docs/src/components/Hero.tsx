import React, { useState } from 'react';
import { Download, ArrowRight, Disc3, Zap, Headphones, Sliders, Shield, Layers, Radio, Monitor, Palette } from 'lucide-react';
import { OFFICIAL_LINKS } from '@/lib/release';
import { getAssetUrl, APP_LOGO, handleScreenshotError, VINLAND_SAGA_ASSETS, handleVinlandBgError } from '@/lib/assets';
import { GithubIcon, SpotifyIcon, YouTubeMusicIcon } from './BrandIcons';
import { AudioVisualizer } from './AudioVisualizer';

type VinlandStyle = 'duality' | 'dawn' | 'emerald' | 'obsidian';
type DesktopViewMode = 'auto' | 'ultrawide' | 'studio' | 'laptop';

interface HeroProps {
  onOpenScreenshot: (index: number) => void;
}

export const Hero: React.FC<HeroProps> = ({ onOpenScreenshot }) => {
  const [artStyle, setArtStyle] = useState<VinlandStyle>('duality');
  const [viewMode, setViewMode] = useState<DesktopViewMode>('auto');

  const getContainerClasses = () => {
    if (viewMode === 'ultrawide') {
      return 'vinland-ultrawide-mask max-w-[2600px] mx-auto';
    }
    if (viewMode === 'studio') {
      return 'max-w-[1920px] mx-auto';
    }
    if (viewMode === 'laptop') {
      return 'max-w-[1440px] mx-auto';
    }
    // auto responsive for desktop displays
    return '2xl:vinland-ultrawide-mask 2xl:max-w-[2600px] mx-auto';
  };

  const getImageClasses = () => {
    const base = 'transform-gpu transition-all duration-700 ease-out';
    if (viewMode === 'ultrawide') {
      return `${base} object-[50%_35%] scale-100 opacity-55 lg:opacity-65`;
    }
    if (viewMode === 'studio') {
      return `${base} object-[52%_30%] scale-105 opacity-50 lg:opacity-60`;
    }
    if (viewMode === 'laptop') {
      return `${base} object-[58%_25%] scale-100 opacity-45 lg:opacity-55`;
    }
    // auto responsive
    return `${base} object-[center_28%] sm:object-[54%_28%] lg:object-[58%_25%] xl:object-[52%_32%] 2xl:object-[50%_35%] scale-100 sm:scale-105 opacity-45 sm:opacity-55 lg:opacity-60`;
  };

  const renderAmbientAura = () => {
    switch (artStyle) {
      case 'dawn':
        return (
          <>
            <div className="absolute inset-0 vinland-dawn-ambient pointer-events-none" />
            <div className="absolute top-1/6 right-1/4 w-[650px] h-[550px] bg-anime-yellow/18 rounded-full blur-[150px] pointer-events-none" />
            <div className="absolute -top-20 left-1/3 w-[500px] h-[450px] bg-amber-500/12 rounded-full blur-[140px] pointer-events-none" />
          </>
        );
      case 'emerald':
        return (
          <>
            <div className="absolute inset-0 vinland-emerald-ambient pointer-events-none" />
            <div className="absolute -top-20 left-1/4 w-[650px] h-[550px] bg-anime-green/20 rounded-full blur-[150px] pointer-events-none" />
            <div className="absolute top-1/4 right-1/5 w-[500px] h-[450px] bg-emerald-400/14 rounded-full blur-[140px] pointer-events-none" />
          </>
        );
      case 'obsidian':
        return (
          <>
            <div className="absolute inset-0 vinland-obsidian-ambient pointer-events-none" />
            <div className="absolute inset-0 bg-graphite-950/45 pointer-events-none" />
          </>
        );
      case 'duality':
      default:
        return (
          <>
            <div className="absolute inset-0 vinland-duality-ambient pointer-events-none" />
            {/* Young Thorfinn fiery crimson & obsidian shadow on left */}
            <div className="absolute -top-20 left-6 sm:left-12 w-[550px] h-[500px] bg-red-600/14 rounded-full blur-[160px] pointer-events-none" />
            {/* Peaceful Farmland Thorfinn golden wheat & sovereign emerald glow on right */}
            <div className="absolute top-1/5 right-6 sm:right-12 w-[600px] h-[550px] bg-anime-yellow/16 rounded-full blur-[150px] pointer-events-none" />
            <div className="absolute -bottom-10 right-1/3 w-[500px] h-[400px] bg-anime-green/12 rounded-full blur-[130px] pointer-events-none" />
          </>
        );
    }
  };

  return (
    <section className="relative pt-36 pb-20 md:pt-44 md:pb-28 overflow-hidden">
      {/* Vinland Saga Desktop Responsive Hero Background */}
      <div className={`absolute inset-0 z-0 pointer-events-none select-none overflow-hidden ${getContainerClasses()}`}>
        <picture className="w-full h-full block">
          {/* Ultrawide 21:9 & 4K displays (3840px+) */}
          <source
            media="(min-width: 1920px)"
            srcSet={`${VINLAND_SAGA_ASSETS.ultrawideWebp} 3840w, ${VINLAND_SAGA_ASSETS.qhdWebp} 2560w`}
            type="image/webp"
          />
          {/* QHD / 1440p standard desktop monitors (1280px - 1920px) */}
          <source
            media="(min-width: 1280px)"
            srcSet={`${VINLAND_SAGA_ASSETS.qhdWebp} 2560w, ${VINLAND_SAGA_ASSETS.fhdWebp} 1920w`}
            type="image/webp"
          />
          {/* Laptop 16:10 / FHD desktop screens (768px - 1280px) */}
          <source
            media="(min-width: 768px)"
            srcSet={`${VINLAND_SAGA_ASSETS.laptopWebp} 1440w, ${VINLAND_SAGA_ASSETS.fhdWebp} 1920w`}
            type="image/webp"
          />
          {/* Compact viewports */}
          <source srcSet={VINLAND_SAGA_ASSETS.laptopWebp} type="image/webp" />
          {/* High-res JPEG fallbacks */}
          <source media="(min-width: 1440px)" srcSet={VINLAND_SAGA_ASSETS.qhdJpg} type="image/jpeg" />
          <source srcSet={VINLAND_SAGA_ASSETS.fhdJpg} type="image/jpeg" />
          {/* Base image element */}
          <img
            src={VINLAND_SAGA_ASSETS.qhdJpg}
            onError={handleVinlandBgError}
            alt="Vinland Saga Thorfinn Duality"
            role="presentation"
            className={`w-full h-full object-cover ${getImageClasses()}`}
          />
        </picture>

        {/* Ambient Mood Aura */}
        {renderAmbientAura()}

        {/* Multi-stop Contrast Gradients for Razor-Sharp Text Readability */}
        <div className="absolute inset-0 bg-gradient-to-t from-graphite-950 via-graphite-950/70 to-graphite-950/35 pointer-events-none" />
        <div className="absolute inset-0 bg-gradient-to-r from-graphite-950/95 via-graphite-950/65 to-graphite-950/30 pointer-events-none" />
        <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_top,transparent_0%,rgba(8,8,9,0.55)_55%,#080809_100%)] pointer-events-none" />
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-12 lg:gap-8 items-center">

          
          {/* Left Column: Copy & Actions */}
          <div className="lg:col-span-7 flex flex-col items-start text-left">
            
            {/* Top Badge */}
            <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-graphite-900/90 border border-white/12 text-xs font-mono text-anime-cream/90 mb-6 backdrop-blur-md shadow-neo-card">
              <Headphones size={13} className="text-anime-green" />
              <span>Sovereign Android Audio Engine</span>
              <span className="text-white/20">|</span>
              <span className="text-anime-green font-semibold">Tri-Engine Ready</span>
            </div>

            {/* Main Headline */}
            <h1 className="text-4xl sm:text-6xl lg:text-7xl font-extrabold tracking-tight text-anime-cream leading-[1.06] mb-6">
              Your Music. <br />
              <span className="text-transparent bg-clip-text bg-gradient-to-r from-anime-green via-anime-yellow to-anime-cream">
                Without Limits.
              </span>
            </h1>

            {/* Description */}
            <p className="text-base sm:text-lg text-anime-cream/75 max-w-2xl leading-relaxed mb-8">
              A high-fidelity Material Design music client for Android. Import Spotify playlists, sync your YouTube Music library, and stream JioSaavn regional audio simultaneously with dynamic artwork-adaptive theming, synchronized karaoke lyrics, and 10-band studio equalization — 100% free and without ads.
            </p>

            {/* Tri-Engine Quick Chips */}
            <div className="flex flex-wrap items-center gap-2 mb-8 font-mono text-xs">
              <span className="text-anime-cream/50 text-[11px] uppercase mr-1">Supported:</span>
              <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-graphite-900/90 border border-anime-green/30 text-anime-green">
                <SpotifyIcon size={13} />
                <span>Spotify Import</span>
              </div>
              <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-graphite-900/90 border border-anime-yellow/30 text-anime-yellow">
                <YouTubeMusicIcon size={13} />
                <span>YT Music Sync</span>
              </div>
              <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-graphite-900/90 border border-white/20 text-anime-cream">
                <Layers size={13} className="text-anime-cream" />
                <span>JioSaavn Dual Stream</span>
              </div>
            </div>

            {/* Primary Action Buttons */}
            <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-4 w-full sm:w-auto mb-8">
              <a
                href={OFFICIAL_LINKS.universalApkDownload}
                download={OFFICIAL_LINKS.universalApkName}
                className="liquid-glass-btn inline-flex items-center justify-center gap-3 px-8 py-4 rounded-2xl text-base font-extrabold text-graphite-950 text-center group cursor-pointer shadow-[0_12px_32px_-4px_rgba(34,197,94,0.65)] hover:shadow-[0_18px_45px_-4px_rgba(34,197,94,0.85)]"
                aria-label="Download Universal APK for Android"
              >
                <Download size={20} className="stroke-[2.5] transition-transform group-hover:-translate-y-0.5" />
                <span>Download Universal APK</span>
              </a>

              <a
                href={OFFICIAL_LINKS.androidRepo}
                target="_blank"
                rel="noopener noreferrer"
                className="inline-flex items-center justify-center gap-2 px-6 py-4 rounded-2xl text-sm font-semibold bg-graphite-900/90 hover:bg-graphite-800 text-anime-cream border border-white/10 hover:border-anime-yellow/40 transition-all text-center"
              >
                <GithubIcon size={17} />
                <span>Source Code</span>
                <ArrowRight size={14} />
              </a>
            </div>

            {/* Specification Feature Badges */}
            <div className="p-3.5 rounded-2xl neo-glass border border-white/10 flex flex-wrap items-center gap-4 text-xs font-mono text-anime-cream/70">
              <div className="flex items-center gap-2">
                <span className="text-anime-cream font-semibold">Package:</span>
                <span className="text-anime-green font-bold">{OFFICIAL_LINKS.universalApkName}</span>
              </div>
              <span className="hidden sm:inline text-white/20">|</span>
              <div className="flex items-center gap-1.5">
                <Shield size={13} className="text-anime-yellow" />
                <span>100% Free & Open-Source</span>
              </div>
              <span className="hidden sm:inline text-white/20">|</span>
              <div>
                <span>Compatibility: </span>
                <span className="text-anime-cream font-medium">Android 8.0+ (All Architectures)</span>
              </div>
            </div>
          </div>

          {/* Right Column: Phone Showcase with 3D Perspective */}
          <div className="lg:col-span-5 flex justify-center relative perspective-1000">
            <div className="relative w-full max-w-[340px] sm:max-w-[380px] group transition-transform duration-500 hover:rotate-y-2 hover:-rotate-x-2">
              
              {/* Subtle Ambient Frame Glow */}
              <div
                className="absolute -inset-3 bg-gradient-to-r from-anime-green/30 via-anime-yellow/20 to-transparent rounded-[46px] blur-2xl opacity-60 group-hover:opacity-90 transition duration-500 pointer-events-none"
                aria-hidden="true"
              />

              {/* Phone Chassis */}
              <div className="relative rounded-[40px] p-3 bg-gradient-to-b from-graphite-900 to-graphite-950 border border-white/20 shadow-2xl backdrop-blur-2xl">
                
                {/* Dynamic Island / Speaker Island */}
                <div className="w-32 h-4 bg-graphite-950 rounded-full mx-auto mb-3 flex items-center justify-between px-3 border border-white/10 shadow-inner">
                  <div className="flex items-center gap-1">
                    <span className="w-1.5 h-1.5 rounded-full bg-graphite-700" />
                    <span className="w-1.5 h-1.5 rounded-full bg-graphite-800" />
                  </div>
                  <div className="flex items-center gap-1">
                    <span className="w-8 h-1 rounded-full bg-graphite-800" />
                  </div>
                </div>

                {/* Display Content: Clickable Real App Screenshot */}
                <div
                  className="relative rounded-[30px] overflow-hidden cursor-pointer group/screen bg-graphite-950 aspect-[9/16] shadow-inner"
                  onClick={() => onOpenScreenshot(7)} // opens canvas_player.jpg
                  title="Click to view full screenshot"
                >
                  <img
                    src={getAssetUrl('assets/screenshots/canvas_player.jpg')}
                    onError={(e) => handleScreenshotError(e, 'canvas_player.jpg')}
                    alt="MKR Music Canvas Player interface"
                    className="w-full h-full object-cover transition-transform duration-500 group-hover/screen:scale-105"
                  />
                  <div className="absolute inset-0 bg-gradient-to-t from-graphite-950/85 via-transparent to-transparent flex items-end p-4">
                    <span className="text-xs font-mono text-anime-cream/90 flex items-center gap-2">
                      <Disc3 size={14} className="text-anime-yellow animate-spin-slow" />
                      <span>Tap to preview canvas player</span>
                    </span>
                  </div>
                </div>

                {/* Floating Badge 1: 0-Buffer Streaming */}
                <div className="absolute -top-3 -left-4 sm:-left-8 neo-glass p-3 rounded-2xl border border-anime-green/40 shadow-neo-glow-green animate-float hidden sm:flex items-center gap-2.5">
                  <div className="w-8 h-8 rounded-xl bg-anime-green/20 flex items-center justify-center text-anime-green">
                    <Zap size={16} />
                  </div>
                  <div className="flex flex-col">
                    <span className="text-[10px] font-mono uppercase text-anime-green font-bold">
                      Instant Audio
                    </span>
                    <span className="text-xs text-anime-cream font-medium">
                      0-Buffer Lag
                    </span>
                  </div>
                </div>

                {/* Floating Badge 2: 10-Band Equalizer */}
                <div className="absolute -bottom-4 -right-4 sm:-right-8 neo-glass p-3 rounded-2xl border border-anime-yellow/40 shadow-neo-glow-yellow animate-float-alt hidden sm:flex items-center gap-2.5">
                  <div className="w-8 h-8 rounded-xl bg-anime-yellow/20 flex items-center justify-center text-anime-yellow">
                    <Sliders size={16} />
                  </div>
                  <div className="flex flex-col">
                    <span className="text-[10px] font-mono uppercase text-anime-yellow font-bold">
                      10-Band DSP
                    </span>
                    <span className="text-xs text-anime-cream font-medium">
                      Studio Equalizer
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Desktop Device Profile & Aesthetic Style Selector */}
        <div className="mt-14 pt-6 pb-2 border-t border-white/10 flex flex-col md:flex-row items-center justify-between gap-5 text-xs font-mono">
          {/* Device Profile */}
          <div className="flex flex-wrap items-center gap-2.5 w-full md:w-auto">
            <span className="text-anime-cream/60 uppercase text-[11px] tracking-wider flex items-center gap-1.5 font-semibold">
              <Monitor size={14} className="text-anime-yellow" />
              <span>Desktop Framing:</span>
            </span>
            <div className="inline-flex p-1 rounded-xl bg-graphite-900/90 border border-white/10 backdrop-blur-md">
              {(
                [
                  { id: 'auto', label: 'Auto (Responsive)' },
                  { id: 'ultrawide', label: 'Ultrawide 21:9' },
                  { id: 'studio', label: 'Studio 16:9' },
                  { id: 'laptop', label: 'Laptop 16:10' },
                ] as const
              ).map(({ id, label }) => (
                <button
                  key={id}
                  onClick={() => setViewMode(id)}
                  className={`px-3 py-1.5 rounded-lg transition-all font-medium text-[11px] cursor-pointer ${
                    viewMode === id
                      ? 'bg-anime-yellow/20 text-anime-yellow border border-anime-yellow/40 shadow-sm font-semibold'
                      : 'text-anime-cream/60 hover:text-anime-cream hover:bg-white/5 border border-transparent'
                  }`}
                  title={`Apply ${label} desktop presentation style`}
                >
                  {label}
                </button>
              ))}
            </div>
          </div>

          {/* Aesthetic Aura */}
          <div className="flex flex-wrap items-center gap-2.5 w-full md:w-auto">
            <span className="text-anime-cream/60 uppercase text-[11px] tracking-wider flex items-center gap-1.5 font-semibold">
              <Palette size={14} className="text-anime-green" />
              <span>Vinland Aesthetic:</span>
            </span>
            <div className="inline-flex p-1 rounded-xl bg-graphite-900/90 border border-white/10 backdrop-blur-md">
              {(
                [
                  { id: 'duality', label: '◈ Duality' },
                  { id: 'dawn', label: '◈ Golden Dawn' },
                  { id: 'emerald', label: '◈ Sovereign Emerald' },
                  { id: 'obsidian', label: '◈ Pure Obsidian' },
                ] as const
              ).map(({ id, label }) => (
                <button
                  key={id}
                  onClick={() => setArtStyle(id)}
                  className={`px-3 py-1.5 rounded-lg transition-all font-medium text-[11px] cursor-pointer ${
                    artStyle === id
                      ? 'bg-anime-green/20 text-anime-green border border-anime-green/40 shadow-sm font-semibold'
                      : 'text-anime-cream/60 hover:text-anime-cream hover:bg-white/5 border border-transparent'
                  }`}
                  title={`Switch to ${label} visual aesthetic`}
                >
                  {label}
                </button>
              ))}
            </div>
          </div>
        </div>

        {/* Embedded Audio Visualizer */}
        <div className="mt-8 pt-8 border-t border-white/5">
          <AudioVisualizer />
        </div>
      </div>
    </section>
  );
};
