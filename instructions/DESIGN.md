---
name: Modern Digital Slot
colors:
  surface: '#0f150e'
  surface-dim: '#0f150e'
  surface-bright: '#353b33'
  surface-container-lowest: '#0a1009'
  surface-container-low: '#171d16'
  surface-container: '#1b211a'
  surface-container-high: '#262c24'
  surface-container-highest: '#30362e'
  on-surface: '#dee4d9'
  on-surface-variant: '#becab9'
  inverse-surface: '#dee4d9'
  inverse-on-surface: '#2c322a'
  outline: '#899484'
  outline-variant: '#3f4a3c'
  surface-tint: '#78dc77'
  primary: '#78dc77'
  on-primary: '#00390a'
  primary-container: '#4caf50'
  on-primary-container: '#003c0b'
  inverse-primary: '#006e1c'
  secondary: '#c7c6c6'
  on-secondary: '#303031'
  secondary-container: '#464747'
  on-secondary-container: '#b6b5b5'
  tertiary: '#ffb1c7'
  on-tertiary: '#650032'
  tertiary-container: '#f26f9d'
  on-tertiary-container: '#690034'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#94f990'
  primary-fixed-dim: '#78dc77'
  on-primary-fixed: '#002204'
  on-primary-fixed-variant: '#005313'
  secondary-fixed: '#e3e2e2'
  secondary-fixed-dim: '#c7c6c6'
  on-secondary-fixed: '#1b1c1c'
  on-secondary-fixed-variant: '#464747'
  tertiary-fixed: '#ffd9e2'
  tertiary-fixed-dim: '#ffb1c7'
  on-tertiary-fixed: '#3e001c'
  on-tertiary-fixed-variant: '#861948'
  background: '#0f150e'
  on-background: '#dee4d9'
  surface-variant: '#30362e'
typography:
  display-lg:
    fontFamily: Hanken Grotesk
    fontSize: 48px
    fontWeight: '700'
    lineHeight: 56px
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Hanken Grotesk
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
  body-md:
    fontFamily: Hanken Grotesk
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  label-sm:
    fontFamily: Hanken Grotesk
    fontSize: 14px
    fontWeight: '500'
    lineHeight: 20px
    letterSpacing: 0.01em
  display-lg-mobile:
    fontFamily: Hanken Grotesk
    fontSize: 36px
    fontWeight: '700'
    lineHeight: 44px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  unit: 8px
  gutter: 16px
  margin-mobile: 16px
  margin-desktop: 32px
  reel-gap: 4px
---

## Brand & Style

The design system is centered on a **Flat-Neumorphic** aesthetic, moving away from the traditional, over-stimulated visual language of casino floors toward a focused, high-end digital experience. It prioritizes clarity, tactile feedback, and a sense of "digital precision."

The brand personality is sophisticated and utilitarian. By stripping away textures like chrome, gold gradients, and lens flares, the UI directs the user’s focus entirely to the mechanics of play and the iconography of the reels. The emotional response should be one of calm engagement rather than frantic excitement, achieved through generous negative space and a restrained dark-mode palette.

## Colors

The palette utilizes a deep monochromatic base to provide high contrast for game-critical elements.

- **Primary (#4CAF50):** Reserved exclusively for the "Spin" or "Start" actions. It represents the "go" state and should be the most luminous element on the screen.
- **Secondary/Neutral (#757575):** Used for structural elements like borders, dividers, and adjustment controls (Bet +/-). It provides visual grounding without competing for attention.
- **Surface (#1E1E1E):** Defines the "well" or container for the slot reels and control bars, creating a subtle layer of depth against the #121212 background.

## Typography

The design system uses **Hanken Grotesk** for its contemporary, clean, and highly legible characteristics, which align perfectly with the modern Android ecosystem.

Typography is treated with a strict hierarchy: 
- **Display styles** are used for win amounts and jackpots, employing a heavier weight and tighter letter spacing for impact.
- **Label styles** (14sp) are used for status text, bet amounts, and balance information to ensure readability at a glance.
- All text should maintain a high contrast ratio against the dark background, defaulting to pure white or very light grey.

## Layout & Spacing

This design system follows a rigid 8dp grid system typical of modern Android development. 

The layout is built around a **Fixed Grid** for the slot reels to maintain the mathematical integrity of the game symbols, while the surrounding UI (controls and headers) uses a **Fluid Grid** to adapt to various screen ratios.

- **The Reel Deck:** Symbols are housed in square cells. Gaps between cells are minimal (4px) to emphasize the vertical motion of the reels.
- **Control Bar:** Positioned at the bottom of the screen, utilizing a 16px horizontal margin from the screen edge.
- **Interaction Zones:** Buttons and adjustment toggles are spaced with 16px gutters to prevent accidental touches.

## Elevation & Depth

Depth is conveyed through **Flat-Neumorphism**. Instead of floating shadows, we use "concave" and "convex" surface treatments:

- **The Reels (Inset):** The main game area should appear slightly recessed into the background using a 1px dark inner border and a very subtle top-down inner shadow.
- **Primary Buttons (Elevated):** The Spin button uses a soft, diffused green outer glow (drop shadow with the primary color at low opacity) to appear "pressed up" from the surface.
- **Separators:** 1px lines in #757575 (at 30% opacity) are used to define regions without adding visual bulk.

## Shapes

The shape language is a mix of geometric precision and ergonomic softness:

- **Reel Cells:** Strict 8dp (rounded-md) corners. This maintains the "square" feel while removing the harshness of 0-degree angles.
- **Spin Button:** Large, fully rounded corners (pill or circular depending on width) to signify its importance as the primary touch target.
- **Adjustment Buttons:** Perfect circles for +/- bet controls to differentiate "tuning" actions from "committing" actions.
- **Secondary Controls:** Pill-shaped (fully rounded) for settings, paytable, or auto-spin to create a distinct visual profile from the reel cells.

## Components

### Buttons
- **Primary (Spin):** Large, Green (#4CAF50) background, white text. Uses a 24dp height padding. On press, it should visually "sink" (neomorphic shift).
- **Adjustment (+/-):** 48x48dp circular targets with a secondary grey border and centered icons.
- **Secondary (Pill):** Ghost-style buttons with a 1px #757575 border and white text for utility functions.

### Slot Cells
- **Container:** #1E1E1E background, 8dp corner radius.
- **Symbol:** Centered within the cell with 12dp internal padding.
- **State:** Winning cells should use a 2px #4CAF50 border stroke to indicate a payline connection.

### Input & Status
- **Display Fields:** (e.g., "Total Bet", "Win") should be styled as recessed wells—darker than the primary surface with centered, high-contrast typography.

### Separators
- **Horizontal/Vertical:** 1px thickness, #757575 color. Use these to separate the header (Balance), the main game, and the footer (Controls) into clear horizontal bands.