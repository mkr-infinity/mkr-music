import React, { useState } from 'react';
import { SlidersHorizontal, RotateCcw, Volume2 } from 'lucide-react';

const FREQUENCIES = ['31Hz', '62Hz', '125Hz', '250Hz', '500Hz', '1kHz', '2kHz', '4kHz', '8kHz', '16kHz'];

const PRESETS: Record<string, number[]> = {
  'Anime Pop': [4, 6, 3, 0, 2, 5, 7, 8, 9, 6],
  'Bass Punch': [9, 8, 6, 3, 1, 0, 0, 1, 2, 3],
  'Vocal Clarity': [1, 2, 4, 6, 8, 8, 6, 4, 2, 1],
  'Studio Hi-Res': [3, 2, 1, 0, 0, 1, 2, 4, 6, 7],
  'Flat': [0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
};

export const InteractiveEqualizer: React.FC = () => {
  const [bands, setBands] = useState<number[]>([4, 6, 3, 0, 2, 5, 7, 8, 9, 6]);
  const [activePreset, setActivePreset] = useState<string>('Anime Pop');

  const handleBandChange = (index: number, val: number) => {
    const updated = [...bands];
    updated[index] = val;
    setBands(updated);
    setActivePreset('Custom');
  };

  const applyPreset = (name: string) => {
    setActivePreset(name);
    setBands([...PRESETS[name]]);
  };

  return (
    <div className="neo-glass-card p-6 sm:p-8 rounded-3xl border border-anime-green/30 hover:border-anime-green/60 relative overflow-hidden">
      {/* Header */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 mb-6">
        <div>
          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-graphite-900/90 border border-white/10 text-xs font-mono text-anime-yellow mb-2">
            <SlidersHorizontal size={13} />
            <span>Parametric DSP Engine</span>
          </div>
          <h3 className="text-xl sm:text-2xl font-bold text-anime-cream">
            Studio 10-Band Graphic Equalizer
          </h3>
          <p className="text-sm text-anime-cream/70">
            Tune acoustic response across 10 decibel frequency bands with instant curve shaping.
          </p>
        </div>

        {/* Preset Selector */}
        <div className="flex flex-wrap items-center gap-1.5 p-1 rounded-2xl bg-graphite-900/90 border border-white/5">
          {Object.keys(PRESETS).map((preset) => (
            <button
              key={preset}
              type="button"
              onClick={() => applyPreset(preset)}
              className={`px-3 py-1.5 rounded-xl text-xs font-medium transition-all ${
                activePreset === preset
                  ? 'bg-anime-green text-graphite-950 font-bold shadow-lg shadow-anime-green/30'
                  : 'text-anime-cream/70 hover:text-anime-cream hover:bg-white/5'
              }`}
            >
              {preset}
            </button>
          ))}
        </div>
      </div>

      {/* Sliders Grid */}
      <div className="grid grid-cols-5 sm:grid-cols-10 gap-2 sm:gap-3 py-4 items-end min-h-[220px]">
        {bands.map((value, idx) => {
          const isBoost = value > 0;
          return (
            <div key={FREQUENCIES[idx]} className="flex flex-col items-center gap-2 group">
              <span className={`text-[11px] font-mono font-semibold transition-colors ${
                isBoost ? 'text-anime-green' : value < 0 ? 'text-anime-yellow' : 'text-anime-cream/50'
              }`}>
                {value > 0 ? `+${value}` : value}
              </span>

              {/* Vertical Slider Track */}
              <div className="relative w-7 sm:w-8 h-36 bg-graphite-950/80 rounded-full p-1 flex flex-col justify-end items-center border border-white/10 overflow-hidden">
                {/* Level Fill */}
                <div
                  className="w-full rounded-full transition-all duration-150"
                  style={{
                    height: `${((value + 12) / 24) * 100}%`,
                    background: isBoost
                      ? 'linear-gradient(to top, rgba(34, 197, 94, 0.4), rgba(34, 197, 94, 0.95))'
                      : 'linear-gradient(to top, rgba(250, 204, 21, 0.3), rgba(250, 204, 21, 0.8))',
                  }}
                />

                {/* Range Input Overlay */}
                <input
                  type="range"
                  min="-12"
                  max="12"
                  value={value}
                  onChange={(e) => handleBandChange(idx, parseInt(e.target.value, 10))}
                  aria-label={`${FREQUENCIES[idx]} equalizer band`}
                  className="absolute inset-0 opacity-0 cursor-pointer w-full h-full"
                />
              </div>

              <span className="text-[10px] font-mono text-anime-cream/60 group-hover:text-anime-cream transition-colors">
                {FREQUENCIES[idx]}
              </span>
            </div>
          );
        })}
      </div>

      {/* Footer Info */}
      <div className="mt-4 pt-4 border-t border-white/5 flex flex-wrap items-center justify-between gap-3 text-xs text-anime-cream/60">
        <span className="flex items-center gap-1.5">
          <Volume2 size={13} className="text-anime-green" />
          <span>Real-time decibel filtering</span>
        </span>
        <button
          type="button"
          onClick={() => applyPreset('Flat')}
          className="inline-flex items-center gap-1 text-anime-cream/70 hover:text-anime-yellow transition-colors font-mono cursor-pointer"
        >
          <RotateCcw size={12} />
          <span>Reset to Flat</span>
        </button>
      </div>
    </div>
  );
};
