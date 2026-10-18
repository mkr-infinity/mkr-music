import React from 'react';
import { Smartphone, HardDrive, Cpu, Shield, ArrowUpRight, Code2 } from 'lucide-react';
import { OFFICIAL_LINKS } from '@/lib/release';
import { GithubIcon } from './BrandIcons';

export const Requirements: React.FC = () => {
  return (
    <section id="requirements" className="py-24 relative overflow-hidden" aria-label="System Requirements & Open Source">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        
        {/* Header without AI sparkles */}
        <div className="text-center max-w-3xl mx-auto mb-16">
          <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-graphite-900/90 border border-white/10 text-xs font-mono text-anime-cream mb-4">
            <Cpu size={13} />
            <span>Hardware & Platform Compatibility</span>
          </div>
          <h2 className="text-3xl sm:text-5xl font-extrabold text-anime-cream tracking-tight mb-6">
            Light on Resources, <br />
            <span className="text-transparent bg-clip-text bg-gradient-to-r from-anime-green via-anime-yellow to-anime-cream">
              Boundless in Reach.
            </span>
          </h2>
          <p className="text-base sm:text-lg text-anime-cream/70 leading-relaxed">
            Engineered to run seamlessly across almost any modern Android smartphone or tablet without consuming excessive memory or battery.
          </p>
        </div>

        {/* Specs Grid */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-16">
          
          <div className="neo-glass-card p-6 sm:p-8 flex flex-col justify-between border-anime-green/30 hover:border-anime-green/60">
            <div>
              <div className="w-12 h-12 rounded-2xl bg-anime-green/15 text-anime-green flex items-center justify-center border border-anime-green/30 mb-6">
                <Smartphone size={24} />
              </div>
              <span className="text-xs font-mono text-anime-green uppercase tracking-wider block mb-2">
                01 · OS Minimum
              </span>
              <h3 className="text-2xl font-bold text-anime-cream mb-2">
                Android 8.0+
              </h3>
              <p className="text-sm text-anime-cream/70 leading-relaxed">
                API 26 (Oreo) or higher. Fully compatible with Android 9, 10, 11, 12, 13, 14, and Android 15.
              </p>
            </div>
            <div className="pt-4 mt-4 border-t border-white/5 text-[11px] font-mono text-anime-cream/50">
              Verified in build.gradle.kts
            </div>
          </div>

          <div className="neo-glass-card neo-glass-yellow-card p-6 sm:p-8 flex flex-col justify-between border-anime-yellow/30 hover:border-anime-yellow/60">
            <div>
              <div className="w-12 h-12 rounded-2xl bg-anime-yellow/15 text-anime-yellow flex items-center justify-center border border-anime-yellow/30 mb-6">
                <HardDrive size={24} />
              </div>
              <span className="text-xs font-mono text-anime-yellow uppercase tracking-wider block mb-2">
                02 · Storage Recommendation
              </span>
              <h3 className="text-2xl font-bold text-anime-cream mb-2">
                500 MB Free
              </h3>
              <p className="text-sm text-anime-cream/70 leading-relaxed">
                The universal package is lightweight (~35 MB), leaving ample room for high-res song caching and offline tracks.
              </p>
            </div>
            <div className="pt-4 mt-4 border-t border-white/5 text-[11px] font-mono text-anime-yellow">
              Configurable 50–1000 song quota
            </div>
          </div>

          <div className="neo-glass-card p-6 sm:p-8 flex flex-col justify-between border-white/10 hover:border-anime-cream/40">
            <div>
              <div className="w-12 h-12 rounded-2xl bg-anime-cream/15 text-anime-cream flex items-center justify-center border border-white/20 mb-6">
                <Cpu size={24} />
              </div>
              <span className="text-xs font-mono text-anime-cream uppercase tracking-wider block mb-2">
                03 · Memory & CPU
              </span>
              <h3 className="text-2xl font-bold text-anime-cream mb-2">
                1 GB RAM
              </h3>
              <p className="text-sm text-anime-cream/70 leading-relaxed">
                Lightweight background service architecture guarantees smooth playback even on budget hardware.
              </p>
            </div>
            <div className="pt-4 mt-4 border-t border-white/5 text-[11px] font-mono text-anime-cream/50">
              Arm64 · Armeabi · x86 Universal
            </div>
          </div>
        </div>

        {/* Open Source Banner */}
        <div id="open-source" className="neo-glass-card p-8 sm:p-12 border border-anime-green/30 hover:border-anime-green/60 relative overflow-hidden">
          <div className="flex flex-col lg:flex-row items-start lg:items-center justify-between gap-8">
            <div className="max-w-2xl">
              <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-graphite-900 border border-white/10 text-xs font-mono text-anime-green mb-3">
                <Code2 size={13} />
                <span>GPL-3.0 Open-Source License</span>
              </div>
              <h3 className="text-2xl sm:text-3xl font-bold text-anime-cream mb-3">
                Built in the Open. Owned by the Listener.
              </h3>
              <p className="text-sm sm:text-base text-anime-cream/75 leading-relaxed">
                MKR Music is 100% free software. No proprietary trackers, no analytics harvesting, no paywalls. Inspect the code, submit pull requests, or fork the repository freely.
              </p>
            </div>

            <div className="flex flex-wrap items-center gap-4">
              <a
                href={OFFICIAL_LINKS.androidRepo}
                target="_blank"
                rel="noopener noreferrer"
                className="inline-flex items-center gap-2 px-6 py-3.5 rounded-2xl bg-gradient-to-r from-anime-green to-emerald-500 text-graphite-950 font-bold text-sm shadow-neo-glow-green hover:brightness-110 transition-all cursor-pointer"
              >
                <GithubIcon size={16} />
                <span>Explore Repository</span>
                <ArrowUpRight size={14} />
              </a>

              <a
                href={OFFICIAL_LINKS.license}
                target="_blank"
                rel="noopener noreferrer"
                className="inline-flex items-center gap-2 px-6 py-3.5 rounded-2xl bg-graphite-900/90 text-anime-cream hover:text-anime-yellow font-semibold text-sm border border-white/10 hover:border-anime-yellow/30 transition-all cursor-pointer"
              >
                <Shield size={16} />
                <span>Read License</span>
              </a>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};
