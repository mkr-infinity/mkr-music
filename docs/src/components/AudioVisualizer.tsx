import React, { useEffect, useRef } from 'react';

export const AudioVisualizer: React.FC = () => {
  const canvasRef = useRef<HTMLCanvasElement | null>(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    let animationFrameId: number;
    let isVisible = true;
    let width = (canvas.width = canvas.parentElement?.clientWidth || window.innerWidth);
    let height = (canvas.height = 140);

    const handleResize = () => {
      if (!canvas) return;
      width = canvas.width = canvas.parentElement?.clientWidth || window.innerWidth;
      height = canvas.height = 140;
    };

    window.addEventListener('resize', handleResize);

    const observer = new IntersectionObserver(([entry]) => {
      isVisible = entry.isIntersecting;
    });
    observer.observe(canvas);

    // Visualizer Bars definition
    const barCount = 48;
    const bars: { phase: number; speed: number; heightMultiplier: number }[] = [];
    for (let i = 0; i < barCount; i++) {
      bars.push({
        phase: Math.random() * Math.PI * 2,
        speed: 0.03 + Math.random() * 0.04,
        heightMultiplier: 0.25 + Math.sin((i / barCount) * Math.PI) * 0.75,
      });
    }

    let time = 0;

    const render = () => {
      if (!isVisible) {
        animationFrameId = requestAnimationFrame(render);
        return;
      }

      ctx.clearRect(0, 0, width, height);

      const barWidth = Math.max(3, (width - barCount * 4) / barCount);
      const gap = 4;
      const startX = (width - (barWidth + gap) * barCount) / 2;

      time += 0.04;

      for (let i = 0; i < barCount; i++) {
        const bar = bars[i];
        bar.phase += bar.speed;
        const currentAmp = Math.abs(Math.sin(bar.phase + time)) * 0.7 + Math.sin(time * 0.5 + i * 0.15) * 0.3;
        const barH = Math.max(8, currentAmp * (height - 20) * bar.heightMultiplier);

        const x = startX + i * (barWidth + gap);
        const y = height / 2 - barH / 2;

        // Gradient based on index (green to yellow anime aesthetic)
        const gradient = ctx.createLinearGradient(x, y, x, y + barH);
        if (i % 3 === 0) {
          gradient.addColorStop(0, 'rgba(250, 204, 21, 0.85)'); // anime-yellow
          gradient.addColorStop(0.5, 'rgba(34, 197, 94, 0.9)'); // anime-green
          gradient.addColorStop(1, 'rgba(250, 204, 21, 0.85)');
        } else {
          gradient.addColorStop(0, 'rgba(34, 197, 94, 0.9)'); // anime-green
          gradient.addColorStop(0.5, 'rgba(251, 248, 238, 0.8)'); // anime-cream
          gradient.addColorStop(1, 'rgba(34, 197, 94, 0.9)');
        }

        ctx.fillStyle = gradient;
        ctx.beginPath();
        ctx.roundRect(x, y, barWidth, barH, [barWidth / 2]);
        ctx.fill();

        // Subtle glow on highest peaks
        if (barH > height * 0.5) {
          ctx.shadowColor = 'rgba(34, 197, 94, 0.5)';
          ctx.shadowBlur = 8;
        } else {
          ctx.shadowColor = 'transparent';
          ctx.shadowBlur = 0;
        }
      }

      animationFrameId = requestAnimationFrame(render);
    };

    render();

    return () => {
      window.removeEventListener('resize', handleResize);
      cancelAnimationFrame(animationFrameId);
      observer.disconnect();
    };
  }, []);

  return (
    <div className="w-full relative py-4 overflow-hidden select-none pointer-events-none">
      <div className="flex items-center justify-between text-[11px] font-mono text-anime-green/80 uppercase tracking-widest px-4 mb-2">
        <span className="flex items-center gap-1.5">
          <span className="text-anime-green">◈</span>
          <span>Acoustic Frequency Monitor · 48 kHz High-Res</span>
        </span>
        <span className="text-anime-yellow/80">32-Bit Float DSP</span>
      </div>
      <canvas
        ref={canvasRef}
        className="w-full h-[140px] opacity-85 transition-opacity duration-300"
        aria-hidden="true"
      />
    </div>
  );
};
