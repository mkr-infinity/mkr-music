import React, { useState, useEffect } from 'react';
import { Navbar } from './components/Navbar';
import { Hero } from './components/Hero';
import { FeaturesShowcase } from './components/FeaturesShowcase';
import { ScreenshotGallery, ScreenshotLightbox } from './components/ScreenshotGallery';
import { Requirements } from './components/Requirements';
import { DeveloperSection } from './components/DeveloperSection';
import { SupportSection } from './components/SupportSection';
import { StickyDownload } from './components/StickyDownload';
import { Footer } from './components/Footer';
import { InteractiveBackground } from './components/InteractiveBackground';

export function App() {
  const [activeScreenshot, setActiveScreenshot] = useState<number | null>(null);

  // Scroll Reveal Observer
  useEffect(() => {
    const handleObserver = () => {
      const items = document.querySelectorAll('.reveal-item');
      const observer = new IntersectionObserver(
        (entries) => {
          entries.forEach((entry) => {
            if (entry.isIntersecting) {
              entry.target.classList.add('is-revealed');
              observer.unobserve(entry.target);
            }
          });
        },
        { threshold: 0.08, rootMargin: '0px 0px -40px 0px' }
      );

      items.forEach((item) => observer.observe(item));

      return () => observer.disconnect();
    };

    const cleanup = handleObserver();
    return cleanup;
  }, []);

  // 60FPS Mouse Repulsive Physical Effect on all cards
  useEffect(() => {
    if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) return;

    let activeCard: HTMLElement | null = null;
    let rAFId: number;

    const handlePointerMove = (e: PointerEvent) => {
      // Fine pointers only (mouse, trackpad, stylus)
      if (e.pointerType === 'touch') return;

      const target = (e.target as HTMLElement)?.closest?.('.neo-glass-card') as HTMLElement | null;

      if (activeCard && activeCard !== target) {
        activeCard.style.transform = '';
        activeCard = null;
      }

      if (!target) return;

      activeCard = target;

      cancelAnimationFrame(rAFId);
      rAFId = requestAnimationFrame(() => {
        if (!activeCard) return;
        const rect = activeCard.getBoundingClientRect();
        const centerX = rect.left + rect.width / 2;
        const centerY = rect.top + rect.height / 2;

        // Normalized offset from card center (-1 to 1)
        const dx = Math.max(-1, Math.min(1, (e.clientX - centerX) / (rect.width / 2)));
        const dy = Math.max(-1, Math.min(1, (e.clientY - centerY) / (rect.height / 2)));

        // Repulsive physics: pushes the card away from the cursor position
        const repelX = -dx * 10;
        const repelY = -dy * 10;
        const tiltX = -dy * 7;
        const tiltY = dx * 7;

        activeCard.style.transform = `perspective(1000px) translate3d(${repelX}px, ${repelY}px, 0) rotateX(${tiltX}deg) rotateY(${tiltY}deg) scale(0.995)`;
      });
    };

    const handlePointerOut = (e: PointerEvent) => {
      if (activeCard && !activeCard.contains(e.relatedTarget as Node)) {
        activeCard.style.transform = '';
        activeCard = null;
      }
    };

    window.addEventListener('pointermove', handlePointerMove, { passive: true });
    document.addEventListener('pointerout', handlePointerOut, { passive: true });

    return () => {
      window.removeEventListener('pointermove', handlePointerMove);
      document.removeEventListener('pointerout', handlePointerOut);
      cancelAnimationFrame(rAFId);
      if (activeCard) activeCard.style.transform = '';
    };
  }, []);

  return (
    <div className="min-h-screen bg-graphite-950 text-anime-cream selection:bg-anime-green selection:text-graphite-950 relative overflow-x-hidden">
      {/* 21st.dev-inspired Interactive Cursor-Reactive Kinetic Grid Background */}
      <InteractiveBackground />

      {/* Curved Floating Navbar */}
      <Navbar />

      {/* Main Content Sections with Background Visibility */}
      <main id="top" className="relative z-10">
        <Hero onOpenScreenshot={(idx) => setActiveScreenshot(idx)} />

        <div className="reveal-item">
          <FeaturesShowcase onOpenScreenshot={(idx) => setActiveScreenshot(idx)} />
        </div>

        <div className="reveal-item">
          <ScreenshotGallery onOpenScreenshot={(idx) => setActiveScreenshot(idx)} />
        </div>

        <div className="reveal-item">
          <Requirements />
        </div>

        <div className="reveal-item">
          <DeveloperSection />
        </div>

        <div className="reveal-item">
          <SupportSection />
        </div>
      </main>

      {/* Footer */}
      <div className="relative z-10">
        <Footer />
      </div>

      {/* Bottom Right Floating Download Button (appears on scroll) */}
      <StickyDownload />

      {/* Fullscreen Screenshot Lightbox */}
      <ScreenshotLightbox
        activeIndex={activeScreenshot}
        onClose={() => setActiveScreenshot(null)}
        onChange={(idx) => setActiveScreenshot(idx)}
      />
    </div>
  );
}

export default App;
