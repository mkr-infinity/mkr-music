import React, { useEffect, useRef, useState } from 'react';

interface MagneticNode {
  ox: number; // original X
  oy: number; // original Y
  x: number;  // current X
  y: number;  // current Y
  vx: number; // velocity X
  vy: number; // velocity Y
}

interface Ripple {
  x: number;
  y: number;
  radius: number;
  maxRadius: number;
  opacity: number;
}

export const InteractiveBackground: React.FC = () => {
  const canvasRef = useRef<HTMLCanvasElement | null>(null);
  const [cursorPos, setCursorPos] = useState({ x: -1000, y: -1000 });

  useEffect(() => {
    const handlePointerMove = (e: PointerEvent) => {
      setCursorPos({ x: e.clientX, y: e.clientY });
    };

    window.addEventListener('pointermove', handlePointerMove, { passive: true });
    return () => window.removeEventListener('pointermove', handlePointerMove);
  }, []);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    let animId: number;
    let width = (canvas.width = window.innerWidth);
    let height = (canvas.height = window.innerHeight);

    const mouse = { x: -9999, y: -9999, targetX: -9999, targetY: -9999 };
    const ripples: Ripple[] = [];
    let nodes: MagneticNode[] = [];

    const spacing = 46;
    const magneticRadius = 240;
    const springStrength = 0.08;
    const damping = 0.82;

    const initNodes = () => {
      nodes = [];
      const cols = Math.ceil(width / spacing) + 1;
      const rows = Math.ceil(height / spacing) + 1;

      for (let c = 0; c < cols; c++) {
        for (let r = 0; r < rows; r++) {
          const x = c * spacing;
          const y = r * spacing;
          nodes.push({
            ox: x,
            oy: y,
            x: x,
            y: y,
            vx: 0,
            vy: 0,
          });
        }
      }
    };

    initNodes();

    const handleResize = () => {
      if (!canvas) return;
      width = canvas.width = window.innerWidth;
      height = canvas.height = window.innerHeight;
      initNodes();
    };

    const handleMouseMove = (e: MouseEvent) => {
      mouse.targetX = e.clientX;
      mouse.targetY = e.clientY;
    };

    const handleMouseDown = (e: MouseEvent) => {
      ripples.push({
        x: e.clientX,
        y: e.clientY,
        radius: 0,
        maxRadius: 300,
        opacity: 0.85,
      });
      if (ripples.length > 5) ripples.shift();
    };

    window.addEventListener('resize', handleResize);
    window.addEventListener('mousemove', handleMouseMove, { passive: true });
    window.addEventListener('mousedown', handleMouseDown, { passive: true });

    const render = () => {
      // Lerp mouse
      mouse.x += (mouse.targetX - mouse.x) * 0.15;
      mouse.y += (mouse.targetY - mouse.y) * 0.15;

      ctx.clearRect(0, 0, width, height);

      // 1. Draw Subtle Static Parchment Texture Points
      ctx.fillStyle = 'rgba(251, 248, 238, 0.025)';
      for (let x = 0; x < width; x += spacing * 2) {
        for (let y = 0; y < height; y += spacing * 2) {
          ctx.beginPath();
          ctx.arc(x, y, 0.7, 0, Math.PI * 2);
          ctx.fill();
        }
      }

      // 2. Process Ripples
      for (let i = ripples.length - 1; i >= 0; i--) {
        const rip = ripples[i];
        rip.radius += 5;
        rip.opacity *= 0.95;

        ctx.beginPath();
        ctx.arc(rip.x, rip.y, rip.radius, 0, Math.PI * 2);
        ctx.strokeStyle = `rgba(250, 204, 21, ${rip.opacity * 0.45})`;
        ctx.lineWidth = 1.2;
        ctx.stroke();

        if (rip.opacity < 0.01 || rip.radius > rip.maxRadius) {
          ripples.splice(i, 1);
        }
      }

      // 3. Magnetic Node Simulation
      for (let i = 0; i < nodes.length; i++) {
        const node = nodes[i];

        // Magnetic Attraction towards cursor
        const dx = mouse.x - node.ox;
        const dy = mouse.y - node.oy;
        const dist = Math.sqrt(dx * dx + dy * dy);

        let targetX = node.ox;
        let targetY = node.oy;
        let proximity = 0;

        if (dist < magneticRadius && dist > 0) {
          proximity = 1 - dist / magneticRadius;
          // Magnetic pull with cubic falloff
          const pull = Math.pow(proximity, 2) * 32;
          const angle = Math.atan2(dy, dx);
          targetX = node.ox + Math.cos(angle) * pull;
          targetY = node.oy + Math.sin(angle) * pull;
        }

        // Ripple displacement force
        for (const rip of ripples) {
          const rdx = node.ox - rip.x;
          const rdy = node.oy - rip.y;
          const rdist = Math.sqrt(rdx * rdx + rdy * rdy);
          const waveWidth = 45;
          const diff = Math.abs(rdist - rip.radius);
          if (diff < waveWidth) {
            const push = (1 - diff / waveWidth) * rip.opacity * 16;
            const angle = Math.atan2(rdy, rdx);
            targetX += Math.cos(angle) * push;
            targetY += Math.sin(angle) * push;
            proximity = Math.max(proximity, rip.opacity * 0.6);
          }
        }

        // Spring physics: force = (target - current) * k
        const ax = (targetX - node.x) * springStrength;
        const ay = (targetY - node.y) * springStrength;

        node.vx = (node.vx + ax) * damping;
        node.vy = (node.vy + ay) * damping;

        node.x += node.vx;
        node.y += node.vy;

        // Render Node
        const radius = proximity > 0.1 ? 1.0 + proximity * 2.4 : 0.8;
        ctx.beginPath();
        ctx.arc(node.x, node.y, radius, 0, Math.PI * 2);

        if (proximity > 0.15) {
          // Warm Vinland Saga magnetic glow (golden wheat & emerald moss)
          ctx.fillStyle = `rgba(250, 204, 21, ${0.4 + proximity * 0.6})`;
          ctx.shadowColor = 'rgba(250, 204, 21, 0.5)';
          ctx.shadowBlur = 5;
        } else {
          ctx.fillStyle = 'rgba(251, 248, 238, 0.08)';
          ctx.shadowBlur = 0;
        }
        ctx.fill();
      }

      ctx.shadowBlur = 0;
      animId = requestAnimationFrame(render);
    };

    render();

    return () => {
      window.removeEventListener('resize', handleResize);
      window.removeEventListener('mousemove', handleMouseMove);
      window.removeEventListener('mousedown', handleMouseDown);
      cancelAnimationFrame(animId);
    };
  }, []);

  return (
    <div className="fixed inset-0 pointer-events-none z-0 overflow-hidden" aria-hidden="true">
      {/* Base Nordic Dark Graphite */}
      <div className="absolute inset-0 bg-graphite-950" />

      {/* Magnetic Cursor Spotlight (Golden Wheat & Forest Green - No Blue) */}
      <div
        className="absolute w-[620px] h-[620px] rounded-full blur-[130px] transition-transform duration-75 ease-out opacity-45 pointer-events-none"
        style={{
          transform: `translate3d(${cursorPos.x - 310}px, ${cursorPos.y - 310}px, 0)`,
          background:
            'radial-gradient(circle, rgba(250, 204, 21, 0.18) 0%, rgba(34, 197, 94, 0.14) 35%, rgba(251, 248, 238, 0.05) 60%, transparent 75%)',
        }}
      />

      {/* Atmospheric Vinland Saga Warm Earth Orbs */}
      <div className="absolute -top-40 -left-40 w-[600px] h-[600px] bg-gradient-to-tr from-anime-green/10 via-anime-yellow/8 to-transparent rounded-full blur-[140px]" />
      <div className="absolute top-1/2 -right-40 w-[550px] h-[550px] bg-gradient-to-bl from-anime-yellow/12 via-anime-green/8 to-transparent rounded-full blur-[140px]" />
      <div className="absolute -bottom-40 left-1/3 w-[650px] h-[650px] bg-gradient-to-t from-anime-yellow/8 via-anime-green/6 to-transparent rounded-full blur-[150px]" />

      {/* Magnetic Canvas */}
      <canvas ref={canvasRef} className="absolute inset-0 w-full h-full opacity-85" />
    </div>
  );
};
