import React, { useState, useEffect } from 'react';
import { createPortal } from 'react-dom';
import {
  Heart,
  Copy,
  Check,
  Coffee,
  X,
  HeartHandshake,
  ExternalLink,
  ShieldAlert,
  CreditCard
} from 'lucide-react';
import { OFFICIAL_LINKS } from '@/lib/release';

export const SupportSection: React.FC = () => {
  const [copied, setCopied] = useState<boolean>(false);
  const [showThankYouModal, setShowThankYouModal] = useState<boolean>(false);
  const [copyError, setCopyError] = useState<string | null>(null);

  const handleCopyUpi = async () => {
    const upi = OFFICIAL_LINKS.upiId;
    setCopyError(null);

    let success = false;
    if (navigator.clipboard && window.isSecureContext) {
      try {
        await navigator.clipboard.writeText(upi);
        success = true;
      } catch {
        success = false;
      }
    }

    if (!success) {
      try {
        const textarea = document.createElement('textarea');
        textarea.value = upi;
        textarea.style.position = 'fixed';
        textarea.style.left = '-999999px';
        textarea.style.top = '-999999px';
        document.body.appendChild(textarea);
        textarea.focus();
        textarea.select();
        success = document.execCommand('copy');
        document.body.removeChild(textarea);
      } catch {
        success = false;
      }
    }

    if (success) {
      setCopied(true);
      setShowThankYouModal(true);
      setTimeout(() => setCopied(false), 3500);
    } else {
      setCopyError('Unable to copy automatically. Please copy the UPI ID manually.');
    }
  };

  useEffect(() => {
    if (!showThankYouModal) return;
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        setShowThankYouModal(false);
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [showThankYouModal]);

  return (
    <section id="support" className="py-24 md:py-32 relative overflow-hidden" aria-label="Support & Voluntary Contributions">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
        
        {/* Section Header */}
        <div className="text-center max-w-3xl mx-auto mb-16">
          <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-graphite-900/90 border border-white/10 text-xs font-mono text-anime-yellow mb-4">
            <Heart size={13} className="text-anime-yellow fill-anime-yellow/30" />
            <span>Voluntary Project Backing</span>
          </div>
          <h2 className="text-3xl sm:text-5xl font-extrabold text-anime-cream tracking-tight mb-6">
            Keep MKR Music <br />
            <span className="text-transparent bg-clip-text bg-gradient-to-r from-anime-yellow via-anime-green to-anime-cream">
              100% Free & Ad-Free.
            </span>
          </h2>
          <p className="text-base sm:text-lg text-anime-cream/70 leading-relaxed">
            MKR Music is an independent open-source project created out of love for pristine acoustics. Financial support is completely optional and directly funds continuous development.
          </p>
        </div>

        {/* Support Cards Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-8 max-w-4xl mx-auto">
          
          {/* Card 1: Direct UPI (India) */}
          <div className="neo-glass-card p-8 sm:p-10 flex flex-col justify-between border-anime-green/30 hover:border-anime-green/50">
            <div>
              <div className="flex items-center justify-between mb-6">
                <div className="w-12 h-12 rounded-2xl bg-anime-green/15 text-anime-green flex items-center justify-center border border-anime-green/30">
                  <CreditCard size={22} />
                </div>
                <span className="text-[10px] font-mono text-anime-green uppercase tracking-wider font-semibold px-2.5 py-1 rounded-full bg-anime-green/10 border border-anime-green/20">
                  Direct UPI (India)
                </span>
              </div>

              <h3 className="text-2xl font-bold text-anime-cream mb-2">
                Instant UPI Contribution
              </h3>
              <p className="text-sm text-anime-cream/70 leading-relaxed mb-6">
                Send voluntary contributions directly through Google Pay, PhonePe, Paytm, BHIM, or any UPI-compatible application.
              </p>

              {/* UPI ID Display Field */}
              <div
                onClick={handleCopyUpi}
                className="p-4 rounded-2xl bg-graphite-950/90 border border-white/10 hover:border-anime-green/50 mb-3 select-all flex items-center justify-between font-mono text-sm cursor-pointer transition-colors group/copy"
                title="Click to copy UPI ID"
              >
                <div className="flex flex-col">
                  <span className="text-[10px] text-anime-cream/50 uppercase tracking-wider">Verified Official UPI ID</span>
                  <span className="text-anime-green font-bold text-base sm:text-lg group-hover/copy:underline">{OFFICIAL_LINKS.upiId}</span>
                </div>
                <div className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-white/5 group-hover/copy:bg-anime-green/10 text-anime-cream/70 group-hover/copy:text-anime-green transition-colors text-xs font-semibold">
                  <Copy size={13} />
                  <span>Click to Copy</span>
                </div>
              </div>

              {/* Supported UPI Apps Pills */}
              <div className="flex flex-wrap items-center gap-1.5 mb-5 font-mono text-[10px] text-anime-cream/60">
                <span className="text-anime-cream/40 mr-0.5">Works with:</span>
                {['Google Pay', 'PhonePe', 'Paytm', 'BHIM', 'Cred', 'Amazon Pay'].map((app) => (
                  <span key={app} className="px-2 py-0.5 rounded-md bg-white/[0.04] border border-white/5">
                    {app}
                  </span>
                ))}
              </div>

              {copyError && (
                <div className="p-3 rounded-xl bg-red-950/50 border border-red-500/30 text-xs text-red-300 flex items-center gap-2 mb-4">
                  <ShieldAlert size={14} />
                  <span>{copyError}</span>
                </div>
              )}
            </div>

            <button
              type="button"
              onClick={handleCopyUpi}
              className={`w-full py-4 px-6 rounded-2xl font-bold text-sm flex items-center justify-center gap-2.5 transition-all focus:outline-none focus:ring-2 focus:ring-anime-green cursor-pointer active:scale-98 ${
                copied
                  ? 'liquid-glass-btn text-graphite-950 font-extrabold shadow-[0_12px_32px_rgba(34,197,94,0.7)]'
                  : 'bg-graphite-900/90 hover:bg-graphite-850 text-anime-cream border border-anime-green/40 hover:border-anime-green shadow-neo-card hover:shadow-neo-glow-green'
              }`}
              aria-label="Copy UPI ID to clipboard"
            >
              {copied ? (
                <>
                  <Check size={18} className="stroke-[3]" />
                  <span>UPI ID Copied to Clipboard!</span>
                </>
              ) : (
                <>
                  <Copy size={16} className="text-anime-green" />
                  <span>Copy UPI ID ({OFFICIAL_LINKS.upiId})</span>
                </>
              )}
            </button>
          </div>

          {/* Card 2: Buy Me a Coffee (International) */}
          <div className="neo-glass-card p-8 sm:p-10 flex flex-col justify-between border-anime-yellow/30 hover:border-anime-yellow/50 neo-glass-yellow-card">
            <div>
              <div className="flex items-center justify-between mb-6">
                <div className="w-12 h-12 rounded-2xl bg-anime-yellow/15 text-anime-yellow flex items-center justify-center border border-anime-yellow/30">
                  <Coffee size={22} />
                </div>
                <span className="text-[10px] font-mono text-anime-yellow uppercase tracking-wider font-semibold px-2.5 py-1 rounded-full bg-anime-yellow/10 border border-anime-yellow/20">
                  International Cards & PayPal
                </span>
              </div>

              <h3 className="text-2xl font-bold text-anime-cream mb-2">
                Buy Me a Coffee
              </h3>
              <p className="text-sm text-anime-cream/70 leading-relaxed mb-6">
                Back development using international debit/credit cards, Apple Pay, Google Pay, or PayPal via the official Buy Me a Coffee page.
              </p>

              <div className="p-4 rounded-2xl bg-graphite-950/90 border border-white/10 mb-3 flex items-center justify-between font-mono text-sm">
                <div className="flex flex-col">
                  <span className="text-[10px] text-anime-cream/50 uppercase tracking-wider">Verified Creator Portal</span>
                  <span className="text-anime-yellow font-bold">buymeacoffee.com/mkr_infinity</span>
                </div>
                <span className="text-xs px-2.5 py-1 rounded-lg bg-white/5 text-anime-cream/60">
                  Worldwide
                </span>
              </div>

              {/* Supported Payment Methods Pills */}
              <div className="flex flex-wrap items-center gap-1.5 mb-5 font-mono text-[10px] text-anime-cream/60">
                <span className="text-anime-cream/40 mr-0.5">Accepts:</span>
                {['Apple Pay', 'Google Pay', 'Visa', 'Mastercard', 'PayPal'].map((method) => (
                  <span key={method} className="px-2 py-0.5 rounded-md bg-white/[0.04] border border-white/5">
                    {method}
                  </span>
                ))}
              </div>
            </div>

            <a
              href={OFFICIAL_LINKS.buyMeACoffee}
              target="_blank"
              rel="noopener noreferrer"
              className="w-full py-4 px-6 rounded-2xl font-bold text-sm bg-gradient-to-r from-anime-yellow via-amber-400 to-anime-yellow text-graphite-950 shadow-[0_8px_25px_-4px_rgba(250,204,21,0.5)] hover:shadow-[0_14px_35px_-4px_rgba(250,204,21,0.7)] hover:brightness-110 flex items-center justify-center gap-2.5 transition-all text-center cursor-pointer active:scale-98 select-none"
              aria-label="Support via Buy Me a Coffee"
            >
              <Heart size={16} className="fill-graphite-950" />
              <span>Support on Buy Me A Coffee</span>
              <ExternalLink size={14} />
            </a>
          </div>
        </div>

        {/* Voluntary Disclaimer */}
        <p className="text-center mt-12 text-xs text-anime-cream/50 max-w-lg mx-auto">
          Contributions are entirely voluntary and help cover build servers, domain costs, and future release testing. Copying the UPI ID does not make a payment until you paste it into your preferred UPI app.
        </p>
      </div>

      {/* Celebratory Appreciation Modal (Portaled to body to guarantee center alignment on all viewports) */}
      {showThankYouModal && typeof document !== 'undefined' && createPortal(
        <div
          role="dialog"
          aria-modal="true"
          aria-labelledby="thank-you-heading"
          className="fixed inset-0 z-[100] flex items-center justify-center p-4 bg-graphite-950/85 backdrop-blur-xl animate-in fade-in duration-200"
          onClick={() => setShowThankYouModal(false)}
        >
          <div
            className="relative w-full max-w-md p-6 sm:p-8 rounded-3xl neo-glass-card border border-anime-green/40 shadow-2xl overflow-hidden text-center m-auto"
            onClick={(e) => e.stopPropagation()}
          >
            {/* Close Button */}
            <button
              type="button"
              onClick={() => setShowThankYouModal(false)}
              className="absolute top-4 right-4 p-2 rounded-xl bg-graphite-900 text-anime-cream hover:bg-white/10 transition-colors cursor-pointer"
              aria-label="Close thank-you message"
            >
              <X size={18} />
            </button>

            {/* Appreciation Icon */}
            <div className="relative mx-auto w-16 h-16 mb-5 flex items-center justify-center">
              <div className="absolute inset-0 rounded-2xl bg-gradient-to-tr from-anime-green to-anime-yellow blur-lg opacity-40" />
              <div className="relative w-full h-full rounded-2xl bg-graphite-950 border border-anime-green/40 flex items-center justify-center shadow-lg">
                <HeartHandshake size={32} className="text-anime-green" />
              </div>
            </div>

            {/* Gratitude Message */}
            <h3 id="thank-you-heading" className="text-2xl font-bold text-anime-cream mb-2">
              Thank You So Much! ❤️
            </h3>
            <p className="text-sm text-anime-cream/80 leading-relaxed mb-6">
              Your voluntary backing powers MKR Music’s independent development, keeping it <strong className="text-anime-green">100% free, private, and ad-free</strong> for music lovers worldwide.
            </p>

            {/* Copied Pill */}
            <div className="p-3.5 rounded-2xl bg-graphite-950 border border-anime-green/30 flex items-center justify-between mb-6 font-mono text-xs">
              <div className="flex items-center gap-2">
                <Check size={16} className="text-anime-green stroke-[3]" />
                <span className="text-anime-cream font-bold">{OFFICIAL_LINKS.upiId}</span>
              </div>
              <span className="text-[11px] font-bold text-anime-green px-2 py-0.5 rounded-full bg-anime-green/15">
                Copied!
              </span>
            </div>

            <p className="text-xs text-anime-cream/50 mb-6">
              Open your preferred UPI app (Google Pay, PhonePe, Paytm, or BHIM) and paste to complete your contribution.
            </p>

            <button
              type="button"
              onClick={() => setShowThankYouModal(false)}
              className="w-full py-3.5 px-6 rounded-2xl font-bold text-sm bg-gradient-to-r from-anime-green to-emerald-500 text-graphite-950 shadow-neo-glow-green hover:brightness-110 transition-all cursor-pointer"
            >
              You're Awesome! Close
            </button>
          </div>
        </div>,
        document.body
      )}
    </section>
  );
};
