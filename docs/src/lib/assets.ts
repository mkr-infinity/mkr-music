import React from 'react';

/**
 * Reliable asset path resolution across GitHub Pages subpaths, Vercel, Netlify, and Local Preview
 */
export function getAssetUrl(path: string): string {
  const clean = path.replace(/^\/+/, '');
  const base = import.meta.env.BASE_URL || './';
  if (base.endsWith('/')) {
    return `${base}${clean}`;
  }
  return `${base}/${clean}`;
}

export const APP_LOGO = getAssetUrl('favicon.jpg');

/**
 * Bulletproof fallback for the main app logo
 */
export function handleLogoError(e: React.SyntheticEvent<HTMLImageElement, Event>) {
  const target = e.currentTarget;
  const attempt = Number(target.getAttribute('data-error-attempt') || 0);
  if (attempt === 0) {
    target.setAttribute('data-error-attempt', '1');
    target.src = getAssetUrl('assets/branding/mkr-music-logo.jpg');
  } else if (attempt === 1) {
    target.setAttribute('data-error-attempt', '2');
    target.src = './favicon.jpg';
  } else if (attempt === 2) {
    target.setAttribute('data-error-attempt', '3');
    target.src = './assets/branding/mkr-music-logo.jpg';
  }
}

/**
 * Bulletproof fallback for screenshot previews
 */
export function handleScreenshotError(
  e: React.SyntheticEvent<HTMLImageElement, Event>,
  filename: string
) {
  const target = e.currentTarget;
  const attempt = Number(target.getAttribute('data-error-attempt') || 0);
  if (attempt === 0) {
    target.setAttribute('data-error-attempt', '1');
    target.src = `./assets/screenshots/${filename}`;
  } else if (attempt === 1) {
    target.setAttribute('data-error-attempt', '2');
    target.src = `assets/screenshots/${filename}`;
  }
}

/**
 * Bulletproof fallback for developer profile avatar
 */
export function handleDeveloperAvatarError(e: React.SyntheticEvent<HTMLImageElement, Event>) {
  const target = e.currentTarget;
  const attempt = Number(target.getAttribute('data-error-attempt') || 0);
  if (attempt === 0) {
    target.setAttribute('data-error-attempt', '1');
    target.src = getAssetUrl('assets/developer_avatar.png');
  } else if (attempt === 1) {
    target.setAttribute('data-error-attempt', '2');
    target.src = './assets/developer_avatar.png';
  } else if (attempt === 2) {
    target.setAttribute('data-error-attempt', '3');
    target.src = APP_LOGO;
  }
}

export const VINLAND_SAGA_ASSETS = {
  ultrawideWebp: getAssetUrl('assets/vinland_saga_desktop_ultrawide.webp'),
  qhdWebp: getAssetUrl('assets/vinland_saga_desktop_qhd.webp'),
  fhdWebp: getAssetUrl('assets/vinland_saga_desktop_fhd.webp'),
  laptopWebp: getAssetUrl('assets/vinland_saga_desktop_laptop.webp'),
  qhdJpg: getAssetUrl('assets/vinland_saga_desktop_qhd.jpg'),
  fhdJpg: getAssetUrl('assets/vinland_saga_desktop_fhd.jpg'),
  original: getAssetUrl('assets/vinland_saga_bg.jpg'),
  legacy: getAssetUrl('assets/solo_leveling_music_bg.jpg'),
};

export const SOLO_LEVELING_BG = VINLAND_SAGA_ASSETS.legacy;

export function handleVinlandBgError(e: React.SyntheticEvent<HTMLImageElement, Event>) {
  const target = e.currentTarget;
  const attempt = Number(target.getAttribute('data-error-attempt') || 0);
  if (attempt === 0) {
    target.setAttribute('data-error-attempt', '1');
    target.src = VINLAND_SAGA_ASSETS.qhdJpg;
  } else if (attempt === 1) {
    target.setAttribute('data-error-attempt', '2');
    target.src = VINLAND_SAGA_ASSETS.original;
  } else if (attempt === 2) {
    target.setAttribute('data-error-attempt', '3');
    target.src = VINLAND_SAGA_ASSETS.legacy;
  } else if (attempt === 3) {
    target.setAttribute('data-error-attempt', '4');
    target.src = './assets/solo_leveling_music_bg.jpg';
  }
}

export const handleSoloLevelingBgError = handleVinlandBgError;


