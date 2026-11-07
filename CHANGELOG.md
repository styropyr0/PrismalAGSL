# Changelog

All notable changes to Prismal are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project
follows [Semantic Versioning](https://semver.org/).

## [1.0.5] - 2026-11-04

### Added
- Rendering tiers: glass can now run as **Legacy**, **Standard** or **Liquid**, and Android 13+
  devices can be switched down to a lower tier (`PrismalGlass.pipelineOverride`) to preview how
  glass degrades on older versions.
- Text magnifier for glass text fields, shown while a field is focused.

### Fixed
- Text selection handles being clipped at the edges of a glass surface, and failing with
  `BasicTextField(state)` (#2).

### Changed
- Improved the look of `PrismalGlassBottomTabs`.
- Improved rendering performance on low-end devices that cannot afford an additional render pass.
- Further performance improvements for lower-end devices.
- Updated README.

## [1.0.4] - 2026-09-30

### Added
- `PrismalGlassMenu` dropdown menu component.
- `PrismalRangeSlider` ranged slider component.
- `PrismalSegmentedControl` selector.
- `PrismalGlassStepper` selector.
- Click support in `PrismalHorizontalSelector`.

### Fixed
- Blurry thumb appearing in some initial views on the switch and slider.

### Changed
- Improved rendering performance and compatibility on older devices.

## [1.0.3] - 2026-08-31

### Fixed
- Selected content being invisible outside of the droplet but visible inside it (#1).

## [1.0.2] - 2026-08-30

### Added
- Droplet shadow control.
- `PrismalGlassButton` accepts more parameters for finer tuning.

### Changed
- `PrismalRulerSelector`'s `onValueChange` is now invoked whenever the value changes, instead of
  comparing the last and current value.
- Updated code comments and README.

## [1.0.1] - 2026-08-13

### Added
- `PrismalRulerSelector`, a scrollable value selector.

### Changed
- Increased droplet depth in `PrismalHorizontalSelector` and added a damped expand animation when
  switching.
- Improved the refraction physics of the horizontal scroll selector.
- Updated the chromatic aberration and scale effect of the glass bottom tabs.
- The minimum required Kotlin version is now 2.2.

### Fixed
- Performance issue with adaptive luminance.

## [1.0.0] - 2026-08-10

### Added
- Initial release of Prismal: glass surfaces with refraction, blur, specular edge, depth shadow and
  inset, plus the first set of glass components.
- Backward compatibility for older Android versions.

[1.0.5]: https://github.com/styropyr0/PrismalAGSL/compare/v1.0.4...v1.0.5
[1.0.4]: https://github.com/styropyr0/PrismalAGSL/compare/v1.0.3...v1.0.4
[1.0.3]: https://github.com/styropyr0/PrismalAGSL/compare/v1.0.2...v1.0.3
[1.0.2]: https://github.com/styropyr0/PrismalAGSL/compare/v1.0.1...v1.0.2
[1.0.1]: https://github.com/styropyr0/PrismalAGSL/compare/v1.0.0...v1.0.1
[1.0.0]: https://github.com/styropyr0/PrismalAGSL/releases/tag/v1.0.0
