import React from 'react';
import { ExternalLink, Globe } from 'lucide-react';
import { OFFICIAL_LINKS } from '@/lib/release';
import { getAssetUrl, handleDeveloperAvatarError } from '@/lib/assets';
import { GithubIcon, InstagramIcon, TelegramIcon } from './BrandIcons';

export const DeveloperSection: React.FC = () => {
  return (
    <section className="py-20 relative overflow-hidden" aria-label="About the Crafter">
      <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
        <div className="neo-glass-card neo-glass-yellow-card rounded-3xl p-8 sm:p-12 border border-anime-yellow/30 hover:border-anime-yellow/60 relative overflow-hidden group">
          
          {/* Subtle Ambient Radial Glow */}
          <div
            className="absolute -top-24 right-0 w-96 h-96 bg-gradient-to-bl from-anime-yellow/15 via-anime-green/10 to-transparent rounded-full blur-3xl pointer-events-none transition-opacity duration-500 group-hover:opacity-100 opacity-60"
            aria-hidden="true"
          />

          <div className="grid grid-cols-1 md:grid-cols-12 gap-8 items-center relative z-10">
            
            {/* Crafter Picture - With Acoustic Pulse Ring & Official Avatar */}
            <div className="md:col-span-4 flex justify-center md:justify-start">
              <div className="relative group/pic">
                {/* Acoustic Halo Ring */}
                <div className="absolute -inset-2 rounded-[28px] bg-gradient-to-tr from-anime-green/40 via-anime-yellow/30 to-anime-cream/20 blur-md opacity-70 group-hover/pic:opacity-100 transition-opacity duration-300" />
                
                <div className="relative w-40 h-40 sm:w-48 sm:h-48 rounded-3xl p-1 bg-gradient-to-tr from-anime-green via-anime-yellow to-anime-cream shadow-2xl overflow-hidden transition-transform duration-300 group-hover/pic:scale-105">
                  <img
                    src="https://avatars.githubusercontent.com/u/125804924?v=4"
                    onError={handleDeveloperAvatarError}
                    alt="Mohammad Kaif Raja (mkr-infinity)"
                    className="w-full h-full object-cover rounded-[22px]"
                  />
                </div>

                {/* Floating Alive Crafter Badge */}
                <div className="absolute -bottom-2.5 -right-2 px-3 py-1 rounded-full bg-graphite-950/95 border border-anime-green/50 text-[10px] font-mono text-anime-green font-bold shadow-xl flex items-center gap-1.5 backdrop-blur-md">
                  <span className="w-1.5 h-1.5 rounded-full bg-anime-green" />
                  <span>CRAFTER</span>
                </div>
              </div>
            </div>

            {/* Crafter Details */}
            <div className="md:col-span-8 flex flex-col items-center md:items-start text-center md:text-left">
              <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-graphite-900/90 border border-anime-yellow/30 text-[11px] font-mono text-anime-yellow mb-3">
                <span>◈ Lead Architect & Maintainer</span>
              </div>

              <h3 className="text-2xl sm:text-4xl font-extrabold text-anime-cream mb-1">
                Mohammad Kaif Raja
              </h3>

              <span className="text-sm font-mono text-anime-green font-semibold mb-4 flex items-center gap-1">
                <span>@mkr-infinity</span>
              </span>

              {/* Roles Badges */}
              <div className="flex flex-wrap items-center justify-center md:justify-start gap-2 mb-5">
                {[
                  'Crafter of MKR Music',
                  'Vibe Coder',
                  'Problem Solver',
                  'Tech Enthusiast',
                ].map((badge) => (
                  <span
                    key={badge}
                    className="px-3 py-1 rounded-full bg-graphite-900/90 border border-white/10 text-xs font-mono text-anime-cream/80 hover:border-anime-yellow/40 transition-colors"
                  >
                    {badge}
                  </span>
                ))}
              </div>

              <p className="text-sm text-anime-cream/80 leading-relaxed max-w-xl mb-6">
                Independent software artisan dedicated to sovereign listening, Android performance engineering, and acoustic precision. Creator and maintainer of MKR Music.
              </p>

              {/* Developer Links */}
              <div className="flex flex-wrap items-center justify-center md:justify-start gap-2.5">
                <a
                  href={OFFICIAL_LINKS.devGitHub}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl bg-graphite-900/90 hover:bg-graphite-800 text-anime-cream border border-white/12 hover:border-anime-green/50 text-xs font-semibold shadow-md transition-all hover:scale-105"
                >
                  <GithubIcon size={15} />
                  <span>GitHub Profile</span>
                  <ExternalLink size={11} className="text-anime-cream/50" />
                </a>

                <a
                  href={OFFICIAL_LINKS.devWebsite}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl bg-graphite-900/90 hover:bg-graphite-800 text-anime-cream border border-white/12 hover:border-anime-yellow/50 text-xs font-semibold shadow-md transition-all hover:scale-105"
                >
                  <Globe size={14} className="text-anime-yellow" />
                  <span>Personal Website</span>
                  <ExternalLink size={11} className="text-anime-cream/50" />
                </a>

                <a
                  href={OFFICIAL_LINKS.instagram}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl bg-graphite-900/90 hover:bg-graphite-800 text-anime-cream border border-white/12 hover:border-anime-yellow/50 text-xs font-semibold shadow-md transition-all hover:scale-105"
                >
                  <InstagramIcon size={14} />
                  <span>Instagram</span>
                </a>

                <a
                  href={OFFICIAL_LINKS.telegram}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl bg-graphite-900/90 hover:bg-graphite-800 text-anime-cream border border-white/12 hover:border-anime-green/50 text-xs font-semibold shadow-md transition-all hover:scale-105"
                >
                  <TelegramIcon size={14} />
                  <span>Telegram</span>
                </a>
              </div>
            </div>

          </div>
        </div>
      </div>
    </section>
  );
};
