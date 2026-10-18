import React from 'react';
import {
  Globe,
  Coffee,
  ArrowUp,
  Download,
  ExternalLink
} from 'lucide-react';
import { OFFICIAL_LINKS } from '@/lib/release';
import { APP_LOGO, handleLogoError } from '@/lib/assets';
import { GithubIcon, InstagramIcon, TelegramIcon } from './BrandIcons';

export const Footer: React.FC = () => {
  const scrollToTop = () => {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  return (
    <footer className="border-t border-white/10 bg-graphite-950/90 pt-16 pb-12 relative overflow-hidden">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        
        {/* Main Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-10 pb-12 border-b border-white/5">
          
          {/* Brand Info (2 cols) */}
          <div className="lg:col-span-2 flex flex-col items-start">
            <a href="#" className="flex items-center gap-3 mb-4 group">
              <div className="w-10 h-10 rounded-full p-0.5 bg-gradient-to-tr from-anime-green via-anime-yellow to-anime-cream shadow-neo-glow-green overflow-hidden">
                <img
                  src={APP_LOGO}
                  onError={handleLogoError}
                  alt="MKR Music"
                  className="w-full h-full object-cover rounded-full"
                />
              </div>
              <div>
                <span className="font-extrabold text-xl text-anime-cream group-hover:text-anime-green transition-colors">
                  MKR Music
                </span>
                <span className="block text-[10px] font-mono text-anime-cream/50">
                  Android Music Client
                </span>
              </div>
            </a>

            <p className="text-sm text-anime-cream/65 leading-relaxed max-w-sm mb-6">
              A free, open-source YouTube Music & local player for Android. Crafted for instantaneous audio streaming, smart caching, and acoustic mastery.
            </p>

            <div className="flex items-center gap-3">
              <a
                href={OFFICIAL_LINKS.androidRepo}
                target="_blank"
                rel="noopener noreferrer"
                className="p-2.5 rounded-xl bg-graphite-900 border border-white/10 text-anime-cream hover:text-anime-green hover:border-anime-green/40 transition-all"
                aria-label="GitHub Repository"
              >
                <GithubIcon size={18} />
              </a>
              <a
                href={OFFICIAL_LINKS.instagram}
                target="_blank"
                rel="noopener noreferrer"
                className="p-2.5 rounded-xl bg-graphite-900 border border-white/10 text-anime-cream hover:text-anime-yellow hover:border-anime-yellow/40 transition-all"
                aria-label="Instagram Profile"
              >
                <InstagramIcon size={18} />
              </a>
              <a
                href={OFFICIAL_LINKS.telegram}
                target="_blank"
                rel="noopener noreferrer"
                className="p-2.5 rounded-xl bg-graphite-900 border border-white/10 text-anime-cream hover:text-anime-green hover:border-anime-green/40 transition-all"
                aria-label="Telegram Channel"
              >
                <TelegramIcon size={18} />
              </a>
              <a
                href={OFFICIAL_LINKS.devWebsite}
                target="_blank"
                rel="noopener noreferrer"
                className="p-2.5 rounded-xl bg-graphite-900 border border-white/10 text-anime-cream hover:text-anime-cream hover:border-white/40 transition-all"
                aria-label="Developer Website"
              >
                <Globe size={18} />
              </a>
            </div>
          </div>

          {/* Quick Navigation */}
          <div>
            <h4 className="text-xs font-mono font-bold uppercase tracking-wider text-anime-green mb-4">
              Explore
            </h4>
            <ul className="space-y-2.5 text-sm text-anime-cream/70">
              <li>
                <a href="#features" className="hover:text-anime-cream transition-colors">
                  Features & UI
                </a>
              </li>
              <li>
                <a href="#sound" className="hover:text-anime-cream transition-colors">
                  10-Band Equalizer
                </a>
              </li>
              <li>
                <a href="#screenshots" className="hover:text-anime-cream transition-colors">
                  App Screenshots
                </a>
              </li>
              <li>
                <a href="#requirements" className="hover:text-anime-cream transition-colors">
                  Specifications
                </a>
              </li>
              <li>
                <a href="#support" className="hover:text-anime-cream transition-colors">
                  Support & Backing
                </a>
              </li>
            </ul>
          </div>

          {/* Developer & Community */}
          <div>
            <h4 className="text-xs font-mono font-bold uppercase tracking-wider text-anime-yellow mb-4">
              Community
            </h4>
            <ul className="space-y-2.5 text-sm text-anime-cream/70">
              <li>
                <a
                  href={OFFICIAL_LINKS.devGitHub}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="inline-flex items-center gap-1 hover:text-anime-cream transition-colors"
                >
                  <span>Developer GitHub</span>
                  <ExternalLink size={12} />
                </a>
              </li>
              <li>
                <a
                  href={OFFICIAL_LINKS.androidRepo}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="inline-flex items-center gap-1 hover:text-anime-cream transition-colors"
                >
                  <span>MKR Music Repo</span>
                  <ExternalLink size={12} />
                </a>
              </li>
              <li>
                <a
                  href={OFFICIAL_LINKS.instagram}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="inline-flex items-center gap-1 hover:text-anime-cream transition-colors"
                >
                  <span>Instagram</span>
                  <ExternalLink size={12} />
                </a>
              </li>
              <li>
                <a
                  href={OFFICIAL_LINKS.telegram}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="inline-flex items-center gap-1 hover:text-anime-cream transition-colors"
                >
                  <span>Telegram</span>
                  <ExternalLink size={12} />
                </a>
              </li>
              <li>
                <a
                  href={OFFICIAL_LINKS.devWebsite}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="inline-flex items-center gap-1 hover:text-anime-cream transition-colors"
                >
                  <span>Developer Site</span>
                  <ExternalLink size={12} />
                </a>
              </li>
            </ul>
          </div>

          {/* Downloads & Releases */}
          <div>
            <h4 className="text-xs font-mono font-bold uppercase tracking-wider text-anime-cream mb-4">
              Downloads
            </h4>
            <ul className="space-y-2.5 text-sm text-anime-cream/70">
              <li>
                <a
                  href={OFFICIAL_LINKS.universalApkDownload}
                  download={OFFICIAL_LINKS.universalApkName}
                  className="inline-flex items-center gap-1.5 text-anime-green font-semibold hover:underline"
                >
                  <Download size={13} />
                  <span>Universal APK</span>
                </a>
              </li>
              <li>
                <a
                  href={OFFICIAL_LINKS.latestRelease}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="hover:text-anime-cream transition-colors"
                >
                  Latest Release
                </a>
              </li>
              <li>
                <a
                  href={OFFICIAL_LINKS.issues}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="hover:text-anime-cream transition-colors"
                >
                  Report an Issue
                </a>
              </li>
              <li>
                <a
                  href={OFFICIAL_LINKS.buyMeACoffee}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="inline-flex items-center gap-1.5 text-anime-yellow font-semibold hover:underline"
                >
                  <Coffee size={13} />
                  <span>Buy Me a Coffee</span>
                </a>
              </li>
            </ul>
          </div>
        </div>

        {/* Bottom Bar without copyright line */}
        <div className="pt-8 flex flex-col sm:flex-row items-center justify-between gap-4 text-xs text-anime-cream/50">
          <span>Crafted for listeners who value music sovereignty and acoustic fidelity.</span>

          <button
            type="button"
            onClick={scrollToTop}
            className="inline-flex items-center gap-1.5 px-3.5 py-1.5 rounded-full bg-graphite-900 border border-white/10 hover:text-anime-cream hover:border-white/30 transition-all font-mono cursor-pointer"
          >
            <span>Back to Top</span>
            <ArrowUp size={13} />
          </button>
        </div>
      </div>
    </footer>
  );
};
